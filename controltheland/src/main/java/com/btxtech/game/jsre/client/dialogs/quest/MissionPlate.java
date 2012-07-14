package com.btxtech.game.jsre.client.dialogs.quest;

import com.btxtech.game.jsre.client.dialogs.DialogManager;
import com.btxtech.game.jsre.client.dialogs.YesNoDialog;
import com.btxtech.game.jsre.client.utg.ClientLevelHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MissionPlate extends Composite {
    private static MissionPlateUiBinder uiBinder = GWT.create(MissionPlateUiBinder.class);
    private int missionId;
    private QuestDialog questDialog;
    @UiField
    Button button;
    @UiField
    Label titel;
    @UiField
    Label rewards;
    @UiField
    HTML description;

    interface MissionPlateUiBinder extends UiBinder<Widget, MissionPlate> {
    }

    public MissionPlate(QuestInfo info, QuestDialog questDialog) {
        this.questDialog = questDialog;
        initWidget(uiBinder.createAndBindUi(this));
        missionId = info.getId();
        titel.setText(info.getTitle());
        rewards.setText("Rewards: " + info.getGold() + " Gold, " + info.getXp() + " Xp");
        description.setHTML(info.getDescription());
    }

    @UiHandler("button")
    void onButtonClick(ClickEvent event) {
        if (ClientLevelHandler.getInstance().hasActiveQuest()) {
            ClickHandler clickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    questDialog.close();
                    ClientLevelHandler.getInstance().startMission(missionId);
                }
            };
            DialogManager.showDialog(new YesNoDialog("Activate Quest", "You have an active quest. Activate a new quest will abort the current quest.", "Activate", clickHandler, "Cancel", null), DialogManager.Type.STACK_ABLE);
        } else {
            questDialog.close();
            ClientLevelHandler.getInstance().startMission(missionId);
        }
    }
}