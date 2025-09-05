
SET search_path TO public;

-- Ensure sequences exist (safe if they already exist).
CREATE SEQUENCE IF NOT EXISTS file_object_id_seq;
CREATE SEQUENCE IF NOT EXISTS file_recommendation_id_seq;
-- ----------------------------
-- Table structure for file_object
-- ----------------------------
CREATE TABLE "public"."file_object" (
                                        "id" int8 NOT NULL DEFAULT nextval('file_object_id_seq'::regclass),
                                        "tenant_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0'::character varying,
                                        "user_id" int8,
                                        "user_name" varchar(64) COLLATE "pg_catalog"."default",
                                        "object_name" text COLLATE "pg_catalog"."default",
                                        "original_name" text COLLATE "pg_catalog"."default",
                                        "bucket_name" varchar(64) COLLATE "pg_catalog"."default",
                                        "content_type" varchar(128) COLLATE "pg_catalog"."default",
                                        "file_size" int8 DEFAULT 0,
                                        "category" varchar(64) COLLATE "pg_catalog"."default",
                                        "ability" varchar(64) COLLATE "pg_catalog"."default",
                                        "content_overview" text COLLATE "pg_catalog"."default",
                                        "file_status" int2 DEFAULT 0,
                                        "sub_category" varchar(256) COLLATE "pg_catalog"."default",
                                        "knowledge_parse_state" int2,
                                        "third_level_category" varchar(256) COLLATE "pg_catalog"."default",
                                        "create_time" timestamp(6),
                                        "update_time" timestamp(6),
                                        CONSTRAINT "file_object_pkey" PRIMARY KEY ("id"),
                                        CONSTRAINT "uk_file_object_user_original_name" UNIQUE ("user_id", "original_name")
);
COMMENT ON COLUMN "public"."file_object"."id" IS '主键，自增 ID';
COMMENT ON COLUMN "public"."file_object"."tenant_id" IS '租户 ID';
COMMENT ON COLUMN "public"."file_object"."user_id" IS '用户 ID';
COMMENT ON COLUMN "public"."file_object"."user_name" IS '用户 名称';
COMMENT ON COLUMN "public"."file_object"."object_name" IS 'MinIO 中对象的 key（路径）';
COMMENT ON COLUMN "public"."file_object"."original_name" IS '原始文件名';
COMMENT ON COLUMN "public"."file_object"."bucket_name" IS '桶名称';
COMMENT ON COLUMN "public"."file_object"."content_type" IS 'MIME 文件类型';
COMMENT ON COLUMN "public"."file_object"."file_size" IS '文件大小（字节）';
COMMENT ON COLUMN "public"."file_object"."create_time" IS '创建时间 / 上传时间';
COMMENT ON COLUMN "public"."file_object"."update_time" IS '更新时间 ';
COMMENT ON COLUMN "public"."file_object"."category" IS '所属分类';
COMMENT ON COLUMN "public"."file_object"."ability" IS '对应能力/应用';
COMMENT ON COLUMN "public"."file_object"."content_overview" IS '内容概述';
COMMENT ON COLUMN "public"."file_object"."file_status" IS '文件状态【0-未解析，1-解析完成，2-解释失败】';
COMMENT ON COLUMN "public"."file_object"."sub_category" IS '所属子类';
COMMENT ON COLUMN "public"."file_object"."knowledge_parse_state" IS '知识库解析状态【0-正在上传/解析中，1-embedding失败，2-向量库插入失败，3-成功】';
COMMENT ON COLUMN "public"."file_object"."third_level_category" IS '三级分类';
COMMENT ON TABLE "public"."file_object" IS 'MinIO 文件元数据表';

-- ----------------------------
-- Indexes structure for table file_object
-- ----------------------------
CREATE INDEX "idx_object_name" ON "public"."file_object" USING btree (
    "object_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );


-- ----------------------------
-- Table structure for file_recommendation
-- ----------------------------
CREATE TABLE "public"."file_recommendation" (
                                                "id" int8 NOT NULL DEFAULT nextval('file_recommendation_id_seq'::regclass),
                                                "tenant_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '0'::character varying,
                                                "file_id" int8,
                                                "keyword" text COLLATE "pg_catalog"."default",
                                                "questions" text COLLATE "pg_catalog"."default",
                                                "create_time" timestamp(6),
                                                "update_time" timestamp(6),
                                                CONSTRAINT "file_recommendation_pkey" PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."file_recommendation"."id" IS '主键，自增 ID';
COMMENT ON COLUMN "public"."file_recommendation"."tenant_id" IS '租户 ID';
COMMENT ON COLUMN "public"."file_recommendation"."file_id" IS '文件id';
COMMENT ON COLUMN "public"."file_recommendation"."keyword" IS '关键词';
COMMENT ON COLUMN "public"."file_recommendation"."questions" IS '问题集';
COMMENT ON COLUMN "public"."file_recommendation"."create_time" IS '创建时间 / 上传时间';
COMMENT ON COLUMN "public"."file_recommendation"."update_time" IS '更新时间 ';
COMMENT ON TABLE "public"."file_recommendation" IS '文件推荐问题生成';


