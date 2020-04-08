DROP TABLE IF EXISTS developer;

CREATE TABLE if not exists `developer`
(
    `name`      varchar(128) NOT NULL COMMENT '姓名',
    `username`  varchar(128) NOT NULL COMMENT '用户名',
    `email`     varchar(128) NOT NULL COMMENT '邮箱',
    `mobile`    varchar(128) NOT NULL COMMENT '手机号',
    `receivers` text         NOT NULL COMMENT '手机号',
    PRIMARY KEY (`name`)
);

CREATE TABLE if not exists `issue`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '标识',
    `issue_iid`    int(10)             NOT NULL COMMENT 'issue标识',
    `group_id`     int(10)             NOT NULL COMMENT '组标识',
    `group_name`   varchar(128)        NOT NULL COMMENT '组名',
    `project_id`   int(10)             NOT NULL COMMENT '项目标识',
    `project_name` varchar(128)        NOT NULL COMMENT '项目名',
    `state`        varchar(128)        NOT NULL COMMENT '状态',
    `title`        varchar(256)        NOT NULL COMMENT '标题',
    `labels`       varchar(128) COMMENT '标签',
    `description`  text COMMENT '描述',
    `author`       json                not null COMMENT '创建人',
    `assignee`     json COMMENT '指派人',
    `milestone`    json COMMENT '里程碑',
    `due_date`     date COMMENT '到期日',
    `created_at`   datetime            NOT NULL COMMENT '创建时间',
    `updated_at`   datetime COMMENT '修改时间',
    `closed_at`    datetime COMMENT '关闭时间',
    PRIMARY KEY (`id`)
);

CREATE TABLE if not exists `participant`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '标识',
    `issue_iid`    int(10)             NOT NULL COMMENT 'issue标识',
    `group_id`     int(10)             NOT NULL COMMENT '组标识',
    `group_name`   varchar(128)        NOT NULL COMMENT '组名',
    `project_id`   int(10)             NOT NULL COMMENT '项目标识',
    `project_name` varchar(128)        NOT NULL COMMENT '项目名',
    `title`        varchar(256)        NOT NULL COMMENT '标题',
    `name`         varchar(128)        NOT NULL COMMENT '姓名',
    `username`     varchar(128)        NOT NULL COMMENT '用户名',
    PRIMARY KEY (`id`)
);