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
package org.eclipse.scout.rt.ui.json.desktop;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonException;
import org.eclipse.scout.rt.ui.json.JsonRendererFactory;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.form.JsonForm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonDesktop extends AbstractJsonPropertyObserverRenderer<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktop.class);
  private static final String WIDGET_ID = "Desktop";

  public static final String PROP_FORM_ID = "formId";
  public static final String PROP_OUTLINE_ID = "outlineId";

  private DesktopListener m_desktopListener;
  private FormListener m_modelFormListener;
  private List<JsonViewButton> m_jsonViewButtons;
  private Map<IOutline, JsonDesktopTree> m_jsonOutlines;
  private Map<IForm, JsonForm> m_jsonForms;

  private String TOOL_BUTTONS = "[{\"id\": \"t1\", \"label\": \"Suche\", \"icon\": \"\uf002\", \"shortcut\": \"F3\"}," +
      "          {\"id\": \"t2\", \"label\": \"Zugriff\", \"icon\": \"\uf144\", \"shortcut\": \"F4\"}," +
      "          {\"id\": \"t3\", \"label\": \"Favoriten\", \"icon\": \"\uf005\", \"shortcut\": \"F6\"}," +
      "          {\"id\": \"t4\", \"label\": \"Muster\", \"icon\": \"\uf01C\", \"shortcut\": \"F7\", \"state\": \"disabled\"}," +
      "          {\"id\": \"t5\", \"label\": \"Telefon\", \"icon\": \"\uf095\", \"shortcut\": \"F8\"}," +
      "          {\"id\": \"t6\", \"label\": \"Cockpit\", \"icon\": \"\uf0E4\", \"shortcut\": \"F9\"}," +
      "          {\"id\": \"t7\", \"label\": \"Prozesse\", \"icon\": \"\uf0D0\",\"shortcut\": \"F10\"}]}]";

  public JsonDesktop(IDesktop desktop, IJsonSession jsonSession) {
    super(desktop, jsonSession);
    m_jsonViewButtons = new LinkedList<JsonViewButton>();
    m_jsonOutlines = new HashMap<>();
    m_jsonForms = new HashMap<>();
  }

  @Override
  public String getObjectType() {
    return "Desktop";
  }

  public IDesktop getDesktop() {
    return getModelObject();
  }

  @Override
  public String getId() {
    return WIDGET_ID;
  }

  @Override
  protected void attachModel() throws JsonException {
    new ClientSyncJob("Desktop opened", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (!getDesktop().isOpened()) {
          getDesktop().getUIFacade().fireDesktopOpenedFromUI();
        }
        if (!getDesktop().isGuiAvailable()) {
          getDesktop().getUIFacade().fireGuiAttached();
        }
      }
    }.runNow(new NullProgressMonitor());

    for (IViewButton viewButton : getDesktop().getViewButtons()) {
      JsonViewButton button = JsonRendererFactory.get().createJsonViewButton(viewButton, getJsonSession());
      m_jsonViewButtons.add(button);
    }

    for (IForm form : getDesktop().getDialogStack()) {
      createAndRegisterJsonForm(form);
    }
    for (IForm form : getDesktop().getViewStack()) {
      createAndRegisterJsonForm(form);
    }

    if (getDesktop().getOutline() != null) {
      JsonDesktopTree jsonOutline = JsonRendererFactory.get().createJsonDesktopTree(getDesktop().getOutline(), getJsonSession());
      m_jsonOutlines.put(getDesktop().getOutline(), jsonOutline);
    }

    //FIXME add listener afterwards -> don't handle events, refactor
    super.attachModel();

    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
      getDesktop().addDesktopListener(m_desktopListener);
    }
  }

  @Override
  protected void detachModel() throws JsonException {
    super.detachModel();

    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }

    for (JsonForm form : CollectionUtility.arrayList(m_jsonForms.values())) {
      disposeAndUnregisterJsonForm(form.getModelObject());
    }
  }

  @Override
  public JSONObject toJson() throws JsonException {
    JSONObject json = super.toJson();
    try {
      JSONArray forms = new JSONArray();
      for (JsonForm jsonForm : m_jsonForms.values()) {
        forms.put(jsonForm.toJson());
      }
      json.put("forms", forms);

      boolean formBased = isFormBased();
      if (!formBased) {
        JSONArray viewButtons = new JSONArray();
        for (JsonViewButton jsonViewButton : m_jsonViewButtons) {
          viewButtons.put(jsonViewButton.toJson());
        }
        json.put("viewButtons", viewButtons);

        JsonDesktopTree jsonDesktopTree = m_jsonOutlines.get(getDesktop().getOutline());
        if (jsonDesktopTree != null) {
          json.put("outline", jsonDesktopTree.toJson());
        }
      }

      json.put("toolButtons", new JSONArray(TOOL_BUTTONS)); //FIXME

      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected boolean isFormBased() {
    //FIXME add property to desktop.  PROP_FORM_BASED Devicetransformer should set it to true in case of mobile
    return getJsonSession().getClientSession().getUserAgent().getUiDeviceType().isTouchDevice();
  }

  protected JsonForm createAndRegisterJsonForm(IForm form) {
    if (!isFormBased() && (form instanceof IOutlineTableForm || form instanceof IOutlineTableForm)) {
      return null; //FIXME ignore desktop forms for the moment, should not be done here, application should handle it or abstractDesktop
    }
    JsonForm jsonForm = JsonRendererFactory.get().createJsonForm(form, getJsonSession());
    m_jsonForms.put(form, jsonForm);
    attachFormListener(form);

    return jsonForm;
  }

  protected String disposeAndUnregisterJsonForm(IForm form) {
    if (!isFormBased() && (form instanceof IOutlineTableForm || form instanceof IOutlineTableForm)) {
      return null;//FIXME ignore desktop forms for the moment, should not be done here, application should handle it or abstractDesktop
    }
    JsonForm jsonForm = m_jsonForms.remove(form);
    jsonForm.dispose();
    detachFormListener(form);

    return jsonForm.getId();
  }

  protected void handleModelDesktopEvent(DesktopEvent event) throws JsonException {
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

  protected void handleModelFormChanged(FormEvent event) throws JsonException {
    switch (event.getType()) {
      case TableEvent.TYPE_ROWS_INSERTED: {
        handleModelFormClosed(event.getForm());
        break;
      }
    }
  }

  protected void handleModelOutlineChanged(IOutline outline) {
    try {
      JsonDesktopTree jsonOutline = m_jsonOutlines.get(outline);
      if (jsonOutline == null) {
        jsonOutline = JsonRendererFactory.get().createJsonDesktopTree(outline, getJsonSession());
        m_jsonOutlines.put(outline, jsonOutline);
        getJsonSession().currentJsonResponse().addCreateEvent(getId(), jsonOutline.toJson());
      }
      else {
        JSONObject jsonEvent = new JSONObject();
        jsonEvent.put(PROP_OUTLINE_ID, jsonOutline.getId());
        getJsonSession().currentJsonResponse().addActionEvent("outlineChanged", getId(), jsonEvent);
      }
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected void handleModelFormAdded(IForm form) {
    try {
      JsonForm jsonForm = m_jsonForms.get(form);
      if (jsonForm == null) {
        jsonForm = createAndRegisterJsonForm(form);
        if (jsonForm != null) {
          getJsonSession().currentJsonResponse().addCreateEvent(getId(), jsonForm.toJson());
        }
      }
      else {
        JSONObject jsonEvent = new JSONObject();
        jsonEvent.put(PROP_FORM_ID, jsonForm.getId());
        getJsonSession().currentJsonResponse().addActionEvent("formAdded", getId(), jsonEvent);
      }
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected void handleModelFormRemoved(IForm form) {
    try {
      JsonForm jsonForm = m_jsonForms.get(form);
      JSONObject jsonEvent = new JSONObject();
      jsonEvent.put(PROP_FORM_ID, jsonForm.getId());
      getJsonSession().currentJsonResponse().addActionEvent("formRemoved", getId(), jsonEvent);
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected void handleModelFormClosed(IForm form) {
    //FIXME what happens if isAutoAddRemoveOnDesktop = false and form removed comes after closing? maybe remove form first?
    if (getDesktop().isShowing(form)) {
      LOG.error("Form closed but is still showing on desktop.");
//      handleModelFormRemoved(form);
    }

    try {
      String formId = disposeAndUnregisterJsonForm(form);
      if (formId != null) {
        JSONObject jsonEvent = new JSONObject();
        jsonEvent.put(PROP_FORM_ID, formId);
        getJsonSession().currentJsonResponse().addActionEvent("formClosed", getId(), jsonEvent);
      }
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  protected void handleModelMessageBoxAdded(final IMessageBox messageBox) {
    new ClientSyncJob("Desktop opened", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        //FIXME implement
        //for the moment auto close messagebox to not block the model thread
        messageBox.getUIFacade().setResultFromUI(IMessageBox.YES_OPTION);
      }
    }.runNow(new NullProgressMonitor());
  }

  protected void handleModelDesktopClosed() {
    LOG.info("Desktop closed.");
    //FIXME what to do? probably http session invalidation -> will terminate EVERY json session (if login is done for all, logout is done for all as well, gmail does the same).
    //Important: Consider tomcat form auth problem, see scout rap logout mechanism for details
  }

  protected void attachFormListener(IForm form) {
    if (m_modelFormListener == null) {
      m_modelFormListener = new P_ModelFormListener();
    }
    form.addFormListener(m_modelFormListener);
  }

  protected void detachFormListener(IForm form) {
    if (m_modelFormListener != null) {
      form.removeFormListener(m_modelFormListener);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonException {
  }

  protected class P_DesktopListener implements DesktopListener {

    @Override
    public void desktopChanged(DesktopEvent e) {
      handleModelDesktopEvent(e);
    }

  }

  protected class P_ModelFormListener implements FormListener {

    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      handleModelFormChanged(e);
    }
  }

}
