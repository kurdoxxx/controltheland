package com.btxtech.game.jsre.client;

import com.btxtech.game.jsre.client.common.ChatMessage;

/**
 * User: beat
 * Date: 03.04.2012
 * Time: 17:18:44
 */
public interface ConnectionI {
    void sendChatMessage(ChatMessage chatMessage);

    void pollChatMessages(Integer lastMessageId);

    GameEngineMode getGameEngineMode();
}