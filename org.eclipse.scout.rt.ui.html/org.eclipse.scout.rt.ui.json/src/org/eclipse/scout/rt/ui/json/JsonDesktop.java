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
package org.eclipse.scout.rt.ui.json;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonDesktop extends AbstractJsonPropertyObserverRenderer<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktop.class);
  private static final String WIDGET_ID = "Desktop";

  private JSONArray m_jsonFormsArray;
  private DesktopListener m_desktopListener;
  private List<JsonViewButton> m_jsonViewButtons;
  private Map<IOutline, JsonDesktopTree> m_jsonOutlines;

  private String TOOL_BUTTONS = "[{\"id\": \"t1\", \"label\": \"Suche\", \"icon\": \"\uf002\", \"shortcut\": \"F3\"}," +
      "          {\"id\": \"t2\", \"label\": \"Zugriff\", \"icon\": \"\uf144\", \"shortcut\": \"F4\"}," +
      "          {\"id\": \"t3\", \"label\": \"Favoriten\", \"icon\": \"\uf005\", \"shortcut\": \"F6\"}," +
      "          {\"id\": \"t4\", \"label\": \"Muster\", \"icon\": \"\uf01C\", \"shortcut\": \"F7\", \"state\": \"disabled\"}," +
      "          {\"id\": \"t5\", \"label\": \"Telefon\", \"icon\": \"\uf095\", \"shortcut\": \"F8\"}," +
      "          {\"id\": \"t6\", \"label\": \"Cockpit\", \"icon\": \"\uf0E4\", \"shortcut\": \"F9\"}," +
      "          {\"id\": \"t7\", \"label\": \"Prozesse\", \"icon\": \"\uf0D0\",\"shortcut\": \"F10\"}]}]";

  public JsonDesktop(IDesktop desktop, IJsonSession jsonSession) {
    super(desktop, jsonSession);
    m_jsonFormsArray = new JSONArray();
    m_jsonViewButtons = new LinkedList<JsonViewButton>();
  }

  public IDesktop getDesktop() {
    return getModelObject();
  }

  @Override
  public String getId() {
    return WIDGET_ID;
  }

  @Override
  protected void attachModel() throws JsonUIException {
    new ClientSyncJob("Desktop opened", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (!getDesktop().isOpened()) {
          fireDesktopOpenedFromUIInternal();
        }
        if (!getDesktop().isGuiAvailable()) {
          fireGuiAttachedFromUIInternal();
        }
      }
    }.runNow(new NullProgressMonitor());

    for (IViewButton viewButton : getDesktop().getViewButtons()) {
      JsonViewButton button = new JsonViewButton(viewButton, getJsonSession());
      button.init();
      m_jsonViewButtons.add(button);
    }
    m_jsonOutlines = new HashMap<IOutline, JsonDesktopTree>();
    JsonDesktopTree jsonOutline = new JsonDesktopTree(this, getDesktop().getOutline(), getJsonSession());
    jsonOutline.init();
    m_jsonOutlines.put(getDesktop().getOutline(), jsonOutline);

    //FIXME add listener afterwards -> don't handle events, refactor
    super.attachModel();

    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
      getDesktop().addDesktopListener(m_desktopListener);
    }
  }

  @Override
  protected void detachModel() throws JsonUIException {
    super.detachModel();

    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }
  }

  @Override
  public JSONObject toJson() throws JsonUIException {
    JSONObject json = new JSONObject();
    try {
      json.put("objectType", "Desktop");
      json.put("id", getId());
      json.put("forms", m_jsonFormsArray);
      JSONArray viewButtons = new JSONArray();
      for (JsonViewButton jsonViewButton : m_jsonViewButtons) {
        viewButtons.put(jsonViewButton.toJson());
      }
      json.put("viewButtons", viewButtons);
      json.put("outline", m_jsonOutlines.get(getDesktop().getOutline()).toJson());
      json.put("toolButtons", new JSONArray(TOOL_BUTTONS)); //FIXME

      return json;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonUIException {
  }

  protected JSONObject formToJson(IForm form) throws JsonUIException {
    try {
      JSONObject jsonForm = new JSONObject();
      jsonForm.put("formId", form.getFormId());
      jsonForm.put(IForm.PROP_TITLE, form.getTitle());
      jsonForm.put(IForm.PROP_ICON_ID, form.getIconId());
      return jsonForm;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  protected void fireDesktopOpenedFromUIInternal() {
    if (getDesktop() != null) {
      getDesktop().getUIFacade().fireDesktopOpenedFromUI();
    }
  }

  protected void fireGuiAttachedFromUIInternal() {
    if (getDesktop() != null) {
      getDesktop().getUIFacade().fireGuiAttached();
    }
  }

  protected void fireGuiDetachedFromUIInternal() {
    if (getDesktop() != null) {
      getDesktop().getUIFacade().fireGuiDetached();
    }
  }

  private class P_DesktopListener implements DesktopListener {
    @Override
    public void desktopChanged(final DesktopEvent e) {
      try {
        switch (e.getType()) {
          case DesktopEvent.TYPE_OUTLINE_CHANGED:
            JsonDesktopTree jsonOutline = m_jsonOutlines.get(e.getOutline());
            if (jsonOutline == null) {
              jsonOutline = new JsonDesktopTree(JsonDesktop.this, e.getOutline(), getJsonSession());
              jsonOutline.init();
              m_jsonOutlines.put(e.getOutline(), jsonOutline);
              getJsonSession().currentJsonResponse().addCreateEvent(getId(), jsonOutline.toJson());
            }
            else {
              JSONObject event = new JSONObject();
              event.put("outlineId", jsonOutline.getId());
              getJsonSession().currentJsonResponse().addActionEvent("outlineChanged", getId(), event);
            }
            break;
          case DesktopEvent.TYPE_FORM_ADDED: {

            IForm form = e.getForm();
            JSONObject jsonForm;
            try {
              jsonForm = formToJson(form);
              m_jsonFormsArray.put(jsonForm);
            }
            catch (JsonUIException e1) {
              LOG.error("", e1);
            }

            LOG.info("Form added.");
            break;
          }
          case DesktopEvent.TYPE_MESSAGE_BOX_ADDED: {
            final IMessageBox messageBox = e.getMessageBox();
            new ClientSyncJob("Desktop opened", getJsonSession().getClientSession()) {
              @Override
              protected void runVoid(IProgressMonitor monitor) throws Throwable {
                //FIXME implement
                //for the moment auto close messagebox to not block the model thread
                messageBox.getUIFacade().setResultFromUI(IMessageBox.YES_OPTION);
              }
            }.runNow(new NullProgressMonitor());

            break;
          }
        }
      }
      catch (JSONException ex) {
        throw new JsonUIException(ex.getMessage(), ex);
      }
    }
  }

}
