ALTER TABLE `logs`
	CHANGE COLUMN `type` `type` ENUM('ROOM_VISIT','FURNI_PURCHASE','ROOM_CHATLOG','MESSENGER_CHATLOG','COMMAND') NULL DEFAULT 'ROOM_CHATLOG' AFTER `id`;
