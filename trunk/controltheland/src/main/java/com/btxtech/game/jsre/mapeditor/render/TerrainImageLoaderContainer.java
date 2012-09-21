package com.btxtech.game.jsre.mapeditor.render;

import com.btxtech.game.jsre.client.ImageHandler;
import com.btxtech.game.jsre.client.renderer.ImageLoaderContainer;

/**
 * User: beat
 * Date: 17.09.12
 * Time: 15:46
 */
public class TerrainImageLoaderContainer extends ImageLoaderContainer<Integer> {
    private static final TerrainImageLoaderContainer INSTANCE = new TerrainImageLoaderContainer();

    /**
     * Singleton
     */
    private TerrainImageLoaderContainer() {
    }

    public static TerrainImageLoaderContainer getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getUrl(Integer integer) {
        return ImageHandler.getTerrainImageUrl(integer);
    }
}