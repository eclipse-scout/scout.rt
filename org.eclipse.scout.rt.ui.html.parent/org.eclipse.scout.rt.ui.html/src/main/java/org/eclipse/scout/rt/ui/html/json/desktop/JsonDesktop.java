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
import org.eclipse.scout.rt.client.ClientSyncJob;
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

  public static final String PROP_OUTLINE = "outline";
  public static final String PROP_FORM = "form";
  public static final String PROP_MESSAGE_BOX = "messageBox";

  private DesktopListener m_desktopListener;

  public JsonDesktop(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Desktop";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachGlobalAdapters(getForms());
    attachGlobalAdapters(getModel().getMessageBoxStack());
    attachAdapters(filterModelActions());
    attachAdapters(getModel().getAddOns());
    if (!isFormBased()) {
      attachAdapters(getModel().getViewButtons());
      attachGlobalAdapter(getModel().getOutline());
      attachGlobalAdapter(getSearchOutline());
    }
    attachAdapter(getBreadcrumbNavigation());
  }

  /**
   * TODO AWE/CGU: (scout, actions) mit Judith besprechen: Aufräumaktion im Bereich IToolButton/IViewButton
   * Anstelle der vielen Marker interfaces möchten wir nur noch IActions haben. Alle IActions werden dann
   * rechts oben angezeigt (es können Menüs und Buttons sein). Die Outlines werden einfach anhand der
   * konfigurierten Outlines erzeugt, ohne dass dafür noch ein Button konfiguriert werden muss. Dann
   * können alle Interfaces und abstrakten Klasse die es heute gibt gelöscht werden.
   * <p>
   * An dieser Stelle filtern wir einfach alle Actions weg, die wir im MiniCRM nicht sehen wollen. Wenn wir den Fork
   * haben, können wir die Konfiguration anpassen. Nach dem Refactoring diese Methode entfernen und nur noch
   * model.getActions() verwenden.
   * <p>
   * Mit Scout-Team besprechen, wie wir mit Menüs in Zukunft umgehen wollen. Wollen wir die Menü-Bar im Model abbilden?
   * (GroupBox, Table, Desktop) Brauchen wir all die Menü-Types dann noch? Können wir die Menü-Logik überhaupt nochmals
   * refactoren? Was machen wir mit der heutigen Button-Bar?
   */
  protected List<IAction> filterModelActions() {
    List<IAction> result = new ArrayList<>();
    for (IAction a : getModel().getActions()) {
      if (hasClassName(a, "UserProfileMenu")
          || hasClassName(a, "PhoneFormToolButton")
          || hasClassName(a, "ProcessAssistantFormToolButton")) {
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
  protected void attachModel() {
    super.attachModel();
    if (m_desktopListener != null) {
      throw new IllegalStateException();
    }
    m_desktopListener = new P_DesktopListener();
    getModel().addDesktopListener(m_desktopListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_desktopListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeDesktopListener(m_desktopListener);
    m_desktopListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdsProperty(json, "forms", getForms());
    putAdapterIdsProperty(json, "messageBoxes", getModel().getMessageBoxStack());
    putAdapterIdsProperty(json, "actions", filterModelActions());
    putAdapterIdsProperty(json, "addOns", getModel().getAddOns());
    if (!isFormBased()) {
      // FIXME CGU: view and tool buttons should be removed from desktop by device transformer
      putAdapterIdsProperty(json, "viewButtons", getModel().getViewButtons());
      putAdapterIdProperty(json, "outline", getModel().getOutline());
      putAdapterIdProperty(json, "searchOutline", getSearchOutline());
    }
    putAdapterIdProperty(json, "breadCrumbNavigation", getBreadcrumbNavigation());
    return json;
  }

  protected IBreadCrumbsNavigation getBreadcrumbNavigation() {
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
      default:
        // NOP
    }
  }

  protected void handleModelOpenUrlInBrowser(String path, IUrlTarget urlTarget) {
    JSONObject json = new JSONObject();
    putProperty(json, "path", path);
    putProperty(json, "urlTarget", urlTarget.toString());
    addActionEvent("openUrlInBrowser", json);
  }

  protected void handleModelOutlineChanged(IOutline outline) {
    if (isFormBased()) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(outline);
    putProperty(jsonEvent, PROP_OUTLINE, jsonAdapter.getId());
    addActionEvent("outlineChanged", jsonEvent);
  }

  protected void handleModelFormAdded(IForm form) {
    if (isFormBlocked(form)) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(form);
    putProperty(jsonEvent, PROP_FORM, jsonAdapter.getId());
    addActionEvent("formAdded", jsonEvent);
  }

  protected void handleModelFormRemoved(IForm form) {
    IJsonAdapter<?> jsonAdapter = getAdapter(form);
    if (jsonAdapter != null) {
      JSONObject jsonEvent = new JSONObject();
      putProperty(jsonEvent, PROP_FORM, jsonAdapter.getId());
      addActionEvent("formRemoved", jsonEvent);
    }
  }

  protected void handleModelFormEnsureVisible(IForm form) {
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = getAdapter(form);
    putProperty(jsonEvent, PROP_FORM, jsonAdapter.getId());
    addActionEvent("formEnsureVisible", jsonEvent);
  }

  protected void handleModelMessageBoxAdded(final IMessageBox messageBox) {
    JSONObject jsonEvent = new JSONObject();
    IJsonAdapter<?> jsonAdapter = attachGlobalAdapter(messageBox);
    putProperty(jsonEvent, PROP_MESSAGE_BOX, jsonAdapter.getId());
    addActionEvent("messageBoxAdded", jsonEvent);
  }

  protected void handleModelDesktopClosed() {
    dispose();
  }

  protected ISearchOutline getSearchOutline() {
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

  protected void handleUiOutlineChanged(JsonEvent event) {
    String outlineId = JsonObjectUtility.getString(event.getData(), "outlineId");
    IJsonAdapter<?> jsonOutline = getJsonSession().getJsonAdapter(outlineId);
    getModel().setOutline((IOutline) jsonOutline.getModel());
  }

  protected class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      handleModelDesktopEvent(e);
    }
  }
}
