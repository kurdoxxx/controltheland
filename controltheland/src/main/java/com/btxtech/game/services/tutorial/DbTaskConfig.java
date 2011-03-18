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

package com.btxtech.game.services.tutorial;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.tutorial.ItemTypeAndPosition;
import com.btxtech.game.jsre.common.tutorial.StepConfig;
import com.btxtech.game.jsre.common.tutorial.TaskConfig;
import com.btxtech.game.services.common.CrudChild;
import com.btxtech.game.services.common.CrudChildServiceHelper;
import com.btxtech.game.services.common.CrudParent;
import com.btxtech.game.services.common.db.IndexUserType;
import com.btxtech.game.services.item.ItemService;
import com.btxtech.game.services.tutorial.hint.DbResourceHintConfig;
import com.btxtech.game.services.tutorial.hint.ResourceHintManager;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: beat
 * Date: 24.07.2010
 * Time: 14:11:15
 */
@Entity(name = "TUTORIAL_TASK_CONFIG")
@TypeDefs({@TypeDef(name = "index", typeClass = IndexUserType.class)})
public class DbTaskConfig implements CrudParent, CrudChild<DbTutorialConfig> {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private boolean clearGame;
    @OneToMany(mappedBy = "dbTaskConfig", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DbItemTypeAndPosition> items;
    private boolean isScrollingAllowed;
    private boolean isSellingAllowed;
    private boolean isOptionAllowed;
    @Type(type = "index")
    @Columns(columns = {@Column(name = "xScroll"), @Column(name = "yScroll")})
    private Index scroll;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @org.hibernate.annotations.IndexColumn(name = "orderIndex", nullable = false, base = 0)
    @JoinColumn(name = "dbTaskConfig", nullable = false)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private List<DbStepConfig> stepConfigs;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "dbTaskConfig", nullable = false)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Set<DbTaskAllowedItem> dbTaskAllowedItems;
    private int accountBalance;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dbTutorialConfig", insertable = false, updatable = false, nullable = false)
    private DbTutorialConfig dbTutorialConfig;
    private int finishImageDuration;
    private String finishedImageContentType;
    @Column(length = 500000)
    private byte[] finishImageData;
    private int houseCount;
    private String taskText;

    @Transient
    private CrudChildServiceHelper<DbItemTypeAndPosition> itemTypeAndPositionCrudHelper;
    @Transient
    private CrudChildServiceHelper<DbStepConfig> stepConfigCrudHelper;
    @Transient
    private CrudChildServiceHelper<DbTaskAllowedItem> allowedItemHelper;

    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void init() {
        items = new HashSet<DbItemTypeAndPosition>();
        scroll = new Index(0, 0);
        stepConfigs = new ArrayList<DbStepConfig>();
        dbTaskAllowedItems = new HashSet<DbTaskAllowedItem>();
    }

    @Override
    public void setParent(DbTutorialConfig crudParent) {
        dbTutorialConfig = crudParent;
    }

    public void setClearGame(boolean clearGame) {
        this.clearGame = clearGame;
    }

    public boolean isClearGame() {
        return clearGame;
    }

    public boolean isScrollingAllowed() {
        return isScrollingAllowed;
    }

    public void setScrollingAllowed(boolean scrollingAllowed) {
        isScrollingAllowed = scrollingAllowed;
    }

    public Index getScroll() {
        return scroll;
    }

    public void setScroll(Index scroll) {
        this.scroll = scroll;
    }

    public int getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(int accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbTaskConfig)) return false;

        DbTaskConfig that = (DbTaskConfig) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    public int getFinishImageDuration() {
        return finishImageDuration;
    }

    public void setFinishImageDuration(int finishImageDuration) {
        this.finishImageDuration = finishImageDuration;
    }

    public String getFinishedImageContentType() {
        return finishedImageContentType;
    }

    public void setFinishedImageContentType(String finishedImageContentType) {
        this.finishedImageContentType = finishedImageContentType;
    }

    public byte[] getFinishImageData() {
        return finishImageData;
    }

    public void setFinishImageData(byte[] finishImageData) {
        this.finishImageData = finishImageData;
    }

    public int getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(int houseCount) {
        this.houseCount = houseCount;
    }

    public boolean isSellingAllowed() {
        return isSellingAllowed;
    }

    public void setSellingAllowed(boolean sellingAllowed) {
        isSellingAllowed = sellingAllowed;
    }

    public boolean isOptionAllowed() {
        return isOptionAllowed;
    }

    public void setOptionAllowed(boolean optionAllowed) {
        isOptionAllowed = optionAllowed;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public TaskConfig createTaskConfig(ResourceHintManager resourceHintManager, ItemService itemService) {
        ArrayList<StepConfig> stepConfigs = new ArrayList<StepConfig>();
        for (DbStepConfig dBstepConfig : this.stepConfigs) {
            stepConfigs.add(dBstepConfig.createStepConfig(resourceHintManager, itemService));
        }

        ArrayList<ItemTypeAndPosition> itemTypeAndPositions = new ArrayList<ItemTypeAndPosition>();
        for (DbItemTypeAndPosition dbItemTypeAndPosition : items) {
            ItemTypeAndPosition itemTypeAndPosition = dbItemTypeAndPosition.createItemTypeAndPosition();
            if (itemTypeAndPosition != null) {
                itemTypeAndPositions.add(itemTypeAndPosition);
            }
        }
        Integer finishImageId = null;
        if (finishedImageContentType != null && finishImageData != null) {
            finishImageId = resourceHintManager.addResource(DbResourceHintConfig.createImageOnly(finishedImageContentType, finishImageData));
        }
        return new TaskConfig(clearGame,
                taskText,
                itemTypeAndPositions,
                isScrollingAllowed,
                isSellingAllowed,
                isOptionAllowed,
                scroll,
                stepConfigs,
                convertAllowedItems(),
                houseCount,
                accountBalance,
                finishImageDuration * 1000,
                name,
                finishImageId);
    }

    public CrudChildServiceHelper<DbItemTypeAndPosition> getItemCrudServiceHelper() {
        if (itemTypeAndPositionCrudHelper == null) {
            itemTypeAndPositionCrudHelper = new CrudChildServiceHelper<DbItemTypeAndPosition>(items, DbItemTypeAndPosition.class, this);
        }
        return itemTypeAndPositionCrudHelper;
    }

    public CrudChildServiceHelper<DbStepConfig> getStepConfigCrudServiceHelper() {
        if (stepConfigCrudHelper == null) {
            stepConfigCrudHelper = new CrudChildServiceHelper<DbStepConfig>(stepConfigs, DbStepConfig.class, this);
        }
        return stepConfigCrudHelper;
    }


    private Collection<Integer> convertAllowedItems() {
        List<Integer> result = new ArrayList<Integer>();
        for (DbTaskAllowedItem dbTaskAllowedItem : dbTaskAllowedItems) {
            result.add(dbTaskAllowedItem.getDbBaseItemType().getId());
        }
        return result;
    }

    public CrudChildServiceHelper<DbTaskAllowedItem> getAllowedItemHelper() {
        if (allowedItemHelper == null) {
            allowedItemHelper = new CrudChildServiceHelper<DbTaskAllowedItem>(dbTaskAllowedItems, DbTaskAllowedItem.class, this);
        }
        return allowedItemHelper;
    }

    public void moveTaskUp(DbStepConfig stepConfig) {
        int i = stepConfigs.indexOf(stepConfig);
        if (i > 0) {
            DbStepConfig old = stepConfigs.set(i - 1, stepConfig);
            stepConfigs.set(i, old);
        }
    }

    public void moveTaskDown(DbStepConfig stepConfig) {
        int i = stepConfigs.indexOf(stepConfig);
        if (i + 2 < stepConfigs.size()) {
            DbStepConfig old = stepConfigs.set(i + 1, stepConfig);
            stepConfigs.set(i, old);
        }
    }
}
