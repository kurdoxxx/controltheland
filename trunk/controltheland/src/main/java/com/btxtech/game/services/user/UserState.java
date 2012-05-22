/*
 * Copyright (c) 2011.
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

package com.btxtech.game.services.user;

import com.btxtech.game.services.base.Base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: beat
 * Date: 19.01.2011
 * Time: 10:42:00
 */
public class UserState implements Serializable {
    private String userName;
    private Base base;
    private int dbLevelId;
    private int xp;
    private String sessionId;
    private boolean sendResurrectionMessage = false;
    private int razarion;
    private Collection<Integer> inventoryItemIds = new ArrayList<>();
    private Collection<Integer> inventoryArtifactIds = new ArrayList<>();

    public boolean isRegistered() {
        return userName != null;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public int getDbLevelId() {
        return dbLevelId;
    }

    public void setDbLevelId(int dbLevelId) {
        this.dbLevelId = dbLevelId;
    }

    public Base getBase() {
        return base;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void increaseXp(int deltaXp) {
        xp += deltaXp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isOnline() {
        return sessionId != null;
    }

    public String getUser() {
        return userName;
    }

    public void setUser(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "UserState: user=" + userName;
    }

    public void setSendResurrectionMessage() {
        sendResurrectionMessage = true;
    }

    public void clearSendResurrectionMessageAndClear() {
        sendResurrectionMessage = false;
    }

    public boolean isSendResurrectionMessage() {
        return sendResurrectionMessage;
    }

    public int getRazarion() {
        return razarion;
    }

    public void setRazarion(int razarion) {
        this.razarion = razarion;
    }

    public void addRazarion(int value) {
        razarion += value;
    }

    public void subRazarion(int value) {
        razarion -= value;
    }

    public void addInventoryItem(int inventoryItemId) {
        inventoryItemIds.add(inventoryItemId);
    }

    public void addInventoryArtifact(int inventoryArtifactId) {
        inventoryArtifactIds.add(inventoryArtifactId);
    }

    public Collection<Integer> getInventoryItemIds() {
        return inventoryItemIds;
    }

    public Collection<Integer> getInventoryArtifactIds() {
        return inventoryArtifactIds;
    }
}
