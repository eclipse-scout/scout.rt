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

import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.newJSONArray;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.json.JSONObject;

public class JsonDesktop extends AbstractJsonPropertyObserverRenderer<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktop.class);

  public static final String PROP_FORM_ID = "formId";
  public static final String PROP_OUTLINE_ID = "outlineId";

  private DesktopListener m_desktopListener;

  private String TOOL_BUTTONS = "[{\"id\": \"t1\", \"label\": \"Suche\", \"icon\": \"\uf002\", \"shortcut\": \"F3\"}," +
      "          {\"id\": \"t2\", \"label\": \"Zugriff\", \"icon\": \"\uf144\", \"shortcut\": \"F4\"}," +
      "          {\"id\": \"t3\", \"label\": \"Favoriten\", \"icon\": \"\uf005\", \"shortcut\": \"F6\"}," +
      "          {\"id\": \"t4\", \"label\": \"Muster\", \"icon\": \"\uf01C\", \"shortcut\": \"F7\", \"state\": \"disabled\"}," +
      "          {\"id\": \"t5\", \"label\": \"Telefon\", \"icon\": \"\uf095\", \"shortcut\": \"F8\"}," +
      "          {\"id\": \"t6\", \"label\": \"Cockpit\", \"icon\": \"\uf0E4\", \"shortcut\": \"F9\"}," +
      "          {\"id\": \"t7\", \"label\": \"Prozesse\", \"icon\": \"\uf0D0\",\"shortcut\": \"F10\"}]}]";

  public JsonDesktop(IDesktop desktop, IJsonSession jsonSession, String id) {
    super(desktop, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "Desktop";
  }

  public IDesktop getDesktop() {
    return getModelObject();
  }

  @Override
  protected void attachModel() {
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

    //FIXME add listener afterwards -> don't handle events, refactor
    super.attachModel();

    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
      getDesktop().addDesktopListener(m_desktopListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();

    List<IForm> modelForms = getForms();
    putProperty(json, "forms", modelObjectsToJson(modelForms));
    putProperty(json, "toolButtons", newJSONArray(TOOL_BUTTONS));

    boolean formBased = isFormBased();
    if (!formBased) {
      //FIXME view and tool buttons should be removed from desktop by device transformer
      putProperty(json, "viewButtons", modelObjectsToJson(getDesktop().getViewButtons()));
      putProperty(json, "outline", modelObjectToJson(getDesktop().getOutline()));
    }
    return json;
  }

  protected boolean isFormBased() {
    //FIXME add property to desktop.  PROP_FORM_BASED Devicetransformer should set it to true in case of mobile
    return getJsonSession().getClientSession().getUserAgent().getUiDeviceType().isTouchDevice();
  }

  protected List<IForm> getForms() {
    List<IForm> forms = new ArrayList<>();
    for (IForm form : getDesktop().getViewStack()) {
      if (!isFormBlocked(form)) {
        forms.add(form);
      }
    }
    for (IForm form : getDesktop().getDialogStack()) {
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
    JsonDesktopTree jsonOutline = (JsonDesktopTree) getJsonSession().getJsonRenderer(outline);
    if (jsonOutline == null) {
      jsonOutline = (JsonDesktopTree) getJsonSession().createJsonRenderer(outline);
      getJsonSession().currentJsonResponse().addCreateEvent(getId(), jsonOutline.toJson());
    }
    else {
      JSONObject jsonEvent = new JSONObject();
      putProperty(jsonEvent, PROP_OUTLINE_ID, jsonOutline.getId());
      getJsonSession().currentJsonResponse().addActionEvent("outlineChanged", getId(), jsonEvent);
    }
  }

  protected void handleModelFormAdded(IForm form) {
    JsonForm jsonForm = (JsonForm) getJsonSession().getJsonRenderer(form);
    if (jsonForm == null) {
      if (!isFormBlocked(form)) {
        jsonForm = (JsonForm) getJsonSession().createJsonRenderer(form);
        if (jsonForm != null) {
          getJsonSession().currentJsonResponse().addCreateEvent(getId(), jsonForm.toJson());
        }
      }
    }
    else {
      JSONObject jsonEvent = new JSONObject();
      putProperty(jsonEvent, PROP_FORM_ID, jsonForm.getId());
      getJsonSession().currentJsonResponse().addActionEvent("formAdded", getId(), jsonEvent);
    }
  }

  protected void handleModelFormRemoved(IForm form) {
    JsonForm jsonForm = (JsonForm) getJsonSession().getJsonRenderer(form);
    if (jsonForm != null) {
      JSONObject jsonEvent = new JSONObject();
      putProperty(jsonEvent, PROP_FORM_ID, jsonForm.getId());
      getJsonSession().currentJsonResponse().addActionEvent("formRemoved", getId(), jsonEvent);
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
