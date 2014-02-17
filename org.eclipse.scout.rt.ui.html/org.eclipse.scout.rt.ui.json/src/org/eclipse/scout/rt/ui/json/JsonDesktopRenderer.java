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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonDesktopRenderer extends AbstractJsonRenderer<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktopRenderer.class);
  private static final String WIDGET_ID = "Desktop";

  private JSONArray m_jsonFormsArray;
  private JSONArray m_jsonPagesArray;
  private DesktopListener m_desktopListener;
  private List<JsonViewButtonRenderer> m_jsonViewButtons;

  public JsonDesktopRenderer(IDesktop desktop, IJsonSession jsonSession) {
    super(desktop, jsonSession);
    m_jsonFormsArray = new JSONArray();
    m_jsonPagesArray = new JSONArray();
    m_jsonViewButtons = new LinkedList<JsonViewButtonRenderer>();
  }

  public IDesktop getDesktop() {
    return getScoutObject();
  }

  @Override
  public String getId() {
    return WIDGET_ID;
  }

  @Override
  protected void attachModel() throws JsonUIException {
    super.attachModel();

    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
      getDesktop().addDesktopListener(m_desktopListener);
    }

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

    IViewButton[] viewButtons = getDesktop().getViewButtons();
    for (IViewButton viewButton : viewButtons) {
      JsonViewButtonRenderer button = new JsonViewButtonRenderer(viewButton, getJsonSession());
      button.init();
      m_jsonViewButtons.add(button);
    }
    if (getDesktop().getOutline().isRootNodeVisible()) {
      IPage rootPage = getDesktop().getOutline().getRootPage();
      m_jsonPagesArray.put(pageToJson(rootPage));
    }
    else {
      IPage[] childPages = getDesktop().getOutline().getRootPage().getChildPages();
      for (IPage childPage : childPages) {
        m_jsonPagesArray.put(pageToJson(childPage));
      }
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
    JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse.put("id", getId());
      jsonResponse.put("forms", m_jsonFormsArray);
      jsonResponse.put("pages", m_jsonPagesArray);
      JSONArray viewButtons = new JSONArray();
      for (JsonViewButtonRenderer jsonViewButton : m_jsonViewButtons) {
        viewButtons.put(jsonViewButton.toJson());
      }
      jsonResponse.put("viewButtons", viewButtons);

      return jsonResponse;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(UIRequest req, UIResponse res) throws JsonUIException {
    if ("startup".equals(req.getEventType())) {
      handleUiStartupEvent(req, res);
    }
  }

  protected void handleUiStartupEvent(UIRequest req, UIResponse res) throws JsonUIException {
    //Instruct gui to create desktop
    res.addCreateEvent(this.toJson());
  }

  protected JSONObject pageToJson(IPage page) throws JsonUIException {
    try {
      JSONObject json = new JSONObject();
      json.put("text", page.getCell().getText());
      json.put("expanded", page.isExpanded());

      if (page.getChildNodeCount() > 0) {
        JSONArray jsonChildPages = new JSONArray();
        for (IPage childPage : page.getChildPages()) {
          jsonChildPages.put(pageToJson(childPage));
        }
        json.put("childPages", jsonChildPages);
      }

      return json;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
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
      switch (e.getType()) {
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
      }
    }
  }

}
