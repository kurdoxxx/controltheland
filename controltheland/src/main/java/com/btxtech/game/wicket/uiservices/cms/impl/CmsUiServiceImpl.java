package com.btxtech.game.wicket.uiservices.cms.impl;

import com.btxtech.game.jsre.common.CmsPredefinedPageDoesNotExistException;
import com.btxtech.game.jsre.common.CmsUtil;
import com.btxtech.game.services.cms.CmsService;
import com.btxtech.game.services.cms.DataProviderInfo;
import com.btxtech.game.services.cms.DbContent;
import com.btxtech.game.services.cms.DbContentActionButton;
import com.btxtech.game.services.cms.DbContentBook;
import com.btxtech.game.services.cms.DbContentBooleanExpressionImage;
import com.btxtech.game.services.cms.DbContentContainer;
import com.btxtech.game.services.cms.DbContentCreateEdit;
import com.btxtech.game.services.cms.DbContentDetailLink;
import com.btxtech.game.services.cms.DbContentDynamicHtml;
import com.btxtech.game.services.cms.DbContentGameLink;
import com.btxtech.game.services.cms.DbContentInvoker;
import com.btxtech.game.services.cms.DbContentInvokerButton;
import com.btxtech.game.services.cms.DbContentList;
import com.btxtech.game.services.cms.DbContentPageLink;
import com.btxtech.game.services.cms.DbContentPlugin;
import com.btxtech.game.services.cms.DbContentSmartPageLink;
import com.btxtech.game.services.cms.DbContentStaticHtml;
import com.btxtech.game.services.cms.DbExpressionProperty;
import com.btxtech.game.services.cms.DbPage;
import com.btxtech.game.services.cms.EditMode;
import com.btxtech.game.services.common.ContentProvider;
import com.btxtech.game.services.common.CrudChild;
import com.btxtech.game.services.connection.NoConnectionException;
import com.btxtech.game.services.connection.Session;
import com.btxtech.game.services.item.itemType.DbItemType;
import com.btxtech.game.services.user.DbContentAccessControl;
import com.btxtech.game.services.user.DbPageAccessControl;
import com.btxtech.game.services.user.UserService;
import com.btxtech.game.wicket.WebCommon;
import com.btxtech.game.wicket.WicketApplication;
import com.btxtech.game.wicket.pages.cms.CmsPage;
import com.btxtech.game.wicket.pages.cms.ItemTypeImage;
import com.btxtech.game.wicket.pages.cms.Message;
import com.btxtech.game.wicket.pages.cms.WritePanel;
import com.btxtech.game.wicket.pages.cms.content.ContentActionButton;
import com.btxtech.game.wicket.pages.cms.content.ContentBook;
import com.btxtech.game.wicket.pages.cms.content.ContentBooleanExpressionImage;
import com.btxtech.game.wicket.pages.cms.content.ContentContainer;
import com.btxtech.game.wicket.pages.cms.ContentContext;
import com.btxtech.game.wicket.pages.cms.content.ContentCreateEdit;
import com.btxtech.game.wicket.pages.cms.content.ContentDetailLink;
import com.btxtech.game.wicket.pages.cms.content.ContentDynamicHtml;
import com.btxtech.game.wicket.pages.cms.content.ContentGameLink;
import com.btxtech.game.wicket.pages.cms.content.ContentInvoker;
import com.btxtech.game.wicket.pages.cms.content.ContentInvokerButton;
import com.btxtech.game.wicket.pages.cms.content.ContentList;
import com.btxtech.game.wicket.pages.cms.content.ContentPageLink;
import com.btxtech.game.wicket.pages.cms.content.ContentSmartPageLink;
import com.btxtech.game.wicket.uiservices.BeanIdPathElement;
import com.btxtech.game.wicket.uiservices.cms.CmsUiService;
import com.btxtech.game.wicket.uiservices.cms.SecurityCmsUiService;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebRequest;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: beat
 * Date: 09.06.2011
 * Time: 16:55:27
 */
@org.springframework.stereotype.Component("cmsUiService")
@DependsOn("cmsService")
public class CmsUiServiceImpl implements CmsUiService {
    public static final String REQUEST_TMP_CREATE_BEAN_ATTRIBUTES = "btxtech.game.tmpCreateBeanAttributes";
    private static final String CURRENT_PATH = ".";
    @Autowired
    private CmsService cmsService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UserService userService;
    @Autowired
    private Session session;
    @Autowired
    private SecurityCmsUiService securityCmsUiService;
    private HibernateTemplate hibernateTemplate;
    private Log log = LogFactory.getLog(CmsUiServiceImpl.class);
    private Map<CmsUtil.CmsPredefinedPage, String> predefinedUrls = new HashMap<CmsUtil.CmsPredefinedPage, String>();

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    @Override
    public Map<CmsUtil.CmsPredefinedPage, String> getPredefinedUrls() {
        return predefinedUrls;
    }

    @Override
    public void setupPredefinedUrls() {
        predefinedUrls.clear();
        for (CmsUtil.CmsPredefinedPage predefinedType : CmsUtil.CmsPredefinedPage.values()) {
            if (cmsService.hasPredefinedDbPage(predefinedType)) {
                predefinedUrls.put(predefinedType, getUrl4CmsPage(predefinedType));
            } else {
                log.warn("Predefined page does not does not exist: " + predefinedType);
            }
        }
    }

    private String getUrl4CmsPage(CmsUtil.CmsPredefinedPage predefinedType) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(WicketApplication.MOUNT_GAME_CMS);
            builder.append('/');
            builder.append(CmsPage.ID);
            builder.append('/');
            builder.append(cmsService.getPredefinedDbPage(predefinedType).getId());
            return builder.toString();
        } catch (CmsPredefinedPageDoesNotExistException e) {
            log.error("", e);
            return "";
        }
    }

    @Override
    public PageParameters getPredefinedDbPageParameters(CmsUtil.CmsPredefinedPage predefinedType) {
        PageParameters pageParameters = new PageParameters();
        try {
            pageParameters.put(CmsPage.ID, Integer.toString(cmsService.getPredefinedDbPage(predefinedType).getId()));
        } catch (CmsPredefinedPageDoesNotExistException e) {
            log.error("", e);
        }
        return pageParameters;
    }

    @Override
    public PageParameters createPageParametersFromBeanId(BeanIdPathElement beanIdPathElement) {
        PageParameters pageParameters = new PageParameters();
        pageParameters.put(CmsPage.ID, Integer.toString(beanIdPathElement.getPageId()));
        List<Serializable> beanIds = new ArrayList<Serializable>();
        BeanIdPathElement tmpBeanIdPathElement = beanIdPathElement;
        while (tmpBeanIdPathElement != null) {
            if (tmpBeanIdPathElement.hasBeanId()) {
                if (beanIds.size() >= CmsPage.MAX_LEVELS) {
                    throw new IllegalStateException("Max level reached");
                }
                beanIds.add(tmpBeanIdPathElement.getBeanId());
            }
            if (tmpBeanIdPathElement.hasParent() && !tmpBeanIdPathElement.getParent().hasSpringBeanName()) {
                tmpBeanIdPathElement = tmpBeanIdPathElement.getParent();
            } else {
                tmpBeanIdPathElement = null;
            }
        }
        Collections.reverse(beanIds);
        for (int level = 0, beanIdsSize = beanIds.size(); level < beanIdsSize; level++) {
            Serializable beanId = beanIds.get(level);
            pageParameters.put(CmsPage.getChildUrlParameter(level), beanId);
        }
        return pageParameters;
    }


    @Override
    public void setPredefinedResponsePage(Component component, CmsUtil.CmsPredefinedPage predefinedType) {
        component.setResponsePage(CmsPage.class, getPredefinedDbPageParameters(predefinedType));
    }

    @Override
    public void setMessageResponsePage(Component component, String message) {
        PageParameters pageParameters = getPredefinedDbPageParameters(CmsUtil.CmsPredefinedPage.MESSAGE);
        pageParameters.put(CmsPage.MESSAGE_ID, message);
        component.setResponsePage(CmsPage.class, pageParameters);
    }

    @Override
    public void setResponsePage(Component component, int dbPageId) {
        PageParameters pageParameters = new PageParameters();
        pageParameters.put(CmsPage.ID, Integer.toString(dbPageId));
        component.setResponsePage(CmsPage.class, pageParameters);
    }

    @Override
    public void setInvokerResponsePage(Component component, int dbPageId, DbContentInvoker dbContentInvoker) {
        PageParameters parameters = new PageParameters();
        parameters.put(CmsPage.ID, Integer.toString(dbPageId));
        parameters.put(CmsPage.INVOKE_ID, Integer.toString(dbContentInvoker.getId()));
        component.setResponsePage(CmsPage.class, parameters);
    }

    @Override
    public void setParentResponsePage(Component component, DbContent dbContent, BeanIdPathElement beanIdPathElement) {
        PageParameters parameters = createPageParametersFromBeanId(beanIdPathElement);
        // Find out if parent was a detail link
        if (dbContent.getParent() instanceof DbContentList) {
            // ??? Why -> dbContent.getParent().getParent().getParent().getParent()
            parameters.put(CmsPage.DETAIL_CONTENT_ID, Integer.toString(dbContent.getParent().getParent().getParent().getParent().getId()));
        }
        component.setResponsePage(CmsPage.class, parameters);
    }

    @Override
    public Component getRootComponent(DbPage dbPage, String componentId, ContentContext contentContext) {
        DbContent dbContent = dbPage.getContent();
        BeanIdPathElement beanIdPathElement = new BeanIdPathElement(dbPage, dbContent);
        PageParameters pageParameters = contentContext.getPageParameters();
        // if the Page should display a child of a ContentList
        if (pageParameters.containsKey(CmsPage.DETAIL_CONTENT_ID)) {
            dbContent = cmsService.getDbContent(pageParameters.getInt(CmsPage.DETAIL_CONTENT_ID));
            beanIdPathElement = createBeanIdPathElement(pageParameters, dbContent, beanIdPathElement);
            beanIdPathElement.setChildDetailPage(true);
            Object bean = getDataProviderBean(beanIdPathElement);
            dbContent = ((DbContentList) dbContent).getDbPropertyBook(bean.getClass().getName());
        } else if (pageParameters.containsKey(CmsPage.CREATE_CONTENT_ID)) {
            dbContent = cmsService.getDbContent(pageParameters.getInt(CmsPage.CREATE_CONTENT_ID));
            beanIdPathElement = createBeanIdPathElement(pageParameters, dbContent, beanIdPathElement);
            beanIdPathElement.setCreateEditPage(true);
        } else if (pageParameters.containsKey(CmsPage.INVOKE_ID)) {
            dbContent = cmsService.getDbContent(pageParameters.getInt(CmsPage.INVOKE_ID));
            beanIdPathElement.setInvokePage(true);
        } else if (pageParameters.containsKey(CmsPage.MESSAGE_ID)) {
            return new Message(componentId, pageParameters.getString(CmsPage.MESSAGE_ID));
        }
        return getComponent(dbContent, null, componentId, beanIdPathElement, contentContext);
    }

    private BeanIdPathElement createBeanIdPathElement(PageParameters pageParameters, DbContent dbContent, BeanIdPathElement beanIdPathElement) {
        DbContent nearestSpringBeanComponent = dbContent;
        List<DbContent> contentPath = new ArrayList<DbContent>();
        contentPath.add(dbContent);
        while (nearestSpringBeanComponent != null && nearestSpringBeanComponent.getSpringBeanName() == null) {
            nearestSpringBeanComponent = nearestSpringBeanComponent.getParent();
            if (nearestSpringBeanComponent != null) {
                contentPath.add(nearestSpringBeanComponent);
            }
        }
        Collections.reverse(contentPath);
        if (nearestSpringBeanComponent == null) {
            throw new IllegalArgumentException("No Spring Bean for detail component found: " + dbContent);
        }
        List<Integer> beanIds = new ArrayList<Integer>();
        for (int level = 0; level < CmsPage.MAX_LEVELS; level++) {
            if (pageParameters.containsKey(CmsPage.getChildUrlParameter(level))) {
                beanIds.add(pageParameters.getInt(CmsPage.getChildUrlParameter(level)));
            }
        }

        for (DbContent content : contentPath) {
            if (content instanceof DataProviderInfo) {
                if (content.getSpringBeanName() != null) {
                    beanIdPathElement = beanIdPathElement.createChildFromDataProviderInfo((DataProviderInfo) content);
                } else if (content.getContentProviderGetter() != null) {
                    beanIdPathElement = beanIdPathElement.createChildFromBeanId(beanIds.remove(0));
                    beanIdPathElement = beanIdPathElement.createChildFromDataProviderInfo((DataProviderInfo) content);
                } else if (content.getExpression() != null) {
                    beanIdPathElement = beanIdPathElement.createChildFromDataProviderInfo((DataProviderInfo) content);
                }
            }
        }

        if (beanIds.size() == 1) {
            beanIdPathElement = beanIdPathElement.createChildFromBeanId(beanIds.remove(0));
        } else if (beanIds.size() > 1) {
            throw new IllegalStateException("Bean Id mismatch: " + beanIds.size());
        }
        return beanIdPathElement;
    }

    @Override
    public <T extends DbContent> T getDbContent(int contentId) {
        return (T) cmsService.getDbContent(contentId);
    }

    @Override
    public Component getComponent(DbContent dbContent, Object bean, String componentId, BeanIdPathElement beanIdPathElement, ContentContext contentContext) {
        try {
            if (dbContent instanceof DbContentList) {
                return new ContentList(componentId, (DbContentList) dbContent, beanIdPathElement, contentContext);
            } else if (dbContent instanceof DbExpressionProperty) {
                return component4ExpressionProperty(componentId, ((DbExpressionProperty) dbContent), bean, beanIdPathElement);
            } else if (dbContent instanceof DbContentDetailLink) {
                return new ContentDetailLink(componentId, (DbContentDetailLink) dbContent, beanIdPathElement);
            } else if (dbContent instanceof DbContentContainer) {
                return new ContentContainer(componentId, (DbContentContainer) dbContent, beanIdPathElement, contentContext);
            } else if (dbContent instanceof DbContentBook) {
                return new ContentBook(componentId, (DbContentBook) dbContent, beanIdPathElement, contentContext);
            } else if (dbContent instanceof DbContentStaticHtml) {
                DbContentStaticHtml dbContentStaticHtml = (DbContentStaticHtml) dbContent;
                Component label = new Label(componentId, dbContentStaticHtml.getHtml()).setEscapeModelStrings(dbContentStaticHtml.getEscapeMarkup());
                if (dbContent.getCssClass() != null) {
                    label.add(new SimpleAttributeModifier("class", dbContent.getCssClass()));
                }
                return label;
            } else if (dbContent instanceof DbContentDynamicHtml) {
                return new ContentDynamicHtml(componentId, (DbContentDynamicHtml) dbContent);
            } else if (dbContent instanceof DbContentPageLink) {
                return new ContentPageLink(componentId, (DbContentPageLink) dbContent);
            } else if (dbContent instanceof DbContentGameLink) {
                return new ContentGameLink(componentId, (DbContentGameLink) dbContent);
            } else if (dbContent instanceof DbContentPlugin) {
                Component component = ((DbContentPlugin) dbContent).getPluginEnum().createComponent(componentId);
                if (dbContent.getCssClass() != null) {
                    component.add(new SimpleAttributeModifier("class", dbContent.getCssClass()));
                }
                return component;
            } else if (dbContent instanceof DbContentActionButton) {
                return new ContentActionButton(componentId, (DbContentActionButton) dbContent, beanIdPathElement);
            } else if (dbContent instanceof DbContentCreateEdit) {
                return new ContentCreateEdit(componentId, (DbContentCreateEdit) dbContent, beanIdPathElement);
            } else if (dbContent instanceof DbContentSmartPageLink) {
                return new ContentSmartPageLink(componentId, (DbContentSmartPageLink) dbContent);
            } else if (dbContent instanceof DbContentBooleanExpressionImage) {
                return new ContentBooleanExpressionImage(componentId, (DbContentBooleanExpressionImage) dbContent, beanIdPathElement);
            } else if (dbContent instanceof DbContentInvokerButton) {
                return new ContentInvokerButton(componentId, (DbContentInvokerButton) dbContent, beanIdPathElement);
            } else if (dbContent instanceof DbContentInvoker) {
                return new ContentInvoker(componentId, (DbContentInvoker) dbContent, beanIdPathElement);
            } else {
                log.warn("CmsUiServiceImpl: No Wicket Component for content: " + dbContent);
                return new Label(componentId, "No content");
            }
        } catch (Exception e) {
            log.error("DbContent: " + dbContent + " bean: " + bean + " id: " + componentId + " " + beanIdPathElement, e);
            return new Label(componentId, "Error!");
        }
    }

    private Component component4ExpressionProperty(String id, DbExpressionProperty dbExpressionProperty, Object bean, BeanIdPathElement beanIdPathElement) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object value;
        try {
            value = getValue(dbExpressionProperty, bean, beanIdPathElement);
        } catch (NestedNullException e) {
            return new Label(id, "-");
        } catch (NoSuchMethodException ie) {
            log.warn("NoSuchMethodException: bean: " + bean + " expression: " + dbExpressionProperty.getExpression());
            throw ie;
        } catch (InvocationTargetException ie) {
            if (ie.getTargetException() instanceof NoConnectionException) {
                return new Label(id, "No Base");
            } else {
                throw ie;
            }
        }
        Component component;
        if (value instanceof DbItemType) {
            component = new ItemTypeImage(id, (DbItemType) value);
        } else {
            if (getEditMode(dbExpressionProperty) != null
                    && !dbExpressionProperty.getExpression().equals(CURRENT_PATH)
                    && PropertyUtils.isWriteable(bean, dbExpressionProperty.getExpression())) {
                // Write
                component = new WritePanel(id, value, beanIdPathElement, dbExpressionProperty);
            } else {
                // Read only
                if (value != null) {
                    String stringValue;
                    if (dbExpressionProperty.getOptionalType() != null) {
                        stringValue = typeToString(value, dbExpressionProperty.getOptionalType());
                    } else {
                        stringValue = value.toString();
                    }
                    component = new Label(id, stringValue);
                    component.setVisible(isReadAllowed(dbExpressionProperty.getId()));
                    component.setEscapeModelStrings(dbExpressionProperty.getEscapeMarkup());
                } else {
                    component = new Label(id, "");
                }
            }
        }
        if (dbExpressionProperty.getCssClass() != null) {
            component.add(new SimpleAttributeModifier("class", dbExpressionProperty.getCssClass()));
        }
        return component;
    }

    private String typeToString(Object value, DbExpressionProperty.Type type) {
        switch (type) {
            case DATE_DDMMYYYY_HH_MM_SS:
                if (value instanceof Date) {
                    return WebCommon.formatDateTime((Date) value);
                } else if (value instanceof Number) {
                    return WebCommon.formatDateTime(new Date(((Number) value).longValue()));
                } else {
                    throw new IllegalArgumentException("Date value must be Number ore Date: " + value);
                }
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private Object getValue(DataProviderInfo dataProviderInfo, Object bean, BeanIdPathElement beanIdPathElement) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (dataProviderInfo.getExpression().equals(CURRENT_PATH)) {
            return bean;
        } else {
            if (dataProviderInfo.getSpringBeanName() != null) {
                bean = getDataProviderBean(beanIdPathElement);
            }
            return PropertyUtils.getProperty(bean, beanIdPathElement.getExpression());
        }
    }

    @Override
    public Object getValue(String springBeanName, String propertyExpression) {
        try {
            Object bean = applicationContext.getBean(springBeanName);
            return PropertyUtils.getProperty(bean, propertyExpression);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getDataProviderBean(BeanIdPathElement beanIdPathElement) {
        try {
            if (beanIdPathElement.isCreateEditPage()) {
                return null;
            } else if (beanIdPathElement.hasSpringBeanName()) {
                return applicationContext.getBean(beanIdPathElement.getSpringBeanName());
            } else if (beanIdPathElement.hasContentProviderGetter() && beanIdPathElement.hasBeanId() && beanIdPathElement.hasParent()) {
                Object bean = getDataProviderBean(beanIdPathElement.getParent());
                Method method = bean.getClass().getMethod(beanIdPathElement.getContentProviderGetter());
                ContentProvider contentProvider = (ContentProvider) method.invoke(bean);
                return contentProvider.readDbChild(beanIdPathElement.getBeanId());
            } else if (beanIdPathElement.hasBeanId() && beanIdPathElement.hasParent()) {
                ContentProvider contentProvider = getContentProvider(beanIdPathElement.getParent());
                return contentProvider.readDbChild(beanIdPathElement.getBeanId());
            } else if (beanIdPathElement.hasExpression() && beanIdPathElement.hasParent()) {
                Object bean = getDataProviderBean(beanIdPathElement.getParent());
                return PropertyUtils.getProperty(bean, beanIdPathElement.getExpression());
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDataProviderBean(Object value, BeanIdPathElement beanIdPathElement, int contentId) {
        if (!isWriteAllowed(contentId)) {
            throw new IllegalStateException("User not allowed to write property: " + contentId);
        }
        if (beanIdPathElement.isCreateEditPage()) {
            putCreateEditTmpBeanAttribute(beanIdPathElement, value);
        } else {
            try {
                Object object;
                if (beanIdPathElement.hasBeanId() && beanIdPathElement.hasExpression()) {
                    object = getDataProviderBean(beanIdPathElement);
                } else {
                    object = getDataProviderBean(beanIdPathElement.getParent());
                }
                PropertyUtils.setProperty(object, beanIdPathElement.getExpression(), value);
            } catch (Exception e) {
                throw new RuntimeException("value: " + value + " " + beanIdPathElement + " contentId: " + contentId, e);
            }
        }
    }


    @Override
    public List getDataProviderBeans(BeanIdPathElement beanIdPathElement) {
        try {
            ContentProvider contentProvider;
            if (beanIdPathElement.hasContentProviderGetter() && !beanIdPathElement.hasSpringBeanName() && !beanIdPathElement.hasBeanId()) {
                Object bean = getDataProviderBean(beanIdPathElement.getParent());
                contentProvider = getContentProvider(beanIdPathElement, bean);
            } else {
                contentProvider = getContentProvider(beanIdPathElement);
            }
            return new ArrayList(contentProvider.readDbChildren());
        } catch (NestedNullException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private ContentProvider getContentProvider(BeanIdPathElement beanIdPathElement, Object bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (ContentProvider) PropertyUtils.getProperty(bean, beanIdPathElement.getContentProviderGetter());
    }

    private ContentProvider getContentProvider(BeanIdPathElement beanIdPathElement) {
        try {
            if (beanIdPathElement.hasContentProviderGetter() && beanIdPathElement.hasSpringBeanName()) {
                Object bean = applicationContext.getBean(beanIdPathElement.getSpringBeanName());
                return getContentProvider(beanIdPathElement, bean);
            } else if (beanIdPathElement.hasContentProviderGetter() && beanIdPathElement.hasBeanId() && beanIdPathElement.hasParent()) {
                ContentProvider contentProvider = getContentProvider(beanIdPathElement.getParent());
                Object bean = contentProvider.readDbChild(beanIdPathElement.getBeanId());
                return getContentProvider(beanIdPathElement, bean);
            } else if (beanIdPathElement.hasContentProviderGetter() && beanIdPathElement.hasParent()) {
                Object bean = getDataProviderBean(beanIdPathElement.getParent());
                return getContentProvider(beanIdPathElement, bean);
            } else {
                throw new IllegalArgumentException(beanIdPathElement.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EditMode getEditMode(int contentId) {
        return getEditMode(cmsService.getDbContent(contentId));
    }

    private EditMode getEditMode(DbContent dbContent) {
        EditMode editMode = session.getEditMode();
        if (editMode == null) {
            return null;
        }
        String springBeanName = dbContent.getNextPossibleSpringBeanName();

        if (springBeanName.equals(editMode.getSpringBeanName())) {
            return editMode;
        } else {
            return null;
        }
    }

    @Override
    public void enterEditMode(int contentId, BeanIdPathElement beanIdPathElement) {
        DbContent dbContent = cmsService.getDbContent(contentId);
        String springBeanName;
        if (beanIdPathElement != null && beanIdPathElement.isChildDetailPage()) {
            springBeanName = dbContent.getNextPossibleSpringBeanName();
        } else {
            springBeanName = dbContent.getSpringBeanName();
        }
        if (springBeanName == null) {
            throw new IllegalArgumentException("No spring bean name in DbContent: " + dbContent);
        }
        EditMode editMode = new EditMode(springBeanName);
        session.setEditMode(editMode);
    }

    @Override
    public void leaveEditMode() {
        hibernateTemplate.clear();
        session.setEditMode(null);
    }

    @Override
    public boolean isEnterEditModeAllowed(int contentId, BeanIdPathElement beanIdPathElement) {
        if (getEditMode(contentId) != null) {
            // Already in edit mode
            return false;
        }
        if (!isWriteAllowed(contentId)) {
            return false;
        }
        if (beanIdPathElement == null) {
            return true;
        }
        DbContent dbContent = cmsService.getDbContent(contentId);
        return beanIdPathElement.isChildDetailPage() || (dbContent.getSpringBeanName() != null && isWriteAllowed(contentId));
    }

    @Override
    public boolean isSaveAllowed(int contentId, BeanIdPathElement beanIdPathElement) {
        if (getEditMode(contentId) == null) {
            return false;
        }
        DbContent dbContent = cmsService.getDbContent(contentId);
        return dbContent.getSpringBeanName() != null || beanIdPathElement.isChildDetailPage();
    }

    @Override
    public boolean isCreateEditAllowed(int contentCreateEditId) {
        DbContentCreateEdit dbContentCreateEdit = (DbContentCreateEdit) cmsService.getDbContent(contentCreateEditId);
        switch (dbContentCreateEdit.getCreateRestricted()) {
            case DENIED: {
                return false;
            }
            case ALLOWED: {
                return true;
            }
            case REGISTERED_USER: {
                return userService.isRegistered();
            }
            case USER: {
                Collection<DbContentAccessControl> dbContentAccessControls = userService.getDbContentAccessControls();
                if (dbContentAccessControls == null) {
                    return false;
                }
                for (DbContentAccessControl dbContentAccessControl : dbContentAccessControls) {
                    if (dbContentCreateEdit.getId().equals(dbContentAccessControl.getDbContent().getId())) {
                        return dbContentAccessControl.isCreateAllowed();
                    }
                }
                return false;
            }
            default:
                throw new IllegalArgumentException("Unknown create restriction: " + dbContentCreateEdit.getReadRestricted());
        }

    }

    private boolean isWriteAllowed(int contentId) {
        DbContent dbContent = cmsService.getDbContent(contentId);
        while (dbContent.getWriteRestricted() == DbContent.Access.INHERIT) {
            dbContent = dbContent.getParent();
            if (dbContent == null) {
                throw new IllegalStateException("Content has no sufficient write restriction configured: " + cmsService.getDbContent(contentId));
            }
        }

        switch (dbContent.getWriteRestricted()) {
            case DENIED: {
                return false;
            }
            case ALLOWED: {
                return true;
            }
            case REGISTERED_USER: {
                return userService.isRegistered();
            }
            case USER: {
                Collection<DbContentAccessControl> dbContentAccessControls = userService.getDbContentAccessControls();
                if (dbContentAccessControls == null) {
                    return false;
                }
                for (DbContentAccessControl dbContentAccessControl : dbContentAccessControls) {
                    if (dbContent.getId().equals(dbContentAccessControl.getDbContent().getId())) {
                        return dbContentAccessControl.isWriteAllowed();
                    }
                }
                return false;
            }
            default:
                throw new IllegalArgumentException("Unknown read restriction: " + dbContent.getReadRestricted());
        }
    }

    @Override
    public boolean isReadAllowed(int contentId) {
        DbContent dbContent = cmsService.getDbContent(contentId);
        while (dbContent.getReadRestricted() == DbContent.Access.INHERIT) {
            dbContent = dbContent.getParent();
            if (dbContent == null) {
                throw new IllegalStateException("Content has no sufficient read restriction configured: " + cmsService.getDbContent(contentId));
            }
        }

        switch (dbContent.getReadRestricted()) {
            case DENIED: {
                return false;
            }
            case ALLOWED: {
                return true;
            }
            case REGISTERED_USER: {
                return userService.isRegistered();
            }
            case USER: {
                Collection<DbContentAccessControl> dbContentAccessControls = userService.getDbContentAccessControls();
                if (dbContentAccessControls == null) {
                    return false;
                }
                for (DbContentAccessControl dbContentAccessControl : dbContentAccessControls) {
                    if (dbContent.getId().equals(dbContentAccessControl.getDbContent().getId())) {
                        return dbContentAccessControl.isReadAllowed();
                    }
                }
                return false;
            }
            default:
                throw new IllegalArgumentException("Unknown read restriction: " + dbContent.getReadRestricted());
        }
    }

    @Override
    public boolean isAllowedGeneric(DbContent.Access access) {
        if (access == null) {
            return false;
        }
        switch (access) {
            case DENIED: {
                return false;
            }
            case ALLOWED: {
                return true;
            }
            case REGISTERED_USER: {
                return userService.isRegistered();
            }
            case USER: {
                throw new UnsupportedOperationException(DbContent.Access.USER + " access is not supported");
            }
            case INHERIT: {
                throw new UnsupportedOperationException(DbContent.Access.INHERIT + " access is not supported");
            }
            default:
                throw new IllegalArgumentException("Unknown access: " + access);
        }
    }

    @Override
    public CrudChild createBean(BeanIdPathElement beanIdPathElement) {
        ContentProvider contentProvider = getContentProvider(beanIdPathElement);
        return contentProvider.createDbChild(userService);
    }

    @Override
    public CrudChild createAndFillBean(BeanIdPathElement beanIdPathElement) {
        CrudChild crudChild = createBean(beanIdPathElement);
        Map<String, Object> attributeMap = (Map<String, Object>) ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest().getAttribute(REQUEST_TMP_CREATE_BEAN_ATTRIBUTES);
        if (attributeMap != null) {
            for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
                try {
                    PropertyUtils.setProperty(crudChild, entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // save root parent
        BeanIdPathElement root = beanIdPathElement.getFirstDbBeanElement();
        ContentProvider rootContentProvider = getContentProvider(root.getParent());
        CrudChild rootCrudChild = (CrudChild) getDataProviderBean(root);
        rootContentProvider.updateDbChild(rootCrudChild);

        return crudChild;
    }

    @Override
    public void deleteBean(BeanIdPathElement beanIdPathElement) {
        ContentProvider contentProvider = getContentProvider(beanIdPathElement.getParent());
        Object bean = getDataProviderBean(beanIdPathElement);
        contentProvider.deleteDbChild((CrudChild) bean);
    }

    @Override
    public void save(BeanIdPathElement beanIdPathElement) {
        hibernateTemplate.flush();
    }

    @Override
    public boolean isPageAccessAllowed(DbPage dbPage) {
        if (!dbPage.isAccessRestricted()) {
            return true;
        }
        Collection<DbPageAccessControl> dbPageAccessControls = userService.getDbPageAccessControls();
        if (dbPageAccessControls == null) {
            return false;
        }
        for (DbPageAccessControl dbPageAccessControl : dbPageAccessControls) {
            if (dbPage.equals(dbPageAccessControl.getDbPage())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void invokeCall(int contentId, BeanIdPathElement beanIdPathElement) {
        try {
            DbContentActionButton dbContentActionButton = (DbContentActionButton) cmsService.getDbContent(contentId);
            Object springBean = getDataProviderBean(beanIdPathElement);
            Object parameterHolder = getDataProviderBean(beanIdPathElement.getParent());
            Object parameter = PropertyUtils.getProperty(parameterHolder, dbContentActionButton.getParameterExpression());
            Method method = springBean.getClass().getMethod(dbContentActionButton.getMethodName(), parameter.getClass());
            method.invoke(springBean, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isConditionFulfilled(int contentId, BeanIdPathElement beanIdPathElement) {
        try {
            DbContentActionButton dbContentActionButton = (DbContentActionButton) cmsService.getDbContent(contentId);
            // Left side
            Object leftSideSpringBean = applicationContext.getBean(dbContentActionButton.getLeftSideSpringBeanName());
            Integer leftSideOperand = (Integer) PropertyUtils.getProperty(leftSideSpringBean, dbContentActionButton.getLeftSideOperandExpression());

            // Right side
            Object rightSideParameterHolder = getDataProviderBean(beanIdPathElement.getParent());
            Integer rightSideOperand = (Integer) PropertyUtils.getProperty(rightSideParameterHolder, dbContentActionButton.getRightSideOperandExpression());

            // Do decision -> leftSideOperand >= leftSideOperand
            return leftSideOperand >= rightSideOperand;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void putCreateEditTmpBeanAttribute(BeanIdPathElement beanIdPathElement, Object value) {
        Map<String, Object> attributeMap = (Map<String, Object>) ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest().getAttribute(REQUEST_TMP_CREATE_BEAN_ATTRIBUTES);
        if (attributeMap == null) {
            attributeMap = new HashMap<String, Object>();
            ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest().setAttribute(REQUEST_TMP_CREATE_BEAN_ATTRIBUTES, attributeMap);
        }
        attributeMap.put(beanIdPathElement.getExpression(), value);
    }

    @Override
    public SecurityCmsUiService getSecurityCmsUiService() {
        return securityCmsUiService;
    }

    @Override
    public BeanIdPathElement createChildBeanIdPathElement(DbContent childDbContent, BeanIdPathElement beanIdPathElement, CrudChild crudChild) {
        BeanIdPathElement childBeanIdPathElement = null;
        if (childDbContent instanceof DataProviderInfo) {
            if (childDbContent.getSpringBeanName() != null || childDbContent.getContentProviderGetter() != null || childDbContent.getExpression() != null) {
                if (crudChild != null) {
                    childBeanIdPathElement = beanIdPathElement.createChildFromBeanId(crudChild.getId());
                    childBeanIdPathElement = childBeanIdPathElement.createChildFromDataProviderInfo((DataProviderInfo) childDbContent);
                } else {
                    childBeanIdPathElement = beanIdPathElement.createChildFromDataProviderInfo((DataProviderInfo) childDbContent);
                }
            } else {
                if (crudChild != null) {
                    childBeanIdPathElement = beanIdPathElement.createChildFromBeanId(crudChild.getId());
                }
            }
        } else if (childDbContent instanceof DbContentDetailLink) {
            childBeanIdPathElement = beanIdPathElement.createChildFromBeanId(crudChild.getId());
        }
        if (childBeanIdPathElement == null) {
            childBeanIdPathElement = beanIdPathElement;
        }
        return childBeanIdPathElement;
    }

    @Override
    public void invoke(DbContentInvoker dbContentInvoker, HashMap<String, String> parameterMap) throws InvocationTargetException {
        try {
            Object bean = applicationContext.getBean(dbContentInvoker.getSpringBeanName());
            int parameterCount = dbContentInvoker.getValueCrud().readDbChildren().size();
            Class[] parameterType = new Class[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                parameterType[i] = String.class;
            }
            Method method = bean.getClass().getMethod(dbContentInvoker.getMethodName(), parameterType);
            Object[] parameters = new Object[parameterCount];
            for (int i = 0; i < parameters.length; i++) {
                String expression = dbContentInvoker.getValueCrud().readDbChildren().get(i).getExpression();
                parameters[i] = parameterMap.get(expression);
            }
            method.invoke(bean, parameters);
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void invokeHiddenMethod(DbContentBook dbContentBook, BeanIdPathElement beanIdPathElement) {
        if (dbContentBook.getHiddenMethodName() == null) {
            return;
        }
        try {
            Object parameter = getDataProviderBean(beanIdPathElement);
            Object bean = getDataProviderBean(beanIdPathElement.getParent());
            Method method = bean.getClass().getMethod(dbContentBook.getHiddenMethodName(), parameter.getClass());
            method.invoke(bean, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
