package com.btxtech.game.jsre.common.gameengine.services.bot;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.gameengine.services.bot.impl.BotRunner;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:26:43
 */
public interface CommonBotService {
    boolean isInRealm(Index point);

    BotRunner getBotRunner(BotConfig botConfig);    
}