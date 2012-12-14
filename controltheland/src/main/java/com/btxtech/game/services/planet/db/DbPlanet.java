package com.btxtech.game.services.planet.db;

import com.btxtech.game.jsre.client.common.RadarMode;
import com.btxtech.game.jsre.common.gameengine.services.PlanetInfo;
import com.btxtech.game.jsre.common.gameengine.services.PlanetLiteInfo;
import com.btxtech.game.services.bot.DbBotConfig;
import com.btxtech.game.services.common.CrudChild;
import com.btxtech.game.services.common.CrudChildServiceHelper;
import com.btxtech.game.services.common.CrudParent;
import com.btxtech.game.services.item.itemType.DbBaseItemType;
import com.btxtech.game.services.terrain.DbRegion;
import com.btxtech.game.services.terrain.DbTerrainSetting;
import com.btxtech.game.services.user.UserService;
import org.hibernate.annotations.Cascade;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: beat
 * Date: 17.01.2012
 * Time: 14:44:56
 */
@Entity(name = "PLANET")
public class DbPlanet implements CrudChild, CrudParent {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(length = 50000)
    private String html;
    private String name;
    // ----- Scope -----
    private int maxMoney;
    private int houseSpace;
    @Enumerated(EnumType.STRING)
    private RadarMode radarMode;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dbPlanet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Collection<DbPlanetItemTypeLimitation> dbPlanetItemTypeLimitations;
    // ----- New Base -----
    @OneToOne(fetch = FetchType.LAZY)
    private DbRegion startRegion;
    @ManyToOne(fetch = FetchType.LAZY)
    private DbBaseItemType startItemType;
    private int startItemFreeRange;
    private int startMoney;
    // ----- Config -----
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private DbTerrainSetting dbTerrainSetting;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "dbPlanet_id")
    private Collection<DbBoxRegion> dbBoxRegions;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Collection<DbBotConfig> dbBotConfigs;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "dbPlanet_id")
    private Collection<DbRegionResource> dbRegionResources;

    @Transient
    private CrudChildServiceHelper<DbPlanetItemTypeLimitation> itemLimitationCrud;
    @Transient
    private CrudChildServiceHelper<DbBoxRegion> boxRegionCrud;
    @Transient
    private CrudChildServiceHelper<DbBotConfig> botCrud;
    @Transient
    private CrudChildServiceHelper<DbRegionResource> regionResourceCrud;

    @Override
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
    public void init(UserService userService) {
        radarMode = RadarMode.NONE;
        dbPlanetItemTypeLimitations = new ArrayList<>();
        dbBoxRegions = new ArrayList<>();
        dbBotConfigs = new ArrayList<>();
        dbRegionResources = new ArrayList<>();
        dbTerrainSetting = new DbTerrainSetting();
        dbTerrainSetting.init(userService);
    }

    @Override
    public void setParent(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParent() {
        return null;
    }

    public int getMaxMoney() {
        return maxMoney;
    }

    public void setMaxMoney(int maxMoney) {
        this.maxMoney = maxMoney;
    }

    public int getHouseSpace() {
        return houseSpace;
    }

    public void setHouseSpace(int houseSpace) {
        this.houseSpace = houseSpace;
    }

    public RadarMode getRadarMode() {
        return radarMode;
    }

    public void setRadarMode(RadarMode radarMode) {
        this.radarMode = radarMode;
    }

    public DbBaseItemType getStartItemType() {
        return startItemType;
    }

    public void setStartItemType(DbBaseItemType startItemType) {
        this.startItemType = startItemType;
    }

    public int getStartItemFreeRange() {
        return startItemFreeRange;
    }

    public void setStartItemFreeRange(int startItemFreeRange) {
        this.startItemFreeRange = startItemFreeRange;
    }

    public DbRegion getStartRegion() {
        return startRegion;
    }

    public void setStartRegion(DbRegion startRegion) {
        this.startRegion = startRegion;
    }

    public int getStartMoney() {
        return startMoney;
    }

    public void setStartMoney(int startMoney) {
        this.startMoney = startMoney;
    }

    public DbTerrainSetting getDbTerrainSetting() {
        return dbTerrainSetting;
    }

    public void setDbTerrainSetting(DbTerrainSetting dbTerrainSetting) {
        this.dbTerrainSetting = dbTerrainSetting;
    }

    public CrudChildServiceHelper<DbBoxRegion> getBoxRegionCrud() {
        if (boxRegionCrud == null) {
            boxRegionCrud = new CrudChildServiceHelper<>(dbBoxRegions, DbBoxRegion.class, this);
        }
        return boxRegionCrud;
    }

    public CrudChildServiceHelper<DbBotConfig> getBotCrud() {
        if (botCrud == null) {
            botCrud = new CrudChildServiceHelper<>(dbBotConfigs, DbBotConfig.class, this);
        }
        return botCrud;
    }

    public CrudChildServiceHelper<DbRegionResource> getRegionResourceCrud() {
        if (regionResourceCrud == null) {
            regionResourceCrud = new CrudChildServiceHelper<>(dbRegionResources, DbRegionResource.class, this);
        }
        return regionResourceCrud;
    }

    public CrudChildServiceHelper<DbPlanetItemTypeLimitation> getItemLimitationCrud() {
        if (itemLimitationCrud == null) {
            itemLimitationCrud = new CrudChildServiceHelper<>(dbPlanetItemTypeLimitations, DbPlanetItemTypeLimitation.class, this);
        }
        return itemLimitationCrud;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public PlanetInfo createPlanetInfo() {
        PlanetInfo planetInfo = new PlanetInfo();
        planetInfo.setPlanetIdAndName(id, name);
        planetInfo.setHouseSpace(houseSpace);
        planetInfo.setMaxMoney(maxMoney);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        for (DbPlanetItemTypeLimitation dbPlanetItemTypeLimitation : this.dbPlanetItemTypeLimitations) {
            itemTypeLimitation.put(dbPlanetItemTypeLimitation.getDbBaseItemType().getId(), dbPlanetItemTypeLimitation.getCount());
        }
        planetInfo.setItemTypeLimitation(itemTypeLimitation);
        planetInfo.setRadarMode(radarMode);
        return planetInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbPlanet)) return false;

        DbPlanet that = (DbPlanet) o;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return getClass().getName() + " " + name + " id: " + id;
    }
}
