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

package com.btxtech.game.jsre.client.control;

/**
 * User: beat
 * Date: 06.12.2010
 * Time: 22:03:20
 */
public enum GameStartupSeq implements StartupSeq {
    COLD_REAL(true) {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return ColdRealGameStartupTaskEnum.values();
        }},
    COLD_SIMULATED(true) {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return ColdSimulatedGameStartupTaskEnum.values();
        }},
    WARM_REAL(false) {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WarmRealGameStartupTaskEnum.values();
        }},
    WARM_SIMULATED(false) {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WarmSimulatedGameStartupTaskEnum.values();
        }};
    private boolean cold;

    GameStartupSeq(boolean isCold) {
        cold = isCold;
    }

    public boolean isCold() {
        return cold;
    }

    @Override
    public boolean isBackEndMode() {
        return false;
    }

    public abstract StartupTaskEnum[] getAbstractStartupTaskEnum();
}
