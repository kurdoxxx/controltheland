package com.btxtech.game.services.user;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.info.InvalidLevelStateException;
import com.btxtech.game.jsre.client.common.info.RealGameInfo;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.gameengine.services.base.BaseAttributes;
import com.btxtech.game.jsre.common.packets.AllianceOfferPacket;
import com.btxtech.game.jsre.common.packets.BaseChangedPacket;
import com.btxtech.game.services.AbstractServiceTest;
import com.btxtech.game.services.ServerConnectionServiceTestHelperNew;
import com.btxtech.game.services.mgmt.MgmtService;
import com.btxtech.game.services.planet.PlanetSystemService;
import com.btxtech.game.services.planet.impl.ServerPlanetServicesImpl;
import com.btxtech.game.services.utg.UserGuidanceService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.List;

/**
 * User: beat
 * Date: 24.04.12
 * Time: 21:50
 */
public class TestAllianceService extends AbstractServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserGuidanceService userGuidanceService;
    @Autowired
    private AllianceService allianceService;
    @Autowired
    private MgmtService mgmtService;
    @Autowired
    private PlanetSystemService planetSystemService;
    @Autowired
    private RegisterService registerService;

    @Test
    @DirtiesContext
    public void addAllianceAndBreak() throws Exception {
        configureSimplePlanetNoResources();
        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u1");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        SimpleBase simpleBase1 = getOrCreateBase();
        UserState userState1 = getUserState();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoMessages(connectionServiceTestHelper, userState1);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u2");
        SimpleBase simpleBase2 = createBase(new Index(2000, 2000));
        UserState userState2 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        connectionServiceTestHelper.clearReceivedPackets();
        connectionServiceTestHelper.clearMessageEntries();
        allianceService.proposeAlliance(simpleBase1);
        verifyAllianceOffers();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoAlliancesInPackets(connectionServiceTestHelper, userState1);
        assertNoAlliancesInPackets(connectionServiceTestHelper, userState2);
        assertNoMessages(connectionServiceTestHelper, userState1);
        assertNoMessages(connectionServiceTestHelper, userState2);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1", "test");
        verifyAllianceOffers("u2");
        verifyAlliances();
        verifyAlliancesFromUser();
        connectionServiceTestHelper.clearReceivedPackets();
        connectionServiceTestHelper.clearMessageEntries();
        allianceService.acceptAllianceOffer("u2");
        verifyAllianceOffers();
        verifyAlliances("u2");
        verifyAlliancesFromUser("u2");
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoMessages(connectionServiceTestHelper, userState1);
        assertMessage(connectionServiceTestHelper, userState2, "alliancesAccepted", "u1", false);
        List<ServerConnectionServiceTestHelperNew.PacketEntry> packets = connectionServiceTestHelper.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(2, packets.size());
        assertAlliancesInPacketToAll(packets, simpleBase1, "u2");
        assertAlliancesInPacketToAll(packets, simpleBase2, "u1");
        connectionServiceTestHelper.clearReceivedPackets();
        connectionServiceTestHelper.clearMessageEntries();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1", "test");
        verifyAllianceOffers();
        verifyAlliances("u2");
        verifyAlliancesFromUser("u2");
        allianceService.breakAlliance("u2");
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoMessages(connectionServiceTestHelper, userState1);
        assertMessage(connectionServiceTestHelper, userState2, "alliancesBroken", "u1", false);
        packets = connectionServiceTestHelper.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(2, packets.size());
        assertAlliancesInPacketToAll(packets, simpleBase1);
        assertAlliancesInPacketToAll(packets, simpleBase2);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u2", "test");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1", "test");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void rejectAlliance() throws Exception {
        configureSimplePlanetNoResources();
        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("U1");
        UserState userState1 = getUserState();
        getOrCreateBase();
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        SimpleBase simpleBase1 = getOrCreateBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("U2");
        UserState userState2 = getUserState();
        createBase(new Index(2000, 2000));
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        connectionServiceTestHelper.clearReceivedPackets();
        allianceService.proposeAlliance(simpleBase1);
        verifyAllianceOffers();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoAlliancesInPackets(connectionServiceTestHelper, userState1);
        assertNoAlliancesInPackets(connectionServiceTestHelper, userState2);
        assertNoMessages(connectionServiceTestHelper, userState1);
        assertNoMessages(connectionServiceTestHelper, userState2);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U1", "test");
        verifyAllianceOffers("U2");
        verifyAlliances();
        verifyAlliancesFromUser();
        connectionServiceTestHelper.clearReceivedPackets();
        allianceService.rejectAllianceOffer("U2");
        verifyAllianceOffers();
        verifyAlliancesFromUser();
        verifyAlliances();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoAlliancesInPackets(connectionServiceTestHelper, userState1);
        assertNoAlliancesInPackets(connectionServiceTestHelper, userState2);
        assertNoMessages(connectionServiceTestHelper, userState1);
        assertMessage(connectionServiceTestHelper, userState2, "alliancesRejected", "U1", false);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U1", "test");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U2", "test");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void addAllianceBothUnregistered() throws Exception {
        configureSimplePlanetNoResources();
        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        verifyAllianceOffers();
        verifyAlliances();
        UserState userState1 = getUserState();
        SimpleBase simpleBase1 = getOrCreateBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createBase(new Index(2000, 2000));
        UserState userState2 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        try {
            allianceService.proposeAlliance(simpleBase1);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        verifyAllianceOffers();
        verifyAlliances();
        try {
            verifyAlliancesFromUser();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // Expected
        }
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoMessages(connectionServiceTestHelper, userState1);
        assertNoMessages(connectionServiceTestHelper, userState2);
    }

    @Test
    @DirtiesContext
    public void addAllianceU1Unverified() throws Exception {
        startFakeMailServer();
        configureSimplePlanetNoResources();
        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        verifyAllianceOffers();
        verifyAlliances();
        SimpleBase simpleBase1 = getOrCreateBase();
        UserState userState1 = getUserState();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createBase(new Index(2000, 2000));
        UserState userState2 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        registerService.register("u1", "xxx", "xxx", "xxx");
        try {
            allianceService.proposeAlliance(simpleBase1);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoMessages(connectionServiceTestHelper, userState1);
        assertNoMessages(connectionServiceTestHelper, userState2);
        stopFakeMailServer();
    }

    @Test
    @DirtiesContext
    public void addAllianceU2Unverified() throws Exception {
        startFakeMailServer();
        configureSimplePlanetNoResources();
        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        verifyAllianceOffers();
        verifyAlliances();
        UserState userState1 = getUserState();
        registerService.register("u2", "xxx", "xxx", "xxx");
        SimpleBase simpleBase1 = getOrCreateBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createBase(new Index(2000, 2000));
        UserState userState2 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        createAndLoginUser("u1");
        allianceService.proposeAlliance(simpleBase1);
        verifyAllianceOffers();
        verifyAlliances();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertMessage(connectionServiceTestHelper, userState1, "alliancesOfferedOnlyRegistered", "u1", false);
        assertMessage(connectionServiceTestHelper, userState2, "alliancesOfferedNotRegistered", "u2", false);
        stopFakeMailServer();
    }

    @Test
    @DirtiesContext
    public void createDeleteBase() throws Exception {
        configureSimplePlanetNoResources();
        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("U1");
        UserState userState1 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        SimpleBase simpleBase1 = getOrCreateBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("U2");
        UserState userState2 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        SimpleBase simpleBase2 = createBase(new Index(2000, 2000));
        allianceService.proposeAlliance(simpleBase1);
        verifyAllianceOffers();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U1", "test");
        verifyAllianceOffers("U2");
        verifyAlliances();
        verifyAlliancesFromUser();
        allianceService.acceptAllianceOffer("U2");
        verifyAllianceOffers();
        verifyAlliances("U2");
        verifyAlliancesFromUser("U2");
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertNoMessages(connectionServiceTestHelper, userState1);
        assertMessage(connectionServiceTestHelper, userState2, "alliancesAccepted", "U1", false);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U2", "test");
        verifyAllianceOffers();
        verifyAlliances("U1");
        verifyAlliancesFromUser("U1");
        connectionServiceTestHelper.clearReceivedPackets();
        connectionServiceTestHelper.clearMessageEntries();
        getMovableService().sellItem(getFirstSynItemId(TEST_START_BUILDER_ITEM_ID));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        List<ServerConnectionServiceTestHelperNew.PacketEntry> packets = connectionServiceTestHelper.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(2, packets.size()); // REMOVE and CHANGE packet
        assertAlliancesInPacketToAll(packets, simpleBase1);
        assertBaseDeletedPacket(connectionServiceTestHelper, simpleBase2);
        assertNoMessages(connectionServiceTestHelper, userState1);
        assertNoMessages(connectionServiceTestHelper, userState2);
        connectionServiceTestHelper.clearReceivedPackets();
        connectionServiceTestHelper.clearMessageEntries();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U1", "test");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser("U2");
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U2", "test");
        verifyAllianceOffers();
        verifyAlliances("U1");
        verifyAlliancesFromUser("U1");
        SimpleBase simpleBase3 = createBase(new Index(3000, 3000));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        packets = connectionServiceTestHelper.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(3, packets.size());
        Assert.assertEquals(BaseChangedPacket.Type.CREATED, ((BaseChangedPacket) packets.get(0).getPacket()).getType());
        Assert.assertEquals(simpleBase3, ((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes().getSimpleBase());
        Assert.assertEquals(BaseChangedPacket.Type.CHANGED, ((BaseChangedPacket) packets.get(1).getPacket()).getType());
        Assert.assertEquals(BaseChangedPacket.Type.CHANGED, ((BaseChangedPacket) packets.get(2).getPacket()).getType());

        assertAlliancesInPacketToAll(packets, simpleBase1, "U2");
        assertAlliancesInPacketToAll(packets, simpleBase3, "U1");

        assertNoMessages(connectionServiceTestHelper, userState1);
        assertNoMessages(connectionServiceTestHelper, userState2);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("U1", "test");
        verifyAllianceOffers();
        verifyAlliances("U2");
        verifyAlliancesFromUser("U2");
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void addAlliancePartnerUnregistered() throws Exception {
        // TODO can not be testes because base becomes abandoned
        /*configureSimplePlanetNoResources();

        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        verifyAllianceOffers();
        verifyAlliances();
        SimpleBase simpleBase1 = getOrCreateBase();
        UserState userState1 = getUserState();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u2");
        UserState userState2 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        createBase(new Index(2000, 2000));
        allianceService.proposeAlliance(simpleBase1);
        verifyAllianceOffers();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertMessage(connectionServiceTestHelper, userState1, "alliancesOfferedOnlyRegistered", "u2", true);
        assertMessage(connectionServiceTestHelper, userState2, "alliancesOfferedNotRegistered", "Base 1", false);*/
    }

    @Test
    @DirtiesContext
    public void addAlliancePartnerAbandoned() throws Exception {
        configureSimplePlanetNoResources();

        ServerConnectionServiceTestHelperNew connectionServiceTestHelper = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelper);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        verifyAllianceOffers();
        verifyAlliances();
        SimpleBase simpleBase1 = getOrCreateBase();
        UserState userState1 = getUserState();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u2");
        UserState userState2 = getUserState();
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        createBase(new Index(2000, 2000));
        allianceService.proposeAlliance(simpleBase1);
        verifyAllianceOffers();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        assertMessage(connectionServiceTestHelper, userState2, "alliancesOfferedBaseAbandoned", "Base 1", false);

    }

    @Test
    @DirtiesContext
    public void backupRestore() throws Exception {
        configureSimplePlanetNoResources();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u1");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        SimpleBase simpleBase1 = getOrCreateBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u2");
        createBase(new Index(2000, 2000));
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        allianceService.proposeAlliance(simpleBase1);
        verifyAllianceOffers();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.backup();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.restore(mgmtService.getBackupSummary().get(0).getDate());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1", "test");
        verifyAllianceOffers("u2");
        verifyAlliancesFromUser();
        verifyAlliances();
        allianceService.acceptAllianceOffer("u2");
        verifyAllianceOffers();
        verifyAlliancesFromUser("u2");
        verifyAlliances("u2");
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.backup();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.restore(mgmtService.getBackupSummary().get(0).getDate());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1", "test");
        verifyAllianceOffers();
        verifyAlliances("u2");
        verifyAlliancesFromUser("u2");
        allianceService.breakAlliance("u2");
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.backup();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.restore(mgmtService.getBackupSummary().get(0).getDate());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u2", "test");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.backup();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        mgmtService.restore(mgmtService.getBackupSummary().get(0).getDate());
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1", "test");
        verifyAllianceOffers();
        verifyAlliances();
        verifyAlliancesFromUser();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();
    }

    @Test
    @DirtiesContext
    public void createDeleteBaseDifferentPlanets() throws Exception {
        configureMultiplePlanetsAndLevels();
        ServerConnectionServiceTestHelperNew connectionServiceTestHelperP1 = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices()), connectionServiceTestHelperP1);
        ServerConnectionServiceTestHelperNew connectionServiceTestHelperP2 = new ServerConnectionServiceTestHelperNew();
        overrideConnectionService(((ServerPlanetServicesImpl) planetSystemService.getPlanet(TEST_PLANET_2_ID).getPlanetServices()), connectionServiceTestHelperP2);

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u1p2");
        userGuidanceService.promote(userService.getUserState(), TEST_LEVEL_2_REAL_ID);
        SimpleBase simpleBase1P1 = getOrCreateBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u2p1");
        userGuidanceService.promote(userService.getUserState(), TEST_LEVEL_2_REAL_ID);
        SimpleBase simpleBase2P1 = createBase(new Index(2000, 2000));
        allianceService.proposeAlliance(simpleBase1P1);
        verifyAllianceOffers();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        // U1 promote give up base on planet 1
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1p2", "test");
        allianceService.acceptAllianceOffer("u2p1");
        connectionServiceTestHelperP1.clearReceivedPackets();
        connectionServiceTestHelperP2.clearReceivedPackets();
        userGuidanceService.promote(userService.getUserState(), TEST_LEVEL_5_REAL_ID);
        getMovableService().surrenderBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        List<ServerConnectionServiceTestHelperNew.PacketEntry> packets = connectionServiceTestHelperP1.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(3, packets.size());
        Assert.assertEquals("Base 1", (((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes()).getName());
        Assert.assertEquals(BaseChangedPacket.Type.CHANGED, (((BaseChangedPacket) packets.get(0).getPacket()).getType()));
        Assert.assertTrue(getBaseChangedPacket(packets.subList(1, 3), simpleBase1P1).getAlliances().isEmpty());
        Assert.assertTrue(getBaseChangedPacket(packets.subList(1, 3), simpleBase2P1).getAlliances().isEmpty());

        // U1 Move to next planet
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        connectionServiceTestHelperP1.clearReceivedPackets();
        connectionServiceTestHelperP2.clearReceivedPackets();
        loginUser("u1p2", "test");
        SimpleBase simpleBase1P2 = createBase(new Index(3000, 3000));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        packets = connectionServiceTestHelperP1.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(1, packets.size());
        Assert.assertEquals(BaseChangedPacket.Type.CHANGED, ((BaseChangedPacket) packets.get(0).getPacket()).getType());
        Assert.assertTrue(((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes().getAlliances().isEmpty());

        packets = connectionServiceTestHelperP2.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(2, packets.size());
        Assert.assertEquals(BaseChangedPacket.Type.CREATED, ((BaseChangedPacket) packets.get(0).getPacket()).getType());
        Assert.assertEquals("u1p2", ((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes().getName());
        Assert.assertEquals(simpleBase1P2, ((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes().getSimpleBase());
        Assert.assertEquals(BaseChangedPacket.Type.CHANGED, ((BaseChangedPacket) packets.get(1).getPacket()).getType());
        Assert.assertTrue(((BaseChangedPacket) packets.get(1).getPacket()).getBaseAttributes().getAlliances().isEmpty());

        // U2 promote give up base on planet 1
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u2p1", "test");
        connectionServiceTestHelperP1.clearReceivedPackets();
        connectionServiceTestHelperP2.clearReceivedPackets();
        userGuidanceService.promote(userService.getUserState(), TEST_LEVEL_5_REAL_ID);
        getMovableService().surrenderBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        packets = connectionServiceTestHelperP1.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(2, packets.size());
        Assert.assertEquals("Base 2", (((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes()).getName());
        Assert.assertEquals(BaseChangedPacket.Type.CHANGED, (((BaseChangedPacket) packets.get(0).getPacket()).getType()));
        Assert.assertTrue(getBaseChangedPacket(packets.subList(1, 2), simpleBase2P1).getAlliances().isEmpty());

        packets = connectionServiceTestHelperP2.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(1, packets.size());
        Assert.assertEquals(BaseChangedPacket.Type.CHANGED, ((BaseChangedPacket) packets.get(0).getPacket()).getType());
        Assert.assertTrue(getBaseChangedPacket(packets, simpleBase1P2).getAlliances().isEmpty());

        // U1 Move to next planet
        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        connectionServiceTestHelperP1.clearReceivedPackets();
        connectionServiceTestHelperP2.clearReceivedPackets();
        loginUser("u2p1", "test");
        SimpleBase simpleBase2P2 = createBase(new Index(4000, 4000));
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        packets = connectionServiceTestHelperP1.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(0, packets.size());

        packets = connectionServiceTestHelperP2.getPacketEntriesToAllBases(BaseChangedPacket.class);
        Assert.assertEquals(3, packets.size());
        Assert.assertEquals(BaseChangedPacket.Type.CREATED, ((BaseChangedPacket) packets.get(0).getPacket()).getType());
        Assert.assertEquals("u2p1", ((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes().getName());
        Assert.assertEquals(simpleBase2P2, ((BaseChangedPacket) packets.get(0).getPacket()).getBaseAttributes().getSimpleBase());
        Assert.assertEquals(1, getBaseChangedPacket(packets.subList(1, 3), simpleBase1P2).getAlliances().size());
        Assert.assertTrue(getBaseChangedPacket(packets.subList(1, 3), simpleBase1P2).isAlliance(simpleBase2P2));
        Assert.assertEquals(1, getBaseChangedPacket(packets.subList(1, 3), simpleBase2P2).getAlliances().size());
        Assert.assertTrue(getBaseChangedPacket(packets.subList(1, 3), simpleBase2P2).isAlliance(simpleBase1P2));
    }

    @Test
    @DirtiesContext
    public void breakAllianceAndAttack() throws Exception {
        configureSimplePlanetNoResources();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u1");
        SimpleBase simpleBase1 = getOrCreateBase();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        createAndLoginUser("u2");
        SimpleBase simpleBase2 = createBase(new Index(2000, 2000));
        sendMoveCommand(getFirstSynItemId(TEST_START_BUILDER_ITEM_ID), new Index(2000, 2000));
        waitForActionServiceDone();
        allianceService.proposeAlliance(simpleBase1);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u1", "test");
        allianceService.acceptAllianceOffer("u2");
        sendBuildCommand(getFirstSynItemId(TEST_START_BUILDER_ITEM_ID), new Index(300, 300), TEST_FACTORY_ITEM_ID);
        waitForActionServiceDone();
        sendFactoryCommand(getFirstSynItemId(TEST_FACTORY_ITEM_ID), TEST_ATTACK_ITEM_ID);
        waitForActionServiceDone();
        sendMoveCommand(getFirstSynItemId(TEST_ATTACK_ITEM_ID), new Index(1800, 2000));
        waitForActionServiceDone();
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        beginHttpSession();
        beginHttpRequestAndOpenSessionInViewFilter();
        loginUser("u2", "test");
        allianceService.breakAlliance("u1");
        assertWholeItemCount(TEST_PLANET_1_ID, 4);
        waitForActionServiceDone();
        // TODO failed on 05.12.2012, 14.03.2013
        assertWholeItemCount(TEST_PLANET_1_ID, 3);
        endHttpRequestAndOpenSessionInViewFilter();
        endHttpSession();

        Assert.assertFalse(planetSystemService.getServerPlanetServices(TEST_PLANET_1_ID).getBaseService().isAlive(simpleBase2));
    }

    private BaseAttributes getBaseChangedPacket(List<ServerConnectionServiceTestHelperNew.PacketEntry> packets, SimpleBase simpleBase) {
        for (ServerConnectionServiceTestHelperNew.PacketEntry packet : packets) {
            if (((BaseChangedPacket) packet.getPacket()).getBaseAttributes().getSimpleBase().equals(simpleBase)) {
                return ((BaseChangedPacket) packet.getPacket()).getBaseAttributes();
            }
        }
        Assert.fail("No BaseAttributes in packet found for base: " + simpleBase);
        return null; // unreachable
    }

    private void verifyAlliancesFromUser(String... allianceNames) throws InvalidLevelStateException {
        Assert.assertEquals(allianceNames.length, allianceService.getAllAlliances().size());
        for (String alliance : allianceService.getAllAlliances()) {
            if (Arrays.binarySearch(allianceNames, alliance) == -1) {
                Assert.fail("Alliance does not exits: " + alliance);
            }
        }
    }

    private void verifyAlliances(String... allianceNames) throws InvalidLevelStateException {
        RealGameInfo realGameInfo = getMovableService().getRealGameInfo(START_UID_1);
        SimpleBase myBase = realGameInfo.getBase();
        for (BaseAttributes baseAttributes : realGameInfo.getAllBase()) {
            if (baseAttributes.getSimpleBase().equals(myBase)) {
                verifyAlliances(baseAttributes, allianceNames);
                return;
            }
        }
        Assert.fail("Own base not found");
    }

    private void verifyAlliances(BaseAttributes myBaseAttributes, String... allianceNames) {
        Assert.assertEquals(allianceNames.length, myBaseAttributes.getAlliances().size());
        for (SimpleBase allianceBase : myBaseAttributes.getAlliances()) {
            String allianceBaseName = planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices().getBaseService().getBaseName(allianceBase);
            if (Arrays.binarySearch(allianceNames, allianceBaseName) == -1) {
                Assert.fail("Alliance does not exits: " + allianceBaseName);
            }
        }
    }

    private void verifyAllianceOffers(String... allianceOfferNames) throws InvalidLevelStateException {
        RealGameInfo realGameInfo = getMovableService().getRealGameInfo(START_UID_1);
        Assert.assertEquals(allianceOfferNames.length, realGameInfo.getAllianceOffers().size());
        for (AllianceOfferPacket allianceOfferPacket : realGameInfo.getAllianceOffers()) {
            if (Arrays.binarySearch(allianceOfferNames, allianceOfferPacket.getActorUserName()) == -1) {
                Assert.fail("Alliance does not exits: " + allianceOfferPacket.getActorUserName());
            }
        }
    }

    private void assertAlliancesInPacketToAll(List<ServerConnectionServiceTestHelperNew.PacketEntry> packets, SimpleBase simpleBase, String... allianceNames) {
        for (ServerConnectionServiceTestHelperNew.PacketEntry packet : packets) {
            BaseChangedPacket baseChangedPacket = (BaseChangedPacket) packet.getPacket();
            if (!baseChangedPacket.getBaseAttributes().getSimpleBase().equals(simpleBase)) {
                continue;
            }
            if (baseChangedPacket.getType() != BaseChangedPacket.Type.CHANGED) {
                continue;
            }
            Assert.assertEquals(allianceNames.length, baseChangedPacket.getBaseAttributes().getAlliances().size());
            for (SimpleBase allianceBase : baseChangedPacket.getBaseAttributes().getAlliances()) {
                String allianceBaseName = planetSystemService.getPlanet(TEST_PLANET_1_ID).getPlanetServices().getBaseService().getBaseName(allianceBase);
                if (Arrays.binarySearch(allianceNames, allianceBaseName) == -1) {
                    Assert.fail("Alliance does not exits: " + allianceBaseName);
                }
            }
            return;
        }
        Assert.fail("No BaseChangedPacket for base found: " + simpleBase);
    }

    private void assertBaseDeletedPacket(ServerConnectionServiceTestHelperNew connectionServiceTestHelper, SimpleBase deletedBase) {
        List<ServerConnectionServiceTestHelperNew.PacketEntry> packets = connectionServiceTestHelper.getPacketEntriesToAllBases(BaseChangedPacket.class);
        for (ServerConnectionServiceTestHelperNew.PacketEntry packet : packets) {
            BaseChangedPacket baseChangedPacket = (BaseChangedPacket) packet.getPacket();
            if (baseChangedPacket.getType() == BaseChangedPacket.Type.REMOVED) {
                Assert.assertEquals(deletedBase, baseChangedPacket.getBaseAttributes().getSimpleBase());
                return;
            }
        }
        Assert.fail("No delete BaseChangedPacket for base found");
    }

    private void assertNoAlliancesInPackets(ServerConnectionServiceTestHelperNew connectionServiceTestHelper, UserState userState) {
        List<ServerConnectionServiceTestHelperNew.PacketEntry> packets = connectionServiceTestHelper.getPacketEntries(userState, BaseChangedPacket.class);
        Assert.assertEquals(0, packets.size());
    }

    private void assertNoMessages(ServerConnectionServiceTestHelperNew connectionServiceTestHelper, UserState userState) {
        List<ServerConnectionServiceTestHelperNew.MessageEntry> packets = connectionServiceTestHelper.getMessageEntries(userState);
        Assert.assertEquals(0, packets.size());
    }

    private void assertMessage(ServerConnectionServiceTestHelperNew connectionServiceTestHelper, UserState userState, String messages, String arg, boolean showRegisterButton) {
        List<ServerConnectionServiceTestHelperNew.MessageEntry> packets = connectionServiceTestHelper.getMessageEntries(userState);
        Assert.assertEquals(1, packets.size());
        ServerConnectionServiceTestHelperNew.MessageEntry messageEntry = packets.get(0);
        Assert.assertEquals(userState, messageEntry.getUserState());
        Assert.assertEquals(messages, messageEntry.getKey());
        if (arg != null) {
            Assert.assertEquals(1, messageEntry.getArgs().length);
            Assert.assertEquals(arg, messageEntry.getArgs()[0]);
        } else {
            Assert.assertNull(messageEntry.getArgs());
        }
        Assert.assertEquals(showRegisterButton, messageEntry.isShowRegisterDialog());
    }
}
