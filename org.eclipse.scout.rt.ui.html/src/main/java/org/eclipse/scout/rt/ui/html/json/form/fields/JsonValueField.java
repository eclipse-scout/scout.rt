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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
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
   * This event is used when display-text has changed after field loses focus or when the display-text has changed while
   * typing (this event is send after each key-press). You can distinct the two cases by looking on the while- Typing
   * flag.
   */
  public static final String EVENT_DISPLAY_TEXT_CHANGED = "displayTextChanged";
  public static final String EVENT_EXPORT_TO_CLIPBOARD = "exportToClipboard";

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
        else if (IContextMenu.PROP_CURRENT_MENU_TYPES.equals(evt.getPropertyName())) {
          @SuppressWarnings("unchecked")
          Set<? extends IMenuType> newValue = (Set<? extends IMenuType>) evt.getNewValue();
          handleModelContextMenuCurrentMenuTypesChanged(newValue);
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
      json.put(PROP_CURRENT_MENU_TYPES, menuTypesToJson(getModel().getContextMenu().getCurrentMenuTypes()));
    }
    return json;
  }

  protected JSONArray menuTypesToJson(Set<? extends IMenuType> menuTypes) {
    JSONArray array = new JSONArray();
    for (IMenuType menuType : menuTypes) {
      array.put(menuType.toString());
    }
    return array;
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  protected void handleModelContextMenuVisibleChanged(boolean visible) {
    addPropertyChangeEvent(PROP_MENUS_VISIBLE, visible);
  }

  protected void handleModelContextMenuCurrentMenuTypesChanged(Set<? extends IMenuType> currentMenuTypes) {
    addPropertyChangeEvent(PROP_CURRENT_MENU_TYPES, menuTypesToJson(currentMenuTypes));
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_DISPLAY_TEXT_CHANGED.equals(event.getType())) {
      handleUiDisplayTextChanged(event);
    }
    else if (EVENT_EXPORT_TO_CLIPBOARD.equals(event.getType())) {
      handleUiExportToClipboard();
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
      handleUiDisplayTextChangedWhileTyping(displayText);
    }
    else {
      handleUiDisplayTextChangedAfterTyping(displayText);
    }
  }

  /**
   * Called by the UI when the displayText has changed but the editing action has not yet finished (
   * <code>whileTyping = true</code>). The model field does not yet change its value. This method is usually only called
   * when the {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY} flag is set.
   */
  protected void handleUiDisplayTextChangedWhileTyping(String displayText) {
    // NOP may be implemented by sub-classes
  }

  /**
   * Called by the UI when the displayText has changed and the editing action has finished (
   * <code>whileTyping = false</code>). The model field parses the displayText and updates its value.
   */
  protected void handleUiDisplayTextChangedAfterTyping(String displayText) {
    // NOP may be implemented by sub-classes
  }

  protected void handleUiExportToClipboard() {
    try {
      BEANS.get(IClipboardService.class).setTextContents(getModel().getDisplayText());
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }
}
