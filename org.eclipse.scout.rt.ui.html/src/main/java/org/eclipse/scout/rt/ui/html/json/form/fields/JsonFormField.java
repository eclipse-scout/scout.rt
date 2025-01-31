/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IStatusMenuMapping;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonGridData;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S00118")
public abstract class JsonFormField<FORM_FIELD extends IFormField> extends AbstractJsonWidget<FORM_FIELD> implements IJsonContextMenuOwner {

  private static final Logger LOG = LoggerFactory.getLogger(JsonFormField.class);

  private PropertyChangeListener m_contextMenuListener;
  private JsonContextMenu<IContextMenu> m_jsonContextMenu;

  public JsonFormField(FORM_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "FormField";
  }

  @Override
  protected void initJsonProperties(FORM_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLabelVisible();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_POSITION, model) {
      @Override
      protected Integer modelValue() {
        return (int) getModel().getLabelPosition();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_HTML_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLabelHtmlEnabled();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_MANDATORY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMandatory();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_TOOLTIP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipText();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_TOOLTIP_ANCHOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipAnchor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_STATUS_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isStatusVisible();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_STATUS_POSITION, model) {
      @Override
      protected String modelValue() {
        return getModel().getStatusPosition();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_FONT, model) {
      @Override
      protected Object modelValue() {
        return getModel().getFont();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return value instanceof FontSpec ? ((FontSpec) value).toPattern() : null;
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_BACKGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getBackgroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_FOREGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getForegroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_FONT, model) {
      @Override
      protected Object modelValue() {
        return getModel().getLabelFont();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return value instanceof FontSpec ? ((FontSpec) value).toPattern() : null;
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_BACKGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabelBackgroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_FOREGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabelForegroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_FOREGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabelForegroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_WIDTH_IN_PIXEL, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLabelWidthInPixel();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_USE_UI_WIDTH, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLabelUseUiWidth();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_ERROR_STATUS, model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getErrorStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return MainJsonObjectFactory.get().createJsonObject(value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>("gridData", model) {
      @Override
      protected GridData modelValue() {
        return getModel().getGridData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonGridData.toJson((GridData) value);
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LOADING, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoading();
      }
    });
    putJsonProperty(new JsonAdapterProperty<FORM_FIELD>(IFormField.PROP_KEY_STROKES, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<>()).build();
      }

      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_PREVENT_INITIAL_FOCUS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isPreventInitialFocus();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_FIELD_STYLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getFieldStyle();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_DISABLED_STYLE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisabledStyle();
      }
    });
    putJsonProperty(new JsonAdapterProperty<FORM_FIELD>(IFormField.PROP_STATUS_MENU_MAPPINGS, model, getUiSession()) {
      @Override
      protected List<IStatusMenuMapping> modelValue() {
        return getModel().getStatusMenuMappings();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    m_jsonContextMenu = new JsonContextMenu<>(getModel().getContextMenu(), this);
    m_jsonContextMenu.init();
  }

  @Override
  protected void disposeChildAdapters() {
    m_jsonContextMenu.dispose();
    super.disposeChildAdapters();
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_contextMenuListener != null) {
      throw new IllegalStateException();
    }
    m_contextMenuListener = evt -> {
      if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
        handleModelContextMenuVisibleChanged((Boolean) evt.getNewValue());
      }
      else if (IContextMenu.PROP_CURRENT_MENU_TYPES.equals(evt.getPropertyName())) {
        @SuppressWarnings("unchecked")
        Set<? extends IMenuType> newValue = (Set<? extends IMenuType>) evt.getNewValue();
        handleModelContextMenuCurrentMenuTypesChanged(newValue);
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
    json.put(PROP_MENUS, m_jsonContextMenu.childActionsToJson());
    json.put(PROP_MENUS_VISIBLE, getModel().getContextMenu().isVisible());
    json.put(PROP_CURRENT_MENU_TYPES, menuTypesToJson(getModel().getContextMenu().getCurrentMenuTypes()));
    return json;
  }

  protected JSONArray menuTypesToJson(Set<? extends IMenuType> menuTypes) {
    JSONArray array = new JSONArray();
    if (menuTypes == null) {
      return array;
    }
    for (IMenuType menuType : menuTypes) {
      String prefix = menuType.getClass().getSimpleName().replace("MenuType", "");
      array.put(prefix + "." + menuType);
    }
    return array;
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    // If a field is set to visibleGranted=false, a PROP_VISIBLE property change event is fired. In most cases,
    // the JsonAdapter is not yet attached, so this event will not be received here. The adapter will not be
    // attached because of the DisplayableFormFieldFilter. There are however rare cases, where the adapter
    // is already attached when visibleGranted is set to false. If the adapter is not yet sent to the UI,
    // we still have the chance to dispose the adapter and pretend it was never attached in the first place.
    // [Similar code exist in JsonAction]
    if (IFormField.PROP_VISIBLE.equals(event.getPropertyName()) && !getModel().isVisibleGranted()) {
      JsonResponse response = getUiSession().currentJsonResponse();
      if (response.containsAdapter(this) && response.isWritable()) {
        dispose();
        return;
      }
      LOG.warn("Setting visibleGranted=false has no effect, because JsonAdapter {} ({}) is already sent to the UI.", getId(), getModel());
    }
    super.handleModelPropertyChange(event);
  }

  @Override
  public void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters) {
    addPropertyChangeEvent(PROP_MENUS, filteredAdapters);
  }

  protected void handleModelContextMenuVisibleChanged(boolean visible) {
    addPropertyChangeEvent(PROP_MENUS_VISIBLE, visible);
  }

  protected void handleModelContextMenuCurrentMenuTypesChanged(Set<? extends IMenuType> currentMenuTypes) {
    addPropertyChangeEvent(PROP_CURRENT_MENU_TYPES, menuTypesToJson(currentMenuTypes));
  }
}
