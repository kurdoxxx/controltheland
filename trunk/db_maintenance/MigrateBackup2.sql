﻿SET SQL_SAFE_UPDATES=0;
ALTER TABLE `gamedb`.`BACKUP_USER_STATUS` ADD COLUMN `deleteMe` BIT NULL;

UPDATE gamedb.BACKUP_USER_STATUS SET deleteMe = FALSE;
UPDATE gamedb.BACKUP_USER_STATUS AS u, gamedb.BACKUP_GENERIC_ITEM AS i
  SET u.deleteMe = TRUE
    WHERE i.backupEntry_id != u.backupEntry_id
      AND i.base_id = u.base_id
      AND u.base_id is not null;

DELETE FROM gamedb.BACKUP_LEVEL_COMPARISON_SYNC_ITEM_TYPE
  WHERE BACKUP_LEVEL_COMPARISON_id IN (
    SELECT id FROM gamedb.BACKUP_LEVEL_COMPARISON WHERE userState_id IN (SELECT id FROM gamedb.BACKUP_USER_STATUS WHERE deleteMe = TRUE));
DELETE FROM gamedb.BACKUP_LEVEL_COMPARISON WHERE userState_id in (SELECT id FROM gamedb.BACKUP_USER_STATUS WHERE deleteMe = TRUE);
DELETE FROM gamedb.BACKUP_USER_STATUS WHERE deleteMe = TRUE;

UPDATE gamedb.BACKUP_GENERIC_ITEM SET baseTarget_id = NULL WHERE backupEntry_id in(70, 71, 72, 73, 74, 77, 78, 79);
UPDATE gamedb.BACKUP_GENERIC_ITEM SET resourceTarget_id = NULL WHERE backupEntry_id in(70, 71, 72, 73, 74, 77, 78, 79);
DELETE FROM gamedb.BACKUP_GENERIC_ITEM WHERE backupEntry_id in(70, 71, 72, 73, 74, 77, 78, 79);

DELETE FROM gamedb.BACKUP_USER_STATUS WHERE id in (541, 554, 557);
DELETE FROM gamedb.BACKUP_BASE WHERE id in (802, 813);

DELETE FROM gamedb.BACKUP_LEVEL_COMPARISON WHERE id <= 353;
DELETE FROM gamedb.BACKUP_BASE WHERE id <= 815;
DELETE FROM gamedb.BACKUP_USER_ITEM_TYPE_ACCESS WHERE id <= 363;

ALTER TABLE `gamedb`.`BACKUP_USER_STATUS` DROP COLUMN `deleteMe`;

SET SQL_SAFE_UPDATES=1;