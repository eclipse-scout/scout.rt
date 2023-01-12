/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.DisplayableFormFieldFilter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonCompositeField;
import org.json.JSONObject;

public class JsonTabBox<TAB_BOX extends ITabBox> extends JsonCompositeField<TAB_BOX, IGroupBox> {

  public JsonTabBox(TAB_BOX model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(TAB_BOX model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<ITabBox>(ITabBox.PROP_SELECTED_TAB, model, getUiSession()) {
      @Override
      protected IGroupBox modelValue() {
        return getModel().getSelectedTab();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder()
            .disposeOnChange(false)
            // ensure adapter is not accidentally created on a property change,
            // it would never be sent to ui but could receive events by the model which eventually would result in exceptions because the ui cannot find the adapter
            .filter(new DisplayableFormFieldFilter<>())
            .build();
      }
    });
    putJsonProperty(new JsonProperty<TAB_BOX>(ITabBox.PROP_TAB_AREA_STYLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTabAreaStyle();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "TabBox";
  }

  @Override
  protected List<IGroupBox> getModelFields() {
    return getModel().getGroupBoxes();
  }

  @Override
  protected String getModelFieldsPropertyName() {
    return "tabItems";
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ITabBox.PROP_SELECTED_TAB.equals(propertyName)) {
      String tabId = data.optString(propertyName);
      IGroupBox selectedTab = getGroupBoxForId(tabId);
      addPropertyEventFilterCondition(propertyName, selectedTab);
      getModel().getUIFacade().setSelectedTabFromUI(selectedTab);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected IGroupBox getGroupBoxForId(String tabId) {
    for (IGroupBox gb : getModel().getGroupBoxes()) {
      // in case group-box is visibleGranted=false getAdapter will return null
      IJsonAdapter<?> adapter = getAdapter(gb);
      if (adapter != null && adapter.getId().equals(tabId)) {
        return (IGroupBox) adapter.getModel();
      }
    }
    return null;
  }
}
