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

package com.btxtech.game.jsre.common.gameengine.services.terrain;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.gameengine.itemType.ItemType;
import java.util.List;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 14:23:09
 */
public interface TerrainService {
    List<Index> setupPathToDestination(Index target, Index destination, int range);

    List<Index> setupPathToDestination(Index start, Index destination);

    public boolean isFree(Index position, ItemType toBePlaced);
}
