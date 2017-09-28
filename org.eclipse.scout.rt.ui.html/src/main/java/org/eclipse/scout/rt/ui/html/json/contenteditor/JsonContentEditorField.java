/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.contenteditor;

import org.eclipse.scout.rt.client.ui.contenteditor.ContentEditorFieldEvent;
import org.eclipse.scout.rt.client.ui.contenteditor.ContentEditorFieldListener;
import org.eclipse.scout.rt.client.ui.contenteditor.ContentElement;
import org.eclipse.scout.rt.client.ui.contenteditor.IContentEditorField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonContentEditorField extends JsonFormField<IContentEditorField> {

  public static final String EVENT_EDIT_ELEMENT = "editElement";
  public static final String EVENT_UPDATE_ELEMENT = "updateElement";
  public static final String PROP_ELEMENT_CONTENT = "elementContent";
  public static final String PROP_SLOT = "slot";
  public static final String PROP_ELEMENT_ID = "elementId";

  private ContentEditorFieldListener m_modelListener = null;
  private ContentEditorFieldEventFilter m_eventFilter;

  public JsonContentEditorField(IContentEditorField model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_eventFilter = new ContentEditorFieldEventFilter();
  }

  @Override
  public String getObjectType() {
    return "ContentEditorField";
  }

  @Override
  protected void initJsonProperties(IContentEditorField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IContentEditorField>(IContentEditorField.PROP_CONTENT, model) {
      @Override
      protected String modelValue() {
        return getModel().getContent();
      }
    });
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    m_modelListener = new P_ModelListener();
    getModel().addContentEditorFieldListener(m_modelListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_modelListener == null) {
      throw new IllegalStateException("Already detached");
    }
    getModel().removeContentEditorFieldListener(m_modelListener);
    m_modelListener = null;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_EDIT_ELEMENT.equals(event.getType())) {
      handleUiEditElement(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiEditElement(JsonEvent event) {
    JSONObject data = event.getData();
    String content = data.getString(PROP_ELEMENT_CONTENT);
    String slot = data.getString(PROP_SLOT);
    String elementId = data.getString(PROP_ELEMENT_ID);
    getModel().getUIFacade().editElementFromUi(new ContentElement(content, slot, elementId));
  }

  @Override
  public void cleanUpEventFilters() {
    super.cleanUpEventFilters();
    m_eventFilter.removeAllConditions();
  }

  public void handleModelEvent(ContentEditorFieldEvent e) {
    // FIXME cbu: remove filter if not used
    e = m_eventFilter.filter(e);
    if (e == null) {
      return;
    }

    if (e.getType() == ContentEditorFieldEvent.TYPE_CONTENT_ELEMENT_UPDATE) {
      handleModelContentElementUpdate(e);
    }
  }

  protected void handleModelContentElementUpdate(ContentEditorFieldEvent e) {
    ContentElement contentElement = e.getContentElement();
    JSONObject json = new JSONObject();
    json.put(PROP_ELEMENT_CONTENT, contentElement.getContent());
    json.put(PROP_SLOT, contentElement.getSlot());
    json.put(PROP_ELEMENT_ID, contentElement.getElementId());
    addActionEvent(EVENT_UPDATE_ELEMENT, json);
  }

  private class P_ModelListener implements ContentEditorFieldListener {

    @Override
    public void contentEditorFieldChanged(ContentEditorFieldEvent e) {
      handleModelEvent(e);
    }
  }
}
