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
package org.eclipse.scout.rt.ui.json.form;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonRendererFactory;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.form.fields.groupbox.JsonGroupBox;
import org.json.JSONObject;

public class JsonForm extends AbstractJsonPropertyObserverRenderer<IForm> {
  public static final String EVENT_FORM_CLOSING = "formClosing";
  public static final String PROP_TITLE = IForm.PROP_TITLE;
  public static final String PROP_ICON_ID = IForm.PROP_ICON_ID;
  public static final String PROP_MINIMIZE_ENABLED = IForm.PROP_MINIMIZE_ENABLED;
  public static final String PROP_MAXIMIZE_ENABLED = IForm.PROP_MAXIMIZE_ENABLED;
  public static final String PROP_MINIMIZED = IForm.PROP_MINIMIZED;
  public static final String PROP_MAXIMIZED = IForm.PROP_MAXIMIZED;
  public static final String PROP_MODAL = "modal";
  public static final String PROP_DISPLAY_HINT = "displayHint";
  public static final String PROP_DISPLAY_VIEW_ID = "displayViewId";
  public static final String PROP_ROOT_GROUP_BOX = "rootGroupBox";

  private FormListener m_modelFormListener;
  private JsonGroupBox m_rootGroupBox;

  public JsonForm(IForm form, IJsonSession session) {
    super(form, session);
  }

  @Override
  public String getObjectType() {
    return "Form";
  }

  @Override
  protected void attachModel() {
    super.attachModel();

    m_rootGroupBox = JsonRendererFactory.get().createJsonFormField(getModelObject().getRootGroupBox(), getJsonSession());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, PROP_TITLE, getModelObject().getTitle());
    putProperty(json, PROP_ICON_ID, getModelObject().getIconId());
    putProperty(json, PROP_MAXIMIZE_ENABLED, getModelObject().isMaximizeEnabled());
    putProperty(json, PROP_MINIMIZE_ENABLED, getModelObject().isMinimizeEnabled());
    putProperty(json, PROP_MAXIMIZED, getModelObject().isMaximized());
    putProperty(json, PROP_MINIMIZED, getModelObject().isMinimized());
    putProperty(json, PROP_MODAL, getModelObject().isModal());
    putProperty(json, PROP_DISPLAY_HINT, displayHintToJson(getModelObject().getDisplayHint()));
    putProperty(json, PROP_DISPLAY_VIEW_ID, getModelObject().getDisplayViewId());
    putProperty(json, PROP_ROOT_GROUP_BOX, m_rootGroupBox.toJson());
    //FIXME return other props
    return json;
  }

  protected String displayHintToJson(int displayHint) {
    switch (displayHint) {
      case IForm.DISPLAY_HINT_DIALOG:
        return "dialog";
      case IForm.DISPLAY_HINT_VIEW:
        return "view";
      case IForm.DISPLAY_HINT_POPUP_DIALOG:
        return "popupDialog";
      case IForm.DISPLAY_HINT_POPUP_WINDOW:
        return "popupWindow";
    }
    return null;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_FORM_CLOSING.equals(event.getEventType())) {
      handleUiFormClosing(event, res);
    }
  }

  public void handleUiFormClosing(JsonEvent event, JsonResponse res) {

    new ClientSyncJob("Form closing", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        getModelObject().getUIFacade().fireFormClosingFromUI();
      }
    }.runNow(new NullProgressMonitor());

  }

}
