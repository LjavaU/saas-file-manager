# SaaS 文件模块风险识别报告

**项目**: tpt-recommend
**模块**: SaaS 文件管理与 RAG 知识库集成
**扫描时间**: 2026-02-09
**线上环境**: 3 节点 × 10C16G，1Gbps 带宽
**假设规模**: 未来 5-10 倍增长

---

## 一、整体结构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端 / API 网关                           │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                     FileController (API 层)                       │
│  - 文件上传/下载/删除/查询                                          │
│  - 分享链接生成/下载                                               │
│  - 文件夹管理                                                      │
│  - 文件统计                                                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                   FileManagerImpl (业务编排层)                    │
│  - 路径生成与租户隔离 (tenantId/userName/[path/]uuid_filename)    │
│  - MinIO 上传/下载/删除编排                                        │
│  - 元数据管理 (file_object 表)                                     │
│  - 异步文件解析调度                                                │
│  - JWT 分享链接生成与验证                                          │
│  - 批量打包下载                                                    │
└─────┬──────────────────┬──────────────────┬─────────────────────┘
      │                  │                  │
      ▼                  ▼                  ▼
┌──────────┐    ┌──────────────┐    ┌─────────────────┐
│ MinIO    │    │ PostgreSQL   │    │ 异步处理线程池   │
│ (存储)   │    │ (元数据)     │    │ (解析/知识库)   │
└──────────┘    └──────────────┘    └────────┬────────┘
                                              │
                        ┌─────────────────────┼─────────────────────┐
                        ▼                     ▼                     ▼
              ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
              │ ExcelAnalysisHandle│  │ WordAnalysisHandle│  │ PdfAnalysisHandle│
              │ (Excel 解析)       │  │ (Word 解析)       │  │ (PDF 解析)       │
              └─────────┬──────────┘  └─────────┬─────────┘  └─────────┬────────┘
                        │                       │                       │
                        ▼                       ▼                       ▼
              ┌─────────────────────────────────────────────────────────┐
              │              外部服务依赖                                │
              │  - LLM Feign (文件分类/字段映射)                         │
              │  - Knowledge Feign (RAG 知识库上传/查询/删除)            │
              └─────────────────────────────────────────────────────────┘
```

---

## 二、关键调用链路说明

### 2.1 文件上传链路

```
用户上传文件
  ↓
POST /open-api/file/upload (FileController:54)
  ↓
FileManagerImpl.upload(file, path) (行138)
  ├─ 步骤1: doUpload() (行145)
  │   ├─ generateUniqueObjectKey() - 生成对象键: tenantId/userName/[path/]UUID_filename
  │   ├─ uploadToMinio() - 上传到 MinIO bucket "recommend"
  │   └─ saveMetadataToDB() - 元数据入库 (最多重试3次处理重名)
  │
  └─ 步骤2: doFileProcess() (行212)
      ├─ FileAnalysisHandleFactory.getHandler(extension)
      ├─ CompletableFuture.runAsync(fileProcessingExecutor) - 异步执行
      │   ├─ ExcelFileAnalysisHandle (支持 xlsx/xls/csv)
      │   │   ├─ minioUtils.saveStreamToTempFile() - 下载到本地临时目录
      │   │   ├─ EasyExcel 读取表头和行数
      │   │   ├─ llmFeign.classify() - 调用 LLM 进行文件分类
      │   │   ├─ llmFeign.alignment() - 调用 LLM 进行字段映射
      │   │   └─ ExcelDataListener 逐行处理业务数据
      │   │
      │   └─ WordFileAnalysisHandle / PdfFileAnalysisHandle
      │       └─ KnowledgeFileHandleTemplate.uploadToKnowledgeBase()
      │           ├─ RestTemplate POST 到知识库服务
      │           ├─ 保存关键词到 file_recommendation 表
      │           └─ 更新 knowledge_parse_state (GRAY → GREEN/RED)
      │
      └─ 状态轮询: KnowledgeParseStatusJobHandler (每30秒)
          ├─ knowledgeFeign.listFiles() - 查询知识库解析状态
          ├─ knowledgeFeign.getRecommendation() - 获取推荐问题
          └─ 更新 file_status 和 knowledge_parse_state
```

**关键路径**:
- **同步部分**: 用户请求 → MinIO 上传 → 数据库写入 (约 200-500ms)
- **异步部分**: 文件解析 → LLM 调用 → 知识库上传 (数秒到数分钟)

---

### 2.2 文件下载链路

#### **普通下载**
```
POST /open-api/file/getOne (FileController:85)
  ↓
FileManagerImpl.getOne(req, response) (行416)
  ├─ minioUtils.getMetadata(bucket, path) - 获取文件元数据
  ├─ response.setContentType(metadata.contentType())
  ├─ response.setHeader("Content-disposition", "attachment;filename=...")
  └─ minioUtils.getFileInputStream(bucket, path) → IOUtils.copy() → response
```

**风险**:
- 接口标记 `@UnAuthentication`，任何人知道路径即可下载 (FileController:89)
- 无权限校验，可能下载其他用户的文件

---

#### **分享链接下载**
```
生成分享链接:
POST /open-api/file/share-link (FileController:209)
  ↓
JwtService.generateDownloadToken(bucket, object, expirySeconds) (JwtService:36)
  ├─ 生成 JWT Token (HS512 签名)
  │   ├─ sub: "file-download"
  │   ├─ b (bucket): "recommend"
  │   ├─ o (object): "tenantId/userName/uuid_filename.ext"
  │   ├─ iat: 签发时间
  │   └─ exp: 过期时间 (默认 86400 秒 = 24 小时)
  │
  └─ 返回: /open-api/file/link-download?ticket=JWT_TOKEN

下载文件:
GET /open-api/file/link-download?ticket=xxx (FileController:218)
  ↓
FileManagerImpl.linkDownload(token) (行1157)
  ├─ JwtService.validateAndParseToken(token) - 验证签名和过期时间
  ├─ 从 Claims 中提取 bucket 和 objectName
  ├─ minioUtils.getMetadata(bucket, objectName)
  └─ StreamingResponseBody 流式返回文件
```

**风险**:
- JWT Secret 硬编码在 application-local.yml (行95)
- 接口 `@UnAuthentication`，无二次鉴权
- 无访问次数限制，可被批量爬取

---

### 2.3 租户文件打包下载链路

```
GET /open-api/file/downloadFilesAsZip?tenantId=xxx&userName=yyy (FileController:199)
  ↓
FileManagerImpl.downloadTenantFilesAsZip(tenantId, userName, response) (行1055)
  ├─ 构建前缀: tenantId/userName/ (若 userName 为空则 tenantId/)
  ├─ minioUtils.listObjects(bucket, prefix) - 递归列举所有文件
  ├─ 创建 ZipOutputStream → response.getOutputStream()
  └─ 遍历 MinIO 对象:
      ├─ minioUtils.getFileInputStream(bucket, fullObjectPath)
      ├─ ZipEntry(relativePath)
      ├─ 边读 MinIO → 边压缩 ZIP → 边发送响应
      └─ zipOut.closeEntry()
```

**风险**:
- 接口 `@UnAuthentication`，无身份校验 (FileController:200)
- 无大小限制，可能压缩数十 GB 数据导致内存溢出/带宽耗尽
- 无并发控制，多个打包请求可能压垮服务器

---

### 2.4 文件删除链路

#### **单文件删除**
```
DELETE /open-api/file/delete/{id} (FileController:58)
  ↓
FileManagerImpl.delete(id) (行342)
  ├─ 查询文件: eq(id).eq(userName) - 有权限校验
  ├─ deleteFileObjectHierarchy(objectName, id) - 删除数据库记录
  │   └─ 若 objectName 以 "/" 结尾，递归删除目录下所有文件
  ├─ minioUtils.removeFile(bucket, objectName) - 删除 MinIO 对象
  └─ 若是知识库文件:
      ├─ fileRecommendationService.remove(fileId) - 删除推荐记录
      └─ knowledgeFeign.deleteKnowledgeBase() - 异步删除知识库
```

**权限校验**: ✅ 有 (FileManagerImpl:344-346)

---

#### **批量删除**
```
DELETE /open-api/file/batchDelete (FileController:67)
  ↓
FileManagerImpl.batchDelete(ids) (行461)
  ├─ fileObjectService.listByIds(ids) - 直接查询，无 userName 过滤 ❌
  ├─ fileObjectService.removeBatchByIds(ids) - 直接删除数据库
  ├─ 遍历文件:
  │   └─ knowledgeFeign.deleteKnowledgeBase() - 删除知识库
  └─ minioUtils.removeFiles(bucket, objectNames) - 批量删除 MinIO
```

**严重安全漏洞**: ❌ 无权限校验，用户可删除任意文件 (FileManagerImpl:463)

---

### 2.5 文件解析链路 (Excel 示例)

```
ExcelFileAnalysisHandle.handleFileAnalysis(fileId, category) (行52)
  ↓
doHandle(file, fileId, originalFilename, userId, category) (行117)
  ├─ 步骤1: 读取表头 (EasyExcel)
  │   ├─ ExtraAttributesListener - 提取表头和行数
  │   └─ MarkdownConverter.generateMarkdownTable(headers) - 转 Markdown
  │
  ├─ 步骤2: 调用 LLM 分类 (行142)
  │   └─ llmFeign.classify(headerMarkdown) → FileClassifyResp
  │       └─ 返回: category, subcategory, third_level_category, summary
  │
  ├─ 步骤3: 更新文件元数据 (行144)
  │   └─ 保存分类、能力、概述到 file_object 表
  │
  ├─ 步骤4: 调用 LLM 字段映射 (行158)
  │   └─ llmFeign.alignment(headerMarkdown, databaseSchema) → FileAlignmentResp
  │       └─ 返回: Map<ExcelColumn, DbField>
  │
  └─ 步骤5: 逐行处理业务数据 (行166)
      └─ ExcelDataListener - 根据映射关系插入数据库
          ├─ DynamicMapper 动态转换
          └─ BusinessDataHandler.processBatch()
```

**外部依赖**:
- **LLM 服务**: http://supcon-file-parsing-indu-dev.supcon5t.com
- **知识库服务**: http://supcon-rag-indu-dev.supcon5t.com

---

## 三、潜在风险清单 (28 个风险点)

### 【P0 级风险 - 严重安全/稳定性问题】

#### **P0-1: 批量删除无权限校验**
- **影响面**: 安全
- **触发条件**: 攻击者获取任意文件 ID 列表，调用 `batchDelete` 接口
- **代码证据**:
  - FileManagerImpl.java:461-478
  - 第463行: `List<FileObject> fileObjects = fileObjectService.listByIds(ids);`
  - 直接使用 `listByIds` 查询，未添加 `eq(FileObject::getUserName, currentUser)`
  - 第465行: `fileObjectService.removeBatchByIds(ids);` 无条件删除
- **后果**: 用户可删除其他租户/用户的文件，导致数据丢失

---

#### **P0-2: 租户拦截器被大量绕过**
- **影响面**: 安全
- **触发条件**: 当上层业务逻辑未补充租户校验时，可跨租户访问数据
- **代码证据**:
  - FileObjectMapper.java:34, 45, 63, 78, 92 - 多处 `@InterceptorIgnore(tenantLine = "true")`
  - `getKnowledgeParsing()` - 第35行: 全表查询知识库解析中的文件
  - `getFileStatistics()` - 第66行: 统计全库文件数量和大小
  - `getByObjectName()` - 第80行: 根据对象名查询，无租户过滤
  - `updateFileAttributes()` - 第93行: 更新文件属性，WHERE 条件仅 objectName
- **后果**: 可能读取/修改其他租户的文件元数据

---

#### **P0-3: 多个敏感接口未鉴权**
- **影响面**: 安全
- **触发条件**: 攻击者直接调用 API，无需登录
- **代码证据**:
  - FileController.java:89 - `getOne()` 标记 `@UnAuthentication`
  - FileController.java:128 - `update()` 标记 `@UnAuthentication`
  - FileController.java:200 - `downloadFilesAsZip()` 标记 `@UnAuthentication`
  - FileController.java:211 - `getShareLink()` 标记 `@UnAuthentication`
  - FileController.java:220 - `linkDownload()` 标记 `@UnAuthentication`
- **后果**:
  - 任何人可下载文件（知道路径即可）
  - 任何人可更新文件属性
  - 任何人可生成分享链接
  - 任何人可打包下载整个租户的文件

---

#### **P0-4: JWT 密钥硬编码**
- **影响面**: 安全
- **触发条件**: 代码泄露或配置文件被获取
- **代码证据**:
  - application-local.yml:95 - 明文存储 JWT Secret
  - `jwt-download-secret: "yXd3ecoPzJLfNvAI7Aq3ay9LPj4V2QxP9WSxJ1ZYDjt1i9gIkyUASsPd7rOOlpuPFeoeCirtvTjxJpbvVnuSaw=="`
  - JwtService.java:26 - 直接从配置文件读取，无加密
- **后果**: 攻击者可伪造任意文件的分享链接

---

#### **P0-5: MinIO 凭证硬编码**
- **影响面**: 安全
- **触发条件**: 配置文件泄露
- **代码证据**:
  - application-local.yml:49-52
  - `endpoint: http://seak8sm1.supcon5t.com:30350`
  - `accessKey: admin`
  - `secretKey: Supcon1304`
- **后果**: 攻击者可直接访问 MinIO，读取/删除/篡改所有文件

---

#### **P0-6: 文件路径遍历风险**
- **影响面**: 安全
- **触发条件**: 用户提交恶意路径参数，如 `../../etc/passwd`
- **代码证据**:
  - FileManagerImpl.java:416 - `getOne(SingleFileQueryReq req)`
  - 第417行: `String path = req.getPath();` 直接使用用户输入
  - 第421行: `minioUtils.getMetadata(bucket, path)` 无路径校验
  - 第432行: `minioUtils.getFileInputStream(bucket, path)` 直接读取
- **后果**: 可能访问其他租户的文件或系统文件

---

#### **P0-7: 租户文件打包下载无限制**
- **影响面**: 稳定性 + 成本
- **触发条件**: 用户请求下载整个租户的文件（可能数十 GB）
- **代码证据**:
  - FileManagerImpl.java:1055 - `downloadTenantFilesAsZip()`
  - 第1075行: `Iterable<Result<Item>> results = minioUtils.listObjects(bucket, tenantPrefix);` - 全量遍历
  - 第1077行: `byte[] buffer = new byte[8192];` - 8KB 缓冲区
  - 无文件数量/总大小限制
  - 无并发控制
- **后果**:
  - 单个请求可能消耗数 GB 内存和带宽
  - 多个并发请求可能压垮服务器
  - 成本失控（出口流量费用）

---

#### **P0-8: 线程池拒绝策略导致任务静默丢失**
- **影响面**: 稳定性
- **触发条件**: 删除请求过多，线程池队列满
- **代码证据**:
  - FileManagerImpl.java:121-127 - `delExecutor` 线程池配置
  - 第127行: `new ThreadPoolExecutor.DiscardPolicy()` - 静默丢弃任务
  - 核心线程: 10，最大线程: 20，队列容量: 50
- **后果**: 知识库删除任务被丢弃，导致数据不一致（数据库已删除但知识库仍存在）

---

### 【P1 级风险 - 重要性能/稳定性问题】

#### **P1-1: 文件上传无全局并发控制**
- **影响面**: 性能 + 稳定性
- **触发条件**: 多用户同时批量上传，单次 20 个文件
- **代码证据**:
  - FileController.java:190 - `batchUpload()` 最多 20 个文件
  - FileManagerImpl.java:1025 - 校验: `if (multipartFiles.size() > 20)`
  - 第1031-1040行: 使用 `fileUploadExecutor` 并发上传（2-4 线程）
  - 配置: application-local.yml:87-92 - 队列容量仅 50
- **后果**:
  - 50 个用户同时上传 → 1000 个文件并发处理
  - MinIO 连接池耗尽
  - 网络带宽打满

---

#### **P1-2: MinIO 递归查询无分页**
- **影响面**: 性能
- **触发条件**: 租户文件数量达到数万级别
- **代码证据**:
  - MinioUtils.java:313-320 - `listObjects()`
  - 第317行: `.recursive(true)` - 递归查询全部对象
  - 无分页机制，返回 `Iterable<Result<Item>>`
  - FileManagerImpl.java:1075 - `downloadTenantFilesAsZip()` 中全量遍历
- **后果**:
  - 单次查询可能返回数万条结果
  - 内存占用激增
  - 响应时间过长（数十秒）

---

#### **P1-3: 文件删除同步阻塞主线程**
- **影响面**: 性能
- **触发条件**: 删除大文件或大量文件
- **代码证据**:
  - FileManagerImpl.java:351 - `delete()` 中同步调用 `minioUtils.removeFile()`
  - FileManagerImpl.java:476 - `batchDelete()` 中同步调用 `minioUtils.removeFiles()`
  - MinioUtils.java:161-175 - `removeFile()` 同步执行
  - MinioUtils.java:239-273 - `removeFiles()` 同步遍历删除
- **后果**:
  - 删除操作阻塞用户请求
  - 删除失败时用户体验差

---

#### **P1-4: 知识库上传使用 RestTemplate 同步调用**
- **影响面**: 性能
- **触发条件**: 知识库服务响应慢或不可用
- **代码证据**:
  - KnowledgeFileHandleTemplate.java:41 - `private final RestTemplate restTemplate = new RestTemplate();`
  - 第107-112行: `restTemplate.exchange()` 同步调用
  - 无超时配置
  - 虽然外层有 `CompletableFuture.runAsync`，但 RestTemplate 本身是阻塞的
- **后果**:
  - 知识库服务慢响应导致线程池耗尽
  - 大文件上传时长时间占用线程

---

#### **P1-5: 文件解析线程池配置不合理**
- **影响面**: 性能 + 稳定性
- **触发条件**: 大量文件同时上传触发解析
- **代码证据**:
  - application-local.yml:81-86 - `file-processing` 线程池配置
  - 核心线程: 20，最大线程: 40，队列容量: 500
  - 线上环境: 3 节点 × 10 核 = 30 核
  - 单节点最大 40 线程，可能超过 CPU 核心数 4 倍
- **后果**:
  - 上下文切换开销大
  - CPU 利用率低
  - 响应时间变长

---

#### **P1-6: 外部服务调用无超时保护**
- **影响面**: 稳定性
- **触发条件**: LLM、知识库服务响应慢或挂掉
- **代码证据**:
  - ExcelFileAnalysisHandle.java:142 - `llmFeign.classify()` 无超时
  - ExcelFileAnalysisHandle.java:158 - `llmFeign.alignment()` 无超时
  - KnowledgeFileHandleTemplate.java:107 - `restTemplate.exchange()` 无超时
  - Feign 配置: application-local.yml:53-55 - 仅启用 OkHttp，无超时配置
- **后果**:
  - 外部服务故障导致请求挂起
  - 线程池资源耗尽
  - 雪崩效应

---

#### **P1-7: 异常吞噬导致问题难以排查**
- **影响面**: 可维护性 + 稳定性
- **触发条件**: 文件解析/删除失败
- **代码证据**:
  - FileManagerImpl.java:219-223 - `exceptionally()` 仅记录日志，不抛出异常
  - FileManagerImpl.java:368-371 - 知识库删除失败仅记录 error 日志
  - FileManagerImpl.java:1104-1107 - 单个文件下载失败跳过，继续处理
  - MinioUtils.java:264 - `result.get()` 异常被吞噬
- **后果**:
  - 用户不知道操作失败
  - 数据不一致（如删除时数据库成功但 MinIO 失败）
  - 问题难以复现和定位

---

#### **P1-8: 临时文件清理不可靠**
- **影响面**: 成本 + 稳定性
- **触发条件**: 文件解析异常或 JVM 崩溃
- **代码证据**:
  - ExcelFileAnalysisHandle.java:73-75 - `finally { FileUtils.deleteTemporaryFile(file, originalFilename); }`
  - 若 `file` 为 null（第69行返回），则不会删除
  - 无定期清理机制
  - 临时目录: `D:/temp/uploads` (application-local.yml:77)
- **后果**:
  - 临时文件堆积，占满磁盘
  - 影响其他服务

---

### 【P2 级风险 - 改进建议】

#### **P2-1: SQL 注入风险**
- **影响面**: 安全
- **触发条件**: 若 `objectName` 参数未校验，可能包含 SQL 特殊字符
- **代码证据**:
  - FileObjectMapper.xml:35-50 - `updateFileAttributes`
  - 第49行: `WHERE object_name = #{req.objectName}`
  - 使用 MyBatis 参数化查询，风险较低
  - 但第39-45行的动态 SQL 拼接若处理不当可能存在风险
- **后果**: 低风险（MyBatis 已做参数化）

---

#### **P2-2: 缺少文件大小上传限制**
- **影响面**: 成本 + 性能
- **触发条件**: 用户上传单个超大文件（接近 10GB）
- **代码证据**:
  - application-local.yml:5-7 - `max-file-size: 10240MB` (10GB)
  - FileManagerImpl.java 中无业务层文件大小校验
- **后果**:
  - 单个请求占用大量内存和带宽
  - MinIO 存储成本高

---

#### **P2-3: 无文件配额管理**
- **影响面**: 成本
- **触发条件**: 租户/用户无限上传文件
- **代码证据**:
  - FileManagerImpl.java:138-156 - `upload()` 无配额校验
  - 无租户级/用户级存储限制
- **后果**:
  - 存储成本失控
  - 某些租户占用大量资源

---

#### **P2-4: LLM 调用无缓存**
- **影响面**: 成本 + 性能
- **触发条件**: 相同文件重复上传
- **代码证据**:
  - ExcelFileAnalysisHandle.java:142 - `llmFeign.classify()` 每次都调用
  - ExcelFileAnalysisHandle.java:158 - `llmFeign.alignment()` 每次都调用
  - 无基于文件哈希或表头的缓存机制
- **后果**:
  - LLM 调用费用高
  - 响应时间慢

---

#### **P2-5: 知识库解析状态轮询低效**
- **影响面**: 性能
- **触发条件**: 大量文件处于解析中状态
- **代码证据**:
  - 定时任务: KnowledgeParseStatusJobHandler (每 30 秒执行一次)
  - FileObjectMapper.java:35 - `getKnowledgeParsing()` 全表扫描
  - 无索引优化提示
- **后果**:
  - 数据库负载高
  - 轮询延迟导致状态更新不及时

---

#### **P2-6: 路径生成逻辑复杂易出错**
- **影响面**: 可维护性
- **触发条件**: 路径参数边界情况
- **代码证议**:
  - FileManagerImpl.java:176-192 - `resolveUploadPath()`
  - 逻辑：若用户目录不存在则回退到共享目录
  - 第185行: `long count = fileObjectService.count(...)` - 多次数据库查询
- **后果**:
  - 逻辑复杂，难以理解
  - 性能开销（每次上传都查询数据库）

---

#### **P2-7: 魔法数字过多**
- **影响面**: 可维护性
- **触发条件**: 需要调整配置时
- **代码证据**:
  - FileManagerImpl.java:296 - `for (int i = 0; i < 3; i++)` - 重试次数硬编码
  - FileManagerImpl.java:1025 - `if (multipartFiles.size() > 20)` - 批量上传限制硬编码
  - FileManagerImpl.java:1077 - `byte[] buffer = new byte[8192];` - 缓冲区大小硬编码
- **后果**:
  - 配置分散，难以统一管理
  - 修改时容易遗漏

---

#### **P2-8: 日志缺少关键上下文**
- **影响面**: 可维护性
- **触发条件**: 生产环境排查问题
- **代码证据**:
  - FileManagerImpl.java:220 - `log.error("文件：{},在处理过程中失败", originalFilename, throwable);`
    - 缺少 fileId、userId、tenantId
  - FileManagerImpl.java:366 - `log.error("删除文件对应的知识库失败: {}", fileObject.getObjectName());`
    - 缺少错误详情
- **后果**:
  - 难以快速定位问题
  - 无法关联用户行为

---

#### **P2-9: 文件名处理不当**
- **影响面**: 安全 + 可维护性
- **触发条件**: 文件名包含特殊字符（如 `../`, `%00` 等）
- **代码证据**:
  - FileManagerImpl.java:146 - `String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();`
  - 仅做空值处理，无特殊字符过滤
  - FileManagerImpl.java:171 - `String uniqueFilename = UUID.fastUUID().toString().replace("-", "") + "_" + originalFilename;`
  - 直接拼接原始文件名
- **后果**:
  - 可能导致路径遍历
  - 文件名显示异常

---

#### **P2-10: 共享文件夹权限控制不清晰**
- **影响面**: 安全 + 可维护性
- **触发条件**: 用户访问共享文件夹
- **代码证据**:
  - FileManagerImpl.java:514-516 - `getSharedPath()` 返回 `tenantId/_shared/`
  - FileManagerImpl.java:664-666 - `isSharedFolder()` 仅判断路径前缀
  - 无明确的权限控制逻辑（谁可以读/写/删除共享文件夹）
- **后果**:
  - 权限模型不明确
  - 可能误删共享文件

---

#### **P2-11: 文件夹删除逻辑有风险**
- **影响面**: 稳定性
- **触发条件**: 删除包含大量文件的文件夹
- **代码证据**:
  - FileManagerImpl.java:375-385 - `deleteFileObjectHierarchy()`
  - 第378行: `fileObjectService.remove(Wrappers.<FileObject>lambdaQuery().likeRight(FileObject::getObjectName, objectName));`
  - 第379行: 使用 `likeRight` 模糊匹配删除
  - 无批量大小限制
- **后果**:
  - 单次删除可能影响数千条记录
  - 数据库锁表风险

---

#### **P2-12: 缺少文件去重机制**
- **影响面**: 成本
- **触发条件**: 用户重复上传相同文件
- **代码证据**:
  - FileManagerImpl.java:138-156 - `upload()` 无文件哈希校验
  - 每次上传都生成新的 UUID 文件名
- **后果**:
  - 相同文件重复存储
  - MinIO 存储成本增加

---

## 四、风险分级总结

| 优先级 | 数量 | 主要问题 |
|--------|------|---------|
| **P0** | 8 | 批量删除无权限、租户拦截器绕过、接口未鉴权、凭证硬编码、路径遍历、打包下载无限制、线程池拒绝策略 |
| **P1** | 8 | 并发控制缺失、递归查询无分页、同步阻塞、无超时保护、异常吞噬、临时文件清理 |
| **P2** | 12 | SQL注入（低风险）、无配额管理、无缓存、日志不规范、权限模型不清晰、性能优化点 |
| **合计** | **28** | |

---

## 五、关键指标估算（假设场景）

### 场景 1: 单个用户批量上传
- **操作**: 上传 20 个 Excel 文件，每个 50MB
- **同步耗时**: MinIO 上传 (50MB × 20 / 100Mbps) ≈ 8 秒
- **异步耗时**:
  - 文件解析: EasyExcel 读取 + LLM 分类 + LLM 映射 ≈ 30-60 秒/文件
  - 知识库上传: 50MB × 20 = 1GB，上传 ≈ 80 秒
- **资源占用**:
  - 线程: 20 个文件处理线程 (最大 40)
  - 内存: 每个文件临时存储 50MB → 1GB 峰值内存
  - 磁盘: D:/temp/uploads 占用 1GB

---

### 场景 2: 租户文件打包下载
- **操作**: 租户有 10000 个文件，总大小 100GB
- **触发**: GET /downloadFilesAsZip?tenantId=xxx
- **执行过程**:
  1. `listObjects` 递归查询 10000 个对象 ≈ 5-10 秒
  2. 边读 MinIO → 边压缩 ZIP → 边发送
  3. 网络传输: 100GB / 1Gbps ≈ 800 秒 (13 分钟)
- **风险**:
  - 单个请求占用 1 个线程 13 分钟
  - 10 个并发请求可能压垮服务器
  - 出口带宽: 100GB × 10 = 1TB

---

### 场景 3: 批量删除攻击
- **操作**: 攻击者获取 10000 个文件 ID，调用 batchDelete
- **执行过程**:
  1. `listByIds(10000)` - 查询数据库 ≈ 1 秒
  2. `removeBatchByIds(10000)` - 删除数据库 ≈ 2 秒
  3. `minioUtils.removeFiles(10000)` - 批量删除 MinIO ≈ 10-30 秒
  4. 知识库删除: 循环调用 10000 次（异步，但可能触发限流）
- **后果**:
  - 数据库: 10000 条记录被删除
  - MinIO: 可能数百 GB 数据被删除
  - 知识库: 删除请求可能失败（数据不一致）

---

## 六、依赖外部服务风险

| 服务 | 地址 | 用途 | 失败影响 |
|------|------|------|---------|
| MinIO | seak8sm1.supcon5t.com:30350 | 文件存储 | 无法上传/下载，系统不可用 |
| PostgreSQL | seak8sm1.supcon5t.com:31230 | 元数据存储 | 无法查询/更新元数据，系统不可用 |
| Redis | gateway.supcon5t.com:26379 | 缓存/会话 | 影响登录态、缓存失效 |
| LLM | supcon-file-parsing-indu-dev.supcon5t.com | 文件分类/映射 | 文件解析失败，标记为 PARSE_FAILED |
| Knowledge | supcon-rag-indu-dev.supcon5t.com | RAG 知识库 | 知识库功能不可用，标记为 RED |
| XxlJob | system-schedule-obp-dev.supcon5t.com | 定时任务 | 知识库状态轮询停止 |

**单点故障风险**:
- MinIO / PostgreSQL 故障 → 系统完全不可用
- LLM / Knowledge 故障 → 文件解析失败，但不影响上传/下载

---

## 七、合规性考量

1. **数据隔离**: 租户拦截器被绕过，可能违反多租户数据隔离要求
2. **访问控制**: 多个接口 `@UnAuthentication`，不符合最小权限原则
3. **审计日志**: 部分操作无详细日志（如批量删除），难以追溯
4. **数据保留**: 无文件自动清理机制，可能违反数据保留政策
5. **加密存储**: MinIO 是否启用加密存储未知

---

## 八、建议监控指标

| 类别 | 指标 | 阈值 |
|------|------|------|
| **性能** | 文件上传耗时 P99 | < 3 秒 |
| | 文件下载耗时 P99 | < 2 秒 |
| | 文件解析耗时 P99 | < 60 秒 |
| **稳定性** | 文件上传失败率 | < 0.1% |
| | 文件解析失败率 | < 1% |
| | 线程池队列满告警 | 队列使用率 > 80% |
| **成本** | MinIO 存储总量 | < 1TB |
| | LLM 调用次数/天 | < 10000 次 |
| | 出口流量/天 | < 100GB |
| **安全** | 未授权访问尝试 | > 10 次/分钟 |
| | 批量删除操作次数 | > 5 次/小时 |

---

## 九、代码热点 (需重点关注)

| 文件 | 行数 | 复杂度 | 风险密度 |
|------|------|--------|---------|
| FileManagerImpl.java | 1220 | ⭐⭐⭐⭐⭐ | 8 个 P0/P1 风险 |
| FileController.java | 239 | ⭐⭐⭐ | 5 个 @UnAuthentication |
| MinioUtils.java | 474 | ⭐⭐⭐ | 无分页、同步删除 |
| FileObjectMapper.java | 95 | ⭐⭐ | 5 个 @InterceptorIgnore |
| JwtService.java | 82 | ⭐⭐ | 密钥硬编码 |
| ExcelFileAnalysisHandle.java | 236 | ⭐⭐⭐⭐ | LLM 调用无超时、无缓存 |
| KnowledgeFileHandleTemplate.java | 168 | ⭐⭐⭐ | RestTemplate 同步调用 |

---

**报告结束**

⚠️ **重要提醒**:
1. 本报告仅识别风险，不提供解决方案
2. P0 级风险建议立即修复
3. P1 级风险建议在下个迭代修复
4. P2 级风险建议纳入技术债务管理

**下一步建议**:
- 召开技术评审会议，讨论 P0/P1 风险的修复优先级
- 制定详细的修复计划和时间表
- 建立代码审查机制，防止类似问题再次出现
