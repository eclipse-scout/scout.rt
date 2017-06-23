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
package org.eclipse.scout.rt.ui.html.json.form.fields.splitbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSplitBox<SPLIT_BOX extends ISplitBox> extends JsonFormField<SPLIT_BOX> {
  private static final Logger LOG = LoggerFactory.getLogger(JsonSplitBox.class);

  public static final String EVENT_SET_SPLITTER_POSITION = "setSplitterPosition";

  private final IFormField m_firstField;
  private final IFormField m_secondField;

  public JsonSplitBox(SPLIT_BOX model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    List<IFormField> fields = model.getFields();
    int numFields = fields.size();
    IFormField firstField = null;
    IFormField secondField = null;

    if (numFields >= 1) {
      firstField = fields.get(0);
    }
    if (numFields >= 2) {
      secondField = fields.get(1);
    }
    if (numFields > 2) {
      LOG.warn("Split box only supports two fields. {} surplus fields are ignored in {}.", (numFields - 2), model);
    }

    m_firstField = getFieldIfVisibleGranted(firstField);
    m_secondField = getFieldIfVisibleGranted(secondField);
  }

  @Override
  public String getObjectType() {
    return "SplitBox";
  }

  protected static IFormField getFieldIfVisibleGranted(IFormField field) {
    return field != null && field.isVisibleGranted() ? field : null;
  }

  @Override
  protected void initJsonProperties(SPLIT_BOX model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_SPLITTER_POSITION, model) {
      @Override
      protected Double modelValue() {
        return getModel().getSplitterPosition();
      }
    });
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_MIN_SPLITTER_POSITION, model) {
      @Override
      protected Double modelValue() {
        return getModel().getMinSplitterPosition();
      }
    });
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_SPLITTER_POSITION_TYPE, model) {
      @Override
      protected String modelValue() {
        return getModel().getSplitterPositionType();
      }
    });
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_SPLITTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSplitterEnabled();
      }
    });
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_COLLAPSIBLE_FIELD, model) {
      @Override
      protected IFormField modelValue() {
        return getModel().getCollapsibleField();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        // Deals with the case the field is configured as collapsible, but it is not visible (granted)
        // in that case it has never been attached.
        IFormField field = getFieldIfVisibleGranted((IFormField) value);
        if (field == null) {
          return null;
        }
        return getAdapter(field).getId();
      }
    });
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_FIELD_COLLAPSED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isFieldCollapsed();
      }
    });
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_COLLAPSE_KEY_STROKE, model) {
      @Override
      protected String modelValue() {
        return getModel().getCollapseKeyStroke();
      }
    });
    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_FIELD_MINIMIZED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isFieldMinimized();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(m_firstField);
    attachAdapter(m_secondField);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, "splitHorizontal", getModel().isSplitHorizontal());
    putAdapterIdProperty(json, "firstField", m_firstField);
    putAdapterIdProperty(json, "secondField", m_secondField);
    return json;
  }

  protected IFormField getFirstField() {
    return m_firstField;
  }

  protected IFormField getSecondField() {
    return m_secondField;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_SET_SPLITTER_POSITION.equals(event.getType())) {
      handleUiSetSplitterPosition(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiSetSplitterPosition(JsonEvent event) {
    double splitterPosition = event.getData().optDouble("splitterPosition");
    addPropertyEventFilterCondition(ISplitBox.PROP_SPLITTER_POSITION, splitterPosition);
    getModel().getUIFacade().setSplitterPositionFromUI(splitterPosition);
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ISplitBox.PROP_FIELD_COLLAPSED.equals(propertyName)) {
      boolean fieldCollapsed = data.getBoolean("fieldCollapsed");
      addPropertyEventFilterCondition(ISplitBox.PROP_FIELD_COLLAPSED, fieldCollapsed);
      getModel().getUIFacade().setFieldCollapsedFromUI(fieldCollapsed);
    }
    else if (ISplitBox.PROP_FIELD_MINIMIZED.equals(propertyName)) {
      boolean fieldMinimized = data.getBoolean("fieldMinimized");
      addPropertyEventFilterCondition(ISplitBox.PROP_FIELD_MINIMIZED, fieldMinimized);
      getModel().getUIFacade().setFieldMinimizedFromUI(fieldMinimized);
    }
    else if (ISplitBox.PROP_MIN_SPLITTER_POSITION.equals(propertyName)) {
      Double minSplitterPosition = JsonObjectUtility.optDouble(data, "minSplitterPosition");
      addPropertyEventFilterCondition(ISplitBox.PROP_MIN_SPLITTER_POSITION, minSplitterPosition);
      getModel().getUIFacade().setMinSplitterPositionFromUI(minSplitterPosition);
    }
  }
}
