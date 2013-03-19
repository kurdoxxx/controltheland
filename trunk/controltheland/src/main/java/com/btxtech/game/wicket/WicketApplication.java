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

package com.btxtech.game.wicket;

import com.btxtech.game.jsre.client.common.Constants;
import com.btxtech.game.jsre.common.CmsUtil;
import com.btxtech.game.jsre.common.CommonJava;
import com.btxtech.game.services.cms.NoDbContentInCacheException;
import com.btxtech.game.services.cms.NoDbPageException;
import com.btxtech.game.services.common.ExceptionHandler;
import com.btxtech.game.services.common.NoSuchChildException;
import com.btxtech.game.services.common.Utils;
import com.btxtech.game.services.mgmt.MgmtService;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.wicket.pages.Game;
import com.btxtech.game.wicket.pages.cms.CmsCssResource;
import com.btxtech.game.wicket.pages.cms.CmsImageResource;
import com.btxtech.game.wicket.pages.cms.CmsItemTypeImageResource;
import com.btxtech.game.wicket.pages.cms.CmsPage;
import com.btxtech.game.wicket.uiservices.InventoryImageResource;
import com.btxtech.game.wicket.uiservices.cms.CmsUiService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * User: beat Date: May 31, 2009 Time: 9:51:34 PM
 */
@Component
public class WicketApplication extends AuthenticatedWebApplication implements ApplicationContextAware {
    @Autowired
    private CmsUiService cmsUiService;
    @Autowired
    private UserService userService;
    @Autowired
    private MgmtService mgmtService;
    private String configurationType;
    private Log log = LogFactory.getLog(WicketApplication.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void init() {
        super.init();
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
        getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
        addComponentInstantiationListener(new SpringComponentInjector(this, applicationContext, true));
        getApplicationSettings().setAccessDeniedPage(CmsPage.class);
        getSharedResources().add(CmsCssResource.CMS_SHARED_CSS_RESOURCES, new CmsCssResource());
        getSharedResources().add(CmsImageResource.CMS_SHARED_IMAGE_RESOURCES, new CmsImageResource());
        getSharedResources().add(CmsItemTypeImageResource.CMS_SHARED_IMAGE_RESOURCES, new CmsItemTypeImageResource());
        getSharedResources().add(InventoryImageResource.SHARED_IMAGE_RESOURCES, new InventoryImageResource());
        mountSharedResource(CmsImageResource.PATH, Application.class.getName() + "/" + CmsImageResource.CMS_SHARED_IMAGE_RESOURCES);
        mountSharedResource(CmsItemTypeImageResource.PATH, Application.class.getName() + "/" + CmsItemTypeImageResource.CMS_SHARED_IMAGE_RESOURCES);
        mountSharedResource(Constants.INVENTORY_PATH, Application.class.getName() + "/" + InventoryImageResource.SHARED_IMAGE_RESOURCES);
        mountBookmarkablePage(CmsUtil.MOUNT_GAME_CMS, CmsPage.class);
        mountBookmarkablePage(CmsUtil.MOUNT_GAME, Game.class);
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return WicketAuthenticatedWebSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return CmsPage.class;
    }

    public Class<CmsPage> getHomePage() {
        return CmsPage.class;
    }

    @Override
    public String getConfigurationType() {
        if (configurationType == null) {
            if (Utils.isTestModeStatic()) {
                configurationType = Application.DEVELOPMENT;
            } else {
                configurationType = Application.DEPLOYMENT;
            }
        }
        return configurationType;
    }

    public final RequestCycle newRequestCycle(final Request request, final Response response) {
        if (Utils.isTestModeStatic()) {
            return super.newRequestCycle(request, response);
        } else {
            return new MyRequestCycle(this, (WebRequest) request, response);
        }
    }

    public final class MyRequestCycle extends WebRequestCycle {

        public MyRequestCycle(final WebApplication application, final WebRequest request, final Response response) {
            super(application, request, response);
        }

        @Override
        public final Page onRuntimeException(final Page cause, final RuntimeException e) {
            if (e instanceof PageExpiredException) {
                log.error("------------------PageExpiredException---------------------------------");
                ExceptionHandler.logParameters(log, userService);
                log.error("URL: " + getRequest().getURL());
                log.error("Page: " + cause);
                log.error(e.getMessage());
                return cmsUiService.getPredefinedNotFound();
            } else if (CommonJava.getMostInnerThrowable(e) instanceof NoSuchChildException) {
                saveServerDebug(cause, e);
                return cmsUiService.getPredefinedNotFound();
            } else if (CommonJava.getMostInnerThrowable(e) instanceof InvalidUrlException) {
                saveServerDebug(cause, e);
                return cmsUiService.getPredefinedNotFound();
            } else if (CommonJava.getMostInnerThrowable(e) instanceof NoDbContentInCacheException) {
                saveServerDebug(cause, e);
                return cmsUiService.getPredefinedNotFound();
            } else if (CommonJava.getMostInnerThrowable(e) instanceof NoDbPageException) {
                saveServerDebug(cause, e);
                return cmsUiService.getPredefinedNotFound();
            } else if (CommonJava.getMostInnerThrowable(e) instanceof NumberFormatException) {
                saveServerDebug(cause, e);
                return cmsUiService.getPredefinedNotFound();
            } else {
                log.error("------------------CMS Unknown Exception---------------------------------");
                ExceptionHandler.logParameters(log, userService);
                log.error("URL: " + getRequest().getURL());
                log.error("Page: " + cause);
                log.error("", e);
                return new CmsPage(new PageParameters());
            }
        }

        private void saveServerDebug(final Page cause, Exception e) {
            mgmtService.saveServerDebug(MgmtService.SERVER_DEBUG_CMS, ((WebRequest)getRequest()).getHttpServletRequest(), cause, e);
        }

        @Override
        protected void logRuntimeException(RuntimeException e) {
            // Do nothing. onRuntimeException() logs the exception
        }
    }

}
