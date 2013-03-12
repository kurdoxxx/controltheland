package com.btxtech.game.wicket.pages.cms.content.plugin.emailverification;

import com.btxtech.game.jsre.client.AdCellHelper;
import com.btxtech.game.jsre.client.AdCellProvision;
import com.btxtech.game.wicket.uiservices.ExternalImage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.template.PackagedTextTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * User: beat
 * Date: 25.01.13
 * Time: 13:19
 */
public class AdCellProvisionPanel extends Panel {
    public AdCellProvisionPanel(String id, AdCellProvision adCellProvision) {
        super(id);
        PackagedTextTemplate jsTemplate = new PackagedTextTemplate(EmailVerificationPanel.class, "AdCellProvisionCode.js");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("BID", adCellProvision.getBid());
        parameters.put("REFERENCE", adCellProvision.getSimpleUser().getId());
        add(new Label("adCellScript", new Model<>(jsTemplate.asString(parameters))).setEscapeModelStrings(false));
        add(new ExternalImage("adCellImage", AdCellHelper.buildAdCellImageUrl(adCellProvision.getSimpleUser().getId(), adCellProvision.getBid())));
    }
}