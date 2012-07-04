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

package com.btxtech.game.services.utg.impl;

import com.btxtech.game.jsre.client.common.LevelScope;
import com.btxtech.game.jsre.client.common.info.InvalidLevelState;
import com.btxtech.game.jsre.client.common.info.RealGameInfo;
import com.btxtech.game.jsre.client.dialogs.quest.QuestInfo;
import com.btxtech.game.jsre.client.dialogs.quest.QuestOverview;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.Territory;
import com.btxtech.game.jsre.common.packets.LevelPacket;
import com.btxtech.game.jsre.common.packets.LevelTaskPacket;
import com.btxtech.game.jsre.common.packets.Message;
import com.btxtech.game.jsre.common.packets.XpPacket;
import com.btxtech.game.jsre.common.tutorial.GameFlow;
import com.btxtech.game.jsre.common.utg.ConditionServiceListener;
import com.btxtech.game.jsre.common.utg.config.ConditionConfig;
import com.btxtech.game.jsre.common.utg.config.ConditionTrigger;
import com.btxtech.game.jsre.common.utg.config.CountComparisonConfig;
import com.btxtech.game.services.base.Base;
import com.btxtech.game.services.base.BaseService;
import com.btxtech.game.services.common.CrudRootServiceHelper;
import com.btxtech.game.services.common.HibernateUtil;
import com.btxtech.game.services.connection.ConnectionService;
import com.btxtech.game.services.history.HistoryService;
import com.btxtech.game.services.item.ItemService;
import com.btxtech.game.services.mgmt.impl.DbUserState;
import com.btxtech.game.services.territory.TerritoryService;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.services.user.UserState;
import com.btxtech.game.services.utg.DbLevel;
import com.btxtech.game.services.utg.DbLevelTask;
import com.btxtech.game.services.utg.DbQuestHub;
import com.btxtech.game.services.utg.LevelActivationException;
import com.btxtech.game.services.utg.UserGuidanceService;
import com.btxtech.game.services.utg.XpService;
import com.btxtech.game.services.utg.condition.ServerConditionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: beat Date: 29.01.2010 Time: 22:04:02
 */
@Component("userGuidanceService")
public class UserGuidanceServiceImpl implements UserGuidanceService, ConditionServiceListener<UserState, Integer> {
    public static final String NO_MISSION_TARGET = "<center>There are no new mission targets.<br /><h1>Please check back later</h1></center>";
    @Autowired
    private BaseService baseService;
    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private UserService userService;
    @Autowired
    private ServerConditionService serverConditionService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private CrudRootServiceHelper<DbQuestHub> crudQuestHub;
    @Autowired
    private TerritoryService territoryService;
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private ItemService itemService;
    @Autowired
    private XpService xpService;
    private Log log = LogFactory.getLog(UserGuidanceServiceImpl.class);
    private Map<Integer, LevelScope> levelScopes = new HashMap<>();
    private final Map<UserState, Collection<Integer>> levelTaskDone = new HashMap<>();
    private final Map<UserState, Integer> activeQuestIds = new HashMap<>();

    @PostConstruct
    public void init() {
        crudQuestHub.init(DbQuestHub.class, "orderIndex", true, true, null);
        serverConditionService.setConditionServiceListener(this);
        try {
            HibernateUtil.openSession4InternalCall(sessionFactory);
            activateLevels();
        } catch (Throwable t) {
            log.error("", t);
        } finally {
            HibernateUtil.closeSession4InternalCall(sessionFactory);
        }
    }

    @Override
    public void createBaseInQuestHub(UserState userState) throws InvalidLevelState {
        DbLevel dbLevel = getDbLevel(userState);
        DbQuestHub dbQuestHub = dbLevel.getParent();
        if (!dbQuestHub.isRealBaseRequired()) {
            throw createInvalidLevelState();
        }
        Territory territory = territoryService.getTerritory(dbQuestHub.getStartTerritory());

        try {
            baseService.createNewBase(userState, dbQuestHub.getStartItemType(), territory, dbQuestHub.getStartItemFreeRange());
        } catch (Exception e) {
            log.error("Can not create base for user: " + userState, e);
        }

        Base base = baseService.getBase(userState);
        base.setAccountBalance(dbQuestHub.getStartMoney());
        baseService.sendAccountBaseUpdate(base.getSimpleBase());

        log.debug("User: " + userState + " will be resurrected: " + dbQuestHub);
    }

    @Override
    public void sendResurrectionMessage(SimpleBase simpleBase) {
        Message message = new Message();
        message.setMessage("You lost your base. A new base was created.");
        connectionService.sendPacket(simpleBase, message);
    }

    @Override
    public void promote(UserState userState, int newDbLevelId) {
        promote(userState, getDbLevel(newDbLevelId));
    }

    private void promote(UserState userState, DbLevel dbNextLevel) {
        // Cleanup
        cleanupConditions(userState);
        // Prepare
        DbLevel dbOldLevel = getDbLevel(userState.getDbLevelId());
        userState.setDbLevelId(dbNextLevel.getId());
        // Tracking
        historyService.addLevelPromotionEntry(userState, dbNextLevel);
        log.debug("User: " + userState + " has been promoted: " + dbOldLevel + " to " + dbNextLevel);

        if (baseService.getBase(userState) != null) {
            Base base = baseService.getBase(userState);
            // Send XP
            XpPacket xpPacket = new XpPacket();
            xpPacket.setXp(0);
            xpPacket.setXp2LevelUp(dbNextLevel.getXp());
            connectionService.sendPacket(base.getSimpleBase(), xpPacket);
            // Level
            LevelPacket levelPacket = new LevelPacket();
            levelPacket.setLevel(getLevelScope(dbNextLevel.getId()));
            connectionService.sendPacket(base.getSimpleBase(), levelPacket);
        }

        // Create base if needed
        if (baseService.getBase(userState) == null && dbNextLevel.getParent().isRealBaseRequired()) {
            try {
                createBaseInQuestHub(userState);
            } catch (InvalidLevelState invalidLevelState) {
                log.error("Error during base creation: " + userState, invalidLevelState);
            }
        }
        // Prepare next level
        activateConditions4Level(userState, dbNextLevel);
        // Post processing
        userState.setXp(0);
        activateNextUnDoneLevelTask(userState, null);
    }

    private void activateNextUnDoneLevelTask(UserState userState, DbLevelTask oldLevelTaskId) {
        DbLevelTask dbLevelTask = getNextUnDoneLevelTask(userState, oldLevelTaskId);
        if (dbLevelTask == null) {
            return;
        }
        activateQuest(userState, dbLevelTask);
    }

    private DbLevelTask getNextUnDoneLevelTask(UserState userState, DbLevelTask oldLevelTask) {
        DbLevel dbLevel = getDbLevel(userState);
        Collection<Integer> levelTaskDone = this.levelTaskDone.get(userState);
        List<DbLevelTask> dbLevelTasks = dbLevel.getLevelTaskCrud().readDbChildren();
        if (oldLevelTask != null) {
            int index = dbLevelTasks.indexOf(oldLevelTask);
            if (index < 0) {
                throw new IllegalArgumentException("Unknown level task: " + oldLevelTask);
            }
            if (index + 1 < dbLevelTasks.size()) {
                for (DbLevelTask dbLevelTask : dbLevelTasks.subList(index + 1, dbLevelTasks.size())) {
                    if (levelTaskDone != null && levelTaskDone.contains(dbLevelTask.getId())) {
                        continue;
                    }
                    return dbLevelTask;
                }
            }
            return getNextUnDoneLevelTask(userState, null);
        } else {
            for (DbLevelTask dbLevelTask : dbLevelTasks) {
                if (levelTaskDone != null && levelTaskDone.contains(dbLevelTask.getId())) {
                    continue;
                }
                return dbLevelTask;
            }
        }
        return null;
    }

    @Override
    public void conditionPassed(UserState userState, Integer taskId) {
        if (HibernateUtil.hasOpenSession(sessionFactory)) {
            conditionPassedInSession(userState, taskId);
        } else {
            HibernateUtil.openSession4InternalCall(sessionFactory);
            try {
                conditionPassedInSession(userState, taskId);
            } finally {
                HibernateUtil.closeSession4InternalCall(sessionFactory);
            }
        }
    }

    private void conditionPassedInSession(UserState userState, Integer taskId) {
        if (taskId != null) {
            handleLevelTaskCompletion(userState, taskId);
        } else {
            DbLevel dbOldLevel = getDbLevel(userState);
            DbLevel dbNextLevel = getNextDbLevel(dbOldLevel);
            promote(userState, dbNextLevel);
        }
    }

    @Override
    public GameFlow onTutorialFinished(int levelTaskId) {
        UserState userState = userService.getUserState();
        serverConditionService.onTutorialFinished(userState, levelTaskId);
        DbLevel newLevel = getDbLevel();

        if (newLevel.getParent().isRealBaseRequired()) {
            return new GameFlow(GameFlow.Type.START_REAL_GAME, null);
        } else {
            DbLevelTask dbLevelTask = newLevel.getFirstTutorialLevelTask();
            return new GameFlow(GameFlow.Type.START_NEXT_LEVEL_TASK_TUTORIAL, dbLevelTask.getId());
        }
    }

    private void activateConditions4Level(UserState userState, DbLevel dbLevel) {
        ConditionConfig levelCondition = new ConditionConfig(ConditionTrigger.XP_INCREASED, new CountComparisonConfig(null, dbLevel.getXp(), null), null);
        serverConditionService.activateCondition(levelCondition, userState, null);
    }

    private void cleanupConditions(UserState userState) {
        serverConditionService.deactivateAllActorConditions(userState);
        synchronized (levelTaskDone) {
            levelTaskDone.remove(userState);
        }
        synchronized (activeQuestIds) {
            activeQuestIds.remove(userState);
        }
    }

    private void handleLevelTaskCompletion(UserState userState, int levelTaskId) {
        DbLevelTask dbLevelTask = (DbLevelTask) sessionFactory.getCurrentSession().get(DbLevelTask.class, levelTaskId);
        synchronized (levelTaskDone) {
            addLevelTaskDone(userState, dbLevelTask);
        }
        removeActiveQuest(userState, dbLevelTask);

        DbLevel oldLevel = getDbLevel(userState);
        // Communication
        log.debug("Level Task completed. userState: " + userState + " " + dbLevelTask);
        historyService.addLevelTaskCompletedEntry(userState, dbLevelTask);
        Base base = baseService.getBase(userState);
        if (base != null) {
            LevelTaskPacket levelTaskPacket = new LevelTaskPacket();
            levelTaskPacket.setCompleted();
            connectionService.sendPacket(base.getSimpleBase(), levelTaskPacket);
        }
        // Rewards
        if (dbLevelTask.getXp() > 0) {
            xpService.onReward(userState, dbLevelTask.getXp());
        }
        if (base != null && dbLevelTask.getMoney() > 0) {
            baseService.depositResource(dbLevelTask.getMoney(), base.getSimpleBase());
            baseService.sendAccountBaseUpdate(base.getSimpleBase());
        }

        // Activate next quest / mission
        if (oldLevel.equals(getDbLevel(userState))) {
            activateNextUnDoneLevelTask(userState, dbLevelTask);
        }
    }

    private void addLevelTaskDone(UserState userState, DbLevelTask dbLevelTask) {
        Collection<Integer> tasks = levelTaskDone.get(userState);
        if (tasks == null) {
            tasks = new ArrayList<>();
            levelTaskDone.put(userState, tasks);
        }
        tasks.add(dbLevelTask.getId());
    }

    private void setActiveQuest(UserState userState, DbLevelTask dbLevelTask) {
        synchronized (activeQuestIds) {
            activeQuestIds.put(userState, dbLevelTask.getId());
        }
    }

    private void removeActiveQuest(UserState userState, DbLevelTask dbLevelTask) {
        synchronized (activeQuestIds) {
            Integer taskId = activeQuestIds.remove(userState);
            if (taskId == null) {
                throw new IllegalArgumentException("DbLevelTask was not active before: " + dbLevelTask + " userState: " + userState);
            }
            if ((int) taskId != dbLevelTask.getId()) {
                throw new IllegalArgumentException("DbLevelTask was not active before: " + dbLevelTask + " userState: " + userState + ". Active level task id: " + taskId);
            }
        }
    }

    @Override
    public void onRemoveUserState(UserState userState) {
        cleanupConditions(userState);
    }

    @Override
    public void setLevelForNewUser(UserState userState) {
        DbLevel dbLevel = new ArrayList<>(crudQuestHub.readDbChildren()).get(0).getLevelCrud().readDbChildren().get(0);
        userState.setDbLevelId(dbLevel.getId());
        activateNextUnDoneLevelTask(userState, null);
        activateConditions4Level(userState, dbLevel);
    }

    private DbLevel getNextDbLevel(DbLevel dbLevel) {
        DbQuestHub dbQuestHub = dbLevel.getParent();
        List<DbLevel> dbLevels = dbQuestHub.getLevelCrud().readDbChildren();
        int index = dbLevels.indexOf(dbLevel);
        if (index < 0) {
            throw new IllegalArgumentException("DbLevel can not be found in own DbQuestHub: " + dbLevel);
        }
        index++;
        if (dbLevels.size() > index) {
            return dbLevels.get(index);
        } else {
            List<DbQuestHub> dbQuestHubs = new ArrayList<>(crudQuestHub.readDbChildren());
            index = dbQuestHubs.indexOf(dbQuestHub);
            if (index < 0) {
                throw new IllegalArgumentException("DbLevel can not be found in own DbQuestHub: " + dbLevel);
            }
            index++;
            if (dbQuestHubs.size() > index) {
                return dbQuestHubs.get(index).getLevelCrud().readDbChildren().get(0);
            } else {
                throw new IllegalArgumentException("Is last DbQuestHub" + dbQuestHub);
            }
        }
    }

    @Override
    public DbLevel getDbLevelCms() {
        // Prevent creating a UserState -> search engine
        if (userService.hasUserState()) {
            return getDbLevel(userService.getUserState());
        } else {
            return null;
        }
    }

    @Override
    public DbLevel getDbLevel() {
        DbLevel dbLevel = (DbLevel) sessionFactory.getCurrentSession().get(DbLevel.class, userService.getUserState().getDbLevelId());
        if (dbLevel == null) {
            log.error("----DbLevel is null----");
            log.error("session: " + userService.getUserState().getSessionId());
        }
        return dbLevel;
    }

    @Override
    public DbLevel getDbLevel(UserState userState) {
        return getDbLevel(userState.getDbLevelId());
    }

    @Override
    public DbLevel getDbLevel(int levelId) {
        return (DbLevel) sessionFactory.getCurrentSession().get(DbLevel.class, levelId);
    }

    public LevelScope getLevelScope(int dbLevelId) {
        LevelScope levelScope = levelScopes.get(dbLevelId);
        if (levelScope == null) {
            throw new IllegalArgumentException("No LevelScope for dbLevelId: " + dbLevelId + ". Did you forget to activate the levels?");
        }
        return levelScope;
    }

    @Override
    public LevelScope getLevelScope() {
        return getLevelScope(userService.getUserState().getDbLevelId());
    }

    @Override
    public LevelScope getLevelScope(SimpleBase simpleBase) {
        UserState userState = baseService.getUserState(simpleBase);
        return getLevelScope(userState.getDbLevelId());
    }

    @Override
    public boolean isStartRealGame() {
        return getDbLevel().getParent().isRealBaseRequired();
    }

    @Override
    public int getDefaultLevelTaskId() {
        DbLevel dbLevel = getDbLevel();
        if (dbLevel.getParent().isRealBaseRequired()) {
            throw new IllegalArgumentException("If real game is required, no default tutorial LevelTask is is available");
        }
        return dbLevel.getFirstTutorialLevelTask().getId();
    }

    @Override
    public void activateLevels() throws LevelActivationException {
        levelScopes.clear();
        for (DbQuestHub dbQuestHub : crudQuestHub.readDbChildren()) {
            for (DbLevel dbLevel : dbQuestHub.getLevelCrud().readDbChildren()) {
                levelScopes.put(dbLevel.getId(), dbLevel.createLevelScope());
            }
        }
    }

    @Override
    public CrudRootServiceHelper<DbQuestHub> getCrudQuestHub() {
        return crudQuestHub;
    }

    @Override
    public QuestOverview getQuestOverview() {
        List<QuestInfo> levelQuests = new ArrayList<>();
        UserState userState = userService.getUserState();
        Collection<Integer> userLevelTaskDone = levelTaskDone.get(userState);
        int questsDone = 0;
        int totalQuests = 0;
        int missionsDone = 0;
        int totalMissions = 0;
        for (DbLevelTask dbLevelTask : getDbLevel().getLevelTaskCrud().readDbChildren()) {
            if (dbLevelTask.isDbTutorialConfig()) {
                totalMissions++;
            } else {
                totalQuests++;
            }
            QuestInfo questInfo = dbLevelTask.createQuestInfo();
            if (userLevelTaskDone == null || !userLevelTaskDone.contains(dbLevelTask.getId())) {
                levelQuests.add(questInfo);
            } else {
                if (dbLevelTask.isDbTutorialConfig()) {
                    missionsDone++;
                } else {
                    questsDone++;
                }
            }
        }
        QuestOverview questOverview = new QuestOverview();
        questOverview.setQuestInfos(levelQuests);
        questOverview.setQuestsDone(questsDone);
        questOverview.setTotalQuests(totalQuests);
        questOverview.setMissionsDone(missionsDone);
        questOverview.setTotalMissions(totalMissions);
        return questOverview;
    }

    @Override
    public void createAndAddBackup(DbUserState dbUserState, UserState userState) {
        DbLevel dbLevel = getDbLevel(userState);
        Collection<DbLevelTask> tasksDone = new ArrayList<>();
        synchronized (levelTaskDone) {
            Collection<Integer> taskIds = levelTaskDone.get(userState);
            if (taskIds != null) {
                for (Integer taskId : taskIds) {
                    tasksDone.add(dbLevel.getLevelTaskCrud().readDbChild(taskId));
                }
            }
        }
        dbUserState.setLevelTasksDone(tasksDone);
        Integer taskId;
        synchronized (activeQuestIds) {
            taskId = activeQuestIds.get(userState);
        }
        if (taskId != null) {
            dbUserState.setActiveQuest(dbLevel.getLevelTaskCrud().readDbChild(taskId));
        }
        serverConditionService.createBackup(dbUserState, userState);
    }

    @Override
    public void restoreBackup(Map<DbUserState, UserState> userStates) {
        serverConditionService.deactivateAll();
        synchronized (activeQuestIds) {
            activeQuestIds.clear();
        }
        synchronized (levelTaskDone) {
            levelTaskDone.clear();
            activeQuestIds.clear();
            for (Map.Entry<DbUserState, UserState> entry : userStates.entrySet()) {
                try {
                    if (entry.getKey().getLevelTasksDone() != null) {
                        for (DbLevelTask taskDone : entry.getKey().getLevelTasksDone()) {
                            addLevelTaskDone(entry.getValue(), taskDone);
                        }
                    }
                    activateConditionsRestore(entry.getValue(), getDbLevel(entry.getValue()), entry.getKey().getActiveQuest());
                    serverConditionService.restoreBackup(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    log.error("Can not restore user: " + entry.getValue().getUser(), e);
                }
            }
        }
    }

    private void activateConditionsRestore(UserState userState, DbLevel dbLevel, DbLevelTask activeQuest) {
        ConditionConfig levelCondition = new ConditionConfig(ConditionTrigger.XP_INCREASED, new CountComparisonConfig(null, dbLevel.getXp(), null), null);
        serverConditionService.activateCondition(levelCondition, userState, null);

        if (activeQuest != null) {
            serverConditionService.activateCondition(activeQuest.createConditionConfig(itemService), userState, activeQuest.getId());
            setActiveQuest(userState, activeQuest);
        }
    }


    @Override
    public InvalidLevelState createInvalidLevelState() {
        if (isStartRealGame()) {
            return new InvalidLevelState(null);
        } else {
            return new InvalidLevelState(getDefaultLevelTaskId());
        }
    }

    @Override
    public void activateQuest(int dbLevelTaskId) {
        UserState userState = userService.getUserState();
        DbLevelTask dbLevelTask = getDbLevel().getLevelTaskCrud().readDbChild(dbLevelTaskId);
        if (levelTaskDone.containsKey(userState) && levelTaskDone.get(userState).contains(dbLevelTaskId)) {
            throw new IllegalArgumentException("DbLevelTask already done: " + dbLevelTask);
        }

        Integer activeQuestId = activeQuestIds.get(userService.getUserState());
        // Deactivate old quest level task
        if (activeQuestId != null) {
            if (activeQuestId == dbLevelTaskId) {
                // Do not activate same quest again
                return;
            }
            deactivateLevelTask(getDbLevel().getLevelTaskCrud().readDbChild(activeQuestId));
        }
        activateQuest(userState, dbLevelTask);
    }

    private void activateQuest(UserState userState, DbLevelTask dbLevelTask) {
        if (levelTaskDone.containsKey(userState) && levelTaskDone.get(userState).contains(dbLevelTask.getId())) {
            throw new IllegalArgumentException("DbLevelTask already done: " + dbLevelTask);
        }
        if (activeQuestIds.containsKey(userState)) {
            throw new IllegalArgumentException("DbLevelTask already activated: " + activeQuestIds.containsKey(userState) + ". Can not activate new level task: " + dbLevelTask);
        }
        serverConditionService.activateCondition(dbLevelTask.createConditionConfig(itemService), userState, dbLevelTask.getId());
        setActiveQuest(userState, dbLevelTask);
        historyService.addLevelTaskActivated(userState, dbLevelTask);
        if (baseService.getBase(userState) != null) {
            Base base = baseService.getBase(userState);
            LevelTaskPacket levelTaskPacket = new LevelTaskPacket();
            levelTaskPacket.setQuestInfo(dbLevelTask.createQuestInfo());
            if (!dbLevelTask.isDbTutorialConfig()) {
                levelTaskPacket.setActiveQuestProgress(serverConditionService.getProgressHtml(userState, dbLevelTask.getId()));
            }
            connectionService.sendPacket(base.getSimpleBase(), levelTaskPacket);
        }
    }

    private void deactivateLevelTask(DbLevelTask dbLevelTask) {
        UserState userState = userService.getUserState();
        if (levelTaskDone.containsKey(userState) && levelTaskDone.get(userState).contains(dbLevelTask.getId())) {
            throw new IllegalArgumentException("DbLevelTask already done: " + dbLevelTask);
        }
        if (!activeQuestIds.containsKey(userState) || activeQuestIds.get(userState) != (int) dbLevelTask.getId()) {
            throw new IllegalArgumentException("DbLevelTask was not active: " + dbLevelTask + ". Active level task id: " + activeQuestIds.containsKey(userState));
        }
        serverConditionService.deactivateActorCondition(userState, dbLevelTask.getId());
        removeActiveQuest(userState, dbLevelTask);
        historyService.addLevelTaskDeactivated(userState, dbLevelTask);
    }

    @Override
    public void fillRealGameInfo(RealGameInfo realGameInfo) {
        DbLevel dbLevel = getDbLevel();
        UserState userState = userService.getUserState();
        // Level task
        Integer activeLevelTaskId = activeQuestIds.get(userState);
        if (activeLevelTaskId != null) {
            DbLevelTask activeTask = dbLevel.getLevelTaskCrud().readDbChild(activeLevelTaskId);
            LevelTaskPacket levelTaskPacket = new LevelTaskPacket();
            levelTaskPacket.setQuestInfo(activeTask.createQuestInfo());
            if (!activeTask.isDbTutorialConfig()) {
                levelTaskPacket.setActiveQuestProgress(serverConditionService.getProgressHtml(userState, activeTask.getId()));
            }
            realGameInfo.setLevelTaskPacket(levelTaskPacket);
        }
        // Xp
        XpPacket xpPacket = new XpPacket();
        xpPacket.setXp(userState.getXp());
        xpPacket.setXp2LevelUp(dbLevel.getXp());
        realGameInfo.setXpPacket(xpPacket);
        // Level
        realGameInfo.setLevel(getLevelScope());
    }

    @Override
    public int getXp2LevelUp(UserState userState) {
        return getLevelScope(userState.getDbLevelId()).getXp2LevelUp();
    }
}
