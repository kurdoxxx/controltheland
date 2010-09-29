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
import com.btxtech.game.services.common.CrudChild;
import com.btxtech.game.services.common.CrudParent;
import com.btxtech.game.services.common.CrudServiceHelper;
import com.btxtech.game.services.common.CrudServiceHelperCollectionImpl;
import com.btxtech.game.services.common.db.RectangleUserType;
import com.btxtech.game.services.user.User;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

/**
 * User: beat
 * Date: 15.03.2010
 * Time: 22:07:46
 */
@Entity(name = "BOT_CONFIG")
@TypeDef(name = "rectangle", typeClass = RectangleUserType.class)
public class DbBotConfig implements CrudChild, Serializable, CrudParent {
    private static final int BASE_FUNDAMENTAL = 0;
    private static final int BASE_BUILDUP = 1;
    private static final int DEFENSE = 2;
    @Id
    @GeneratedValue
    private Integer id;
    private int actionDelay;
    @OneToOne(fetch = FetchType.EAGER)
    private User user;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @Where(clause = "type=" + BASE_BUILDUP)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Set<DbBotItemCount> baseBuildup;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @Where(clause = "type=" + BASE_FUNDAMENTAL)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Set<DbBotItemCount> baseFundamental;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @Where(clause = "type=" + DEFENSE)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Set<DbBotItemCount> defence;
    @Type(type = "rectangle")
    @Columns(columns = {@Column(name = "coreRectX"), @Column(name = "coreRectY"), @Column(name = "coreRectWidth"), @Column(name = "coreRectHeight")})
    private Rectangle core;
    private int coreSuperiority;
    @Type(type = "rectangle")
    @Columns(columns = {@Column(name = "realmRectX"), @Column(name = "realmRectY"), @Column(name = "realmRectWidth"), @Column(name = "realmRectHeight")})
    private Rectangle realm;
    private int realmSuperiority;
    private String name;
    @Transient
    private CrudServiceHelper<DbBotItemCount> baseBuildupCrudServiceHelper;
    @Transient
    private CrudServiceHelper<DbBotItemCount> baseFundamentalCrudServiceHelper;
    @Transient
    private CrudServiceHelper<DbBotItemCount> defenceCrudServiceHelper;

    public int getActionDelay() {
        return actionDelay;
    }

    public void setActionDelay(int actionDelay) {
        this.actionDelay = actionDelay;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<DbBotItemCount> getBaseFundamental() {
        return baseFundamental;
    }

    public void setBaseFundamental(Set<DbBotItemCount> baseFundamental) {
        this.baseFundamental = baseFundamental;
    }

    public Set<DbBotItemCount> getBaseBuildup() {
        return baseBuildup;
    }

    public void setBaseBuildup(Set<DbBotItemCount> baseBuildup) {
        this.baseBuildup = baseBuildup;
    }

    public Set<DbBotItemCount> getDefence() {
        return defence;
    }

    public void setDefence(Set<DbBotItemCount> defence) {
        this.defence = defence;
    }

    public Rectangle getCore() {
        return core;
    }

    public void setCore(Rectangle core) {
        this.core = core;
    }

    public Rectangle getRealm() {
        return realm;
    }

    public void setRealm(Rectangle realm) {
        this.realm = realm;
    }

    public int getCoreSuperiority() {
        return coreSuperiority;
    }

    public void setCoreSuperiority(int coreSuperiority) {
        this.coreSuperiority = coreSuperiority;
    }

    public int getRealmSuperiority() {
        return realmSuperiority;
    }

    public void setRealmSuperiority(int realmSuperiority) {
        this.realmSuperiority = realmSuperiority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbBotConfig)) return false;

        DbBotConfig dbBotConfig = (DbBotConfig) o;

        return !(id != null ? !id.equals(dbBotConfig.id) : dbBotConfig.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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
        actionDelay = 3000;
        baseBuildup = new HashSet<DbBotItemCount>();
    }

    @Override
    public void setParent(Object o) {
        // Ignore
    }

    public CrudServiceHelper<DbBotItemCount> getBaseBuildupCrudServiceHelper() {
        if (baseBuildupCrudServiceHelper == null) {
            baseBuildupCrudServiceHelper = new CrudServiceHelperCollectionImpl<DbBotItemCount>(baseBuildup, DbBotItemCount.class, this) {
                @Override
                protected void initChild(DbBotItemCount dbBotItemCount) {
                    dbBotItemCount.setType(BASE_BUILDUP);
                }
            };
        }
        return baseBuildupCrudServiceHelper;
    }

    public CrudServiceHelper<DbBotItemCount> getBaseFundamentalCrudServiceHelper() {
        if (baseFundamentalCrudServiceHelper == null) {
            baseFundamentalCrudServiceHelper = new CrudServiceHelperCollectionImpl<DbBotItemCount>(baseFundamental, DbBotItemCount.class, this) {
                @Override
                protected void initChild(DbBotItemCount dbBotItemCount) {
                    dbBotItemCount.setType(BASE_FUNDAMENTAL);
                }
            };
        }
        return baseFundamentalCrudServiceHelper;
    }

    public CrudServiceHelper<DbBotItemCount> getDefenceCrudServiceHelper() {
        if (defenceCrudServiceHelper == null) {
            defenceCrudServiceHelper = new CrudServiceHelperCollectionImpl<DbBotItemCount>(defence, DbBotItemCount.class, this) {
                @Override
                protected void initChild(DbBotItemCount dbBotItemCount) {
                    dbBotItemCount.setType(DEFENSE);
                }
            };
        }
        return defenceCrudServiceHelper;
    }
}
