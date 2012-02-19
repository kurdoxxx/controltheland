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

import com.btxtech.game.jsre.common.gameengine.services.utg.GameStartupState;
import com.btxtech.game.jsre.common.gameengine.services.utg.UserAction;
import com.btxtech.game.services.connection.Session;
import com.btxtech.game.services.utg.DbUserAction;
import com.btxtech.game.services.utg.GameStartup;
import com.btxtech.game.services.utg.GameTrackingInfo;
import com.btxtech.game.services.utg.PageAccess;
import com.btxtech.game.services.utg.UserDetails;
import com.btxtech.game.services.utg.UserTrackingService;
import com.btxtech.game.services.utg.VisitorDetailInfo;
import com.btxtech.game.services.utg.VisitorInfo;
import com.btxtech.game.wicket.pages.basepage.BasePage;
import com.btxtech.game.wicket.pages.entergame.EnterBasePanel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

/**
 * User: beat
 * Date: 12.01.2010
 * Time: 22:41:05
 */
@Component("userTrackingService")
public class UserTrackingServiceImpl implements UserTrackingService {
    @Autowired
    private Session session;
    private HibernateTemplate hibernateTemplate;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    @Override
    public void pageAccess(BasePage basePage) {
        PageAccess pageAccess = new PageAccess(session.getSessionId(), basePage.getClass().getName(), basePage.getAdditionalPageInfo());
        hibernateTemplate.saveOrUpdate(pageAccess);
    }

    @Override
    public void newSession(UserDetails userDetails) {
        hibernateTemplate.saveOrUpdate(userDetails);
    }

    @Override
    public void gameStartup(GameStartupState state, Date timeStamp) {
        GameStartup gameStartup = new GameStartup(session.getSessionId(), state, timeStamp);
        hibernateTemplate.saveOrUpdate(gameStartup);
    }

    @Override
    public void saveUserActions(ArrayList<UserAction> userActions) {
        ArrayList<DbUserAction> dbUserActions = new ArrayList<DbUserAction>();
        for (UserAction userAction : userActions) {
            dbUserActions.add(new DbUserAction(userAction, session.getSessionId()));
        }
        hibernateTemplate.saveOrUpdateAll(dbUserActions);
    }

    @Override
    public List<VisitorInfo> getVisitorInfos() {
        ArrayList<VisitorInfo> visitorInfos = new ArrayList<VisitorInfo>();
        List<Object[]> datesAndHits = (List<Object[]>) hibernateTemplate.find("select u.timeStamp, u.sessionId, count(p) from com.btxtech.game.services.utg.UserDetails u, com.btxtech.game.services.utg.PageAccess p where u.sessionId = p.sessionId and u.isCrawler = false group by u.sessionId order by u.timeStamp desc");
        for (Object[] datesAndHit : datesAndHits) {
            Date timeStamp = (Date) datesAndHit[0];
            String sessionId = (String) datesAndHit[1];
            int hits = ((Long) datesAndHit[2]).intValue();
            int enterSetupHits = getHitsForPage(sessionId, EnterBasePanel.class);
            int enterGameHits = getHitsForGameStartup(sessionId);
            visitorInfos.add(new VisitorInfo(timeStamp, sessionId, hits, enterSetupHits, enterGameHits));
        }
        return visitorInfos;
    }

    private int getHitsForPage(final String sessionId, final Class<EnterBasePanel> enterBasePanelClass) {
        List<Integer> list = (List<Integer>) hibernateTemplate.execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(PageAccess.class);
                criteria.add(Restrictions.eq("sessionId", sessionId));
                criteria.add(Restrictions.eq("page", enterBasePanelClass.getName()));
                criteria.setProjection(Projections.rowCount());
                return criteria.list();
            }
        });
        return list.get(0);
    }

    private int getHitsForGameStartup(final String sessionId) {
        List<Integer> list = (List<Integer>) hibernateTemplate.execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(GameStartup.class);
                criteria.add(Restrictions.eq("sessionId", sessionId));
                criteria.add(Restrictions.eq("state", GameStartupState.SERVER));
                criteria.setProjection(Projections.rowCount());
                return criteria.list();
            }
        });
        return list.get(0);
    }

    @Override
    public VisitorDetailInfo getVisitorDetails(final String sessionId) {
        List<UserDetails> list = (List<UserDetails>) hibernateTemplate.execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(UserDetails.class);
                criteria.add(Restrictions.eq("sessionId", sessionId));
                return criteria.list();
            }
        });
        if (list.size() != 1) {
            throw new IllegalStateException("Only 1 UserDetails expected: " + list.size());
        }
        VisitorDetailInfo visitorDetailInfo = new VisitorDetailInfo(list.get(0));
        visitorDetailInfo.setGameTrackingInfos(getGameTrackingInfos(sessionId));
        visitorDetailInfo.setPageAccessHistory(getPageAccessHistory(sessionId));
        return visitorDetailInfo;
    }

    private List<GameTrackingInfo> getGameTrackingInfos(final String sessionId) {
        ArrayList<GameTrackingInfo> gameTrackingInfos = new ArrayList<GameTrackingInfo>();
        // Get all game startups
        List<GameStartup> list = (List<GameStartup>) hibernateTemplate.execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(GameStartup.class);
                criteria.add(Restrictions.eq("sessionId", sessionId));
                criteria.addOrder(Order.asc("timeStamp"));
                return criteria.list();
            }
        });
        // Sort game startups
        GameTrackingInfo gameTrackingInfo = null;
        for (GameStartup gameStartup : list) {
            switch (gameStartup.getState()) {
                case SERVER:
                    gameTrackingInfo = new GameTrackingInfo();
                    gameTrackingInfos.add(gameTrackingInfo);
                    gameTrackingInfo.setServerGameStartup(gameStartup);
                    break;
                case CLIENT_START:
                    gameTrackingInfo.setClientStartGameStartup(gameStartup);
                    break;
                case CLIENT_RUNNING:
                    gameTrackingInfo.setClientRunningGameStartup(gameStartup);
                    break;
            }
        }

        GameTrackingInfo previous = null;
        for (GameTrackingInfo trackingInfo : gameTrackingInfos) {
            if (previous == null) {
                if (trackingInfo.getClientRunningGameStartup() != null) {
                    previous = trackingInfo;
                }
                continue;
            }
            if (trackingInfo.getClientRunningGameStartup() == null) {
                continue;
            }

            List<DbUserAction> userActions = getUserAction(sessionId, previous.getClientStartGameStartup().getClientTimeStamp(), trackingInfo.getClientStartGameStartup().getClientTimeStamp());
            previous.setUserAction(userActions);
            if (previous.getClientRunningGameStartup() != null) {
                previous = trackingInfo;
            }
        }
        // Add last one
        if (previous != null && previous.getClientRunningGameStartup() != null) {
            List<DbUserAction> userActions = getUserAction(sessionId, previous.getClientStartGameStartup().getClientTimeStamp(), null);
            previous.setUserAction(userActions);
        }
        return gameTrackingInfos;
    }

    private List<DbUserAction> getUserAction(final String sessionId, final Date from, final Date to) {
        List<DbUserAction> list = (List<DbUserAction>) hibernateTemplate.execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(DbUserAction.class);
                criteria.add(Restrictions.eq("sessionId", sessionId));
                criteria.add(Restrictions.ge("clientTimeStamp", from));
                if (to != null) {
                    criteria.add(Restrictions.le("clientTimeStamp", to));
                }
                criteria.addOrder(Order.asc("clientTimeStamp"));
                return criteria.list();
            }
        });
        return list;
    }

    private List<PageAccess> getPageAccessHistory(final String sessionId) {
        List<PageAccess> list = (List<PageAccess>) hibernateTemplate.execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(PageAccess.class);
                criteria.add(Restrictions.eq("sessionId", sessionId));
                criteria.addOrder(Order.asc("timeStamp"));
                return criteria.list();
            }
        });
        return list;
    }

}