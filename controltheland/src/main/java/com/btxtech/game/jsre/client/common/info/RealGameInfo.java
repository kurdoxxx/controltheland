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

package com.btxtech.game.jsre.client.common.info;

import com.btxtech.game.jsre.common.LevelStatePacket;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.Territory;
import com.btxtech.game.jsre.common.gameengine.services.base.BaseAttributes;

import java.util.Collection;

/**
 * User: beat
 * Date: 16.07.2010
 * Time: 23:41:47
 */
public class RealGameInfo extends GameInfo {
    private SimpleBase base;
    private double accountBalance;
    private int energyGenerating;
    private int energyConsuming;
    private Collection<BaseAttributes> allBases;
    private int houseSpace;
    private Collection<Territory> territories;
    private LevelStatePacket levelStatePacket;

    public SimpleBase getBase() {
        return base;
    }

    public void setBase(SimpleBase base) {
        this.base = base;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public int getEnergyGenerating() {
        return energyGenerating;
    }

    public void setEnergyGenerating(int energyGenerating) {
        this.energyGenerating = energyGenerating;
    }

    public int getEnergyConsuming() {
        return energyConsuming;
    }

    public void setEnergyConsuming(int energyConsuming) {
        this.energyConsuming = energyConsuming;
    }

    public Collection<BaseAttributes> getAllBase() {
        return allBases;
    }

    public void setAllBases(Collection<BaseAttributes> allBases) {
        this.allBases = allBases;
    }

    public int getHouseSpace() {
        return houseSpace;
    }

    public void setHouseSpace(int houseSpace) {
        this.houseSpace = houseSpace;
    }

    public Collection<Territory> getTerritories() {
        return territories;
    }

    public void setTerritories(Collection<Territory> territories) {
        this.territories = territories;
    }

    public LevelStatePacket getLevelStatePacket() {
        return levelStatePacket;
    }

    public void setLevelStatePacket(LevelStatePacket levelStatePacket) {
        this.levelStatePacket = levelStatePacket;
    }
}
