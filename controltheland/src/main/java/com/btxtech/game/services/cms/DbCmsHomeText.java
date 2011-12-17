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

package com.btxtech.game.services.cms;

import com.btxtech.game.services.common.CrudChild;
import com.btxtech.game.services.user.UserService;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * User: beat
 * Date: 07.07.2010
 * Time: 20:47:23
 */
@Deprecated
@Entity(name = "CMS_HOME_TEXT")
public class DbCmsHomeText implements CrudChild {
    @Id
    @GeneratedValue
    private Integer id;
    private boolean isActive;
    private String internalName;
    @Column(length = 50000)
    private String text;

    @Override
    public Serializable getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getName() {
        return internalName;
    }

    public void setName(String internalName) {
        this.internalName = internalName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void init(UserService userService) {
    }

    @Override
    public void setParent(Object o) {
        // Np parent
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbCmsHomeText)) return false;

        DbCmsHomeText that = (DbCmsHomeText) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}