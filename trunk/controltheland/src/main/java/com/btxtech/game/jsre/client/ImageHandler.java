/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.game.jsre.client;

import com.btxtech.game.jsre.client.common.Constants;
import com.btxtech.game.jsre.common.gameengine.itemType.BaseItemType;
import com.btxtech.game.jsre.common.gameengine.itemType.BuildupStep;
import com.btxtech.game.jsre.common.gameengine.itemType.ItemType;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: beat
 * Date: Jun 14, 2009
 * Time: 4:35:18 PM
 */
public class ImageHandler {
    public static final int EXPLOSION_EDGE_LENGTH = 200;
    public static final int DEFAULT_IMAGE_NUMBER = 9;
    private static final int QUEST_PROGRESS_IMAGES_HEIGHT = 20;

    public static final String PNG_SUFFIX = ".png";
    public static final String IMAGES = "images";
    public static final String EXPLOSION = "effects";
    public static final String ICONS = "icons";
    public static final String TIPS = "tips";
    public static final String COCKPIT = "/" + IMAGES + "/cockpit/";
    public static final String BTN_IMAGE_PATH = COCKPIT;
    public static final String BTN_UP_IMAGE = "-up.png";
    public static final String BTN_DOWN_IMAGE = "-down.png";
    public static final String SPLASH_IMAGE_PREFIX = "/images/splash/";

    /**
     * Singleton
     */
    private ImageHandler() {

    }

    public static String getImageBackgroundUrl(String bgImageUrl) {
        StringBuilder builder = new StringBuilder();
        builder.append("url(");
        builder.append(bgImageUrl);
        builder.append(") no-repeat 0px 0px");
        return builder.toString();
    }

    public static String getItemTypeImageBackgroundUrl(SyncItem syncItem) {
        int xOffset = syncItem.getSyncItemArea().getBoundingBox().angelToImageOffset(syncItem.getSyncItemArea().getAngel());
        StringBuilder builder = new StringBuilder();
        builder.append("url(");
        builder.append(getItemTypeSpriteMapUrl(syncItem.getItemType().getId()));
        builder.append(") no-repeat -");
        builder.append(xOffset);
        builder.append("px 0px");
        return builder.toString();
    }

    public static Image getItemTypeImage(ItemType itemType, Integer width, Integer height) {
        String url = getItemTypeSpriteMapUrl(itemType.getId());
        Image image;
        if (width != null && height != null) {
            int xOffset = itemType.getBoundingBox().getCosmeticImageIndex() * width;
            image = new Image(url, xOffset, 0, width, height);
            image.setPixelSize(width, height);
            String spriteWidth = Integer.toString(width * itemType.getBoundingBox().getAngelCount());
            image.getElement().getStyle().setProperty("backgroundSize", spriteWidth + "px " + Integer.toString(height) + "px");
        } else {
            int xOffset = itemType.getBoundingBox().getCosmeticImageIndex() * itemType.getBoundingBox().getImageWidth();
            image = new Image(url, xOffset, 0, itemType.getBoundingBox().getImageWidth(), itemType.getBoundingBox().getImageHeight());
        }
        return image;
    }

    public static String getQuestProgressItemTypeImageString(ItemType itemType) {
        double scale = (double) QUEST_PROGRESS_IMAGES_HEIGHT / (double) itemType.getBoundingBox().getImageHeight();
        StringBuilder builder = new StringBuilder();
        builder.append("<img border='0' src='/game/clear.cache.gif' style='width:");
        builder.append((int) (itemType.getBoundingBox().getImageWidth() * scale));
        builder.append("px; height:");
        builder.append(QUEST_PROGRESS_IMAGES_HEIGHT);
        builder.append("px; background-image: url(");
        builder.append(getItemTypeSpriteMapUrl(itemType.getId()));
        builder.append("); background-repeat: no-repeat; background-position: -");
        builder.append((int) (itemType.getBoundingBox().angelToImageOffset(itemType.getBoundingBox().getCosmeticAngel()) * scale));
        builder.append("px 0px; background-size: ");
        builder.append((int) (itemType.getBoundingBox().getImageWidth() * scale * itemType.getBoundingBox().getAngelCount()));
        builder.append("px ");
        builder.append(QUEST_PROGRESS_IMAGES_HEIGHT);
        builder.append("px;");
        builder.append("'></img>");
        return builder.toString();
    }

    public static String getItemTypeSpriteMapUrl(int itemId) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.ITEM_IMAGE_URL);
        url.append("?");
        url.append(Constants.ITEM_TYPE_SPRITE_MAP_ID);
        url.append("=");
        url.append(itemId);
        return url.toString();
    }

    public static String getBuildupStepImageUrl(BaseItemType baseItemType, BuildupStep buildupStep) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.ITEM_IMAGE_URL);
        url.append("?");
        url.append(Constants.TYPE);
        url.append("=");
        url.append(Constants.TYPE_BUILDUP_STEP);
        url.append("&");
        url.append(Constants.ITEM_TYPE_ID);
        url.append("=");
        url.append(baseItemType.getId());
        url.append("&");
        url.append(Constants.ITEM_IMAGE_BUILDUP_STEP);
        url.append("=");
        url.append(buildupStep.getImageId());
        return url.toString();
    }

    public static String getMuzzleFlashImageUrl(BaseItemType baseItemType) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.MUZZLE_ITEM_IMAGE_URL);
        url.append("?");
        url.append(Constants.ITEM_TYPE_ID);
        url.append("=");
        url.append(baseItemType.getId());
        url.append("&");
        url.append(Constants.TYPE);
        url.append("=");
        url.append(Constants.TYPE_IMAGE);
        return url.toString();
    }

    public static String getSurfaceImagesUrl(int id) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.TERRAIN_CONTROLLER_URL);
        url.append("?");
        url.append(Constants.TERRAIN_IMG_TYPE);
        url.append("=");
        url.append(Constants.TERRAIN_IMG_TYPE_SURFACE);
        url.append("&");
        url.append(Constants.TERRAIN_IMG_TYPE_IMG_ID);
        url.append("=");
        url.append(Integer.toString(id));
        return url.toString();
    }

    public static String getTerrainImageUrl(int id) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.TERRAIN_CONTROLLER_URL);
        url.append("?");
        url.append(Constants.TERRAIN_IMG_TYPE);
        url.append("=");
        url.append(Constants.TERRAIN_IMG_TYPE_FOREGROUND);
        url.append("&");
        url.append(Constants.TERRAIN_IMG_TYPE_IMG_ID);
        url.append("=");
        url.append(Integer.toString(id));
        return url.toString();
    }

    public static String getInventoryItemUrl(int id) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.INVENTORY_PATH);
        url.append("?");
        url.append(Constants.INVENTORY_TYPE);
        url.append("=");
        url.append(Constants.INVENTORY_TYPE_ITEM);
        url.append("&");
        url.append(Constants.INVENTORY_ID);
        url.append("=");
        url.append(Integer.toString(id));
        return url.toString();
    }

    public static String getInventoryArtifactUrl(int id) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.INVENTORY_PATH);
        url.append("?");
        url.append(Constants.INVENTORY_TYPE);
        url.append("=");
        url.append(Constants.INVENTORY_TYPE_ARTIFACT);
        url.append("&");
        url.append(Constants.INVENTORY_ID);
        url.append("=");
        url.append(Integer.toString(id));
        return url.toString();
    }

    public static Image getButtonUpImage(String name) {
        return new Image(BTN_IMAGE_PATH + name + BTN_UP_IMAGE);
    }

    public static Image getButtonDownImage(String name) {
        return new Image(BTN_IMAGE_PATH + name + BTN_DOWN_IMAGE);
    }

    public static String getSplashImageUrl(String name) {
        return SPLASH_IMAGE_PREFIX + name;
    }

    public static Image getTerrainImage(int id) {
        return new Image(getTerrainImageUrl(id));
    }

    public static Image getSurfaceImage(int id) {
        return new Image(getSurfaceImagesUrl(id));
    }

    public static String getExplosion() {
        return "/" + IMAGES + "/" + EXPLOSION + "/" + "ex4" + PNG_SUFFIX;
    }

    public static Widget getTipImage(String tip) {
        return new Image("/" + IMAGES + "/" + TIPS + "/" + tip);
    }

    public static String getCockpitImageUrl(String image) {
        return COCKPIT + image;
    }

    public static Image getCockpitImage(String image) {
        return new Image(getCockpitImageUrl(image));
    }
}
