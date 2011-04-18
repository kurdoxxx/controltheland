package com.btxtech.game.services.connection;

import com.btxtech.game.jsre.client.MovableService;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.info.RealityInfo;
import com.btxtech.game.jsre.common.AccountBalancePacket;
import com.btxtech.game.jsre.common.LevelPacket;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.XpBalancePacket;
import com.btxtech.game.jsre.common.gameengine.syncObjects.Id;
import com.btxtech.game.jsre.common.tutorial.TutorialConfig;
import com.btxtech.game.services.BaseTestService;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.services.utg.DbRealGameLevel;
import com.btxtech.game.services.utg.UserGuidanceService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

/**
 * User: beat
 * Date: 04.04.2011
 * Time: 16:26:59
 */
public class PacketSendingTest extends BaseTestService {
    @Autowired
    private MovableService movableService;
    @Autowired
    private UserGuidanceService userGuidanceService;
    @Autowired
    private UserService userService;

    @Test
    @DirtiesContext
    public void testCreateAndSell() throws Exception {
        configureMinimalGame();
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        System.out.println("----- testSimple -----");

        movableService.sendTutorialProgress(TutorialConfig.TYPE.TUTORIAL, "", "", 0, 0);
        System.out.println("----- real game entered -----");
        RealityInfo realityInfo = (RealityInfo) movableService.getGameInfo(); // Connection is created here. Don't call movableService.getGameInfo() again!
        SimpleBase simpleBase = realityInfo.getBase();
        // Buy
        sendBuildCommand(getFirstSynItemId(simpleBase, TEST_START_BUILDER_ITEM_ID), new Index(1000, 100), TEST_FACTORY_ITEM_ID);
        waitForActionServiceDone();

        AccountBalancePacket accountBalancePacket = new AccountBalancePacket();
        accountBalancePacket.setAccountBalance(998);
        assertPackagesIgnoreSyncItemInfoAndClear(accountBalancePacket);

        // Sell
        movableService.sellItem(getFirstSynItemId(simpleBase, TEST_FACTORY_ITEM_ID));
        waitForActionServiceDone();
        accountBalancePacket = new AccountBalancePacket();
        accountBalancePacket.setAccountBalance(999);
        assertPackagesIgnoreSyncItemInfoAndClear(accountBalancePacket);

        System.out.println("----- testSimple end -----");
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void testKillXp() throws Exception {
        configureMinimalGame();
        System.out.println("****** testKillXp ******");
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        // Create target
        movableService.sendTutorialProgress(TutorialConfig.TYPE.TUTORIAL, "", "", 0, 0);
        Id target = getFirstSynItemId(TEST_START_BUILDER_ITEM_ID);
        sendBuildCommand(target, new Index(500, 100), TEST_FACTORY_ITEM_ID);
        waitForActionServiceDone();

        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        // Actor
        movableService.sendTutorialProgress(TutorialConfig.TYPE.TUTORIAL, "", "", 0, 0);
        RealityInfo realityInfo = (RealityInfo) movableService.getGameInfo(); // Connection is created here. Don't call movableService.getGameInfo() again!
        SimpleBase simpleBase = realityInfo.getBase();
        // Attack
        sendBuildCommand(getFirstSynItemId(simpleBase, TEST_START_BUILDER_ITEM_ID), new Index(1000, 100), TEST_FACTORY_ITEM_ID);
        waitForActionServiceDone();
        sendFactoryCommand(getFirstSynItemId(simpleBase, TEST_FACTORY_ITEM_ID), TEST_ATTACK_ITEM_ID);
        waitForActionServiceDone();
        clearPackets();

        sendAttackCommand(getFirstSynItemId(simpleBase, TEST_ATTACK_ITEM_ID), target);
        waitForActionServiceDone();

        XpBalancePacket xpBalancePacket = new XpBalancePacket();
        xpBalancePacket.setXp(1);
        Thread.sleep(3000);
        assertPackagesIgnoreSyncItemInfoAndClear(xpBalancePacket);

        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void testLevelUp() throws Exception {
        configureMinimalGame();
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        // Create target
        movableService.sendTutorialProgress(TutorialConfig.TYPE.TUTORIAL, "", "", 0, 0);
        RealityInfo realityInfo = (RealityInfo) movableService.getGameInfo(); // Connection is created here. Don't call movableService.getGameInfo() again!
        SimpleBase simpleBase = realityInfo.getBase();
        clearPackets();
        userGuidanceService.promote(userService.getUserState(), TEST_LEVEL_3_REAL_ID);

        DbRealGameLevel dbRealGameLevel = userGuidanceService.getDbLevel();
        LevelPacket levelPacket = new LevelPacket();
        levelPacket.setLevel(dbRealGameLevel.getLevel());

        AccountBalancePacket accountBalancePacket = new AccountBalancePacket();
        accountBalancePacket.setAccountBalance(1500);

        XpBalancePacket xpBalancePacket = new XpBalancePacket();
        xpBalancePacket.setXp(500);

        assertPackagesIgnoreSyncItemInfoAndClear(levelPacket, accountBalancePacket, xpBalancePacket);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

    }
}
