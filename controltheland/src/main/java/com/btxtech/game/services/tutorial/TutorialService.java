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

package com.btxtech.game.services.tutorial;

import com.btxtech.game.jsre.client.common.info.InvalidLevelState;
import com.btxtech.game.services.common.CrudRootServiceHelper;

/**
 * User: beat
 * Date: 24.07.2010
 * Time: 11:32:53
 */
public interface TutorialService {
    CrudRootServiceHelper<DbTutorialConfig> getDbTutorialCrudRootServiceHelper();

    DbTutorialConfig getDbTutorialConfig(int levelTaskId) throws InvalidLevelState;
}