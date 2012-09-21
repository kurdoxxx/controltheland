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

package com.btxtech.game.services.connection.impl;

import com.btxtech.game.jsre.client.GameEngineMode;
import com.btxtech.game.jsre.common.NoConnectionException;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncBaseItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import com.btxtech.game.jsre.common.packets.ChatMessage;
import com.btxtech.game.jsre.common.packets.Packet;
import com.btxtech.game.services.common.HibernateUtil;
import com.btxtech.game.services.connection.ClientLogEntry;
import com.btxtech.game.services.connection.Connection;
import com.btxtech.game.services.connection.ConnectionStatistics;
import com.btxtech.game.services.connection.NoBaseException;
import com.btxtech.game.services.connection.ServerConnectionService;
import com.btxtech.game.services.connection.Session;
import com.btxtech.game.services.planet.Base;
import com.btxtech.game.services.planet.PlanetSystemService;
import com.btxtech.game.services.user.User;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.services.utg.UserTrackingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: beat
 * Date: Jul 15, 2009
 * Time: 1:20:27 PM
 */
@Component("connectionService")
public class ServerConnectionServiceImpl extends TimerTask implements ServerConnectionService {
    private static final long USER_TRACKING_PERIODE = 10 * 1000;
    private static final int MAX_NO_TICK_COUNT = 20;
    @Autowired
    private Session session;
    @Autowired
    private UserTrackingService userTrackingService;
    @Autowired
    private UserService userService;
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private PlanetSystemService planetSystemService;
    private Timer timer;
    private Log log = LogFactory.getLog(ServerConnectionServiceImpl.class);
    private final ArrayList<Connection> onlineConnection = new ArrayList<>();
    private ChatMessageQueue chatMessageQueue = new ChatMessageQueue();

    @PostConstruct
    public void init() {
        timer = new Timer(getClass().getName(), true);
        timer.scheduleAtFixedRate(this, 0, USER_TRACKING_PERIODE);
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void sendSyncInfo(SyncItem syncItem) {
        synchronized (onlineConnection) {
            for (Connection connection : onlineConnection) {
                try {
                    connection.sendBaseSyncItem(syncItem);
                } catch (Throwable t) {
                    log.error("", t);
                }
            }
        }
    }

    @Override
    public void sendSyncInfos(Collection<SyncBaseItem> syncItem) {
        for (SyncItem item : syncItem) {
            sendSyncInfo(item);
        }
    }

    @Override
    public void sendPacket(Packet packet) {
        synchronized (onlineConnection) {
            for (Connection connection : onlineConnection) {
                try {
                    connection.sendPacket(packet);
                } catch (Throwable t) {
                    log.error("", t);
                }
            }
        }
    }

    @Override
    public void sendPacket(SimpleBase base, Packet packet) {
        synchronized (onlineConnection) {
            for (Connection connection : onlineConnection) {
                try {
                    if (connection.getBase().getSimpleBase().equals(base)) {
                        connection.sendPacket(packet);
                        return;
                    }
                } catch (Throwable t) {
                    log.error("", t);
                }
            }
        }
    }

    @Override
    public void sendChatMessage(ChatMessage chatMessage) {
        User user = userService.getUser();
        String name;
        if (user != null) {
            name = user.getUsername();
        } else if (userService.getUserState().getBase() != null) {
            name = planetSystemService.getServerPlanetServices().getBaseService().getBaseName();
        } else {
            name = "Guest";
        }
        chatMessageQueue.initAndPutMessage(name, chatMessage);
        sendPacket(chatMessage);
        userTrackingService.trackChatMessage(chatMessage);
    }

    @Override
    public List<ChatMessage> pollChatMessages(Integer lastMessageId) {
        return chatMessageQueue.peekMessages(lastMessageId);
    }

    @Override
    public void run() {
        try {
            synchronized (onlineConnection) {
                for (Iterator<Connection> it = onlineConnection.iterator(); it.hasNext(); ) {
                    Connection connection = it.next();
                    try {
                        int tickCount = connection.resetAndGetTickCount();
                        if (connection.getNoTickCount() > MAX_NO_TICK_COUNT) {
                            log.info("User kicked due timeout: " + planetSystemService.getServerPlanetServices(connection.getBase().getSimpleBase()).getBaseService().getBaseName(connection.getBase().getSimpleBase()));
                            if (connection.getBase() != null && connection.getBase().getUserState() != null && connection.getBase().getUserState().getUser() != null) {
                                HibernateUtil.openSession4InternalCall(sessionFactory);
                                try {
                                    userTrackingService.onUserLeftGame(userService.getUser(connection.getBase().getUserState().getUser()));
                                } finally {
                                    HibernateUtil.closeSession4InternalCall(sessionFactory);
                                }

                            }
                            connection.setClosed(NoConnectionException.Type.TIMED_OUT);
                            it.remove();
                        } else {
                            double ticksPerSecond = (double) tickCount / (double) (USER_TRACKING_PERIODE / 1000);
                            if (!Double.isInfinite(ticksPerSecond) && !Double.isNaN(ticksPerSecond)) {
                                String baseName = planetSystemService.getServerPlanetServices(connection.getBase().getSimpleBase()).getBaseService().getBaseName(connection.getBase().getSimpleBase());
                                ConnectionStatistics connectionStatistics = new ConnectionStatistics(baseName, connection.getSessionId(), ticksPerSecond);
                                HibernateUtil.openSession4InternalCall(sessionFactory);
                                try {
                                    sessionFactory.getCurrentSession().saveOrUpdate(connectionStatistics);
                                } finally {
                                    HibernateUtil.closeSession4InternalCall(sessionFactory);
                                }
                            }
                        }
                    } catch (Throwable t) {
                        log.error("", t);
                    }
                }
            }
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void clientLog(String message, Date date) {
        try {
            String baseName = null;
            try {
                baseName = planetSystemService.getServerPlanetServices().getBaseService().getBaseName();
            } catch (NoBaseException e) {
                // Ignore
            }
            ClientLogEntry clientLogEntry = new ClientLogEntry(message, date, session, baseName);
            // hibernateTemplate.saveOrUpdate(clientLogEntry);
            log.info(clientLogEntry.getFormatMessage());
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void createConnection(Base base, String startUuid) {
        // Connection to same base from same browser
        Connection connection = session.getConnection();
        if (connection != null) {
            log.warn("Existing connection will be terminated I");
            closeConnection(connection, NoConnectionException.Type.ANOTHER_CONNECTION_EXISTS);
        }
        // Connection to same base from different browser
        Connection preventConcurrentException = null;
        synchronized (onlineConnection) {
            for (Connection existingConnection : onlineConnection) {
                if (existingConnection.getBase().equals(base)) {
                    preventConcurrentException = existingConnection;
                    break;
                }
            }
        }
        if (preventConcurrentException != null) {
            log.warn("Existing connection will be terminated II");
            closeConnection(preventConcurrentException, NoConnectionException.Type.ANOTHER_CONNECTION_EXISTS);
        }

        connection = new Connection(session.getSessionId(), startUuid);
        connection.setBase(base);
        session.setConnection(connection);
        log.debug("Connection established");
        synchronized (onlineConnection) {
            onlineConnection.add(connection);
        }
        if (base.getUserState() != null && base.getUserState().getUser() != null) {
            userTrackingService.onUserEnterGame(userService.getUser(base.getUserState().getUser()));
        }
    }

    private void closeConnection(Connection connection, NoConnectionException.Type closedReason) {
        if (connection.getBase() != null && connection.getBase().getUserState() != null && connection.getBase().getUserState().getUser() != null) {
            userTrackingService.onUserLeftGame(userService.getUser(connection.getBase().getUserState().getUser()));
        }
        connection.setClosed(closedReason);
        log.debug("Connection closed 1");
        synchronized (onlineConnection) {
            onlineConnection.remove(connection);
        }
    }

    @Override
    public void closeConnection(SimpleBase simpleBase, NoConnectionException.Type closedReason) {
        Connection connection = null;
        synchronized (onlineConnection) {
            for (Iterator<Connection> iterator = onlineConnection.iterator(); iterator.hasNext(); ) {
                Connection online = iterator.next();
                if (online.getBase().getSimpleBase().equals(simpleBase)) {
                    connection = online;
                    iterator.remove();
                }
            }
        }
        if (connection == null) {
            throw new IllegalStateException("Online connection does not exist for base: " + simpleBase);
        }

        connection.setClosed(closedReason);
        log.debug("Connection closed 2");
    }


    @Override
    public Connection getConnection(String startUuid) throws NoConnectionException {
        Connection connection = session.getConnection();
        if (connection == null) {
            throw new NoConnectionException(NoConnectionException.Type.NON_EXISTENT);
        }
        if (connection.isClosed()) {
            throw new NoConnectionException(connection.getClosedReason());
        }
        if (!connection.getStartUuid().equals(startUuid)) {
            throw new NoConnectionException(NoConnectionException.Type.ANOTHER_CONNECTION_EXISTS);
        }
        return connection;
    }

    @Override
    public boolean hasConnection() {
        Connection connection = session.getConnection();
        return connection != null && !connection.isClosed();
    }

    @Override
    public boolean hasConnection(SimpleBase simpleBase) {
        synchronized (onlineConnection) {
            for (Connection connection : onlineConnection) {
                if (connection.getBase().getSimpleBase().equals(simpleBase)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Collection<SimpleBase> getOnlineBases() {
        HashSet<SimpleBase> simpleBases = new HashSet<>();
        synchronized (onlineConnection) {
            for (Connection connection : onlineConnection) {
                if (connection.getBase() != null) {
                    simpleBases.add(connection.getBase().getSimpleBase());
                }
            }
        }
        return simpleBases;
    }

    @Override
    public GameEngineMode getGameEngineMode() {
        return GameEngineMode.MASTER;
    }
}