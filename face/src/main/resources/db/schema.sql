PRAGMA foreign_keys = false;

-- ----------------------------
-- Table structure for face
-- ----------------------------
CREATE TABLE "face" (
    "id" TEXT NOT NULL,
    "vector" blob NOT NULL,
    "metadata" TEXT,
    PRIMARY KEY ("id"),
    CONSTRAINT "id" UNIQUE ("id" ASC)
);

PRAGMA foreign_keys = true;
