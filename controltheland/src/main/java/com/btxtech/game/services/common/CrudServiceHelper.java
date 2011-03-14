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

package com.btxtech.game.services.common;

import com.btxtech.game.services.terrain.DbTerrainImagePosition;
import java.io.Serializable;
import java.util.Collection;

/**
 * User: beat
 * Date: 23.07.2010
 * Time: 23:51:06
 */
@Deprecated
public interface CrudServiceHelper<T extends CrudChild> {
    Collection<T> readDbChildren();

    T readDbChild(Serializable id);

    void deleteDbChild(T child);

    void updateDbChildren(Collection<T> children);

    void createDbChild();

    void createDbChild(Class<? extends T> createClass);

    void deleteAllChildren();

    void addChild(T t);
}
