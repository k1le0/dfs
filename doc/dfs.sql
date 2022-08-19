

create table dfs_lock
(
    id int auto_increment,
    `key` varchar(128) not null,
    expire_time datetime null,
    create_time datetime not null,
    constraint dfs_lock_pk
        primary key (id)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

create unique index dfs_lock_key_uindex
    on dfs_lock (`key`);

drop PROCEDURE if exists clearDsExpiredAndAbnormalLocks;
CREATE PROCEDURE clearDsExpiredAndAbnormalLocks()
begin
    DECLARE lock_cursor int DEFAULT 0;-- 定义游标循环数
    DECLARE lock_id int(11);-- 主键
    DECLARE list CURSOR FOR SELECT id from dfs_lock where (TIME_TO_SEC(timediff(now(),heartbeat_time))>60) or (expire_time IS NOT NULL and expire_time <=now());
DECLARE CONTINUE HANDLER FOR NOT FOUND SET lock_cursor=1;
open list;
fetch list into lock_id;
while lock_cursor<>1 do
delete from dfs_lock where id=lock_id;
fetch list into lock_id;
end while;
close list;
end;

CREATE EVENT IF NOT EXISTS clearDsExpiredAndAbnormalLocks_event
    on schedule EVERY 20 SECOND
do call clearDsExpiredAndAbnormalLocks();










create table dfs_node
(
    id             int auto_increment
        primary key,
    node_id        int      not null comment '节点id',
    heartbeat_time datetime not null,
    create_time    datetime not null,
    constraint dfs_node_node_id_uindex
        unique (node_id)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 'dfs服务节点信息';






drop PROCEDURE if exists clearDFSNodeRegistry;
CREATE PROCEDURE clearDFSNodeRegistry()
begin
    DECLARE node_cursor int DEFAULT 0;-- 定义游标循环数
    DECLARE node_id int(11);-- 主键
    DECLARE list CURSOR FOR SELECT id from dfs_node where TIME_TO_SEC(timediff(now(),heartbeat_time))>60;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET node_cursor=1;
open list;
fetch list into node_id;
while node_cursor<>1 do
delete from dfs_node where id=node_id;
fetch list into node_id;
end while;
    -- 关闭游标
close list;
end;


CREATE EVENT IF NOT EXISTS DFSNodeRegistry_event
    on schedule EVERY 20 SECOND
do call clearDFSNodeRegistry();
/*my.ini
event_scheduler=ON*/
show variables like '%event_sche%';
set global event_scheduler=1;