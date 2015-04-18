package com.cometproject.server.network.messages.incoming.room.moderation;

import com.cometproject.server.game.rooms.types.RoomInstance;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.room.settings.RoomBannedListMessageComposer;
import com.cometproject.server.network.messages.types.MessageEvent;
import com.cometproject.server.network.sessions.Session;


public class GetBannedUsersMessageEvent implements Event {
    public void handle(Session client, MessageEvent msg) {
        if (client.getPlayer().getEntity() == null || client.getPlayer().getEntity().getRoom() == null) return;

        RoomInstance room = client.getPlayer().getEntity().getRoom();

        if ((room.getData().getOwnerId() != client.getPlayer().getId() && !client.getPlayer().getPermissions().hasPermission("room_full_control"))) {
            return;
        }

        client.send(new RoomBannedListMessageComposer(room.getId(), room.getRights().getBannedPlayers()));
    }
}