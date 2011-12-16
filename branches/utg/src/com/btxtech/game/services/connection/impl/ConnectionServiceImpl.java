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

import com.btxtech.game.jsre.common.NoConnectionException;
import com.btxtech.game.jsre.common.Packet;
import com.btxtech.game.jsre.common.SimpleBase;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import com.btxtech.game.jsre.common.gameengine.syncObjects.command.BaseCommand;
import com.btxtech.game.services.base.Base;
import com.btxtech.game.services.base.BaseService;
import com.btxtech.game.services.connection.ClientLogEntry;
import com.btxtech.game.services.connection.Connection;
import com.btxtech.game.services.connection.ConnectionService;
import com.btxtech.game.services.connection.ConnectionStatistics;
import com.btxtech.game.services.connection.Interaction;
import com.btxtech.game.services.connection.Session;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

/**
 * User: beat
 * Date: Jul 15, 2009
 * Time: 1:20:27 PM
 */
@Component("connectionService")
public class ConnectionServiceImpl extends TimerTask implements ConnectionService {
    public static final long USER_TRACKING_PERIODE = 10 * 1000;
    @Autowired
    private Session session;
    @Autowired
    private BaseService baseService;
    private Timer timer;
    private Log log = LogFactory.getLog(ConnectionServiceImpl.class);
    private HibernateTemplate hibernateTemplate;
    private final ArrayList<Connection> onlineConnection = new ArrayList<Connection>();
    private static final int MAX_NO_TICK_COUNT = 50;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    @PostConstruct
    public void init() {
        timer = new Timer(getClass().getName(), true);
        timer.scheduleAtFixedRate(this, 0, USER_TRACKING_PERIODE);
    }

    @PreDestroy
    public void cleanup() {
        if (timer != null) {
            timer.cancel();
            timer = null;
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
    public void run() {
        for (Iterator<Connection> it = onlineConnection.iterator(); it.hasNext();) {
            Connection connection = it.next();
            try {
                int tickCount = connection.resetAndGetTickCount();
                if (connection.getNoTickCount() > MAX_NO_TICK_COUNT) {
                    log.info("User kicked due timeout: " + connection.getBase().getName());
                    connection.setClosed();
                    it.remove();
                } else {
                    double ticksPerSecond = (double) tickCount / (double) (USER_TRACKING_PERIODE / 1000);
                    ConnectionStatistics connectionStatistics = new ConnectionStatistics(connection.getBase().getSimpleBase(), connection.getSessionId(), ticksPerSecond);
                    hibernateTemplate.saveOrUpdate(connectionStatistics);
                }
            } catch (Throwable t) {
                log.error("", t);
            }
        }
    }

    @Override
    public void clientLog(String message) {
        try {
            ClientLogEntry clientLogEntry = new ClientLogEntry(message, session);
            hibernateTemplate.saveOrUpdate(clientLogEntry);
            log.info(clientLogEntry.getFormatMessage());
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void saveUserInteraction(BaseCommand baseCommand) {
        try {
            Interaction userInteraction = new Interaction(session.getConnection(), baseCommand);
            log.debug(userInteraction);
            hibernateTemplate.saveOrUpdate(userInteraction);
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void createConnection(Base base) {
        Connection connection = session.getConnection();
        if (connection != null) {
            log.info("Existing connection will be terminated");
            closeConnection();
        }
        connection = new Connection(session.getSessionId());
        connection.setBase(base);
        session.setConnection(connection);
        synchronized (onlineConnection) {
            onlineConnection.add(connection);
        }
    }

    @Override
    public void closeConnection() {
        Connection connection = session.getConnection();
        if (connection == null) {
            throw new IllegalStateException("Connection does not exist");
        }
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection already closed");
        }
        connection.setClosed();
        session.setConnection(null);
        synchronized (onlineConnection) {
            onlineConnection.remove(connection);
        }
    }

    @Override
    public Connection getConnection() throws NoConnectionException {
        Connection connection = session.getConnection();
        if (connection == null) {
            throw new NoConnectionException("Connection does not exist");
        }
        if (connection.isClosed()) {
            throw new NoConnectionException("Connection already closed");
        }
        return connection;
    }

    @Override
    public boolean hasConnection() {
        Connection connection = session.getConnection();
        if (connection == null) {
            return false;
        }
        if (connection.isClosed()) {
            return false;
        }
        return true;
    }

}