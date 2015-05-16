package com.cometproject.server.network.messages.outgoing.quests;

import com.cometproject.api.networking.messages.IComposer;
import com.cometproject.server.network.messages.composers.MessageComposer;
import com.cometproject.server.network.messages.headers.Composers;

public class QuestStoppedMessageComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.CancelQuestMessageComposer;
    }

    @Override
    public void compose(IComposer msg) {
        msg.writeBoolean(false); //???
    }
}