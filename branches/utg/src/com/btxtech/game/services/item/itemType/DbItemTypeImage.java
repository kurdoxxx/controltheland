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

package com.btxtech.game.services.item.itemType;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * User: beat
 * Date: 14.12.2009
 * Time: 22:47:20
 */
@Entity(name = "ITEM_TYPE_IMAGE")
public class DbItemTypeImage implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne(optional = false)
    private DbItemType itemType;
    private int number;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false, length = 500000)
    private byte[] data;

    public DbItemType getItemType() {
        return itemType;
    }

    public void setItemType(DbItemType itemType) {
        this.itemType = itemType;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbItemTypeImage that = (DbItemTypeImage) o;
        return id != null ? id.equals(that.id):super.equals(that);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        } else {
            return super.hashCode();
        }
    }
}
