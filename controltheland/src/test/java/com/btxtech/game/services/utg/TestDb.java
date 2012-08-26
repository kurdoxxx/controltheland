package com.btxtech.game.services.utg;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.client.dialogs.quest.QuestInfo;
import com.btxtech.game.jsre.common.CommonJava;
import com.btxtech.game.jsre.common.gameengine.itemType.ItemType;
import com.btxtech.game.jsre.common.utg.config.ConditionConfig;
import com.btxtech.game.jsre.common.utg.config.ConditionTrigger;
import com.btxtech.game.jsre.common.utg.config.ItemTypePositionComparisonConfig;
import com.btxtech.game.jsre.common.utg.config.SyncItemTypeComparisonConfig;
import com.btxtech.game.services.AbstractServiceTest;
import com.btxtech.game.services.common.HibernateUtil;
import com.btxtech.game.services.item.ItemService;
import com.btxtech.game.services.territory.DbTerritory;
import com.btxtech.game.services.territory.TerritoryService;
import com.btxtech.game.services.tutorial.DbTutorialConfig;
import com.btxtech.game.services.tutorial.TutorialService;
import com.btxtech.game.services.utg.condition.DbAbstractComparisonConfig;
import com.btxtech.game.services.utg.condition.DbComparisonItemCount;
import com.btxtech.game.services.utg.condition.DbConditionConfig;
import com.btxtech.game.services.utg.condition.DbItemTypePositionComparisonConfig;
import com.btxtech.game.services.utg.condition.DbSyncItemTypeComparisonConfig;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: beat
 * Date: 07.03.2011
 * Time: 17:15:43
 */
public class TestDb extends AbstractServiceTest {
    @Autowired
    private UserGuidanceService userGuidanceService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private TerritoryService territoryService;
    @Autowired
    private TutorialService tutorialService;
    @Autowired
    private SessionFactory sessionFactory;

    @Test
    @DirtiesContext
    public void createModifyDelete() {
        // Setup environment
        DbTutorialConfig tut1 = createTutorial1();
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        int startItemTypeId = createSimpleBuilding().getId();
        endHttpRequestAndOpenSessionInViewFilter();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbTerritory dbTerritory = setupSimpleTerritory("Teritory1", startItemTypeId);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        List<DbQuestHub> dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(0, dbQuestHubs.size());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // --------------------- CREATE ---------------------

        // Setup QuestionHub
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub1 = userGuidanceService.getCrudQuestHub().createDbChild();
        dbQuestHub1.setStartItemFreeRange(99);
        dbQuestHub1.setStartItemType(itemService.getDbBaseItemType(startItemTypeId));
        dbQuestHub1.setStartTerritory(territoryService.getDbTerritoryCrudServiceHelper().readDbChild(dbTerritory.getId()));
        dbQuestHub1.setName("dbQuestHub1");
        dbQuestHub1.setStartMoney(1234);
        DbLevel dbLevel1 = dbQuestHub1.getLevelCrud().createDbChild();
        dbLevel1.setHouseSpace(15);
        dbLevel1.setHtml("abcdef");
        dbLevel1.setInternalDescription("internal");
        dbLevel1.setItemSellFactor(0.23);
        dbLevel1.setMaxMoney(200);
        dbLevel1.setNumber(1);
        dbLevel1.setXp(599);
        DbLevelTask dbLevelTask11 = dbLevel1.getLevelTaskCrud().createDbChild();
        dbLevelTask11.setMoney(16);
        dbLevelTask11.setXp(34);
        dbLevelTask11.setName("dbLevelTask11");
        dbLevelTask11.setDbTutorialConfig(tutorialService.getDbTutorialCrudRootServiceHelper().readDbChild(tut1.getId()));
        DbLevelTask dbLevelTask12 = dbLevel1.getLevelTaskCrud().createDbChild();
        dbLevelTask12.setMoney(17);
        dbLevelTask12.setXp(35);
        dbLevelTask12.setName("dbLevelTask12");
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(1, dbQuestHubs.size());
        dbQuestHub1 = dbQuestHubs.get(0);
        Assert.assertEquals(99, dbQuestHub1.getStartItemFreeRange());
        Assert.assertEquals("dbQuestHub1", dbQuestHub1.getName());
        Assert.assertEquals(1234, dbQuestHub1.getStartMoney());
        Assert.assertEquals(dbTerritory.getId(), dbQuestHub1.getStartTerritory().getId());
        Assert.assertEquals(startItemTypeId, (int) dbQuestHub1.getStartItemType().getId());
        Assert.assertTrue(dbQuestHub1.isRealBaseRequired());
        Assert.assertEquals(1, dbQuestHub1.getLevelCrud().readDbChildren().size());
        dbLevel1 = dbQuestHub1.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(15, dbLevel1.getHouseSpace());
        Assert.assertEquals("abcdef", dbLevel1.getHtml());
        Assert.assertEquals("internal", dbLevel1.getInternalDescription());
        Assert.assertEquals(0.23, dbLevel1.getItemSellFactor(), 0.0001);
        Assert.assertEquals(200, dbLevel1.getMaxMoney());
        Assert.assertEquals(1, dbLevel1.getNumber());
        Assert.assertEquals(2, dbLevel1.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(599, dbLevel1.getXp());
        dbLevelTask11 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask11.getId());
        Assert.assertEquals(16, dbLevelTask11.getMoney());
        Assert.assertEquals(34, dbLevelTask11.getXp());
        Assert.assertEquals("dbLevelTask11", dbLevelTask11.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask11.getDbTutorialConfig().getId());
        dbLevelTask12 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask12.getId());
        Assert.assertEquals(17, dbLevelTask12.getMoney());
        Assert.assertEquals(35, dbLevelTask12.getXp());
        Assert.assertEquals("dbLevelTask12", dbLevelTask12.getName());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Setup new Level
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        dbQuestHub1.setStartItemFreeRange(99);
        DbLevel dbLevel2 = dbQuestHub1.getLevelCrud().createDbChild();
        dbLevel2.setHouseSpace(19);
        dbLevel2.setHtml("qwert");
        dbLevel2.setInternalDescription("internal2");
        dbLevel2.setItemSellFactor(0.24);
        dbLevel2.setMaxMoney(201);
        dbLevel2.setNumber(2);
        dbLevel2.setXp(333);
        DbLevelTask dbLevelTask21 = dbLevel2.getLevelTaskCrud().createDbChild();
        dbLevelTask21.setMoney(17);
        dbLevelTask21.setXp(39);
        dbLevelTask21.setName("dbLevelTask21");
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(1, dbQuestHubs.size());
        dbQuestHub1 = dbQuestHubs.get(0);
        Assert.assertEquals(99, dbQuestHub1.getStartItemFreeRange());
        Assert.assertEquals("dbQuestHub1", dbQuestHub1.getName());
        Assert.assertEquals(1234, dbQuestHub1.getStartMoney());
        Assert.assertEquals(dbTerritory.getId(), dbQuestHub1.getStartTerritory().getId());
        Assert.assertEquals(startItemTypeId, (int) dbQuestHub1.getStartItemType().getId());
        Assert.assertTrue(dbQuestHub1.isRealBaseRequired());
        Assert.assertEquals(2, dbQuestHub1.getLevelCrud().readDbChildren().size());
        dbLevel1 = dbQuestHub1.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(15, dbLevel1.getHouseSpace());
        Assert.assertEquals("abcdef", dbLevel1.getHtml());
        Assert.assertEquals("internal", dbLevel1.getInternalDescription());
        Assert.assertEquals(0.23, dbLevel1.getItemSellFactor(), 0.0001);
        Assert.assertEquals(200, dbLevel1.getMaxMoney());
        Assert.assertEquals(1, dbLevel1.getNumber());
        Assert.assertEquals(2, dbLevel1.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(599, dbLevel1.getXp());
        dbLevelTask11 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask11.getId());
        Assert.assertEquals(16, dbLevelTask11.getMoney());
        Assert.assertEquals(34, dbLevelTask11.getXp());
        Assert.assertEquals("dbLevelTask11", dbLevelTask11.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask11.getDbTutorialConfig().getId());
        dbLevelTask12 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask12.getId());
        Assert.assertEquals(17, dbLevelTask12.getMoney());
        Assert.assertEquals(35, dbLevelTask12.getXp());
        Assert.assertEquals("dbLevelTask12", dbLevelTask12.getName());
        dbLevel2 = dbQuestHub1.getLevelCrud().readDbChildren().get(1);
        Assert.assertEquals(19, dbLevel2.getHouseSpace());
        Assert.assertEquals("qwert", dbLevel2.getHtml());
        Assert.assertEquals("internal2", dbLevel2.getInternalDescription());
        Assert.assertEquals(0.24, dbLevel2.getItemSellFactor(), 0.0001);
        Assert.assertEquals(201, dbLevel2.getMaxMoney());
        Assert.assertEquals(2, dbLevel2.getNumber());
        Assert.assertEquals(1, dbLevel2.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(333, dbLevel2.getXp());
        dbLevelTask21 = dbLevel2.getLevelTaskCrud().readDbChild(dbLevelTask21.getId());
        Assert.assertEquals(17, dbLevelTask21.getMoney());
        Assert.assertEquals(39, dbLevelTask21.getXp());
        Assert.assertEquals("dbLevelTask21", dbLevelTask21.getName());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Setup new QuestionHub
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub2 = userGuidanceService.getCrudQuestHub().createDbChild();
        dbQuestHub2.setName("dbQuestHub2");
        dbQuestHub2.setRealBaseRequired(false);
        DbLevel dbLevel3 = dbQuestHub2.getLevelCrud().createDbChild();
        dbLevel3.setHouseSpace(25);
        dbLevel3.setHtml("abcdef3");
        dbLevel3.setInternalDescription("internal3");
        dbLevel3.setItemSellFactor(3);
        dbLevel3.setMaxMoney(203);
        dbLevel3.setNumber(3);
        dbLevel3.setXp(74);
        DbLevelTask dbLevelTask31 = dbLevel3.getLevelTaskCrud().createDbChild();
        dbLevelTask31.setMoney(36);
        dbLevelTask31.setXp(87);
        dbLevelTask31.setName("dbLevelTask31");
        dbLevelTask31.setDbTutorialConfig(tutorialService.getDbTutorialCrudRootServiceHelper().readDbChild(tut1.getId()));
        DbLevelTask dbLevelTask32 = dbLevel3.getLevelTaskCrud().createDbChild();
        dbLevelTask32.setMoney(18);
        dbLevelTask32.setXp(90);
        dbLevelTask32.setName("dbLevelTask32");
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub2);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(2, dbQuestHubs.size());
        dbQuestHub1 = dbQuestHubs.get(0);
        Assert.assertEquals(99, dbQuestHub1.getStartItemFreeRange());
        Assert.assertEquals("dbQuestHub1", dbQuestHub1.getName());
        Assert.assertEquals(1234, dbQuestHub1.getStartMoney());
        Assert.assertEquals(dbTerritory.getId(), dbQuestHub1.getStartTerritory().getId());
        Assert.assertEquals(startItemTypeId, (int) dbQuestHub1.getStartItemType().getId());
        Assert.assertTrue(dbQuestHub1.isRealBaseRequired());
        Assert.assertEquals(2, dbQuestHub1.getLevelCrud().readDbChildren().size());
        dbLevel1 = dbQuestHub1.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(15, dbLevel1.getHouseSpace());
        Assert.assertEquals("abcdef", dbLevel1.getHtml());
        Assert.assertEquals("internal", dbLevel1.getInternalDescription());
        Assert.assertEquals(0.23, dbLevel1.getItemSellFactor(), 0.0001);
        Assert.assertEquals(200, dbLevel1.getMaxMoney());
        Assert.assertEquals(1, dbLevel1.getNumber());
        Assert.assertEquals(2, dbLevel1.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(599, dbLevel1.getXp());
        dbLevelTask11 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask11.getId());
        Assert.assertEquals(16, dbLevelTask11.getMoney());
        Assert.assertEquals(34, dbLevelTask11.getXp());
        Assert.assertEquals("dbLevelTask11", dbLevelTask11.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask11.getDbTutorialConfig().getId());
        dbLevelTask12 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask12.getId());
        Assert.assertEquals(17, dbLevelTask12.getMoney());
        Assert.assertEquals(35, dbLevelTask12.getXp());
        Assert.assertEquals("dbLevelTask12", dbLevelTask12.getName());
        dbLevel2 = dbQuestHub1.getLevelCrud().readDbChildren().get(1);
        Assert.assertEquals(19, dbLevel2.getHouseSpace());
        Assert.assertEquals("qwert", dbLevel2.getHtml());
        Assert.assertEquals("internal2", dbLevel2.getInternalDescription());
        Assert.assertEquals(0.24, dbLevel2.getItemSellFactor(), 0.0001);
        Assert.assertEquals(201, dbLevel2.getMaxMoney());
        Assert.assertEquals(2, dbLevel2.getNumber());
        Assert.assertEquals(1, dbLevel2.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(333, dbLevel2.getXp());
        dbLevelTask21 = dbLevel2.getLevelTaskCrud().readDbChild(dbLevelTask21.getId());
        Assert.assertEquals(17, dbLevelTask21.getMoney());
        Assert.assertEquals(39, dbLevelTask21.getXp());
        Assert.assertEquals("dbLevelTask21", dbLevelTask21.getName());
        dbQuestHub2 = dbQuestHubs.get(1);
        Assert.assertEquals("dbQuestHub2", dbQuestHub2.getName());
        Assert.assertFalse(dbQuestHub2.isRealBaseRequired());
        Assert.assertNull(dbQuestHub2.getStartTerritory());
        Assert.assertEquals(1, dbQuestHub2.getLevelCrud().readDbChildren().size());
        dbLevel3 = dbQuestHub2.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(25, dbLevel3.getHouseSpace());
        Assert.assertEquals("abcdef3", dbLevel3.getHtml());
        Assert.assertEquals("internal3", dbLevel3.getInternalDescription());
        Assert.assertEquals(3, dbLevel3.getItemSellFactor(), 0.0001);
        Assert.assertEquals(203, dbLevel3.getMaxMoney());
        Assert.assertEquals(3, dbLevel3.getNumber());
        Assert.assertEquals(2, dbLevel3.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(74, dbLevel3.getXp());
        dbLevelTask31 = dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask31.getId());
        Assert.assertEquals(36, dbLevelTask31.getMoney());
        Assert.assertEquals(87, dbLevelTask31.getXp());
        Assert.assertEquals("dbLevelTask31", dbLevelTask31.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask31.getDbTutorialConfig().getId());
        dbLevelTask32 = dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask32.getId());
        Assert.assertEquals(18, dbLevelTask32.getMoney());
        Assert.assertEquals(90, dbLevelTask32.getXp());
        Assert.assertEquals("dbLevelTask32", dbLevelTask32.getName());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // --------------------- MODIFY ---------------------
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        dbQuestHubs.get(0).setName("xxx");
        dbQuestHubs.get(0).getLevelCrud().readDbChild(dbLevel1.getId()).setMaxMoney(500);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHubs.get(0));
        dbQuestHubs.get(1).getLevelCrud().readDbChild(dbLevel3.getId()).getLevelTaskCrud().readDbChild(dbLevelTask32.getId()).setXp(191);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHubs.get(1));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(2, dbQuestHubs.size());
        dbQuestHub1 = dbQuestHubs.get(0);
        Assert.assertEquals(99, dbQuestHub1.getStartItemFreeRange());
        Assert.assertEquals("xxx", dbQuestHub1.getName());
        Assert.assertEquals(1234, dbQuestHub1.getStartMoney());
        Assert.assertEquals(dbTerritory.getId(), dbQuestHub1.getStartTerritory().getId());
        Assert.assertEquals(startItemTypeId, (int) dbQuestHub1.getStartItemType().getId());
        Assert.assertTrue(dbQuestHub1.isRealBaseRequired());
        Assert.assertEquals(2, dbQuestHub1.getLevelCrud().readDbChildren().size());
        dbLevel1 = dbQuestHub1.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(15, dbLevel1.getHouseSpace());
        Assert.assertEquals("abcdef", dbLevel1.getHtml());
        Assert.assertEquals("internal", dbLevel1.getInternalDescription());
        Assert.assertEquals(0.23, dbLevel1.getItemSellFactor(), 0.0001);
        Assert.assertEquals(500, dbLevel1.getMaxMoney());
        Assert.assertEquals(1, dbLevel1.getNumber());
        Assert.assertEquals(2, dbLevel1.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(599, dbLevel1.getXp());
        dbLevelTask11 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask11.getId());
        Assert.assertEquals(16, dbLevelTask11.getMoney());
        Assert.assertEquals(34, dbLevelTask11.getXp());
        Assert.assertEquals("dbLevelTask11", dbLevelTask11.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask11.getDbTutorialConfig().getId());
        dbLevelTask12 = dbLevel1.getLevelTaskCrud().readDbChild(dbLevelTask12.getId());
        Assert.assertEquals(17, dbLevelTask12.getMoney());
        Assert.assertEquals(35, dbLevelTask12.getXp());
        Assert.assertEquals("dbLevelTask12", dbLevelTask12.getName());
        dbLevel2 = dbQuestHub1.getLevelCrud().readDbChildren().get(1);
        Assert.assertEquals(19, dbLevel2.getHouseSpace());
        Assert.assertEquals("qwert", dbLevel2.getHtml());
        Assert.assertEquals("internal2", dbLevel2.getInternalDescription());
        Assert.assertEquals(0.24, dbLevel2.getItemSellFactor(), 0.0001);
        Assert.assertEquals(201, dbLevel2.getMaxMoney());
        Assert.assertEquals(2, dbLevel2.getNumber());
        Assert.assertEquals(1, dbLevel2.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(333, dbLevel2.getXp());
        dbLevelTask21 = dbLevel2.getLevelTaskCrud().readDbChild(dbLevelTask21.getId());
        Assert.assertEquals(17, dbLevelTask21.getMoney());
        Assert.assertEquals(39, dbLevelTask21.getXp());
        Assert.assertEquals("dbLevelTask21", dbLevelTask21.getName());
        dbQuestHub2 = dbQuestHubs.get(1);
        Assert.assertEquals("dbQuestHub2", dbQuestHub2.getName());
        Assert.assertFalse(dbQuestHub2.isRealBaseRequired());
        Assert.assertNull(dbQuestHub2.getStartTerritory());
        Assert.assertEquals(1, dbQuestHub2.getLevelCrud().readDbChildren().size());
        dbLevel3 = dbQuestHub2.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(25, dbLevel3.getHouseSpace());
        Assert.assertEquals("abcdef3", dbLevel3.getHtml());
        Assert.assertEquals("internal3", dbLevel3.getInternalDescription());
        Assert.assertEquals(3, dbLevel3.getItemSellFactor(), 0.0001);
        Assert.assertEquals(203, dbLevel3.getMaxMoney());
        Assert.assertEquals(3, dbLevel3.getNumber());
        Assert.assertEquals(2, dbLevel3.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(74, dbLevel3.getXp());
        dbLevelTask31 = dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask31.getId());
        Assert.assertEquals(36, dbLevelTask31.getMoney());
        Assert.assertEquals(87, dbLevelTask31.getXp());
        Assert.assertEquals("dbLevelTask31", dbLevelTask31.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask31.getDbTutorialConfig().getId());
        dbLevelTask32 = dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask32.getId());
        Assert.assertEquals(18, dbLevelTask32.getMoney());
        Assert.assertEquals(191, dbLevelTask32.getXp());
        Assert.assertEquals("dbLevelTask32", dbLevelTask32.getName());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // --------------------- DELETE ---------------------

        // Delete QuestHub1
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        userGuidanceService.getCrudQuestHub().deleteDbChild(dbQuestHubs.get(0));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(1, dbQuestHubs.size());
        dbQuestHub2 = dbQuestHubs.get(0);
        Assert.assertEquals("dbQuestHub2", dbQuestHub2.getName());
        Assert.assertFalse(dbQuestHub2.isRealBaseRequired());
        Assert.assertNull(dbQuestHub2.getStartTerritory());
        Assert.assertEquals(1, dbQuestHub2.getLevelCrud().readDbChildren().size());
        dbLevel3 = dbQuestHub2.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(25, dbLevel3.getHouseSpace());
        Assert.assertEquals("abcdef3", dbLevel3.getHtml());
        Assert.assertEquals("internal3", dbLevel3.getInternalDescription());
        Assert.assertEquals(3, dbLevel3.getItemSellFactor(), 0.0001);
        Assert.assertEquals(203, dbLevel3.getMaxMoney());
        Assert.assertEquals(3, dbLevel3.getNumber());
        Assert.assertEquals(2, dbLevel3.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(74, dbLevel3.getXp());
        dbLevelTask31 = dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask31.getId());
        Assert.assertEquals(36, dbLevelTask31.getMoney());
        Assert.assertEquals(87, dbLevelTask31.getXp());
        Assert.assertEquals("dbLevelTask31", dbLevelTask31.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask31.getDbTutorialConfig().getId());
        dbLevelTask32 = dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask32.getId());
        Assert.assertEquals(18, dbLevelTask32.getMoney());
        Assert.assertEquals(191, dbLevelTask32.getXp());
        Assert.assertEquals("dbLevelTask32", dbLevelTask32.getName());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Delete dbLevelTask32
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        dbLevel3 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0);
        dbLevel3.getLevelTaskCrud().deleteDbChild(dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask32.getId()));
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHubs.get(0));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(1, dbQuestHubs.size());
        dbQuestHub2 = dbQuestHubs.get(0);
        Assert.assertEquals("dbQuestHub2", dbQuestHub2.getName());
        Assert.assertFalse(dbQuestHub2.isRealBaseRequired());
        Assert.assertNull(dbQuestHub2.getStartTerritory());
        Assert.assertEquals(1, dbQuestHub2.getLevelCrud().readDbChildren().size());
        dbLevel3 = dbQuestHub2.getLevelCrud().readDbChildren().get(0);
        Assert.assertEquals(25, dbLevel3.getHouseSpace());
        Assert.assertEquals("abcdef3", dbLevel3.getHtml());
        Assert.assertEquals("internal3", dbLevel3.getInternalDescription());
        Assert.assertEquals(3, dbLevel3.getItemSellFactor(), 0.0001);
        Assert.assertEquals(203, dbLevel3.getMaxMoney());
        Assert.assertEquals(3, dbLevel3.getNumber());
        Assert.assertEquals(1, dbLevel3.getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(74, dbLevel3.getXp());
        dbLevelTask31 = dbLevel3.getLevelTaskCrud().readDbChild(dbLevelTask31.getId());
        Assert.assertEquals(36, dbLevelTask31.getMoney());
        Assert.assertEquals(87, dbLevelTask31.getXp());
        Assert.assertEquals("dbLevelTask31", dbLevelTask31.getName());
        Assert.assertEquals(tut1.getId(), dbLevelTask31.getDbTutorialConfig().getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Delete dbLevel3
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        dbLevel3 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0);
        dbQuestHubs.get(0).getLevelCrud().deleteDbChild(dbLevel3);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHubs.get(0));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Assert.assertEquals(1, dbQuestHubs.size());
        dbQuestHub2 = dbQuestHubs.get(0);
        Assert.assertEquals("dbQuestHub2", dbQuestHub2.getName());
        Assert.assertFalse(dbQuestHub2.isRealBaseRequired());
        Assert.assertNull(dbQuestHub2.getStartTerritory());
        Assert.assertEquals(0, dbQuestHub2.getLevelCrud().readDbChildren().size());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void orderingDbLevel() {
        // Setup QuestHubs
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub1 = userGuidanceService.getCrudQuestHub().createDbChild();
        DbLevel dbLevel11 = dbQuestHub1.getLevelCrud().createDbChild();
        DbLevel dbLevel12 = dbQuestHub1.getLevelCrud().createDbChild();
        DbLevel dbLevel13 = dbQuestHub1.getLevelCrud().createDbChild();
        DbLevel dbLevel14 = dbQuestHub1.getLevelCrud().createDbChild();
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        DbQuestHub dbQuestHub2 = userGuidanceService.getCrudQuestHub().createDbChild();
        DbLevel dbLevel21 = dbQuestHub2.getLevelCrud().createDbChild();
        DbLevel dbLevel22 = dbQuestHub2.getLevelCrud().createDbChild();
        DbLevel dbLevel23 = dbQuestHub2.getLevelCrud().createDbChild();
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub2);
        DbQuestHub dbQuestHub3 = userGuidanceService.getCrudQuestHub().createDbChild();
        DbLevel dbLevel31 = dbQuestHub3.getLevelCrud().createDbChild();
        DbLevel dbLevel32 = dbQuestHub3.getLevelCrud().createDbChild();
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub3);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        List<DbQuestHub> dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(3, dbQuestHubs.size());
        Assert.assertEquals(4, dbQuestHubs.get(0).getLevelCrud().readDbChildren().size());
        Assert.assertEquals(3, dbQuestHubs.get(1).getLevelCrud().readDbChildren().size());
        Assert.assertEquals(2, dbQuestHubs.get(2).getLevelCrud().readDbChildren().size());
        // Ordering
        Assert.assertEquals(dbQuestHub1.getId(), dbQuestHubs.get(0).getId());
        Assert.assertEquals(dbQuestHub2.getId(), dbQuestHubs.get(1).getId());
        Assert.assertEquals(dbQuestHub3.getId(), dbQuestHubs.get(2).getId());
        Assert.assertEquals(dbLevel11.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel12.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel13.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(2).getId());
        Assert.assertEquals(dbLevel14.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(3).getId());
        Assert.assertEquals(dbLevel21.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel22.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel23.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(2).getId());
        Assert.assertEquals(dbLevel31.getId(), dbQuestHubs.get(2).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel32.getId(), dbQuestHubs.get(2).getLevelCrud().readDbChildren().get(1).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify CmsOrderIndex
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        int coi11 = readCmsOrderIndex(dbLevel11);
        int coi12 = readCmsOrderIndex(dbLevel12);
        int coi13 = readCmsOrderIndex(dbLevel13);
        int coi14 = readCmsOrderIndex(dbLevel14);
        int coi21 = readCmsOrderIndex(dbLevel21);
        int coi22 = readCmsOrderIndex(dbLevel22);
        int coi23 = readCmsOrderIndex(dbLevel23);
        int coi31 = readCmsOrderIndex(dbLevel31);
        int coi32 = readCmsOrderIndex(dbLevel32);
        Assert.assertTrue(coi11 < coi12);
        Assert.assertTrue(coi12 < coi13);
        Assert.assertTrue(coi13 < coi14);
        Assert.assertTrue(coi14 < coi21);
        Assert.assertTrue(coi21 < coi22);
        Assert.assertTrue(coi22 < coi23);
        Assert.assertTrue(coi23 < coi31);
        Assert.assertTrue(coi31 < coi32);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Modify ordering
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        Collections.swap(dbQuestHubs, 0, 1);
        userGuidanceService.getCrudQuestHub().updateDbChildren(dbQuestHubs);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(3, dbQuestHubs.size());
        Assert.assertEquals(4, dbQuestHubs.get(1).getLevelCrud().readDbChildren().size());
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().size());
        Assert.assertEquals(2, dbQuestHubs.get(2).getLevelCrud().readDbChildren().size());
        // Ordering
        Assert.assertEquals(dbQuestHub1.getId(), dbQuestHubs.get(1).getId());
        Assert.assertEquals(dbQuestHub2.getId(), dbQuestHubs.get(0).getId());
        Assert.assertEquals(dbQuestHub3.getId(), dbQuestHubs.get(2).getId());
        Assert.assertEquals(dbLevel11.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel12.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel13.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(2).getId());
        Assert.assertEquals(dbLevel14.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(3).getId());
        Assert.assertEquals(dbLevel21.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel22.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel23.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(2).getId());
        Assert.assertEquals(dbLevel31.getId(), dbQuestHubs.get(2).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel32.getId(), dbQuestHubs.get(2).getLevelCrud().readDbChildren().get(1).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify CmsOrderIndex
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        coi11 = readCmsOrderIndex(dbLevel11);
        coi12 = readCmsOrderIndex(dbLevel12);
        coi13 = readCmsOrderIndex(dbLevel13);
        coi14 = readCmsOrderIndex(dbLevel14);
        coi21 = readCmsOrderIndex(dbLevel21);
        coi22 = readCmsOrderIndex(dbLevel22);
        coi23 = readCmsOrderIndex(dbLevel23);
        coi31 = readCmsOrderIndex(dbLevel31);
        coi32 = readCmsOrderIndex(dbLevel32);
        Assert.assertTrue(coi21 < coi22);
        Assert.assertTrue(coi22 < coi23);
        Assert.assertTrue(coi23 < coi11);
        Assert.assertTrue(coi11 < coi12);
        Assert.assertTrue(coi12 < coi13);
        Assert.assertTrue(coi13 < coi14);
        Assert.assertTrue(coi14 < coi31);
        Assert.assertTrue(coi31 < coi32);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Modify ordering
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub3 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub3.getId());
        Collections.swap(dbQuestHub3.getLevelCrud().readDbChildren(), 0, 1);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub3);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(3, dbQuestHubs.size());
        Assert.assertEquals(4, dbQuestHubs.get(1).getLevelCrud().readDbChildren().size());
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().size());
        Assert.assertEquals(2, dbQuestHubs.get(2).getLevelCrud().readDbChildren().size());
        // Ordering
        Assert.assertEquals(dbQuestHub1.getId(), dbQuestHubs.get(1).getId());
        Assert.assertEquals(dbQuestHub2.getId(), dbQuestHubs.get(0).getId());
        Assert.assertEquals(dbQuestHub3.getId(), dbQuestHubs.get(2).getId());
        Assert.assertEquals(dbLevel11.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel12.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel13.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(2).getId());
        Assert.assertEquals(dbLevel14.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(3).getId());
        Assert.assertEquals(dbLevel21.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel22.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel23.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(2).getId());
        Assert.assertEquals(dbLevel31.getId(), dbQuestHubs.get(2).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel32.getId(), dbQuestHubs.get(2).getLevelCrud().readDbChildren().get(0).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify CmsOrderIndex
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        coi11 = readCmsOrderIndex(dbLevel11);
        coi12 = readCmsOrderIndex(dbLevel12);
        coi13 = readCmsOrderIndex(dbLevel13);
        coi14 = readCmsOrderIndex(dbLevel14);
        coi21 = readCmsOrderIndex(dbLevel21);
        coi22 = readCmsOrderIndex(dbLevel22);
        coi23 = readCmsOrderIndex(dbLevel23);
        coi31 = readCmsOrderIndex(dbLevel31);
        coi32 = readCmsOrderIndex(dbLevel32);
        Assert.assertTrue(coi21 < coi22);
        Assert.assertTrue(coi22 < coi23);
        Assert.assertTrue(coi23 < coi11);
        Assert.assertTrue(coi11 < coi12);
        Assert.assertTrue(coi12 < coi13);
        Assert.assertTrue(coi13 < coi14);
        Assert.assertTrue(coi14 < coi32);
        Assert.assertTrue(coi32 < coi31);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Delete QuestHub & Levels
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        userGuidanceService.getCrudQuestHub().deleteDbChild(dbQuestHub1);
        dbQuestHub2 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub2.getId());
        dbLevel22 = dbQuestHub2.getLevelCrud().readDbChild(dbLevel22.getId());
        dbQuestHub2.getLevelCrud().deleteDbChild(dbLevel22);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub2);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(2, dbQuestHubs.size());
        Assert.assertEquals(2, dbQuestHubs.get(1).getLevelCrud().readDbChildren().size());
        Assert.assertEquals(2, dbQuestHubs.get(0).getLevelCrud().readDbChildren().size());
        // Ordering
        Assert.assertEquals(dbQuestHub2.getId(), dbQuestHubs.get(0).getId());
        Assert.assertEquals(dbQuestHub3.getId(), dbQuestHubs.get(1).getId());
        Assert.assertEquals(dbLevel21.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getId());
        Assert.assertEquals(dbLevel23.getId(), dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel31.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(1).getId());
        Assert.assertEquals(dbLevel32.getId(), dbQuestHubs.get(1).getLevelCrud().readDbChildren().get(0).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify CmsOrderIndex
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        coi21 = readCmsOrderIndex(dbLevel21);
        coi23 = readCmsOrderIndex(dbLevel23);
        coi31 = readCmsOrderIndex(dbLevel31);
        coi32 = readCmsOrderIndex(dbLevel32);
        Assert.assertTrue(coi21 < coi23);
        Assert.assertTrue(coi23 < coi32);
        Assert.assertTrue(coi32 < coi31);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    private int readCmsOrderIndex(DbLevel dbLevel) {
        dbLevel = (DbLevel) sessionFactory.getCurrentSession().get(DbLevel.class, dbLevel.getId());
        return dbLevel.getCmsOrderIndex();
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("unchecked")
    public void dbItemTypePositionComparisonConfig() throws Exception {
        configureItemTypes();
        // Setup
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub1 = userGuidanceService.getCrudQuestHub().createDbChild();
        DbLevel dbLevel1 = dbQuestHub1.getLevelCrud().createDbChild();
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Create
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        dbLevel1 = dbQuestHub1.getLevelCrud().readDbChild(dbLevel1.getId());
        DbConditionConfig dbConditionConfig1 = new DbConditionConfig();
        DbLevelTask dbLevelTask = dbLevel1.getLevelTaskCrud().createDbChild();
        dbLevelTask.setDbConditionConfig(dbConditionConfig1);
        dbConditionConfig1.setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION);
        DbItemTypePositionComparisonConfig comparisonConfig = new DbItemTypePositionComparisonConfig();
        DbComparisonItemCount dbComparisonItemCount = comparisonConfig.getCrudDbComparisonItemCount().createDbChild();
        dbComparisonItemCount.setCount(1);
        dbComparisonItemCount.setItemType(itemService.getDbBaseItemType(TEST_ATTACK_ITEM_ID));
        dbComparisonItemCount = comparisonConfig.getCrudDbComparisonItemCount().createDbChild();
        dbComparisonItemCount.setCount(2);
        dbComparisonItemCount.setItemType(itemService.getDbBaseItemType(TEST_HARVESTER_ITEM_ID));
        comparisonConfig.setRegion(new Rectangle(101, 202, 3003, 4004));
        comparisonConfig.setTimeInMinutes(10);
        dbConditionConfig1.setDbAbstractComparisonConfig(comparisonConfig);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbLevel1 = userGuidanceService.getDbLevel(dbLevel1.getId());
        dbLevelTask = CommonJava.getFirst(dbLevel1.getLevelTaskCrud().readDbChildren());
        dbConditionConfig1 = dbLevelTask.getDbConditionConfig();
        ConditionConfig conditionConfig = dbConditionConfig1.createConditionConfig(itemService);
        Assert.assertEquals(ConditionTrigger.SYNC_ITEM_POSITION, conditionConfig.getConditionTrigger());
        ItemTypePositionComparisonConfig itemTypePositionComparisonConfig = (ItemTypePositionComparisonConfig) conditionConfig.getAbstractComparisonConfig();
        Rectangle rectangle = (Rectangle) getPrivateField(ItemTypePositionComparisonConfig.class, itemTypePositionComparisonConfig, "region");
        Assert.assertEquals(new Rectangle(101, 202, 3003, 4004), rectangle);
        Integer time = (Integer) getPrivateField(ItemTypePositionComparisonConfig.class, itemTypePositionComparisonConfig, "time");
        Assert.assertEquals((int) time, 10 * 60 * 1000);
        Map<ItemType, Integer> itemTypes = (Map<ItemType, Integer>) getPrivateField(ItemTypePositionComparisonConfig.class, itemTypePositionComparisonConfig, "itemTypes");
        Assert.assertEquals(2, itemTypes.size());
        Assert.assertEquals(1, (int) itemTypes.get(itemService.getItemType(TEST_ATTACK_ITEM_ID)));
        Assert.assertEquals(2, (int) itemTypes.get(itemService.getItemType(TEST_HARVESTER_ITEM_ID)));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Remove dbComparisonItemCount
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub = CommonJava.getFirst(userGuidanceService.getCrudQuestHub().readDbChildren());
        dbLevel1 = dbQuestHub.getLevelCrud().readDbChild(dbLevel1.getId());
        dbLevelTask = CommonJava.getFirst(dbLevel1.getLevelTaskCrud().readDbChildren());
        comparisonConfig = (DbItemTypePositionComparisonConfig) dbLevelTask.getDbConditionConfig().getDbAbstractComparisonConfig();
        dbComparisonItemCount = CommonJava.getFirst(comparisonConfig.getCrudDbComparisonItemCount().readDbChildren());
        comparisonConfig.getCrudDbComparisonItemCount().deleteDbChild(dbComparisonItemCount);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        Assert.assertEquals(1, HibernateUtil.loadAll(sessionFactory, DbComparisonItemCount.class).size());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Remove whole comparision
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub = CommonJava.getFirst(userGuidanceService.getCrudQuestHub().readDbChildren());
        dbLevel1 = dbQuestHub.getLevelCrud().readDbChild(dbLevel1.getId());
        dbLevelTask = CommonJava.getFirst(dbLevel1.getLevelTaskCrud().readDbChildren());
        dbLevelTask.setDbConditionConfig(null);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        Assert.assertEquals(0, HibernateUtil.loadAll(sessionFactory, DbConditionConfig.class).size());
        Assert.assertEquals(0, HibernateUtil.loadAll(sessionFactory, DbAbstractComparisonConfig.class).size());
        Assert.assertEquals(0, HibernateUtil.loadAll(sessionFactory, DbComparisonItemCount.class).size());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void dbSyncItemTypeComparisonConfig() throws Exception {
        configureItemTypes();
        // Setup
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub1 = userGuidanceService.getCrudQuestHub().createDbChild();
        DbLevel dbLevel1 = dbQuestHub1.getLevelCrud().createDbChild();
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Create
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        dbLevel1 = dbQuestHub1.getLevelCrud().readDbChild(dbLevel1.getId());
        DbLevelTask dbLevelTask = dbLevel1.getLevelTaskCrud().createDbChild();
        DbConditionConfig dbConditionConfig1 = new DbConditionConfig();
        dbLevelTask.setDbConditionConfig(dbConditionConfig1);
        dbConditionConfig1.setConditionTrigger(ConditionTrigger.SYNC_ITEM_BUILT);
        DbSyncItemTypeComparisonConfig comparisonConfig = new DbSyncItemTypeComparisonConfig();
        DbComparisonItemCount dbComparisonItemCount = comparisonConfig.getCrudDbComparisonItemCount().createDbChild();
        dbComparisonItemCount.setCount(1);
        dbComparisonItemCount.setItemType(itemService.getDbBaseItemType(TEST_ATTACK_ITEM_ID));
        dbComparisonItemCount = comparisonConfig.getCrudDbComparisonItemCount().createDbChild();
        dbComparisonItemCount.setCount(2);
        dbComparisonItemCount.setItemType(itemService.getDbBaseItemType(TEST_HARVESTER_ITEM_ID));
        dbConditionConfig1.setDbAbstractComparisonConfig(comparisonConfig);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbLevel1 = userGuidanceService.getDbLevel(dbLevel1.getId());
        dbLevelTask = CommonJava.getFirst(dbLevel1.getLevelTaskCrud().readDbChildren());
        dbConditionConfig1 = dbLevelTask.getDbConditionConfig();
        ConditionConfig conditionConfig = dbConditionConfig1.createConditionConfig(itemService);
        Assert.assertEquals(ConditionTrigger.SYNC_ITEM_BUILT, conditionConfig.getConditionTrigger());
        SyncItemTypeComparisonConfig syncItemTypeComparisonConfig = (SyncItemTypeComparisonConfig) conditionConfig.getAbstractComparisonConfig();
        Map<ItemType, Integer> itemTypes = (Map<ItemType, Integer>) getPrivateField(SyncItemTypeComparisonConfig.class, syncItemTypeComparisonConfig, "itemTypeCount");
        Assert.assertEquals(2, itemTypes.size());
        Assert.assertEquals(1, (int) itemTypes.get(itemService.getItemType(TEST_ATTACK_ITEM_ID)));
        Assert.assertEquals(2, (int) itemTypes.get(itemService.getItemType(TEST_HARVESTER_ITEM_ID)));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Remove dbComparisonItemCount
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub = CommonJava.getFirst(userGuidanceService.getCrudQuestHub().readDbChildren());
        dbLevel1 = dbQuestHub.getLevelCrud().readDbChild(dbLevel1.getId());
        dbLevelTask = CommonJava.getFirst(dbLevel1.getLevelTaskCrud().readDbChildren());
        comparisonConfig = (DbSyncItemTypeComparisonConfig) dbLevelTask.getDbConditionConfig().getDbAbstractComparisonConfig();
        dbComparisonItemCount = CommonJava.getFirst(comparisonConfig.getCrudDbComparisonItemCount().readDbChildren());
        comparisonConfig.getCrudDbComparisonItemCount().deleteDbChild(dbComparisonItemCount);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        Assert.assertEquals(1, HibernateUtil.loadAll(sessionFactory, DbComparisonItemCount.class).size());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Remove whole comparision
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub = CommonJava.getFirst(userGuidanceService.getCrudQuestHub().readDbChildren());
        dbLevel1 = dbQuestHub.getLevelCrud().readDbChild(dbLevel1.getId());
        dbLevelTask = CommonJava.getFirst(dbLevel1.getLevelTaskCrud().readDbChildren());
        dbLevelTask.setDbConditionConfig(null);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        Assert.assertEquals(0, HibernateUtil.loadAll(sessionFactory, DbConditionConfig.class).size());
        Assert.assertEquals(0, HibernateUtil.loadAll(sessionFactory, DbAbstractComparisonConfig.class).size());
        Assert.assertEquals(0, HibernateUtil.loadAll(sessionFactory, DbComparisonItemCount.class).size());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void orderingDbLevelTask() {
        // Setup QuestHubs
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub1 = userGuidanceService.getCrudQuestHub().createDbChild();
        DbLevel dbLevel11 = dbQuestHub1.getLevelCrud().createDbChild();
        DbLevelTask dbLevelTask111 = dbLevel11.getLevelTaskCrud().createDbChild();
        DbLevelTask dbLevelTask112 = dbLevel11.getLevelTaskCrud().createDbChild();
        DbLevelTask dbLevelTask113 = dbLevel11.getLevelTaskCrud().createDbChild();
        DbLevel dbLevel12 = dbQuestHub1.getLevelCrud().createDbChild();
        DbLevelTask dbLevelTask121 = dbLevel12.getLevelTaskCrud().createDbChild();
        DbLevelTask dbLevelTask122 = dbLevel12.getLevelTaskCrud().createDbChild();
        DbLevelTask dbLevelTask123 = dbLevel12.getLevelTaskCrud().createDbChild();
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        List<DbQuestHub> dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren().size());
        // Ordering
        List<DbLevelTask> dbLevelTasks11 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren();
        List<DbLevelTask> dbLevelTasks12 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren();
        Assert.assertEquals(dbLevelTask111.getId(), dbLevelTasks11.get(0).getId());
        Assert.assertEquals(dbLevelTask112.getId(), dbLevelTasks11.get(1).getId());
        Assert.assertEquals(dbLevelTask113.getId(), dbLevelTasks11.get(2).getId());
        Assert.assertEquals(dbLevelTask121.getId(), dbLevelTasks12.get(0).getId());
        Assert.assertEquals(dbLevelTask122.getId(), dbLevelTasks12.get(1).getId());
        Assert.assertEquals(dbLevelTask123.getId(), dbLevelTasks12.get(2).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Modify ordering
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        dbLevelTasks11 = dbQuestHub1.getLevelCrud().readDbChild(dbLevel11.getId()).getLevelTaskCrud().readDbChildren();
        Collections.swap(dbLevelTasks11, 0, 1);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren().size());
        // Ordering
        dbLevelTasks11 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren();
        dbLevelTasks12 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren();
        Assert.assertEquals(dbLevelTask111.getId(), dbLevelTasks11.get(1).getId());
        Assert.assertEquals(dbLevelTask112.getId(), dbLevelTasks11.get(0).getId());
        Assert.assertEquals(dbLevelTask113.getId(), dbLevelTasks11.get(2).getId());
        Assert.assertEquals(dbLevelTask121.getId(), dbLevelTasks12.get(0).getId());
        Assert.assertEquals(dbLevelTask122.getId(), dbLevelTasks12.get(1).getId());
        Assert.assertEquals(dbLevelTask123.getId(), dbLevelTasks12.get(2).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Modify ordering
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        dbLevelTasks11 = dbQuestHub1.getLevelCrud().readDbChild(dbLevel12.getId()).getLevelTaskCrud().readDbChildren();
        Collections.swap(dbLevelTasks11, 1, 2);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren().size());
        // Ordering
        dbLevelTasks11 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren();
        dbLevelTasks12 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren();
        Assert.assertEquals(dbLevelTask111.getId(), dbLevelTasks11.get(1).getId());
        Assert.assertEquals(dbLevelTask112.getId(), dbLevelTasks11.get(0).getId());
        Assert.assertEquals(dbLevelTask113.getId(), dbLevelTasks11.get(2).getId());
        Assert.assertEquals(dbLevelTask121.getId(), dbLevelTasks12.get(0).getId());
        Assert.assertEquals(dbLevelTask122.getId(), dbLevelTasks12.get(2).getId());
        Assert.assertEquals(dbLevelTask123.getId(), dbLevelTasks12.get(1).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Modify delete
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        dbLevelTask123 = dbQuestHub1.getLevelCrud().readDbChild(dbLevel12.getId()).getLevelTaskCrud().readDbChild(dbLevelTask123.getId());
        dbQuestHub1.getLevelCrud().readDbChild(dbLevel12.getId()).getLevelTaskCrud().deleteDbChild(dbLevelTask123);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(3, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(2, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren().size());
        // Ordering
        dbLevelTasks11 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren();
        dbLevelTasks12 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren();
        Assert.assertEquals(dbLevelTask111.getId(), dbLevelTasks11.get(1).getId());
        Assert.assertEquals(dbLevelTask112.getId(), dbLevelTasks11.get(0).getId());
        Assert.assertEquals(dbLevelTask113.getId(), dbLevelTasks11.get(2).getId());
        Assert.assertEquals(dbLevelTask121.getId(), dbLevelTasks12.get(0).getId());
        Assert.assertEquals(dbLevelTask122.getId(), dbLevelTasks12.get(1).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Modify add
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHub1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId());
        DbLevelTask dbLevelTask114 = dbQuestHub1.getLevelCrud().readDbChild(dbLevel11.getId()).getLevelTaskCrud().createDbChild();
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // Verify
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        dbQuestHubs = new ArrayList<>(userGuidanceService.getCrudQuestHub().readDbChildren());
        // Size
        Assert.assertEquals(4, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren().size());
        Assert.assertEquals(2, dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren().size());
        // Ordering
        dbLevelTasks11 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(0).getLevelTaskCrud().readDbChildren();
        dbLevelTasks12 = dbQuestHubs.get(0).getLevelCrud().readDbChildren().get(1).getLevelTaskCrud().readDbChildren();
        Assert.assertEquals(dbLevelTask111.getId(), dbLevelTasks11.get(1).getId());
        Assert.assertEquals(dbLevelTask112.getId(), dbLevelTasks11.get(0).getId());
        Assert.assertEquals(dbLevelTask113.getId(), dbLevelTasks11.get(2).getId());
        Assert.assertEquals(dbLevelTask114.getId(), dbLevelTasks11.get(3).getId());
        Assert.assertEquals(dbLevelTask121.getId(), dbLevelTasks12.get(0).getId());
        Assert.assertEquals(dbLevelTask122.getId(), dbLevelTasks12.get(1).getId());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void createQuestInfo() {
        DbTutorialConfig tut1 = createTutorial1();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        DbQuestHub dbQuestHub1 = userGuidanceService.getCrudQuestHub().createDbChild();
        DbLevel dbLevel11 = dbQuestHub1.getLevelCrud().createDbChild();
        DbLevelTask dbLevelTask111 = dbLevel11.getLevelTaskCrud().createDbChild();
        dbLevelTask111.setName("name1");
        dbLevelTask111.setHtml("html1");
        dbLevelTask111.setXp(11);
        dbLevelTask111.setMoney(12);
        dbLevelTask111.setDbTutorialConfig(tutorialService.getDbTutorialCrudRootServiceHelper().readDbChild(tut1.getId()));
        DbLevelTask dbLevelTask112 = dbLevel11.getLevelTaskCrud().createDbChild();
        dbLevelTask112.setName("name2");
        dbLevelTask112.setHtml("html2");
        dbLevelTask112.setXp(21);
        dbLevelTask112.setMoney(22);
        DbConditionConfig dbConditionConfig = new DbConditionConfig();
        dbConditionConfig.setRadarPositionHint(new Index(100,200));
        dbLevelTask112.setDbConditionConfig(dbConditionConfig);
        userGuidanceService.getCrudQuestHub().updateDbChild(dbQuestHub1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        QuestInfo questInfo1 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId()).getLevelCrud().readDbChild(dbLevel11.getId()).getLevelTaskCrud().readDbChildren().get(0).createQuestInfo();
        QuestInfo questInfo2 = userGuidanceService.getCrudQuestHub().readDbChild(dbQuestHub1.getId()).getLevelCrud().readDbChild(dbLevel11.getId()).getLevelTaskCrud().readDbChildren().get(1).createQuestInfo();
        Assert.assertEquals(new QuestInfo("name1", "html1", 11, 12, dbLevelTask111.getId(), QuestInfo.Type.MISSION, null), questInfo1);
        Assert.assertEquals(new QuestInfo("name2", "html2", 21, 22, dbLevelTask112.getId(), QuestInfo.Type.QUEST, new Index(100,200)), questInfo2);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

    }
}