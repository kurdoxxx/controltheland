package com.btxtech.game.jsre.client.cockpit.menu;

import com.btxtech.game.jsre.client.ClientI18nHelper;
import com.btxtech.game.jsre.client.Connection;
import com.btxtech.game.jsre.client.GwtCommon;
import com.btxtech.game.jsre.client.SimpleUser;
import com.btxtech.game.jsre.client.dialogs.DialogManager;
import com.btxtech.game.jsre.client.dialogs.LoginDialog;
import com.btxtech.game.jsre.client.dialogs.RegisterDialog;
import com.btxtech.game.jsre.client.dialogs.YesNoDialog;
import com.btxtech.game.jsre.common.CmsUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

public class MenuBarPanel extends Composite {

    private static MenuBarPanelUiBinder uiBinder = GWT.create(MenuBarPanelUiBinder.class);
    @UiField
    MenuItem register;
    @UiField
    MenuItem login;
    @UiField
    MenuItem registerSubMenuBar;
    @UiField
    MenuItem logout;
    @UiField
    MenuItem messages;
    @UiField
    MenuItem news;
    @UiField
    MenuItem newBase;

    interface MenuBarPanelUiBinder extends UiBinder<Widget, MenuBarPanel> {
    }

    public MenuBarPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        logout.setVisible(false);

        register.setCommand(new Command() {

            @Override
            public void execute() {
                DialogManager.showDialog(new RegisterDialog(), DialogManager.Type.PROMPTLY);
            }
        });
        login.setCommand(new Command() {

            @Override
            public void execute() {
                DialogManager.showDialog(new LoginDialog(), DialogManager.Type.PROMPTLY);
            }
        });
        logout.setCommand(new Command() {

            @Override
            public void execute() {
                DialogManager.showDialog(new YesNoDialog(ClientI18nHelper.CONSTANTS.logout(),
                        ClientI18nHelper.CONSTANTS.logoutText(Connection.getInstance().getSimpleUser().getName()),
                        ClientI18nHelper.CONSTANTS.logout(),
                        new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                Connection.getInstance().logout();
                                Window.Location.replace(GwtCommon.getPredefinedUrl(CmsUtil.CmsPredefinedPage.HOME));
                            }
                        },
                        ClientI18nHelper.CONSTANTS.cancel(),
                        null
                ), DialogManager.Type.PROMPTLY);
            }
        });
        messages.setCommand(new Command() {

            @Override
            public void execute() {
                newBase.addStyleName("gwt-MenuBar-blink");
            }
        });
        news.setEnabled(false);
        newBase.setCommand(new Command() {

            @Override
            public void execute() {
                newBase.removeStyleName("gwt-MenuBar-blink");
            }
        });
        setSimpleUser(null);
    }

    public void setSimpleUser(SimpleUser simpleUser) {
        if (simpleUser != null) {
            logout.setVisible(true);
            registerSubMenuBar.setVisible(false);
        } else {
            logout.setVisible(false);
            registerSubMenuBar.setVisible(true);
        }
    }

}
