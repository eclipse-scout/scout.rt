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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigation;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigationService;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
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
    super.attachModel();
    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
      getModel().addDesktopListener(m_desktopListener);
    }

    // attach child adapters
    attachAdapters(getForms());
    attachAdapters(getModel().getToolButtons());
    if (!isFormBased()) {
      attachAdapters(getModel().getViewButtons());
      optAttachAdapter(getModel().getOutline());
    }
    optAttachAdapter(getBreadcrumbNavigation());
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
    putAdapterIdsProperty(json, "forms", getForms());
    putAdapterIdsProperty(json, "toolButtons", getModel().getToolButtons());
    if (!isFormBased()) {
      // FIXME view and tool buttons should be removed from desktop by device transformer
      putAdapterIdsProperty(json, "viewButtons", getModel().getViewButtons());
      putAdapterIdProperty(json, "outline", getModel().getOutline());
    }
    optPutAdapterIdProperty(json, "breadCrumbNavigation", getBreadcrumbNavigation());
    return json;
  }

  private IBreadCrumbsNavigation getBreadcrumbNavigation() {
    final IHolder<IBreadCrumbsNavigation> breadCrumbsNavigation = new Holder<>();
    ClientSyncJob job = new ClientSyncJob("AbstractJsonSession#init", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        IBreadCrumbsNavigationService service = SERVICES.getService(IBreadCrumbsNavigationService.class);
        if (service != null) {
          breadCrumbsNavigation.setValue(service.getBreadCrumbsNavigation());
        }
      }
    };
    job.runNow(new NullProgressMonitor());
    try {
      job.throwOnError();
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
    return breadCrumbsNavigation.getValue();
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
    addActionEvent("outlineChanged", attachToJsonId(PROP_OUTLINE, outline));
  }

  protected void handleModelFormAdded(IForm form) {
    if (isFormBlocked(form)) {
      return;
    }
    addActionEvent("formAdded", attachToJsonId(PROP_FORM, form));
  }

  protected void handleModelFormRemoved(IForm form) {
    IJsonAdapter<?> formAdapter = getAdapter(form);
    if (formAdapter != null) {
      addActionEvent("formRemoved", toJsonId(PROP_FORM, formAdapter));
    }
  }

  protected void handleModelFormEnsureVisible(IForm form) {
    addActionEvent("formEnsureVisible", toJsonId(PROP_FORM, getAdapter(form)));
  }

  protected void handleModelMessageBoxAdded(final IMessageBox messageBox) {
    // FIXME implement
    // for the moment auto close messagebox to not block the model thread
    messageBox.getUIFacade().setResultFromUI(IMessageBox.YES_OPTION);
  }

  protected void handleModelDesktopClosed() {
    dispose();
    // FIXME what to do? probably http session invalidation -> will terminate EVERY json session (if login is done
    // for all, logout is done for all as well, gmail does the same).
    // Important: Consider tomcat form auth problem, see scout rap logout mechanism for details
  }

  protected void handleSearch(JsonEvent event) {
    ISearchOutline searchOutline = getSearchOutline();
    String query = event.getData().optString("query");
    try {
      searchOutline.search(query);
      // TODO AWE: (search) C.GU fragen wie such-ergebnisse ans GUI übermittelt werden event-technisch
      String status = searchOutline.getSearchStatus();
      JSONObject json = new JSONObject();
      JsonObjectUtility.putProperty(json, "status", status);
      addActionEvent("searchPerformed", json);
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
  }

  private ISearchOutline getSearchOutline() {
    for (IOutline outline : getModel().getAvailableOutlines()) {
      if (outline instanceof ISearchOutline) {
        return (ISearchOutline) outline;
      }
    }
    return null;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("search".equals(event.getType())) {
      handleSearch(event);
    }
  }

  /**
   * Creates an adapter instance, attaches the adapter and returns a JSON object with a single property where the value
   * is the ID of the adapter.
   */
  private JSONObject attachToJsonId(String popertyName, Object model) {
    return toJsonId(popertyName, attachAdapter(model));
  }

  /**
   * Returns a JSON object with a single property where the value is the ID of the adapter.
   */
  private JSONObject toJsonId(String propertyName, IJsonAdapter<?> jsonAdapter) {
    return putProperty(new JSONObject(), propertyName, jsonAdapter.getId());
  }

  protected class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      handleModelDesktopEvent(e);
    }
  }

}
