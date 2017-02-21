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
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonCompositeField;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONObject;

public class JsonTabBox<TAB_BOX extends ITabBox> extends JsonCompositeField<TAB_BOX, IGroupBox> implements IJsonContextMenuOwner {
  private JsonContextMenu<IContextMenu> m_jsonContextMenu;

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
        return new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "TabBox";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    m_jsonContextMenu = new JsonContextMenu<IContextMenu>(getModel().getContextMenu(), this);
    m_jsonContextMenu.init();
  }

  @Override
  protected void disposeChildAdapters() {
    m_jsonContextMenu.dispose();
    super.disposeChildAdapters();
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
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(PROP_MENUS, m_jsonContextMenu.childActionsToJson());
    return json;
  }

  @Override
  public void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters) {
    addPropertyChangeEvent(PROP_MENUS, filteredAdapters);
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ITabBox.PROP_SELECTED_TAB.equals(propertyName)) {
      String tabId = data.optString("selectedTab");
      IGroupBox selectedTab = getGroupBoxForId(tabId);
      addPropertyEventFilterCondition(ITabBox.PROP_SELECTED_TAB, selectedTab);
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
