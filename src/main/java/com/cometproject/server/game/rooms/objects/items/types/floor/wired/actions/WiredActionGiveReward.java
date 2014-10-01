package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.CometManager;
import com.cometproject.server.game.items.types.ItemDefinition;
import com.cometproject.server.game.players.components.types.InventoryItem;
import com.cometproject.server.game.rooms.entities.GenericEntity;
import com.cometproject.server.game.rooms.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.network.messages.outgoing.catalog.SendPurchaseAlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.wired.WiredRewardMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.UpdateInventoryMessageComposer;
import com.cometproject.server.storage.queries.items.ItemDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WiredActionGiveReward extends WiredActionItem {
    private static final Map<Integer, Map<Integer, Long>> rewardTimings = Maps.newConcurrentMap();

    private static final int PARAM_HOW_OFTEN = 0;
    private static final int PARAM_UNIQUE = 1;
    private static final int PARAM_TOTAL_REWARD_LIMIT = 2;

    private static final int REWARD_LIMIT_ONCE = 0;
    private static final int REWARD_LIMIT_DAY = 1;
    private static final int REWARD_LIMIT_HOUR = 2;

    private static final long ONE_DAY = 86400;
    private static final long ONE_HOUR = 3600;

    // increments and will be reset when the room is unloaded.
    private int totalRewardCounter = 0;

    private List<Reward> rewards;

    /**
     * The default constructor
     *
     * @param id       The ID of the item
     * @param itemId   The ID of the item definition
     * @param roomId   The ID of the room
     * @param owner    The ID of the owner
     * @param x        The position of the item on the X axis
     * @param y        The position of the item on the Y axis
     * @param z        The position of the item on the z axis
     * @param rotation The orientation of the item
     * @param data     The JSON object associated with this item
     */
    public WiredActionGiveReward(int id, int itemId, int roomId, int owner, int x, int y, double z, int rotation, String data) {
        super(id, itemId, roomId, owner, x, y, z, rotation, data);

        if (!rewardTimings.containsKey(this.getId())) {
            rewardTimings.put(this.getId(), Maps.newConcurrentMap());
        }
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public int getInterface() {
        return 17;
    }

    @Override
    public boolean evaluate(GenericEntity entity, Object data) {
        if (this.getWiredData().getParams().size() != 3 || !(entity instanceof PlayerEntity) || this.rewards.size() == 0) {
            return false;
        }

        PlayerEntity playerEntity = ((PlayerEntity) entity);

        final int howOften = this.getWiredData().getParams().get(PARAM_HOW_OFTEN);
        final boolean unique = this.getWiredData().getParams().get(PARAM_UNIQUE) == 1;
        final int totalRewardLimit = this.getWiredData().getParams().get(PARAM_TOTAL_REWARD_LIMIT);

        int errorCode = -1;

        switch (howOften) {
            case REWARD_LIMIT_ONCE:
                if (rewardTimings.get(this.getId()).containsKey(playerEntity.getPlayerId())) {
                    errorCode = 1;
                }
                break;

            case REWARD_LIMIT_DAY:
                if (rewardTimings.get(this.getId()).containsKey(playerEntity.getPlayerId())) {
                    long lastReward = rewardTimings.get(this.getId()).get(playerEntity.getPlayerId());

                    if ((Comet.getTime() - lastReward) < ONE_DAY) {
                        errorCode = 2;
                    }
                }
                break;

            case REWARD_LIMIT_HOUR:
                if (rewardTimings.get(this.getId()).containsKey(playerEntity.getPlayerId())) {
                    long lastReward = rewardTimings.get(this.getId()).get(playerEntity.getPlayerId());

                    if ((Comet.getTime() - lastReward) < ONE_HOUR) {
                        errorCode = 3;
                    }
                }
                break;
        }

        if (totalRewardLimit != 0) {
            if (this.totalRewardCounter >= totalRewardLimit) {
                errorCode = 0;
            }
        }

        if(errorCode != -1) {
            playerEntity.getPlayer().getSession().send(WiredRewardMessageComposer.compose(errorCode));
            return false;
        }

        this.totalRewardCounter++;

        final double probability = Math.random();
        boolean receivedReward = false;

        for (Reward reward : this.rewards) {
            boolean giveReward = unique || (probability <= reward.probability);

            if (giveReward) {
                if (reward.isBadge) {
                    if (!playerEntity.getPlayer().getInventory().hasBadge(reward.productCode)) {
                        playerEntity.getPlayer().getInventory().addBadge(reward.productCode, true);
                        continue;
                    }
                } else {
                    String[] itemData = reward.productCode.split("%");
                    String extraData = "0";

                    if (itemData.length == 2) {
                        extraData = itemData[1];
                    }

                    if (!StringUtils.isNumeric(itemData[0]))
                        continue;

                    int itemId = Integer.parseInt(reward.productCode);
                    ItemDefinition itemDefinition = CometManager.getItems().getDefinition(itemId);

                    if (itemDefinition != null) {
                        int newItem = ItemDao.createItem(playerEntity.getPlayerId(), itemId, extraData);

                        InventoryItem inventoryItem = new InventoryItem(newItem, itemId, extraData);

                        playerEntity.getPlayer().getInventory().addItem(inventoryItem);

                        playerEntity.getPlayer().getSession().send(UpdateInventoryMessageComposer.compose());
                        playerEntity.getPlayer().getSession().send(SendPurchaseAlertMessageComposer.compose(Arrays.asList(inventoryItem)));

                        playerEntity.getPlayer().getSession().send(WiredRewardMessageComposer.compose(6));
                    }
                }

                receivedReward = true;
            }
        }


        if(!receivedReward) {
            playerEntity.getPlayer().getSession().send(WiredRewardMessageComposer.compose(4));
        }

        if(rewardTimings.get(this.getId()).containsKey(playerEntity.getPlayerId())) {
            rewardTimings.get(this.getId()).replace(playerEntity.getPlayerId(), Comet.getTime());
        } else {
            rewardTimings.get(this.getId()).put(playerEntity.getPlayerId(), Comet.getTime());
        }
        return false;
    }

    @Override
    public void onDataRefresh() {
        if (this.rewards == null)
            this.rewards = Lists.newArrayList();
        else
            this.rewards.clear();

        if (rewardTimings.containsKey(this.getId())) {
            rewardTimings.get(this.getId()).clear();
        }

        final String[] data = this.getWiredData().getText().split(";");

        for (String reward : data) {
            final String[] rewardData = reward.split(",");
            if (rewardData.length != 3 || !StringUtils.isNumeric(rewardData[2])) continue;

            this.rewards.add(new Reward(rewardData[0].equals("0"), rewardData[1], Integer.parseInt(rewardData[2])));
        }
    }

    @Override
    public void onPickup() {
        super.onPickup();
        rewardTimings.get(this.getId()).remove(this.getId());
    }

    public class Reward {
        private boolean isBadge;
        private String productCode;
        private int probability;

        public Reward(boolean isBadge, String productCode, int probability) {
            this.isBadge = isBadge;
            this.productCode = productCode;
            this.probability = probability;
        }
    }
}