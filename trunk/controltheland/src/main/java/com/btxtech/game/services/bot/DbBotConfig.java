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

package com.btxtech.game.services.bot;

import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.common.gameengine.services.bot.BotConfig;
import com.btxtech.game.jsre.common.gameengine.services.bot.BotEnragementStateConfig;
import com.btxtech.game.services.common.CrudChild;
import com.btxtech.game.services.common.CrudListChildServiceHelper;
import com.btxtech.game.services.common.CrudParent;
import com.btxtech.game.services.common.db.RectangleUserType;
import com.btxtech.game.services.item.ItemService;
import com.btxtech.game.services.user.UserService;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * User: beat
 * Date: 15.03.2010
 * Time: 22:07:46
 */
@Entity(name = "BOT_CONFIG")
@TypeDef(name = "rectangle", typeClass = RectangleUserType.class)
public class DbBotConfig implements CrudChild, CrudParent {
    @Id
    @GeneratedValue
    private Integer id;
    private int actionDelay;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "dbBotConfig_id")
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @org.hibernate.annotations.IndexColumn(name = "orderIndex", base = 0)
    private List<DbBotEnragementStateConfig> enragementStateConfigs;
    @Type(type = "rectangle")
    @Columns(columns = {@Column(name = "realmRectX"), @Column(name = "realmRectY"), @Column(name = "realmRectWidth"), @Column(name = "realmRectHeight")})
    private Rectangle realm;
    private String name;
    private Long minInactiveMs;
    private Long maxInactiveMs;
    private Long minActiveMs;
    private Long maxActiveMs;
    private boolean realGameBot;

    @Transient
    private CrudListChildServiceHelper<DbBotEnragementStateConfig> enrageStateCrud;

    public Integer getId() {
        return id;
    }

    public int getActionDelay() {
        return actionDelay;
    }

    public void setActionDelay(int actionDelay) {
        this.actionDelay = actionDelay;
    }

    public Rectangle getRealm() {
        return realm;
    }

    public void setRealm(Rectangle realm) {
        this.realm = realm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbBotConfig)) return false;

        DbBotConfig dbBotConfig = (DbBotConfig) o;

        return id != null && id.equals(dbBotConfig.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Long getMinInactiveMs() {
        return minInactiveMs;
    }

    public void setMinInactiveMs(Long minInactiveMs) {
        this.minInactiveMs = minInactiveMs;
    }

    public Long getMaxInactiveMs() {
        return maxInactiveMs;
    }

    public void setMaxInactiveMs(Long maxInactiveMs) {
        this.maxInactiveMs = maxInactiveMs;
    }

    public Long getMinActiveMs() {
        return minActiveMs;
    }

    public void setMinActiveMs(Long minActiveMs) {
        this.minActiveMs = minActiveMs;
    }

    public Long getMaxActiveMs() {
        return maxActiveMs;
    }

    public void setMaxActiveMs(Long maxActiveMs) {
        this.maxActiveMs = maxActiveMs;
    }

    public boolean isRealGameBot() {
        return realGameBot;
    }

    public void setRealGameBot(boolean realGameBot) {
        this.realGameBot = realGameBot;
    }

    @Override
    public void init(UserService userService) {
        actionDelay = 3000;
        enragementStateConfigs = new ArrayList<>();
    }

    @Override
    public void setParent(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParent() {
        return null;
    }

    public CrudListChildServiceHelper<DbBotEnragementStateConfig> getEnrageStateCrud() {
        if (enrageStateCrud == null) {
            enrageStateCrud = new CrudListChildServiceHelper<>(enragementStateConfigs, DbBotEnragementStateConfig.class, this);
        }
        return enrageStateCrud;
    }

    public BotConfig createBotConfig(ItemService itemService) {
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        if (enragementStateConfigs != null) {
            for (DbBotEnragementStateConfig dbBotEnragementStateConfig : enragementStateConfigs) {
                botEnragementStateConfigs.add(dbBotEnragementStateConfig.createBotEnragementStateConfigg(itemService));
            }
        }
        int tmpId;
        if (id != null) {
            tmpId = id;
        } else {
            tmpId = System.identityHashCode(this);
        }
        return new BotConfig(tmpId, actionDelay, botEnragementStateConfigs, realm, name, minInactiveMs, maxInactiveMs, minActiveMs, maxActiveMs);
    }
}
