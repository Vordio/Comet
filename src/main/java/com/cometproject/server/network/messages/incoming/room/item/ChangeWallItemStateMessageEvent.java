package com.cometproject.server.network.messages.incoming.room.item;

import com.cometproject.server.game.rooms.items.RoomItemWall;
import com.cometproject.server.network.messages.incoming.IEvent;
import com.cometproject.server.network.messages.types.Event;
import com.cometproject.server.network.sessions.Session;

public class ChangeWallItemStateMessageEvent implements IEvent {
    @Override
    public void handle(Session client, Event msg) throws Exception {
        int itemId = msg.readInt();

        RoomItemWall item = client.getPlayer().getEntity().getRoom().getItems().getWallItem(itemId);

        if (item == null) {
            return;
        }

        item.onInteract(client.getPlayer().getEntity(), 0, false);
    }
}