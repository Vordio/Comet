package com.cometproject.server.network.messages.incoming.room.action;

import com.cometproject.server.config.Locale;
import com.cometproject.server.game.CometManager;
import com.cometproject.server.game.rooms.entities.GenericEntity;
import com.cometproject.server.game.rooms.entities.RoomEntityType;
import com.cometproject.server.game.rooms.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.filter.FilterResult;
import com.cometproject.server.network.messages.incoming.IEvent;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.WisperMessageComposer;
import com.cometproject.server.network.messages.types.Event;
import com.cometproject.server.network.sessions.Session;

public class WhisperMessageEvent implements IEvent {
    public void handle(Session client, Event msg) {
        String text = msg.readString();

        String user = text.split(" ")[0];
        String message = text.replace(user + " ", "");

        GenericEntity userTo = client.getPlayer().getEntity().getRoom().getEntities().getEntityByName(user, RoomEntityType.PLAYER);

        if (userTo == null || user.equals(client.getPlayer().getData().getUsername()))
            return;

        String filteredMessage = TalkMessageEvent.filterMessage(message);

        if (!client.getPlayer().getPermissions().hasPermission("bypass_filter")) {
            FilterResult filterResult = CometManager.getRooms().getFilter().filter(message);

            if (filterResult.isBlocked()) {
                client.send(AdvancedAlertMessageComposer.compose(Locale.get("game.message.blocked").replace("%s", filterResult.getChatMessage())));
                return;
            } else if (filterResult.wasModified()) {
                filteredMessage = filterResult.getChatMessage();
            }
        }

        if (!client.getPlayer().getEntity().onChat(filteredMessage))
            return;

        client.send(WisperMessageComposer.compose(client.getPlayer().getEntity().getVirtualId(), filteredMessage));
        ((PlayerEntity) userTo).getPlayer().getSession().send(WisperMessageComposer.compose(client.getPlayer().getEntity().getVirtualId(), filteredMessage));

        for (PlayerEntity entity : client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntitiesByPermission("room_see_whisper")) {
            if (entity.getPlayer().getId() != client.getPlayer().getId() && !user.equals(entity.getUsername()))
                entity.getPlayer().getSession().send(WisperMessageComposer.compose(client.getPlayer().getEntity().getVirtualId(), "Whisper to " + user + ": " + filteredMessage));
        }
    }
}
