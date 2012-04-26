package com.btxtech.game.services.user;

import com.btxtech.game.jsre.common.AllianceOfferPacket;
import com.btxtech.game.jsre.common.SimpleBase;

import java.util.Collection;

/**
 * User: beat
 * Date: 24.04.12
 * Time: 10:12
 */
public interface AllianceService {
    void proposeAlliance(SimpleBase partner);

    void acceptAllianceOffer(String partnerUserName);

    void rejectAllianceOffer(String partnerUserName);

    void breakAlliance(String partnerUserName);

    void restoreAlliances();

    Collection<AllianceOfferPacket> getPendingAllianceOffers();

    void onBaseCreatedOrDeleted(String userName);
}
