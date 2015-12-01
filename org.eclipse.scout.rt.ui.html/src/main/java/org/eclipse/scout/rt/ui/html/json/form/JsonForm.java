/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form;

import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonForm<FORM extends IForm> extends AbstractJsonPropertyObserver<FORM> {
  private static final Logger LOG = LoggerFactory.getLogger(JsonForm.class);

  public static final String PROP_FORM_ID = "formId";
  public static final String PROP_TITLE = IForm.PROP_TITLE;
  public static final String PROP_SUB_TITLE = IForm.PROP_SUB_TITLE;
  public static final String PROP_ICON_ID = IForm.PROP_ICON_ID;
  public static final String PROP_MODAL = "modal";
  public static final String PROP_DISPLAY_HINT = "displayHint";
  public static final String PROP_DISPLAY_VIEW_ID = "displayViewId";
  public static final String PROP_CLOSABLE = "closable";
  public static final String PROP_CACHE_BOUNDS_KEY = "cacheBoundsKey";
  public static final String PROP_FORM_FIELD = "formField";
  public static final String PROP_ROOT_GROUP_BOX = "rootGroupBox";
  public static final String PROP_INITIAL_FOCUS = "initialFocus";
  public static final String PROP_CACHE_BOUNDS = "cacheBounds";

  public static final String EVENT_FORM_CLOSING = "formClosing";
  public static final String EVENT_REQUEST_FOCUS = "requestFocus";

  private final IDesktop m_desktop;
  private FormListener m_formListener;

  public JsonForm(FORM form, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(form, uiSession, id, parent);
    m_desktop = uiSession.getClientSession().getDesktop();
  }

  @Override
  public String getObjectType() {
    return "Form";
  }

  @Override
  protected void initJsonProperties(FORM model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IForm>(PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<IForm>(PROP_SUB_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getSubTitle();
      }
    });
    putJsonProperty(new JsonProperty<IForm>(PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });
    putJsonProperty(new JsonProperty<IForm>(PROP_CACHE_BOUNDS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCacheBounds();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getRootGroupBox());

    attachGlobalAdapters(m_desktop.getViews(getModel()));
    attachGlobalAdapters(m_desktop.getDialogs(getModel(), false));
    attachGlobalAdapters(m_desktop.getMessageBoxes(getModel()));
    attachGlobalAdapters(m_desktop.getFileChoosers(getModel()));
  }

  @Override
  protected void attachModel() {
    super.attachModel();

    Assertions.assertNull(m_formListener);
    m_formListener = new P_FormListener();
    getModel().addFormListener(m_formListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();

    Assertions.assertNotNull(m_formListener);
    getModel().removeFormListener(m_formListener);
    m_formListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    IForm model = getModel();
    putProperty(json, PROP_MODAL, model.isModal());
    putProperty(json, PROP_DISPLAY_HINT, displayHintToJson(model.getDisplayHint()));
    putProperty(json, PROP_DISPLAY_VIEW_ID, model.getDisplayViewId());
    putProperty(json, PROP_CLOSABLE, isClosable());
    putProperty(json, PROP_CACHE_BOUNDS_KEY, model.computeCacheBoundsKey());
    putAdapterIdProperty(json, PROP_ROOT_GROUP_BOX, model.getRootGroupBox());
    setInitialFocusProperty(json);
    putAdapterIdsProperty(json, "views", m_desktop.getViews(getModel()));
    putAdapterIdsProperty(json, "dialogs", m_desktop.getDialogs(getModel(), false));
    putAdapterIdsProperty(json, "messageBoxes", m_desktop.getMessageBoxes(getModel()));
    putAdapterIdsProperty(json, "fileChoosers", m_desktop.getFileChoosers(getModel()));
    return json;
  }

  public void setInitialFocusProperty(JSONObject json) {
    //check for request focus events in history
    IEventHistory<FormEvent> h = getModel().getEventHistory();
    if (h != null) {
      for (FormEvent e : h.getRecentEvents()) {
        if (e.getType() == FormEvent.TYPE_REQUEST_FOCUS) {
          IJsonAdapter<?> formFieldAdapter = JsonAdapterUtility.findChildAdapter(this, e.getFormField());
          if (formFieldAdapter == null) {
            LOG.error("Cannot handle requestFocus event, because adapter for " + e.getFormField() + " could not be resolved in " + toString());
            return;
          }

          putProperty(json, PROP_INITIAL_FOCUS, formFieldAdapter.getId());
        }
      }
    }
  }

  protected boolean isClosable() {
    for (IFormField f : getModel().getAllFields()) {
      if (f.isEnabled() && f.isVisible() && (f instanceof IButton)) {
        switch (((IButton) f).getSystemType()) {
          case IButton.SYSTEM_TYPE_CLOSE:
          case IButton.SYSTEM_TYPE_CANCEL: {
            return true;
          }
        }
      }
    }
    return false;
  }

  protected String displayHintToJson(int displayHint) {
    switch (displayHint) {
      case IForm.DISPLAY_HINT_DIALOG:
        return "dialog";
      case IForm.DISPLAY_HINT_VIEW:
        return "view";
      case IForm.DISPLAY_HINT_POPUP_WINDOW:
        return "popupWindow";
      default:
        return null;
    }
  }

  // ==== FormListener ==== //
  protected void handleModelFormChanged(FormEvent event) {
    switch (event.getType()) {
      case FormEvent.TYPE_CLOSED:
        handleModelFormClosed(event.getForm());
        break;
      case FormEvent.TYPE_REQUEST_FOCUS:
        handleModelRequestFocus(event.getFormField());
        break;
      default:
        // NOP
    }
  }

  protected void handleModelFormClosed(IForm form) {
    dispose();

    // JSON adapter is now disposed. To dispose it on the UI, too, we have to send an explicit
    // event to the UI session. We _don't_ send an EVENT_FORM_CLOSED event for this form adapter,
    // because:
    // a) the event targets in the JSON response should not be disposed (in fact, when the adapter
    //    was disposed, all existing events for that adapter were removed from the response)
    // b) the form may not have been sent to the UI (e.g. opening and closing a form in
    //    the same request). If we would send a FORM_CLOSED event for a form that does
    //    not exist on the UI, an error would be thrown.
    getUiSession().sendDisposeAdapterEvent(this);
  }

  protected void handleModelRequestFocus(IFormField formField) {
    IJsonAdapter<?> formFieldAdapter = JsonAdapterUtility.findChildAdapter(this, formField);
    if (formFieldAdapter == null) {
      LOG.error("Cannot handle requestFocus event, because adapter for " + formField + " could not be resolved in " + toString());
      return;
    }

    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_FORM_FIELD, formFieldAdapter.getId());
    addActionEvent(EVENT_REQUEST_FOCUS, jsonEvent);
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_FORM_CLOSING.equals(event.getType())) {
      handleUiFormClosing(event);
    }
  }

  public void handleUiFormClosing(JsonEvent event) {
    getModel().getUIFacade().fireFormClosingFromUI();
  }

  protected class P_FormListener implements FormListener {

    @Override
    public void formChanged(FormEvent e) {
      handleModelFormChanged(e);
    }
  }
}
