package com.btxtech.game.jsre.client.dialogs;

import com.btxtech.game.jsre.client.ClientI18nHelper;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * User: beat
 * Date: 20.05.13
 * Time: 13:40
 */
public class HistoryDialog extends Dialog {
    public HistoryDialog() {
        super(ClientI18nHelper.CONSTANTS.historyDialogTitle());
    }

    @Override
    protected void setupPanel(VerticalPanel dialogVPanel) {
    }
}
