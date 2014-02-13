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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
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

public class JsonDesktop extends AbstractJsonRenderer<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktop.class);

  private JSONArray m_jsonFormsArray;
  private JSONArray m_jsonViewButtonsArray;
  private JSONArray m_jsonPagesArray;
  private DesktopListener m_desktopListener;

  public JsonDesktop(IDesktop desktop, IJsonEnvironment jsonEnvironment) {
    super(desktop, jsonEnvironment);
    m_jsonFormsArray = new JSONArray();
    m_jsonViewButtonsArray = new JSONArray();
    m_jsonPagesArray = new JSONArray();
  }

  public IDesktop getDesktop() {
    return getScoutObject();
  }

  @Override
  public String getId() {
    return "Desktop";
  }

  @Override
  public void attachScout() throws ProcessingException {
    super.attachScout();

    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
      getDesktop().addDesktopListener(m_desktopListener);
    }

    new ClientSyncJob("Desktop opened", getJsonEnvironment().getClientSession()) {
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
      JsonViewButton button = new JsonViewButton(viewButton, getJsonEnvironment());
      button.init();
      m_jsonViewButtonsArray.put(button.toJson());
    }
    if (getDesktop().getOutline().isRootNodeVisible()) {
      IPage rootPage = getDesktop().getOutline().getRootPage();
      m_jsonPagesArray.put(toJson(rootPage));
    }
    else {
      IPage[] childPages = getDesktop().getOutline().getRootPage().getChildPages();
      for (IPage childPage : childPages) {
        m_jsonPagesArray.put(toJson(childPage));
      }
    }
  }

  @Override
  protected void detachScout() throws ProcessingException {
    super.detachScout();
    if (m_desktopListener != null) {
      getDesktop().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }
  }

  @Override
  public JSONObject toJson() throws ProcessingException {
    JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse.put("type", "desktop");
      jsonResponse.put("forms", m_jsonFormsArray);
      jsonResponse.put("viewButtons", m_jsonViewButtonsArray);
      jsonResponse.put("pages", m_jsonPagesArray);

      return jsonResponse;
    }
    catch (JSONException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(String type) throws ProcessingException {
    if ("startup".equals(type)) {
      getJsonEnvironment().addCreateEvent(this);
    }
  }

  protected JSONObject toJson(IPage page) throws ProcessingException {
    try {
      JSONObject json = new JSONObject();
      json.put("text", page.getCell().getText());
      json.put("expanded", page.isExpanded());

      if (page.getChildNodeCount() > 0) {
        JSONArray jsonChildPages = new JSONArray();
        for (IPage childPage : page.getChildPages()) {
          jsonChildPages.put(toJson(childPage));
        }
        json.put("childPages", jsonChildPages);
      }

      return json;
    }
    catch (JSONException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  protected JSONObject toJson(IForm form) throws ProcessingException {
    try {
      JSONObject jsonForm = new JSONObject();
      jsonForm.put("formId", form.getFormId());
      jsonForm.put(IForm.PROP_TITLE, form.getTitle());
      jsonForm.put(IForm.PROP_ICON_ID, form.getIconId());
      return jsonForm;
    }
    catch (JSONException e) {
      throw new ProcessingException(e.getMessage(), e);
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
            jsonForm = toJson(form);
            m_jsonFormsArray.put(jsonForm);
          }
          catch (ProcessingException e1) {
            LOG.error("", e1);
          }

          LOG.info("Form added.");
          break;
        }
      }
    }
  }

}
