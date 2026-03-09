
SET search_path TO public;

-- Ensure sequences exist (safe if they already exist).
CREATE SEQUENCE IF NOT EXISTS file_object_id_seq;
CREATE SEQUENCE IF NOT EXISTS file_recommendation_id_seq;

-- Table structure for file_object
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
COMMENT ON COLUMN "public"."file_object"."id" IS 'Primary key ID';
COMMENT ON COLUMN "public"."file_object"."tenant_id" IS 'Tenant ID';
COMMENT ON COLUMN "public"."file_object"."user_id" IS 'User ID';
COMMENT ON COLUMN "public"."file_object"."user_name" IS 'User name';
COMMENT ON COLUMN "public"."file_object"."object_name" IS 'Object key in MinIO';
COMMENT ON COLUMN "public"."file_object"."original_name" IS 'Original file name';
COMMENT ON COLUMN "public"."file_object"."bucket_name" IS 'Bucket name';
COMMENT ON COLUMN "public"."file_object"."content_type" IS 'MIME content type';
COMMENT ON COLUMN "public"."file_object"."file_size" IS 'File size in bytes';
COMMENT ON COLUMN "public"."file_object"."create_time" IS 'Created or uploaded time';
COMMENT ON COLUMN "public"."file_object"."update_time" IS 'Updated time';
COMMENT ON COLUMN "public"."file_object"."category" IS 'Top-level category';
COMMENT ON COLUMN "public"."file_object"."ability" IS 'Ability or application';
COMMENT ON COLUMN "public"."file_object"."content_overview" IS 'Content overview';
COMMENT ON COLUMN "public"."file_object"."file_status" IS 'File status: 0-unparsed, 1-parsed, 2-parse failed';
COMMENT ON COLUMN "public"."file_object"."sub_category" IS 'Sub category';
COMMENT ON COLUMN "public"."file_object"."knowledge_parse_state" IS 'Knowledge parsing state: 0-uploading/parsing, 1-embedding failed, 2-vector store insert failed, 3-success';
COMMENT ON COLUMN "public"."file_object"."third_level_category" IS 'Third-level category';
COMMENT ON TABLE "public"."file_object" IS 'File metadata';

-- Indexes structure for table file_object
CREATE INDEX "idx_object_name" ON "public"."file_object" USING btree (
    "object_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- Table structure for file_recommendation
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
COMMENT ON COLUMN "public"."file_recommendation"."id" IS 'Primary key ID';
COMMENT ON COLUMN "public"."file_recommendation"."tenant_id" IS 'Tenant ID';
COMMENT ON COLUMN "public"."file_recommendation"."file_id" IS 'File ID';
COMMENT ON COLUMN "public"."file_recommendation"."keyword" IS 'Keyword';
COMMENT ON COLUMN "public"."file_recommendation"."questions" IS 'Recommended questions';
COMMENT ON COLUMN "public"."file_recommendation"."create_time" IS 'Created time';
COMMENT ON COLUMN "public"."file_recommendation"."update_time" IS 'Updated time';
COMMENT ON TABLE "public"."file_recommendation" IS 'File recommendation records';
