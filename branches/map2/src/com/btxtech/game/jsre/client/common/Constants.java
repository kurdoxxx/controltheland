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

package com.btxtech.game.jsre.client.common;

/**
 * User: beat
 * Date: May 23, 2009
 * Time: 3:07:05 PM
 */
public class Constants {
    // Terrain
    public static final String URL_PARAM_TERRAIN_TILE_ID = "id";
    public static final int TILE_WIDTH = 100;
    public static final int TILE_HEIGHT = 100;
    public static final int SCROLL_DISTANCE = 500;
    public static final String TERRAIN = "/spring/terrain?id=";

    // Item Images
    public static final String ITEM_IMAGE_URL = "/spring/item";
    public static final String MUZZLE_ITEM_IMAGE_URL = "/spring/muzzle";
    public static final String TYPE = "type";
    public static final String TYPE_IMAGE = "img";
    public static final String TYPE_SOUND = "snd";
    public static final String ITEM_IMAGE_ID = "id";
    public static final String ITEM_IMAGE_INDEX = "ix";

    // zIndex
    public static final int Z_INDEX_DIALOG = 12;
    public static final int Z_INDEX_GROUP_SELECTION_FRAME = 11;
    public static final int Z_INDEX_SPEECH_BUBBLE = 10;
    public static final int Z_INDEX_PLACEABLE_PREVIEW = 9;
    public static final int Z_INDEX_TOP_MAP_PANEL = 8;
    public static final int Z_INDEX_EXPLOSION = 7;
    public static final int Z_INDEX_MUZZLE_FLASH = 6;
    public static final int Z_INDEX_MACHINE_GUN_ATTACK = 5;
    public static final int Z_INDEX_MOVABLE = 4;
    public static final int Z_INDEX_MONEY = 3;
    public static final int Z_INDEX_BUILDING = 2;
    public static final int Z_INDEX_TERRAIN = 1;

    // Financial
    public static final int START_MONEY = 50000;
    public static final int MONEY_STACK_COUNT = 5;

    // Distances
    public static final int MIN_FREE_MONEY_DISTANCE = 100;


}
