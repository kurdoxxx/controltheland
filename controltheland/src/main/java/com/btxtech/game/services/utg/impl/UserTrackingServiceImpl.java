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

import com.btxtech.game.jsre.client.common.UserMessage;
import com.btxtech.game.jsre.common.StartupTaskInfo;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.AttackCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BaseCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BuilderCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.FactoryCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.MoneyCollectCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.MoveCommand;
import com.btxtech.game.jsre.common.gameengine.syncObjects.syncInfos.SyncItemInfo;
import com.btxtech.game.jsre.common.tutorial.TutorialConfig;
import com.btxtech.game.jsre.common.utg.tracking.BrowserWindowTracking;
import com.btxtech.game.jsre.common.utg.tracking.DialogTracking;
import com.btxtech.game.jsre.common.utg.tracking.EventTrackingItem;
import com.btxtech.game.jsre.common.utg.tracking.EventTrackingStart;
import com.btxtech.game.jsre.common.utg.tracking.SelectionTrackingItem;
import com.btxtech.game.jsre.common.utg.tracking.TerrainScrollTracking;
import com.btxtech.game.services.base.Base;
import com.btxtech.game.services.base.BaseService;
import com.btxtech.game.services.common.HibernateUtil;
import com.btxtech.game.services.connection.ConnectionService;
import com.btxtech.game.services.connection.NoConnectionException;
import com.btxtech.game.services.connection.Session;
import com.btxtech.game.services.history.HistoryService;
import com.btxtech.game.services.user.User;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.services.utg.DbLevelTask;
import com.btxtech.game.services.utg.DbUserMessage;
import com.btxtech.game.services.utg.LifecycleTrackingInfo;
import com.btxtech.game.services.utg.RealGameTrackingInfo;
import com.btxtech.game.services.utg.SessionDetailDto;
import com.btxtech.game.services.utg.SessionOverviewDto;
import com.btxtech.game.services.utg.TutorialTrackingInfo;
import com.btxtech.game.services.utg.UserGuidanceService;
import com.btxtech.game.services.utg.UserTrackingFilter;
import com.btxtech.game.services.utg.UserTrackingService;
import com.btxtech.game.services.utg.condition.ServerConditionService;
import com.btxtech.game.services.utg.tracker.DbBrowserWindowTracking;
import com.btxtech.game.services.utg.tracker.DbDialogTracking;
import com.btxtech.game.services.utg.tracker.DbEventTrackingItem;
import com.btxtech.game.services.utg.tracker.DbEventTrackingStart;
import com.btxtech.game.services.utg.tracker.DbPageAccess;
import com.btxtech.game.services.utg.tracker.DbScrollTrackingItem;
import com.btxtech.game.services.utg.tracker.DbSelectionTrackingItem;
import com.btxtech.game.services.utg.tracker.DbSessionDetail;
import com.btxtech.game.services.utg.tracker.DbStartupTask;
import com.btxtech.game.services.utg.tracker.DbSyncItemInfo;
import com.btxtech.game.services.utg.tracker.DbTutorialProgress;
import com.btxtech.game.services.utg.tracker.DbUserCommand;
import com.btxtech.game.services.utg.tracker.DbUserHistory;
import com.btxtech.game.services.utg.tracker.DbWindowClosed;
import com.btxtech.game.wicket.pages.Game;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * User: beat
 * Date: 12.01.2010
 * Time: 22:41:05
 */
@Component("userTrackingService")
public class UserTrackingServiceImpl implements UserTrackingService {
    @Autowired
    private Session session;
    @Autowired
    private BaseService baseService;
    @Autowired
    private UserGuidanceService userGuidanceService;
    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private ServerConditionService serverConditionService;
    @Autowired
    private UserService userService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private SessionFactory sessionFactory;
    private Log log = LogFactory.getLog(UserTrackingServiceImpl.class);

    @Override
    @Transactional
    public void pageAccess(Class theClass) {
        try {
            DbPageAccess dbPageAccess = new DbPageAccess(session.getSessionId(), theClass.getName(), null);
            sessionFactory.getCurrentSession().save(dbPageAccess);
        } catch (NoConnectionException e) {
            log.error("", e);
        }
    }

    @Override
    @Transactional
    public void pageAccess(String pageName, String additional) {
        try {
            DbPageAccess dbPageAccess = new DbPageAccess(session.getSessionId(), pageName, additional);
            sessionFactory.getCurrentSession().saveOrUpdate(dbPageAccess);
        } catch (NoConnectionException e) {
            log.error("", e);
        }
    }

    @Override
    public boolean hasCookieToAdd() {
        return session.getCookieIdToBeSet() != null;
    }

    @Override
    public String getAndClearCookieToAdd() {
        String cookieId = session.getCookieIdToBeSet();
        if (cookieId == null) {
            throw new IllegalStateException("cookieId == null");
        }
        session.clearCookieIdToBeSet();
        return cookieId;
    }

    @Override
    @Transactional
    public void saveBrowserDetails(DbSessionDetail dbSessionDetail) {
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(dbSessionDetail);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SessionOverviewDto> getSessionOverviewDtos(UserTrackingFilter filter) {
        ArrayList<SessionOverviewDto> sessionOverviewDtos = new ArrayList<SessionOverviewDto>();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.add(GregorianCalendar.DAY_OF_YEAR, -filter.getDays());


        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbSessionDetail.class);
        if (!filter.getJsEnabled().equals(UserTrackingFilter.BOTH)) {
            criteria.add(Restrictions.eq("javaScriptDetected", filter.getJsEnabled().equals(UserTrackingFilter.ENABLED)));
        }
        criteria.add(Restrictions.gt("timeStamp", gregorianCalendar.getTime()));
        if (filter.getSessionId() != null && !filter.getSessionId().trim().isEmpty()) {
            criteria.add(Restrictions.eq("sessionId", filter.getSessionId()));
        }
        if (filter.getCookieId() != null && !filter.getCookieId().trim().isEmpty()) {
            criteria.add(Restrictions.eq("cookieId", filter.getCookieId()));
        }

        criteria.addOrder(Order.desc("timeStamp"));
        List<DbSessionDetail> browserDetails = criteria.list();

        for (DbSessionDetail browserDetail : browserDetails) {
            int startAttempts = getStartAttempts(browserDetail.getSessionId());
            boolean failure = hasFailureStarts(browserDetail.getSessionId());
            int enterGameHits = getGameAttempts(browserDetail.getSessionId());
            int commands = getUserCommandCount(browserDetail.getSessionId(), null, null, null);
            int levelPromotions = historyService.getLevelPromotionCount(browserDetail.getSessionId());
            sessionOverviewDtos.add(new SessionOverviewDto(browserDetail.getTimeStamp(),
                    browserDetail.getSessionId(),
                    getPageHits(browserDetail.getSessionId()),
                    enterGameHits,
                    startAttempts,
                    failure,
                    commands,
                    levelPromotions,
                    browserDetail.getReferer()));
        }
        return sessionOverviewDtos;
    }

    private int getStartAttempts(final String sessionId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbStartupTask.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        criteria.setProjection(Projections.countDistinct("startUuid"));
        return ((Number) criteria.list().get(0)).intValue();
    }

    private boolean hasFailureStarts(final String sessionId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbStartupTask.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        criteria.add(Restrictions.isNotNull("failureText"));
        criteria.setProjection(Projections.rowCount());
        return ((Number) criteria.list().get(0)).intValue() > 0;
    }

    private int getPageHits(final String sessionId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbPageAccess.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        criteria.setProjection(Projections.rowCount());
        return ((Number) criteria.list().get(0)).intValue();
    }

    private int getGameAttempts(final String sessionId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbPageAccess.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        criteria.add(Restrictions.eq("page", Game.class.getName()));
        criteria.setProjection(Projections.rowCount());
        return ((Number) criteria.list().get(0)).intValue();
    }

    private int getUserCommandCount(final String sessionId, final Class<? extends BaseCommand> command, final Date from, final Date to) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbUserCommand.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        if (command != null) {
            criteria.add(Restrictions.eq("interactionClass", command.getName()));
        }
        if (from != null) {
            criteria.add(Restrictions.ge("clientTimeStamp", from));
        }
        if (to != null) {
            criteria.add(Restrictions.lt("clientTimeStamp", to));
        }
        criteria.setProjection(Projections.rowCount());
        return ((Number) criteria.list().get(0)).intValue();
    }

    @SuppressWarnings("unchecked")
    private List<DbUserCommand> getUserCommand(LifecycleTrackingInfo lifecycleTrackingInfo) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbUserCommand.class);
        criteria.add(Restrictions.eq("sessionId", lifecycleTrackingInfo.getSessionId()));
        criteria.add(Restrictions.ge("timeStampMs", lifecycleTrackingInfo.getStartServer()));
        if (lifecycleTrackingInfo.getNextReaGameLifecycleTrackingInfo() != null) {
            criteria.add(Restrictions.lt("timeStampMs", lifecycleTrackingInfo.getNextReaGameLifecycleTrackingInfo().getStartServer()));
        }
        return (List<DbUserCommand>) criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SessionDetailDto getSessionDetailDto(final String sessionId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbSessionDetail.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        List<DbSessionDetail> list = criteria.list();
        if (list.size() != 1) {
            throw new IllegalStateException("Only 1 DbSessionDetail expected: " + list.size());
        }
        SessionDetailDto sessionDetailDto = new SessionDetailDto(list.get(0));
        sessionDetailDto.setLifecycleTrackingInfos(getLifecycleTrackingInfos(sessionId));
        sessionDetailDto.setPageAccessHistory(getPageAccessHistory(sessionId));
        sessionDetailDto.setAttackCommands(getUserCommandCount(sessionId, AttackCommand.class, null, null));
        sessionDetailDto.setMoveCommands(getUserCommandCount(sessionId, MoveCommand.class, null, null));
        sessionDetailDto.setBuilderCommands(getUserCommandCount(sessionId, BuilderCommand.class, null, null));
        sessionDetailDto.setFactoryCommands(getUserCommandCount(sessionId, FactoryCommand.class, null, null));
        sessionDetailDto.setMoneyCollectCommands(getUserCommandCount(sessionId, MoneyCollectCommand.class, null, null));
        sessionDetailDto.setGameAttempts(getGameAttempts(sessionId));
        return sessionDetailDto;
    }

    @SuppressWarnings("unchecked")
    private List<LifecycleTrackingInfo> getLifecycleTrackingInfos(final String sessionId) {
        // Get all start uuids
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbStartupTask.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        criteria.setProjection(Projections.groupProperty("startUuid"));
        List<String> uuids = criteria.list();
        ArrayList<LifecycleTrackingInfo> lifecycleTrackingInfos = new ArrayList<LifecycleTrackingInfo>();
        LifecycleTrackingInfo lastReaGameLifecycleTrackingInfo = null;
        for (String uuid : uuids) {
            criteria = sessionFactory.getCurrentSession().createCriteria(DbStartupTask.class);
            criteria.add(Restrictions.eq("startUuid", uuid));
            criteria.addOrder(Order.asc("clientTimeStamp"));
            List<DbStartupTask> dbStartupTasks = criteria.list();
            String levelTaskName = getLevelTaskName(dbStartupTasks);
            LifecycleTrackingInfo lifecycleTrackingInfo = new LifecycleTrackingInfo(dbStartupTasks, levelTaskName);
            if (lifecycleTrackingInfo.isRealGame()) {
                if (lastReaGameLifecycleTrackingInfo != null) {
                    lastReaGameLifecycleTrackingInfo.setNextReaGameLifecycleTrackingInfo(lifecycleTrackingInfo);
                }
                lastReaGameLifecycleTrackingInfo = lifecycleTrackingInfo;
            }
            lifecycleTrackingInfos.add(lifecycleTrackingInfo);
        }
        Collections.sort(lifecycleTrackingInfos);
        return lifecycleTrackingInfos;
    }

    private String getLevelTaskName(List<DbStartupTask> dbStartupTasks) {
        try {
            for (DbStartupTask dbStartupTask : dbStartupTasks) {
                if (dbStartupTask.getLevelTaskId() != null) {
                    return ((DbLevelTask) sessionFactory.getCurrentSession().get(DbLevelTask.class, dbStartupTask.getLevelTaskId())).getName();
                }
            }
        } catch (Exception e) {
            log.error("getLifecycleTrackingInfos", e);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LifecycleTrackingInfo getLifecycleTrackingInfo(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbStartupTask.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        List<DbStartupTask> startups = criteria.list();
        return new LifecycleTrackingInfo(startups, getLevelTaskName(startups));
    }

    @Override
    public RealGameTrackingInfo getGameTracking(LifecycleTrackingInfo lifecycleTrackingInfo) {
        RealGameTrackingInfo trackingInfoReal = new RealGameTrackingInfo();
        trackingInfoReal.setUserCommands(getUserCommand(lifecycleTrackingInfo));
        trackingInfoReal.setHistoryElements(historyService.getHistoryElements(lifecycleTrackingInfo.getStartServer(), lifecycleTrackingInfo.getNextStartServer(), lifecycleTrackingInfo.getSessionId(), lifecycleTrackingInfo.getBaseId()));
        return trackingInfoReal;
    }

    @Override
    public TutorialTrackingInfo getTutorialTrackingInfo(LifecycleTrackingInfo lifecycleTrackingInfo) {
        TutorialTrackingInfo tutorialTrackingInfo = new TutorialTrackingInfo();
        tutorialTrackingInfo.setDbEventTrackingStart(getDbEventTrackingStart(lifecycleTrackingInfo.getStartUuid()));
        tutorialTrackingInfo.setDbTutorialProgresss(getDbTutorialProgresses(lifecycleTrackingInfo.getStartUuid()));
        return tutorialTrackingInfo;
    }

    @SuppressWarnings("unchecked")
    private List<DbTutorialProgress> getDbTutorialProgresses(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbTutorialProgress.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    private List<DbPageAccess> getPageAccessHistory(final String sessionId) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbPageAccess.class);
        criteria.add(Restrictions.eq("sessionId", sessionId));
        criteria.addOrder(Order.asc("timeStamp"));
        return criteria.list();
    }

    @Override
    @Transactional
    public void saveUserCommand(BaseCommand baseCommand) {
        try {
            DbUserCommand dbUserCommand = new DbUserCommand(session.getConnection(), baseCommand, baseService.getBaseName(baseService.getBase().getSimpleBase()));
            // log.debug("User Command: " + dbUserCommand);
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserCommand);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    public void onUserCreated(User user) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setSessionId(session.getSessionId());
            dbUserHistory.setCookieId(session.getCookieId());
            dbUserHistory.setCreated();
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    public void onUserLoggedIn(User user, Base base) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setSessionId(session.getSessionId());
            dbUserHistory.setCookieId(session.getCookieId());
            dbUserHistory.setLoggedIn();
            if (base != null) {
                dbUserHistory.setBaseName(baseService.getBaseName(base.getSimpleBase()));
            }
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    public void onUserLoggedOut(User user) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setLoggedOut();
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    @Deprecated
    public void onBaseCreated(User user, String baseName) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setBaseCreated();
            dbUserHistory.setBaseName(baseName);
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    @Deprecated
    public void onBaseDefeated(User user, Base base) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setBaseDefeated();
            dbUserHistory.setBaseName(baseService.getBaseName(base.getSimpleBase()));
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    @Deprecated
    public void onBaseSurrender(User user, Base base) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setBaseSurrender();
            dbUserHistory.setBaseName(baseService.getBaseName(baseService.getBase().getSimpleBase()));
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }


    @Override
    @Transactional
    // ???
    public void onUserEnterGame(User user) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setGameEntered();
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    // ???
    public void onUserLeftGame(User user) {
        try {
            DbUserHistory dbUserHistory = new DbUserHistory(user);
            dbUserHistory.setGameLeft();
            sessionFactory.getCurrentSession().saveOrUpdate(dbUserHistory);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    public void trackUserMessage(UserMessage userMessage) {
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(new DbUserMessage(userMessage));
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    @Transactional
    public void trackWindowsClosed(String startUUid) {
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(new DbWindowClosed(session.getSessionId(), startUUid));
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void onJavaScriptDetected(Boolean html5Support) {
        session.onJavaScriptDetected(html5Support);
    }

    @Override
    public boolean isJavaScriptDetected() {
        return session.isJavaScriptDetected();
    }

    @Override
    public boolean isHtml5Support() {
        return session.isHtml5Support();
    }

    @Override
    @Transactional
    public void onTutorialProgressChanged(TutorialConfig.TYPE type, String startUuid, int taskId, String tutorialTaskName, long duration, long clientTimeStamp) {
        try {
            DbLevelTask dbLevelTask = (DbLevelTask) sessionFactory.getCurrentSession().get(DbLevelTask.class, taskId);
            sessionFactory.getCurrentSession().save(new DbTutorialProgress(session.getSessionId(), type.name(), startUuid, dbLevelTask.getName(), tutorialTaskName, duration, clientTimeStamp));
        } catch (Exception e) {
            log.error("onTutorialProgressChanged", e);
        }
    }

    @Override
    @Transactional
    public void onEventTrackingStart(EventTrackingStart eventTrackingStart) {
        sessionFactory.getCurrentSession().save(new DbEventTrackingStart(eventTrackingStart, session.getSessionId()));
    }

    @Override
    @Transactional
    public void onEventTrackerItems(Collection<EventTrackingItem> eventTrackingItems, Collection<SyncItemInfo> syncItemInfos, Collection<SelectionTrackingItem> selectionTrackingItems, Collection<TerrainScrollTracking> terrainScrollTrackings, Collection<BrowserWindowTracking> browserWindowTrackings, Collection<DialogTracking> dialogTrackings) {
        onEventTrackerItems(eventTrackingItems);
        saveSyncItemInfos(syncItemInfos);
        saveSelections(selectionTrackingItems);
        saveScrollTrackingItems(terrainScrollTrackings);
        saveBrowserWindowTrackings(browserWindowTrackings);
        saveDialogTrackings(dialogTrackings);
    }

    private void onEventTrackerItems(Collection<EventTrackingItem> eventTrackingItems) {
        ArrayList<DbEventTrackingItem> dbEventTrackingItems = new ArrayList<DbEventTrackingItem>();
        for (EventTrackingItem eventTrackingItem : eventTrackingItems) {
            dbEventTrackingItems.add(new DbEventTrackingItem(eventTrackingItem));
        }
        HibernateUtil.saveOrUpdateAll(sessionFactory, dbEventTrackingItems);
    }

    private void saveSyncItemInfos(Collection<SyncItemInfo> syncItemInfos) {
        ArrayList<DbSyncItemInfo> dbSyncItemInfos = new ArrayList<DbSyncItemInfo>();
        for (SyncItemInfo syncItemInfo : syncItemInfos) {
            dbSyncItemInfos.add(new DbSyncItemInfo(syncItemInfo));
        }
        HibernateUtil.saveOrUpdateAll(sessionFactory, dbSyncItemInfos);
    }

    private void saveSelections(Collection<SelectionTrackingItem> selectionTrackingItems) {
        ArrayList<DbSelectionTrackingItem> dbSelectionTrackingItems = new ArrayList<DbSelectionTrackingItem>();
        for (SelectionTrackingItem command : selectionTrackingItems) {
            dbSelectionTrackingItems.add(new DbSelectionTrackingItem(command));
        }
        HibernateUtil.saveOrUpdateAll(sessionFactory, dbSelectionTrackingItems);
    }

    private void saveScrollTrackingItems(Collection<TerrainScrollTracking> terrainScrollTrackings) {
        ArrayList<DbScrollTrackingItem> dbScrollTrackingItems = new ArrayList<DbScrollTrackingItem>();
        for (TerrainScrollTracking terrainScroll : terrainScrollTrackings) {
            dbScrollTrackingItems.add(new DbScrollTrackingItem(terrainScroll));
        }
        HibernateUtil.saveOrUpdateAll(sessionFactory, dbScrollTrackingItems);
    }

    private void saveBrowserWindowTrackings(Collection<BrowserWindowTracking> browserWindowTrackings) {
        ArrayList<DbBrowserWindowTracking> dbBrowserWindowTrackings = new ArrayList<DbBrowserWindowTracking>();
        for (BrowserWindowTracking browserWindowTracking : browserWindowTrackings) {
            dbBrowserWindowTrackings.add(new DbBrowserWindowTracking(browserWindowTracking));
        }
        HibernateUtil.saveOrUpdateAll(sessionFactory, dbBrowserWindowTrackings);
    }

    private void saveDialogTrackings(Collection<DialogTracking> dialogTrackings) {
        ArrayList<DbDialogTracking> dbDialogTrackings = new ArrayList<DbDialogTracking>();
        for (DialogTracking dialogTracking : dialogTrackings) {
            dbDialogTrackings.add(new DbDialogTracking(dialogTracking));
        }
        HibernateUtil.saveOrUpdateAll(sessionFactory, dbDialogTrackings);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DbEventTrackingStart getDbEventTrackingStart(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbEventTrackingStart.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        List<DbEventTrackingStart> dbEventTrackingStarts = criteria.list();
        if (dbEventTrackingStarts.isEmpty()) {
            return null;
        } else {
            return dbEventTrackingStarts.get(0);
        }
    }

    @Override
    @Transactional
    public void saveStartupTask(StartupTaskInfo startupTaskInfo, String startUuid, Integer levelTaskId) {
        String baseName = null;
        Integer baseId = null;
        try {
            if (levelTaskId == null) {
                baseId = baseService.getBase().getBaseId();
                baseName = baseService.getBaseName(baseService.getBase().getSimpleBase());
            }
        } catch (NoConnectionException e) {
            // Ignore
        }
        sessionFactory.getCurrentSession().save(new DbStartupTask(session.getSessionId(), startupTaskInfo, startUuid, userGuidanceService.getDbLevel(), levelTaskId, userService.getUser(), baseId, baseName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DbEventTrackingItem> getDbEventTrackingItem(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbEventTrackingItem.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DbSelectionTrackingItem> getDbSelectionTrackingItems(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbSelectionTrackingItem.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DbSyncItemInfo> getDbSyncItemInfos(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbSyncItemInfo.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DbScrollTrackingItem> getDbScrollTrackingItems(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbScrollTrackingItem.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DbBrowserWindowTracking> getDbBrowserWindowTrackings(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbBrowserWindowTracking.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DbDialogTracking> getDbDialogTrackings(String startUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DbDialogTracking.class);
        criteria.add(Restrictions.eq("startUuid", startUuid));
        criteria.addOrder(Order.asc("clientTimeStamp"));
        return criteria.list();
    }
}