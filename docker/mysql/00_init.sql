SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `yzpc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `yzpc`;

source /docker-entrypoint-initdb.d/sql/yzpc_mysql_data.sql;

SET FOREIGN_KEY_CHECKS = 1;
