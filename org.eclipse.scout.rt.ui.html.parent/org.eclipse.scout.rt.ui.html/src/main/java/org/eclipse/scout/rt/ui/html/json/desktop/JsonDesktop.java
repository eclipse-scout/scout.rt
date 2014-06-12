/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigation;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigationService;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.service.SERVICES;
import org.json.JSONObject;

public class JsonDesktop extends AbstractJsonPropertyObserver<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktop.class);

  public static final String PROP_OUTLINE = "outline";
  public static final String PROP_FORM = "form";

  private DesktopListener m_desktopListener;

  private String TOOL_BUTTONS = "[" +
      "          {\"id\": \"t2\", \"objectType\": \"ToolButton\", \"text\": \"Zugriff\", \"icon\": \"\uf144\", \"shortcut\": \"F4\"}," +
      "          {\"id\": \"t3\", \"objectType\": \"ToolButton\", \"text\": \"Favoriten\", \"icon\": \"\uf005\", \"shortcut\": \"F6\"}," +
      "          {\"id\": \"t4\", \"objectType\": \"ToolButton\", \"text\": \"Muster\", \"icon\": \"\uf01C\", \"shortcut\": \"F7\", \"state\": \"disabled\"}," +
      "          {\"id\": \"t5\", \"objectType\": \"ToolButton\", \"text\": \"Telefon\", \"icon\": \"\uf095\", \"shortcut\": \"F8\"}," +
      "          {\"id\": \"t6\", \"objectType\": \"ToolButton\", \"text\": \"Cockpit\", \"icon\": \"\uf0E4\", \"shortcut\": \"F9\"}," +
      "          {\"id\": \"t7\", \"objectType\": \"ToolButton\", \"text\": \"Prozesse\", \"icon\": \"\uf0D0\",\"shortcut\": \"F10\"}]}]";

  public JsonDesktop(IDesktop desktop, IJsonSession jsonSession, String id) {
    super(desktop, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "Desktop";
  }

  @Override
  protected void attachModel() {
    if (!getModel().isOpened()) {
      getModel().getUIFacade().fireDesktopOpenedFromUI();
    }
    if (!getModel().isGuiAvailable()) {
      getModel().getUIFacade().fireGuiAttached();
    }

    //FIXME add listener afterwards -> don't handle events, refactor
    super.attachModel();

    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
      getModel().addDesktopListener(m_desktopListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_desktopListener != null) {
      getModel().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();

    List<IForm> modelForms = getForms();
    putProperty(json, "forms", modelsToJson(modelForms));
    putProperty(json, "toolButtons", modelsToJson(getModel().getToolButtons()));

    boolean formBased = isFormBased();
    if (!formBased) {
      //FIXME view and tool buttons should be removed from desktop by device transformer
      putProperty(json, "viewButtons", modelsToJson(getModel().getViewButtons()));
      putProperty(json, "outline", modelToJson(getModel().getOutline()));
    }

    final IHolder<IBreadCrumbsNavigation> breadCrumbsNavigation = new Holder<>();
    IBreadCrumbsNavigationService service = SERVICES.getService(IBreadCrumbsNavigationService.class);
    if (service != null) {
      breadCrumbsNavigation.setValue(service.getBreadCrumbsNavigation());
    }
    if (breadCrumbsNavigation.getValue() != null) {
      putProperty(json, "breadCrumbNavigation", modelToJson(breadCrumbsNavigation.getValue()));
    }
    return json;
  }

  protected boolean isFormBased() {
    //FIXME add property to desktop.  PROP_FORM_BASED Devicetransformer should set it to true in case of mobile
    return getJsonSession().getClientSession().getUserAgent().getUiDeviceType().isTouchDevice();
  }

  protected List<IForm> getForms() {
    List<IForm> forms = new ArrayList<>();
    for (IForm form : getModel().getViewStack()) {
      if (!isFormBlocked(form)) {
        forms.add(form);
      }
    }
    for (IForm form : getModel().getDialogStack()) {
      if (!isFormBlocked(form)) {
        forms.add(form);
      }
    }

    return forms;
  }

  protected boolean isFormBlocked(IForm form) {
    //FIXME ignore desktop forms for the moment, should not be done here, application should handle it or abstractDesktop
    return (!isFormBased() && (form instanceof IOutlineTableForm || form instanceof IOutlineTreeForm));
  }

  protected void handleModelDesktopEvent(DesktopEvent event) {
    switch (event.getType()) {
      case DesktopEvent.TYPE_OUTLINE_CHANGED:
        handleModelOutlineChanged(event.getOutline());
        break;
      case DesktopEvent.TYPE_FORM_ADDED: {
        handleModelFormAdded(event.getForm());
        break;
      }
      case DesktopEvent.TYPE_FORM_REMOVED: {
        handleModelFormRemoved(event.getForm());
        break;
      }
      case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE: {
        handleModelFormEnsureVisible(event.getForm());
        break;
      }
      case DesktopEvent.TYPE_MESSAGE_BOX_ADDED: {
        handleModelMessageBoxAdded(event.getMessageBox());
        break;
      }
      case DesktopEvent.TYPE_DESKTOP_CLOSED: {
        handleModelDesktopClosed();
        break;
      }
    }
  }

  protected void handleModelOutlineChanged(IOutline outline) {
    if (isFormBased()) {
      return;
    }
    getJsonSession().currentJsonResponse().addActionEvent("outlineChanged", getId(), newJsonObjectForModel(PROP_OUTLINE, outline));
  }

  protected void handleModelFormAdded(IForm form) {
    if (isFormBlocked(form)) {
      return;
    }
    getJsonSession().currentJsonResponse().addActionEvent("formAdded", getId(), newJsonObjectForModel(PROP_FORM, form));
  }

  protected void handleModelFormRemoved(IForm form) {
    getJsonSession().currentJsonResponse().addActionEvent("formRemoved", getId(), newJsonObjectForModel(PROP_FORM, form));
  }

  protected void handleModelFormEnsureVisible(IForm form) {
    getJsonSession().currentJsonResponse().addActionEvent("formEnsureVisible", getId(), newJsonObjectForModel(PROP_FORM, form));
  }

  protected void handleModelMessageBoxAdded(final IMessageBox messageBox) {
    //FIXME implement
    //for the moment auto close messagebox to not block the model thread
    messageBox.getUIFacade().setResultFromUI(IMessageBox.YES_OPTION);
  }

  protected void handleModelDesktopClosed() {
    LOG.info("Desktop closed");
    dispose();
    //FIXME what to do? probably http session invalidation -> will terminate EVERY json session (if login is done for all, logout is done for all as well, gmail does the same).
    //Important: Consider tomcat form auth problem, see scout rap logout mechanism for details
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
  }

  protected class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      handleModelDesktopEvent(e);
    }

  }

}
