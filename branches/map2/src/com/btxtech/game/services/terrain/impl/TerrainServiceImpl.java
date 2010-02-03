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

package com.btxtech.game.services.terrain.impl;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainImagePosition;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainType;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainImage;
import com.btxtech.game.services.terrain.DbTerrainImagePosition;
import com.btxtech.game.services.terrain.DbTerrainSetting;
import com.btxtech.game.services.terrain.TerrainChangeListener;
import com.btxtech.game.services.terrain.TerrainFieldTile;
import com.btxtech.game.services.terrain.DbTerrainImage;
import com.btxtech.game.services.terrain.TerrainService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * User: beat
 * Date: May 22, 2009
 * Time: 11:56:20 AM
 */
public class TerrainServiceImpl implements TerrainService {
    private HibernateTemplate hibernateTemplate;
    private List<TerrainImagePosition> terrainImagePositions = new ArrayList<TerrainImagePosition>();
    private List<TerrainImage> terrainImages = new ArrayList<TerrainImage>();
    private HashMap<Integer, DbTerrainImage> dbTerrainImages = new HashMap<Integer, DbTerrainImage>();
    private Log log = LogFactory.getLog(TerrainServiceImpl.class);
    private ArrayList<TerrainChangeListener> terrainChangeListeners = new ArrayList<TerrainChangeListener>();
    private DbTerrainSetting dbTerrainSetting;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    @PostConstruct
    public void init() {
        loadTerrain();
    }


    private void loadTerrain() {
        List<DbTerrainSetting> dbTerrainSettings = hibernateTemplate.loadAll(DbTerrainSetting.class);
        if (dbTerrainSettings.isEmpty()) {
            dbTerrainSetting = createDefaultTerrainSettings();
        } else {
            if (dbTerrainSettings.size() > 1) {
                log.error("More than one terrain setting row found: " + dbTerrainSettings.size());
            }
            dbTerrainSetting = dbTerrainSettings.get(0);
        }
        terrainImagePositions.clear();
        List<DbTerrainImagePosition> dbTerrainImagePositions = hibernateTemplate.loadAll(DbTerrainImagePosition.class);
        for (DbTerrainImagePosition dbTerrainImagePosition : dbTerrainImagePositions) {
            terrainImagePositions.add(dbTerrainImagePosition.createTerrainImagePosition());
        }


        List<DbTerrainImage> imageList = hibernateTemplate.loadAll(DbTerrainImage.class);
        terrainImages.clear();
        dbTerrainImages = new HashMap<Integer, DbTerrainImage>();
        for (DbTerrainImage dbTerrainImage : imageList) {
            dbTerrainImages.put(dbTerrainImage.getId(), dbTerrainImage);
            terrainImages.add(dbTerrainImage.createTerrainImage());
        }

        for (TerrainChangeListener terrainChangeListener : terrainChangeListeners) {
            terrainChangeListener.onTerrainChanged();
        }
    }

    private DbTerrainSetting createDefaultTerrainSettings() {
        DbTerrainSetting dbTerrainSetting = new DbTerrainSetting();
        dbTerrainSetting.setTileWidth(100);
        dbTerrainSetting.setTileHeight(100);
        dbTerrainSetting.setTileXCount(50);
        dbTerrainSetting.setTileYCount(50);
        hibernateTemplate.saveOrUpdate(dbTerrainSetting);
        return dbTerrainSetting;
    }

    @Override
    public void addTerrainChangeListener(TerrainChangeListener terrainChangeListener) {
        terrainChangeListeners.add(terrainChangeListener);
    }

    @Override
    public void removeTerrainChangeListener(TerrainChangeListener terrainChangeListener) {
        terrainChangeListeners.remove(terrainChangeListener);
    }

    @Override
    public DbTerrainSetting getTerrainSetting() {
        return dbTerrainSetting;
    }

    @Override
    public DbTerrainImage getTerrainImage(int id) {
        DbTerrainImage dbTerrainImage = dbTerrainImages.get(id);
        if (dbTerrainImage == null) {
            throw new IllegalArgumentException("No terrain image for id: " + id);
        }
        return dbTerrainImage;
    }

    @Override
    public List<DbTerrainImage> getDbTerrainImagesCopy() {
        return new ArrayList<DbTerrainImage>(dbTerrainImages.values());
    }

    @Override
    public Collection<TerrainImage> getTerrainImages() {
        return terrainImages;
    }

    @Override
    public List<TerrainImagePosition> getTerrainImagePositions() {
        return terrainImagePositions;
    }

    @Override
    public void saveAndActivateTerrainImages(List<DbTerrainImage> dbTerrainImages, byte[] bgImage, String bgImageType) {
        dbTerrainSetting.setBgImageData(bgImage);
        dbTerrainSetting.setBgContentType(bgImageType);
        hibernateTemplate.saveOrUpdate(dbTerrainSetting);
        hibernateTemplate.saveOrUpdateAll(dbTerrainImages);
        ArrayList<DbTerrainImage> doBeDeleted = new ArrayList<DbTerrainImage>(this.dbTerrainImages.values());
        doBeDeleted.removeAll(dbTerrainImages);
        if (!doBeDeleted.isEmpty()) {
            hibernateTemplate.deleteAll(doBeDeleted);
        }
        loadTerrain();
    }

    @Override
    public void saveAndActivateTerrainImagePositions(List<TerrainImagePosition> terrainImagePositions) {
        List<DbTerrainImagePosition> dbTerrainImagePositions = hibernateTemplate.loadAll(DbTerrainImagePosition.class);
        hibernateTemplate.deleteAll(dbTerrainImagePositions);
        ArrayList<DbTerrainImagePosition> dbTerrainImagePositionsNew = new ArrayList<DbTerrainImagePosition>();
        for (TerrainImagePosition terrainImagePosition : terrainImagePositions) {
            DbTerrainImagePosition dbTerrainImagePosition = new DbTerrainImagePosition(terrainImagePosition.getTileIndex());
            DbTerrainImage dbTerrainImage = getTerrainImage(terrainImagePosition.getImageId());
            dbTerrainImagePosition.setTerrainImage(dbTerrainImage);
            dbTerrainImagePositionsNew.add(dbTerrainImagePosition);
        }
        hibernateTemplate.saveOrUpdateAll(dbTerrainImagePositionsNew);
        loadTerrain();
    }

    //////////////// DUMMY IMPL  ////////////////
    @Override
    @Deprecated
    public TerrainFieldTile getTerrainFieldTile(int indexX, int indexY) {
        return new TerrainFieldTileImpl(new Index(indexX, indexY), new TileImpl(null, TerrainType.LAND));
    }

    @Override
    @Deprecated
    public Map<Index, TerrainFieldTile> getTerrainFieldTilesCopy() {
        HashMap<Index, TerrainFieldTile> map = new HashMap<Index, TerrainFieldTile>();
        for (int x = 0; x < getTerrainSetting().getTileWidth(); x++) {
            for (int y = 0; y < getTerrainSetting().getTileHeight(); y++) {
                TerrainFieldTile terrainFieldTile = new TerrainFieldTileImpl(new Index(x, y), new TileImpl(null, TerrainType.LAND));
                map.put(new Index(x, y), terrainFieldTile);
            }
        }
        return map;
    }

}
