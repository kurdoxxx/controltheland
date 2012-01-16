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

package com.btxtech.game.wicket.pages.mgmt.condition;

import com.btxtech.game.services.utg.condition.DbConditionConfig;
import com.btxtech.game.services.utg.condition.DbItemTypePositionComparisonConfig;
import com.btxtech.game.services.utg.condition.DbPositionComparisonConfig;
import com.btxtech.game.wicket.uiservices.ItemTypePanel;
import com.btxtech.game.wicket.uiservices.RectanglePanel;
import com.btxtech.game.wicket.uiservices.TerritoryPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 * User: beat
 * Date: 28.10.2011
 * Time: 23:13:41
 */
public class PositionComparisonConfigPanel extends Panel {

    public PositionComparisonConfigPanel(String id) {
        super(id);
        setDefaultModel(new CompoundPropertyModel<DbPositionComparisonConfig>(new IModel<DbPositionComparisonConfig>() {

            @Override
            public DbPositionComparisonConfig getObject() {
                // TODO Why does not wicket do this?
                DbConditionConfig dbConditionConfig = (DbConditionConfig) getParent().getDefaultModelObject();
                return (DbPositionComparisonConfig) dbConditionConfig.getDbAbstractComparisonConfig();
            }

            @Override
            public void setObject(DbPositionComparisonConfig object) {
                // Ignore
            }

            @Override
            public void detach() {
            }
        }));
        add(new RectanglePanel("region"));
    }
}