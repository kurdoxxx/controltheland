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

package com.btxtech.game.jsre.client.simulation;

import com.btxtech.game.jsre.client.ClientBase;
import com.btxtech.game.jsre.common.tutorial.StepConfig;

/**
 * User: beat
 * Date: 18.07.2010
 * Time: 16:15:26
 */
public class Step {
    private StepConfig stepConfig;

    public Step(StepConfig stepConfig) {
        this.stepConfig = stepConfig;
        SimulationConditionServiceImpl.getInstance().activateCondition(stepConfig.getConditionConfig(), ClientBase.getInstance().getSimpleBase(), null);
    }

    public StepConfig getStepConfig() {
        return stepConfig;
    }

}