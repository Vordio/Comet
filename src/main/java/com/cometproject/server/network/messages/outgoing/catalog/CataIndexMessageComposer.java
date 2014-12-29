package com.cometproject.server.network.messages.outgoing.catalog;

import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.catalog.types.CatalogItem;
import com.cometproject.server.game.catalog.types.CatalogPage;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.items.types.ItemDefinition;
import com.cometproject.server.network.messages.headers.Composers;
import com.cometproject.server.network.messages.types.Composer;

import java.util.List;


public class CataIndexMessageComposer {

    public static Composer compose(int rank) {
        List<CatalogPage> pages = CatalogManager.getInstance().getPagesForRank(rank);

        Composer msg = new Composer(Composers.CatalogIndexMessageComposer);

        msg.writeBoolean(true);
        msg.writeInt(0);
        msg.writeInt(-1);
        msg.writeString("root");
        msg.writeString("");
        msg.writeInt(0);

        msg.writeInt(count(-1, pages));
        for (CatalogPage page : pages) {
            if (page.getParentId() != -1) {
                continue;
            }

            msg.writeBoolean(true); // visible
            msg.writeInt(page.getIcon());
            msg.writeInt(page.getId());

            msg.writeString(page.getLinkName().equals("undefined") ? page.getCaption().toLowerCase().replaceAll("[^A-Za-z0-9]", "").replace(" ", "_") : page.getLinkName());
            msg.writeString(page.getCaption());

            msg.writeInt(page.getOfferSize());

            for (CatalogItem item : page.getItems().values()) {
                ItemDefinition itemDefinition = ItemManager.getInstance().getDefinition(item.getItems().get(0));

                if(itemDefinition != null) {
                    int offerId = itemDefinition.getOfferId();

                    if (offerId != -1)
                        msg.writeInt(offerId);
                }
            }

            msg.writeInt(count(page.getId(), pages));

            for (CatalogPage child : pages) {
                if (child.getParentId() != page.getId()) {
                    continue;
                }

                msg.writeBoolean(true); // visible
                msg.writeInt(child.getIcon());
                msg.writeInt(child.getId());
                msg.writeString(page.getLinkName().equals("undefined") ? page.getCaption().toLowerCase().replaceAll("[^A-Za-z0-9]", "").replace(" ", "_") : page.getLinkName());
                msg.writeString(child.getCaption());
                msg.writeInt(child.getOfferSize());

                for (CatalogItem item : child.getItems().values()) {
                    ItemDefinition itemDefinition = ItemManager.getInstance().getDefinition(item.getItems().get(0));

                    if (itemDefinition != null && itemDefinition.getOfferId() != -1 && itemDefinition.getOfferId() != 0) {
                        msg.writeInt(itemDefinition.getOfferId());
                    }
                }


                msg.writeInt(0); //tree size
            }
        }

        msg.writeBoolean(false);
        msg.writeString("NORMAL");

        return msg;
    }

    private static int count(int index, List<CatalogPage> pages) {
        int i = 0;

        for (CatalogPage page : pages) {
            if (page.getParentId() == index) {
                i++;
            }
        }

        return i;
    }
}
