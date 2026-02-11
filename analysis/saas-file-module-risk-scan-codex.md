# SaaS 文件模块结构扫描与风险识别报告

## 1) 模块整体结构图（文字）
- 接入层：`src/main/java/com/supcon/tptrecommend/openapi/FileController.java` 暴露上传、下载、目录、分享、重解析等 API。
- 业务编排层：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` 负责对象路径生成、MinIO读写、元数据落库、异步解析调度、分享链接生成。
- 数据访问层：`src/main/java/com/supcon/tptrecommend/service/impl/FileObjectServiceImpl.java` + `src/main/java/com/supcon/tptrecommend/mapper/FileObjectMapper.java` + `src/main/java/com/supcon/tptrecommend/mapper/xml/FileObjectMapper.xml`。
- 存储层：`src/main/java/com/supcon/tptrecommend/common/utils/MinioUtils.java`，操作 MinIO bucket/object、目录占位符、流式读写、临时文件落地。
- 解析层：`FileAnalysisHandleFactory` 按扩展名分发到 `ExcelFileAnalysisHandle` / `WordFileAnalysisHandle` / `PdfFileFileAnalysisHandle`。
- 外部系统集成：`KnowledgeFeign` / `IndexFeign` / `LlmFeign` 及 `KnowledgeFileHandleTemplate`；异步状态轮询由 `KnowledgeParseStatusJobHandler`、`IndexParseStatusJobHandle` 完成。
- 安全令牌层：`JwtService` 生成/校验下载 ticket（JWT），`createShareLink` 与 `linkDownload` 使用。

## 2) 关键调用链路

### 2.1 上传链路
- 入口：`FileController.upload` / `batchUpload`。
- 编排：`FileManagerImpl.upload` -> `doUpload` -> `generateUniqueObjectKey` -> `uploadToMinio` -> `saveMetadataToDB`。
- 解析触发：`doFileProcess` 异步调用策略处理器。
- 关键特征：上传和解析解耦，解析状态通过 DB 字段和 websocket 推送更新。

### 2.2 下载链路
- 普通下载：`FileController.getOne` -> `FileManagerImpl.getOne` -> `MinioUtils.getMetadata/getFileInputStream` -> 直接写 `HttpServletResponse`。
- 分享下载：`FileController.linkDownload` -> `FileManagerImpl.linkDownload` -> `JwtService.validateAndParseToken` -> 取 token 内 bucket/object 流式回传。
- 租户打包下载：`FileController.downloadTenantFilesAsZip` -> `FileManagerImpl.downloadTenantFilesAsZip` -> `MinioUtils.listObjects` 全量遍历并压缩输出。

### 2.3 分享链路
- 入口：`FileController.getShareLink`。
- 生成：`FileManagerImpl.createShareLink` 调 `JwtService.generateDownloadToken(bucket, object, expirationSecond)`。
- 使用：客户端带 `ticket` 调 `link-download`，服务端仅校验 token 后读取对象。

### 2.4 解析链路
- 入口：上传后 `FileManagerImpl.doFileProcess`。
- 路由：`FileAnalysisHandleFactory.getHandler(extension)`。
- Excel：`ExcelFileAnalysisHandle`（表头提取 -> LLM 分类/映射 -> 业务处理器）。
- Word/PDF：`WordFileAnalysisHandle` / `PdfFileFileAnalysisHandle`，并可走 `KnowledgeFileHandleTemplate.uploadToKnowledgeBase`。
- 状态回写：`KnowledgeParseStatusJobHandler` 与 `IndexParseStatusJobHandle` 轮询外部状态并更新 `file_status/knowledge_parse_state`。

## 3) 潜在风险清单（仅识别与定位）

### P0

1. 未鉴权的任意对象下载入口
- 影响面：`安全`
- 触发条件：调用 `getOne` 并传入任意可猜测 `path`。
- 代码证据：`src/main/java/com/supcon/tptrecommend/openapi/FileController.java` `FileController.getOne`（`@UnAuthentication`，line 89）；`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `getOne` 直接使用请求 path 读取 MinIO（line 416-433）。

2. 未鉴权的文件属性更新入口，且按 object_name 直接更新
- 影响面：`安全 / 可维护性`
- 触发条件：匿名调用 `update`，提供目标 `objectName`。
- 代码证据：`src/main/java/com/supcon/tptrecommend/openapi/FileController.java` `update`（`@UnAuthentication`，line 128）；`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `update/findFileOrFail`（line 743, 777）；`src/main/java/com/supcon/tptrecommend/mapper/xml/FileObjectMapper.xml` `WHERE object_name = #{req.objectName}`（line 49）。

3. 未鉴权的租户文件打包下载，可枚举全量数据
- 影响面：`安全 / 成本 / 稳定性`
- 触发条件：匿名调用 `/downloadFilesAsZip`，`tenantId/userName` 为空时前缀为空。
- 代码证据：`src/main/java/com/supcon/tptrecommend/openapi/FileController.java`（line 199-200）；`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `downloadTenantFilesAsZip` 将空参数映射为 `tenantPrefix = ""`（line 1055-1063），随后 `listObjects(bucket, tenantPrefix)` 全量遍历（line 1075）。

4. 未鉴权的分享链接签发，允许指定任意 bucket/object
- 影响面：`安全`
- 触发条件：匿名调用 `/share-link` 并提交任意 `bucketName/objectName`。
- 代码证据：`src/main/java/com/supcon/tptrecommend/openapi/FileController.java`（line 209, 211）；`src/main/java/com/supcon/tptrecommend/dto/fileshare/FileShareRequest.java` 仅 `@NotBlank`（line 14, 18）；`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `createShareLink` 直接签 token（line 1142-1148）。

5. 批量删除未校验归属，按 ID 列表直接删除
- 影响面：`安全 / 稳定性`
- 触发条件：提供跨用户/跨租户文件 ID 列表。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `batchDelete` 使用 `listByIds/removeBatchByIds`（line 461-466）并删除 MinIO 对象（line 476），无 user/tenant 条件。

6. `detail` 接口按 objectName 集合查询，缺少归属限制
- 影响面：`安全`
- 触发条件：请求中携带他人对象路径。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `detail` 仅 `in(object_name, req.paths)`（line 445-448）。

7. Mapper 多处显式绕过租户拦截
- 影响面：`安全 / 可维护性`
- 触发条件：调用这些 DAO 方法且上层未补充租户约束。
- 代码证据：`src/main/java/com/supcon/tptrecommend/mapper/FileObjectMapper.java` `@InterceptorIgnore(tenantLine = "true")` 出现在 `getKnowledgeParsing/updateKnowledgeParseState/getFileStatistics/getByObjectName/updateFileAttributes`（line 34,45,63,78,92）。

8. 配置文件包含明文敏感凭据
- 影响面：`安全 / 成本`
- 触发条件：代码仓库泄露、日志回显、配置误分发。
- 代码证据：`src/main/resources/application-local.yml` 明文 `sup.datasource.password`、`minio.accessKey/secretKey`、`app.security.jwt-download-secret`（line 23, 50-51, 95）。

### P1

9. 分享 token 过期时间未设置上限
- 影响面：`安全`
- 触发条件：`expirationSecond` 传入极大值，生成长期有效下载票据。
- 代码证据：`src/main/java/com/supcon/tptrecommend/dto/fileshare/FileShareRequest.java` `expirationSecond` 可外部传入（line 21）；`src/main/java/com/supcon/tptrecommend/common/utils/JwtService.java` `generateDownloadToken` 直接使用该值计算过期（line 36-38）。

10. 上传路径决策可回退到共享目录
- 影响面：`安全 / 可维护性`
- 触发条件：传入 `path` 且用户目录对象不存在时，自动写入 `_shared` 前缀。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `resolveUploadPath`（line 176-192）。

11. 上传缺少服务端文件类型白名单/内容校验
- 影响面：`安全 / 成本`
- 触发条件：上传任意扩展名/内容文件。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `doUpload` 直接上传并落库（line 145-155）；解析仅在后续 `getHandler(extension)` 决定是否处理（line 213）。

12. 单请求上传体积上限过大，放大 DoS 与资源占用风险
- 影响面：`性能 / 稳定性 / 成本`
- 触发条件：大文件或并发大文件上传。
- 代码证据：`src/main/resources/application-local.yml` `max-file-size/max-request-size: 10240MB`（line 6-7）。

13. 目录浏览存在 N+1 存储调用
- 影响面：`性能 / 成本`
- 触发条件：目录项多、并发高。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `listFiles` 循环内调用 `minioUtils.countFilePrefix`（line 562, 606）；`src/main/java/com/supcon/tptrecommend/common/utils/MinioUtils.java` `countFilePrefix` 每次 `listObjects` 全遍历（line 390+）。

14. ZIP 下载为单请求全量流式压缩，缺少范围限制
- 影响面：`性能 / 稳定性 / 成本`
- 触发条件：租户数据量大或空前缀全量导出。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `downloadTenantFilesAsZip` 遍历全部对象并边读边压缩（line 1075-1110）。

15. 删除知识库异步线程池使用 `DiscardPolicy`，任务可静默丢弃
- 影响面：`稳定性 / 可维护性`
- 触发条件：并发删除高于线程池承载。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/impl/FileManagerImpl.java` `delExecutor`（line 121-127）。

16. 指标解析轮询任务对 Redis 返回值缺少空判
- 影响面：`稳定性`
- 触发条件：`hGetAll` 返回 `null`。
- 代码证据：`src/main/java/com/supcon/tptrecommend/job/IndexParseStatusJobHandle.java` `fileTaskMap.forEach` 前无 null 检查（line 44-46）。

17. 指标解析轮询中租户上下文清理点不覆盖早返回分支
- 影响面：`安全 / 稳定性`
- 触发条件：`userId == null` 分支 `return`，未执行本次循环体末尾 `TenantContext.clear()`。
- 代码证据：`src/main/java/com/supcon/tptrecommend/job/IndexParseStatusJobHandle.java` `setCurrentTenant`（line 50）、`return`（line 54）、`clear`（line 84）。

### P2

18. 知识库关键词落库未显式设置 tenantId
- 影响面：`可维护性 / 安全`
- 触发条件：依赖自动填充失败或上下文缺失时，推荐记录租户字段不完整。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/strategy/KnowledgeFileHandleTemplate.java` `saveFileKeywordsToRecommendation` 仅设置 `fileId/keyword` 后 `save`（line 151-156）；对比 `KnowledgeParseStatusJobHandler` 在另一条路径有显式 `setTenantId`（line 164）。

19. Word 解析异常分支多处直接 `return`，状态回写不一致
- 影响面：`稳定性 / 可维护性`
- 触发条件：文件过大、LLM转换失败、响应异常。
- 代码证据：`src/main/java/com/supcon/tptrecommend/manager/strategy/impl/WordFileAnalysisHandle.java` 在 `fileSize > 100MB`、调用失败等分支直接 `return`（line 72,85,92），仅分类成功路径有 `updateById`（line 131）。

20. 临时文件目录为固定共享路径，跨任务争用与清理残留风险
- 影响面：`稳定性 / 成本`
- 触发条件：高并发解析、进程异常中断。
- 代码证据：`src/main/resources/application-local.yml` `file.temp-dir: D:/temp/uploads`（line 77）；`src/main/java/com/supcon/tptrecommend/common/utils/MinioUtils.java` `saveStreamToTempFile` 使用该目录并 `REPLACE_EXISTING`（line 453,467）。
