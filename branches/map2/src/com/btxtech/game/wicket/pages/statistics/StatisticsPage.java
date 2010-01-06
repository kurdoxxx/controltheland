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

package com.btxtech.game.wicket.pages.statistics;

import com.btxtech.game.wicket.pages.basepage.BasePage;

/**
 * User: beat
 * Date: Sep 13, 2009
 * Time: 7:27:03 PM
 */
public class StatisticsPage extends BasePage {
    public StatisticsPage() {
        add(new BaseUpTime(0));
        add(new BaseKills(0));
        add(new BaseSize(0));
        add(new BaseMoney(0));
    }
}
