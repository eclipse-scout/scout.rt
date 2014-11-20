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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.Scout5ExtensionUtil;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigation;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigationService;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IUrlTarget;
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

public class JsonDesktop<T extends IDesktop> extends AbstractJsonPropertyObserver<T> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktop.class);

  public static final String PROP_OUTLINE = "outline";
  public static final String PROP_FORM = "form";
  public static final String PROP_MESSAGE_BOX = "messageBox";

  private DesktopListener m_desktopListener;

  private IOutline m_previousOutline;

  public JsonDesktop(T desktop, IJsonSession jsonSession, String id) {
    super(desktop, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "Desktop";
  }

  @Override
  protected void createChildAdapters() {
    super.createChildAdapters();
    attachAdapters(getForms());
    attachAdapters(getModel().getMessageBoxStack());
    attachAdapters(filterModelActions());
    attachAdapters(Scout5ExtensionUtil.IDesktop_getAddOns(getModel()));
    if (!isFormBased()) {
      attachAdapters(getModel().getViewButtons());
      optAttachAdapter(getModel().getOutline());
      optAttachAdapter(getSearchOutline());
    }
    optAttachAdapter(getBreadcrumbNavigation());
  }

  /**
   * TODO CGU/AWE: (scout) mit Judith besprechen: Aufräumaktion im Bereich IToolButton/IViewButton
   * Anstelle der vielen Marker interfaces möchten wir nur noch IActions haben. Alle IActions werden dann
   * rechts oben angezeigt (es können Menüs und Buttons sein). Die Outlines werden einfach anhand der
   * konfigurierten Outlines erzeugt, ohne dass dafür noch ein Button konfiguriert werden muss. Dann
   * können alle Interfaces und abstrakten Klasse die es heute gibt gelöscht werden. <br/>
   * An dieser Stelle filtern wir einfach alle Actions weg, die wir im MiniCRM nicht sehen wollen.
   * Wenn wir den Fork haben, können wir die Konfiguration anpassen. Nach dem Refactoring diese Methode
   * entfernen und nur noch model.getActions() verwenden.
   */
  private List<IAction> filterModelActions() {
    List<IAction> result = new ArrayList<>();
    for (IAction a : getModel().getActions()) {
      if (hasClassName(a, "UserProfileMenu") ||
          hasClassName(a, "PhoneFormToolButton") ||
          hasClassName(a, "ProcessAssistantFormToolButton")) {
        result.add(a);
      }
    }
    // Noch ein Hack: in AbstractDesktop#getActions() ist hart-kodiert, dass menus vor toolButtons
    // geadded werden, unabhängig von der konfigurierten Reihenfolge (Order). Dieses Problem müsste
    // auch gelöst sein, wenn alles nur noch IActions sind.
    Collections.reverse(result);
    return result;
  }

  private boolean hasClassName(IAction a, String className) {
    return a.getClass().getName().contains(className);
  }

  @Override
  protected void disposeChildAdapters() {
    disposeAdapters(getForms());
    disposeAdapters(getModel().getMessageBoxStack());
    disposeAdapters(filterModelActions());
    disposeAdapters(Scout5ExtensionUtil.IDesktop_getAddOns(getModel()));
    if (!isFormBased()) {
      disposeAdapters(getModel().getViewButtons());
      optDisposeAdapter(getModel().getOutline());
    }
    optDisposeAdapter(getBreadcrumbNavigation());
  }

  @Override
  protected void attachModel() {
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
    putAdapterIdsProperty(json, "forms", getForms());
    putAdapterIdsProperty(json, "messageBoxes", getModel().getMessageBoxStack());
    putAdapterIdsProperty(json, "actions", filterModelActions());
    putAdapterIdsProperty(json, "addOns", Scout5ExtensionUtil.IDesktop_getAddOns(getModel()));
    if (!isFormBased()) {
      // FIXME CGU: view and tool buttons should be removed from desktop by device transformer
      putAdapterIdsProperty(json, "viewButtons", getModel().getViewButtons());
      optPutAdapterIdProperty(json, "outline", getModel().getOutline());
      optPutAdapterIdProperty(json, "searchOutline", getSearchOutline());
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
    // FIXME CGU: add property to desktop.  PROP_FORM_BASED Devicetransformer should set it to true in case of mobile
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
    // FIXME CGU: ignore desktop forms for the moment, should not be done here, application should handle it or abstractDesktop
    return (!isFormBased() && (form instanceof IOutlineTableForm || form instanceof IOutlineTreeForm));
  }

  protected void handleModelDesktopEvent(DesktopEvent event) {
    switch (event.getType()) {
      case DesktopEvent.TYPE_OUTLINE_CHANGED:
        handleModelOutlineChanged(event.getOutline());
        break;
      case DesktopEvent.TYPE_FORM_ADDED:
        handleModelFormAdded(event.getForm());
        break;
      case DesktopEvent.TYPE_FORM_REMOVED:
        handleModelFormRemoved(event.getForm());
        break;
      case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE:
        handleModelFormEnsureVisible(event.getForm());
        break;
      case DesktopEvent.TYPE_OPEN_URL_IN_BROWSER:
        handleModelOpenUrlInBrowser(event.getPath(), event.getUrlTarget());
        break;
      case DesktopEvent.TYPE_MESSAGE_BOX_ADDED:
        handleModelMessageBoxAdded(event.getMessageBox());
        break;
      case DesktopEvent.TYPE_DESKTOP_CLOSED:
        handleModelDesktopClosed();
        break;
    }
  }

  private void handleModelOpenUrlInBrowser(String path, IUrlTarget urlTarget) {
    JSONObject json = new JSONObject();
    putProperty(json, "path", path);
    putProperty(json, "urlTarget", urlTarget.toString());
    addActionEvent("openUrlInBrowser", json);
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
    addActionEvent("messageBoxAdded", attachToJsonId(PROP_MESSAGE_BOX, messageBox));
  }

  protected void handleModelDesktopClosed() {
    dispose();
    // FIXME CGU: what to do? probably http session invalidation -> will terminate EVERY json session (if login is done
    // for all, logout is done for all as well, gmail does the same).
    // Important: Consider tomcat form auth problem, see scout rap logout mechanism for details
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
    if ("outlineChanged".equals(event.getType())) {
      handleUiOutlineChanged(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  private void handleUiOutlineChanged(JsonEvent event) {
    String outlineId = JsonObjectUtility.getString(event.getData(), "outlineId");
    IJsonAdapter<?> jsonOutline = getJsonSession().getJsonAdapter(outlineId);
    getModel().setOutline((IOutline) jsonOutline.getModel());
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
