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

package com.btxtech.game.wicket.pages.cms;

import com.btxtech.game.services.cms.CmsService;
import com.btxtech.game.services.cms.DbPage;
import com.btxtech.game.wicket.uiservices.cms.CmsUiService;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.debug.PageView;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class CmsPage extends WebPage {
    public static final String ID = "id";
    private static final String CHILD_ID = "childId";
    public static final String DETAIL_CONTENT_ID = "detailId";
    public static final String CREATE_CONTENT_ID = "createId";
    @SpringBean
    private CmsService cmsService;
    @SpringBean
    private CmsUiService cmsUiService;
    private int pageId;
    public static final int MAX_LEVELS = 20;

    public CmsPage(final PageParameters pageParameters) {
        setDefaultModel(new CompoundPropertyModel<DbPage>(new LoadableDetachableModel<DbPage>() {

            @Override
            protected DbPage load() {
                DbPage dbPage;
                if (pageParameters.containsKey(ID)) {
                    pageId = pageParameters.getInt(ID);
                    dbPage = cmsService.getPage(pageId);
                } else {
                    dbPage = cmsService.getHomePage();
                    pageId = dbPage.getId();
                }
                return dbPage;
            }
        }));
        DbPage dbPage = (DbPage) getDefaultModelObject();
        add(CmsCssResource.createCss("css", dbPage));
        add(new Ads("right", dbPage));
        add(new Menu("menu"));
        add(new Header("header", dbPage));
        add(new Footer("footer", dbPage));
        Form form = new Form("form");
        add(form);
        form.add(cmsUiService.getRootComponent(dbPage, "content", pageParameters));
        //////////////////////
        add(new Link<Void>("displayPageViewLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                CmsPage.this.replace(new PageView("componentTree", CmsPage.this));
                setVisible(false);
            }
        });

        add(new Label("componentTree", ""));
        //////////////////////
    }

    @Override
    public boolean isVisible() {
        DbPage dbPage = (DbPage) getDefaultModelObject();
        return cmsUiService.isPageAccessAllowed(dbPage);
    }

    public static String getChildUrlParameter(int level) {
        if (level == 0) {
            return CHILD_ID;
        } else {
            return CHILD_ID + level;
        }
    }
}
