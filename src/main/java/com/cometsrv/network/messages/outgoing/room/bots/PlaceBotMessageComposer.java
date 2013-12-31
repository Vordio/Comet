package com.cometsrv.network.messages.outgoing.room.bots;

import com.cometsrv.game.rooms.types.components.bots.Bot;
import com.cometsrv.network.messages.headers.Composers;
import com.cometsrv.network.messages.types.Composer;

public class PlaceBotMessageComposer {
    public static Composer compose(Bot bot) {
        Composer msg = new Composer(Composers.PlaceBotMessageComposer);

        msg.writeInt(1);

        msg.writeInt(bot.getId());
        msg.writeString(bot.getName());
        msg.writeString(bot.getMotto());
        msg.writeString(bot.getFigure());
        msg.writeInt(bot.getId());//vid

        msg.writeInt(bot.getX());
        msg.writeInt(bot.getY());
        msg.writeDouble(bot.getZ());

        msg.writeInt(4);
        msg.writeInt(3); // 2 = user 4 = bot

        msg.writeString(bot.getGender().toLowerCase());
        msg.writeInt(bot.getOwnerId());
        msg.writeString(bot.getOwner());

        msg.writeInt(4);
        msg.writeShort(1);
        msg.writeShort(2);
        msg.writeShort(5);
        msg.writeShort(4);

        return msg;
    }
}
