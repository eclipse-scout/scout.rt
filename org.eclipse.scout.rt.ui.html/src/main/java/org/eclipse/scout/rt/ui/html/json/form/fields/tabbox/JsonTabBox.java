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
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.DisplayableFormFieldFilter;
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

    putJsonProperty(new JsonProperty<ITabBox>(ITabBox.PROP_SELECTED_TAB, model) {
      @Override
      protected IGroupBox modelValue() {
        return getModel().getSelectedTab();
      }

      @Override
      public Integer prepareValueForToJson(Object value) {
        // instead of returning a whole adapter here, we simply return the index of the group-box (=tab)
        IGroupBox selectedTab = (IGroupBox) value;
        return getIndexForGroupBox(selectedTab);
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
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.SELECTED.matches(event)) {
      handleUiTabSelected(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiTabSelected(JsonEvent event) {
    int tabIndex = event.getData().optInt("tabIndex");
    IGroupBox selectedTabBox = getGroupBoxForIndex(tabIndex);
    if (selectedTabBox != null) {
      addPropertyEventFilterCondition(ITabBox.PROP_SELECTED_TAB, selectedTabBox);
      getModel().getUIFacade().setSelectedTabFromUI(selectedTabBox);
    }
  }

  protected IGroupBox getGroupBoxForIndex(int index) {
    DisplayableFormFieldFilter<IFormField> filter = new DisplayableFormFieldFilter<>();
    int i = 0;
    for (IGroupBox gb : getModel().getGroupBoxes()) {
      // Don't count invisible group boxes (they are not sent to the UI, see JsonCompositeField)
      if (!filter.accept(gb)) {
        continue;
      }
      if (i == index) {
        return gb;
      }
      i++;
    }
    return null;
  }

  protected int getIndexForGroupBox(IGroupBox groupBox) {
    List<IGroupBox> groupBoxes = getModel().getGroupBoxes();
    DisplayableFormFieldFilter<IFormField> filter = new DisplayableFormFieldFilter<>();
    int i = 0;
    for (IGroupBox gb : groupBoxes) {
      // Don't count invisible group boxes (they are not sent to the UI, see JsonCompositeField)
      if (!filter.accept(gb)) {
        continue;
      }
      if (gb == groupBox) {
        return i;
      }
      i++;
    }
    throw new IllegalStateException("selected tab not found in group-boxes list");
  }
}
