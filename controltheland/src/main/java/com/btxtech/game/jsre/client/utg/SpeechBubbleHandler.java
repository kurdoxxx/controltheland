package com.btxtech.game.jsre.client.utg;

import com.btxtech.game.jsre.client.ClientBase;
import com.btxtech.game.jsre.client.cockpit.item.ItemCockpit;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.terrain.TerrainView;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import com.google.gwt.user.client.Timer;

/**
 * User: beat
 * Date: 12.11.2011
 * Time: 17:00:22
 */
public class SpeechBubbleHandler {
    private static final SpeechBubbleHandler INSTANCE = new SpeechBubbleHandler();
    private SpeechBubble itemSpeechBubble;
    private SyncItem syncItem;
    private boolean mouseOverSpeechBubble = false;
    private boolean mouseOverItemType = false;

    public static SpeechBubbleHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Singleton
     */
    private SpeechBubbleHandler() {
    }

    public void show(SyncItem syncItem) {
        if (syncItem.equals(this.syncItem)) {
            mouseOverItemType = true;
            return;
        }
        hide();
        mouseOverSpeechBubble = false;
        mouseOverItemType = true;
        itemSpeechBubble = new SpeechBubble(syncItem, setupHtml(syncItem), true);
        itemSpeechBubble.setBgColor(setupColor(syncItem));
        Index position = syncItem.getSyncItemArea().getPosition();
        TerrainView.getInstance().toAbsoluteIndex(position);
        this.syncItem = syncItem;
    }

    private void hide() {
        if (itemSpeechBubble != null) {
            itemSpeechBubble.close();
            itemSpeechBubble = null;
            syncItem = null;
        }
        mouseOverItemType = false;
        mouseOverSpeechBubble = false;
    }

    private String setupColor(SyncItem syncItem) {
        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (ClientBase.getInstance().isMyOwnProperty(syncBaseItem)) {
                return "#b2ffb2";
            } else {
                return "#ffa6a6";
            }
        } else {
            return "#FFFFFF";
        }
    }

    private String setupHtml(SyncItem syncItem) {
        StringBuilder builder = new StringBuilder();
        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (ClientBase.getInstance().isMyOwnProperty(syncBaseItem)) {
                builder.append(syncBaseItem.getBaseItemType().getDescription());
                if (ItemCockpit.hasItemCockpit(syncBaseItem) || syncBaseItem.hasSyncMovable()) {
                    builder.append("</br><b>Click on it!</b>");
                }
            } else {
                builder.append("Attack this <b>enemy</b> unit!");
                builder.append("</br>");
                builder.append(ClientBase.getInstance().getBaseName(syncBaseItem.getBase()));
            }
        } else {
            builder.append("Gather <b>money</b> from this gold.");
        }
        return builder.toString();
    }

    public void onSpeechBubbleMouseOver() {
        mouseOverSpeechBubble = true;
    }

    public void onSpeechBubbleMouseOut() {
        mouseOverSpeechBubble = false;
        deferredClose();
    }

    public void onSyncItemMouseOut(SyncItem syncItem) {
        if (syncItem.equals(this.syncItem)) {
            mouseOverItemType = false;
            deferredClose();
        }
    }

    private void deferredClose() {
        Timer timer = new Timer() {
            @Override
            public void run() {
                if (!mouseOverSpeechBubble && !mouseOverItemType) {
                    hide();
                }
            }
        };
        timer.schedule(500);
    }

    public void itemKilled(SyncItem syncItem) {
        if (syncItem.equals(this.syncItem)) {
            hide();
        }
    }
}
