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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONObject;

/**
 * Base class used to create JSON output for Scout form-fields with a value. When a sub-class need to provide a custom
 * <code>valueToJson()</code> method for the value property, it should replace the default JsonProperty for PROP_VALUE ,
 * with it's own implementation by calling <code>putJsonProperty()</code>.
 *
 * @param <VALUE_FIELD>
 */
public abstract class JsonValueField<VALUE_FIELD extends IValueField<?>> extends JsonFormField<VALUE_FIELD> implements IJsonContextMenuOwner {

  /**
   * This event is used when display-text has changed after field loses focus or when the display-text has changed
   * while typing (this event is send after each key-press). You can distinct the two cases by looking on the while-
   * Typing flag.
   */
  public static final String EVENT_DISPLAY_TEXT_CHANGED = "displayTextChanged";

  private PropertyChangeListener m_contextMenuListener;

  public JsonValueField(VALUE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ValueField";
  }

  @Override
  protected void initJsonProperties(VALUE_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<VALUE_FIELD>(IValueField.PROP_DISPLAY_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayText();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_contextMenuListener != null) {
      throw new IllegalStateException();
    }
    m_contextMenuListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          handleModelContextMenuVisibleChanged((Boolean) evt.getNewValue());
        }
      }
    };
    getModel().getContextMenu().addPropertyChangeListener(m_contextMenuListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_contextMenuListener == null) {
      throw new IllegalStateException();
    }
    getModel().getContextMenu().removePropertyChangeListener(m_contextMenuListener);
    m_contextMenuListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      json.put(PROP_MENUS, jsonContextMenu.childActionsToJson());
      json.put(PROP_MENUS_VISIBLE, getModel().getContextMenu().isVisible());
    }
    return json;
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  protected void handleModelContextMenuVisibleChanged(boolean visible) {
    addPropertyChangeEvent(PROP_MENUS_VISIBLE, visible);
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_DISPLAY_TEXT_CHANGED.equals(event.getType())) {
      handleUiDisplayTextChanged(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiDisplayTextChanged(JsonEvent event) {
    String displayText = event.getData().getString(IValueField.PROP_DISPLAY_TEXT);
    addPropertyEventFilterCondition(IValueField.PROP_DISPLAY_TEXT, displayText);
    boolean whileTyping = event.getData().optBoolean("whileTyping", false);
    if (whileTyping) {
      handleUiDisplayTextChangedImpl(displayText);
    }
    else {
      handleUiTextChangedImpl(displayText);
    }
  }

  protected void handleUiTextChangedImpl(String displayText) {
    // NOP may be implemented by sub-classes
  }

  protected void handleUiDisplayTextChangedImpl(String displayText) {
    // NOP may be implemented by sub-classes
  }

}
