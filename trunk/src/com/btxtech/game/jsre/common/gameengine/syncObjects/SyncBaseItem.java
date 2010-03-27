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

package com.btxtech.game.jsre.common.gameengine.syncObjects;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.InsufficientFundsException;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.gameengine.ItemDoesNotExistException;
import com.btxtech.game.jsre.common.gameengine.itemType.BaseItemType;
import com.btxtech.game.jsre.common.gameengine.services.Services;
import com.btxtech.game.jsre.common.gameengine.services.items.NoSuchItemTypeException;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.AttackCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BaseCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BuilderCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.FactoryCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.MoneyCollectCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.MoveCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.syncInfos.SyncItemInfo;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 19:11:49
 */
public class SyncBaseItem extends SyncItem {
    private BaseItemType baseItemType;
    private SimpleBase base;
    private boolean isBuild = false;
    private double health;
    private SyncMovable syncMovable;
    private SyncTurnable syncTurnable;
    private SyncWeapon syncWeapon;
    private SyncFactory syncFactory;
    private SyncBuilder syncBuilder;
    private SyncHarvester syncHarvester;
    private SyncGenerator syncGenerator;
    private SyncConsumer syncConsumer;
    private SyncSpecial syncSpecial;

    public SyncBaseItem(Id id, Index position, BaseItemType baseItemType, Services services, SimpleBase base) {
        super(id, position, baseItemType, services);
        this.baseItemType = baseItemType;
        this.base = base;
        setup();
    }

    private void setup() {
        if (baseItemType.getMovableType() != null) {
            syncMovable = new SyncMovable(baseItemType.getMovableType(), this);
        }

        if (baseItemType.getTurnableType() != null) {
            syncTurnable = new SyncTurnable(baseItemType.getTurnableType(), this);
        }

        if (baseItemType.getWeaponType() != null) {
            syncWeapon = new SyncWeapon(baseItemType.getWeaponType(), this);
        }

        if (baseItemType.getFactoryType() != null) {
            syncFactory = new SyncFactory(baseItemType.getFactoryType(), this);
        }

        if (baseItemType.getBuilderType() != null) {
            syncBuilder = new SyncBuilder(baseItemType.getBuilderType(), this);
        }

        if (baseItemType.getHarvesterType() != null) {
            syncHarvester = new SyncHarvester(baseItemType.getHarvesterType(), this);
        }

        if (baseItemType.getGeneratorType() != null) {
            syncGenerator = new SyncGenerator(baseItemType.getGeneratorType(), this);
        }

        if (baseItemType.getConsumerType() != null) {
            syncConsumer = new SyncConsumer(baseItemType.getConsumerType(), this);
        }

        if (baseItemType.getSpecialType() != null) {
            syncSpecial = new SyncSpecial(baseItemType.getSpecialType(), this);
        }
    }


    private void checkBase(SimpleBase syncBase) {
        if (base == null && syncBase == null) {
            return;
        }
        if (base == null) {
            throw new IllegalArgumentException(this + " this.base == null; sync base: " + syncBase);
        }

        if (!base.equals(syncBase)) {
            throw new IllegalArgumentException(this + " bases do not macht: client: " + base + " sync: " + syncBase);
        }
    }

    public SimpleBase getBase() {
        return base;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException {
        checkBase(syncItemInfo.getBase());
        super.synchronize(syncItemInfo);
        health = syncItemInfo.getHealth();
        setBuild(syncItemInfo.isBuild());

        if (syncMovable != null) {
            syncMovable.synchronize(syncItemInfo);
        }

        if (syncTurnable != null) {
            syncTurnable.synchronize(syncItemInfo);
        }

        if (syncWeapon != null) {
            syncWeapon.synchronize(syncItemInfo);
        }

        if (syncFactory != null) {
            syncFactory.synchronize(syncItemInfo);
        }

        if (syncBuilder != null) {
            syncBuilder.synchronize(syncItemInfo);
        }

        if (syncHarvester != null) {
            syncHarvester.synchronize(syncItemInfo);
        }

        if (syncConsumer != null) {
            syncConsumer.synchronize(syncItemInfo);
        }
    }

    @Override
    public SyncItemInfo getSyncInfo() {
        SyncItemInfo syncItemInfo = super.getSyncInfo();
        syncItemInfo.setBase(base);
        syncItemInfo.setHealth(health);
        syncItemInfo.setBuild(isBuild);
        if (syncMovable != null) {
            syncMovable.fillSyncItemInfo(syncItemInfo);
        }

        if (syncTurnable != null) {
            syncTurnable.fillSyncItemInfo(syncItemInfo);
        }

        if (syncWeapon != null) {
            syncWeapon.fillSyncItemInfo(syncItemInfo);
        }

        if (syncFactory != null) {
            syncFactory.fillSyncItemInfo(syncItemInfo);
        }

        if (syncBuilder != null) {
            syncBuilder.fillSyncItemInfo(syncItemInfo);
        }

        if (syncHarvester != null) {
            syncHarvester.fillSyncItemInfo(syncItemInfo);
        }

        if (syncConsumer != null) {
            syncConsumer.fillSyncItemInfo(syncItemInfo);
        }

        return syncItemInfo;
    }

    public boolean tick(double factor) throws InsufficientFundsException, ItemDoesNotExistException, NoSuchItemTypeException {
        if (hasSyncConsumer() && !getSyncConsumer().isOperating()) {
            return false;
        }

        if (syncWeapon != null && syncWeapon.isActive()) {
            return syncWeapon.tick(factor);
        }

        if (syncFactory != null && syncFactory.isActive()) {
            return syncFactory.tick(factor);
        }

        if (syncBuilder != null && syncBuilder.isActive()) {
            return syncBuilder.tick(factor);
        }

        if (syncHarvester != null && syncHarvester.isActive()) {
            return syncHarvester.tick(factor);
        }

        return syncMovable != null && syncMovable.isActive() && syncMovable.tick(factor);
    }

    public void stop() {
        if (syncWeapon != null) {
            syncWeapon.stop();
        }

        if (syncFactory != null) {
            syncFactory.stop();
        }

        if (syncBuilder != null) {
            syncBuilder.stop();
        }

        if (syncHarvester != null) {
            syncHarvester.stop();
        }

        if (syncMovable != null) {
            syncMovable.stop();
        }
    }

    public void executeCommand(BaseCommand baseCommand) throws ItemDoesNotExistException, InsufficientFundsException, NoSuchItemTypeException {
        checkId(baseCommand);

        if (baseCommand instanceof AttackCommand) {
            getSyncWaepon().executeCommand((AttackCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof MoveCommand) {
            getSyncMovable().executeCommand((MoveCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof MoneyCollectCommand) {
            getSyncHarvester().executeCommand((MoneyCollectCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof BuilderCommand) {
            getSyncBuilder().executeCommand((BuilderCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof FactoryCommand) {
            getSyncFactory().executeCommand((FactoryCommand) baseCommand);
            return;
        }

        throw new IllegalArgumentException("Command not supported: " + baseCommand);
    }

    private void checkId(BaseCommand baseCommand) {
        if (!baseCommand.getId().equals(getId())) {
            throw new IllegalArgumentException(this + "Id do not match: " + getId() + " command: " + baseCommand.getId());
        }
    }

    public SyncMovable getSyncMovable() {
        if (syncMovable == null) {
            throw new IllegalStateException(this + " has no SyncMovable");
        }
        return syncMovable;
    }

    public boolean hasSyncMovable() {
        return syncMovable != null;
    }

    public SyncTurnable getSyncTurnable() {
        if (syncTurnable == null) {
            throw new IllegalStateException(this + " has no SyncTurnable");
        }
        return syncTurnable;
    }

    public boolean hasSyncTurnable() {
        return syncTurnable != null;
    }

    public boolean hasSyncHarvester() {
        return syncHarvester != null;
    }

    public SyncHarvester getSyncHarvester() {
        if (syncHarvester == null) {
            throw new IllegalStateException(this + " has no SyncHarvester");
        }
        return syncHarvester;
    }

    public SyncFactory getSyncFactory() {
        if (syncFactory == null) {
            throw new IllegalStateException(this + " has no SyncFactory");
        }
        return syncFactory;
    }

    public boolean hasSyncFactory() {
        return syncFactory != null;
    }

    public boolean hasSyncWaepon() {
        return syncWeapon != null;
    }

    public SyncWeapon getSyncWaepon() {
        if (syncWeapon == null) {
            throw new IllegalStateException(this + " has no syncWeapon");
        }
        return syncWeapon;
    }

    public boolean hasSyncBuilder() {
        return syncBuilder != null;
    }

    public SyncBuilder getSyncBuilder() {
        if (syncBuilder == null) {
            throw new IllegalStateException(this + " has no SyncBuilder");
        }
        return syncBuilder;
    }

    public boolean hasSyncGenerator() {
        return syncGenerator != null;
    }

    public SyncGenerator getSyncGenerator() {
        if (syncGenerator == null) {
            throw new IllegalStateException(this + " has no SyncGenerator");
        }
        return syncGenerator;
    }

    public boolean hasSyncConsumer() {
        return syncConsumer != null;
    }

    public SyncConsumer getSyncConsumer() {
        if (syncConsumer == null) {
            throw new IllegalStateException(this + " has no SyncConsumer");
        }
        return syncConsumer;
    }

    public boolean hasSyncSpecial() {
        return syncSpecial != null;
    }

    public SyncSpecial getSyncSpecial() {
        if (syncConsumer == null) {
            throw new IllegalStateException(this + " has no SyncSpecial");
        }
        return syncSpecial;
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem) {
        return !getBase().equals(syncBaseItem.getBase());
    }

    public void decreaseHealth(double progress, SyncBaseItem actor) {
        health -= progress;
        fireItemChanged(SyncItemListener.Change.HEALTH);
        if (health <= 0) {
            health = 0;
            getServices().getItemService().killBaseSyncObject(this, actor, false);
        }
    }

    public void increaseHealth(double progress) {
        health += progress;
        if (health >= baseItemType.getHealth()) {
            health = baseItemType.getHealth();
        }
        fireItemChanged(SyncItemListener.Change.HEALTH);
    }

    public boolean isReady() {
        return isBuild;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isHealthy() {
        return health >= baseItemType.getHealth();
    }

    public void setBuild(boolean isBuild) {
        if (this.isBuild == isBuild) {
            return;
        }

        this.isBuild = isBuild;
        if (syncConsumer != null) {
            syncConsumer.setConsuming(isBuild);
        }
        if (syncGenerator != null) {
            syncGenerator.setGenerating(isBuild);
        }
        fireItemChanged(SyncItemListener.Change.BUILD);
    }

    public BaseItemType getBaseItemType() {
        return baseItemType;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void setFullHealth() {
        health = baseItemType.getHealth();
    }

    @Override
    public String toString() {
        if (hasSyncHarvester()) {
            return super.toString() + " target: " + getSyncHarvester().getTarget();
        } else {
            return super.toString();
        }
    }
}
