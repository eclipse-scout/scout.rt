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
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.DisplayableFormFieldFilter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonCompositeField;

public class JsonTabBox<T extends ITabBox> extends JsonCompositeField<T, IGroupBox> {

  public JsonTabBox(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
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
  protected List<IGroupBox> getModelFields() {
    return getModel().getGroupBoxes();
  }

  @Override
  protected String getModelFieldsPropertyName() {
    return "tabItems";
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
    List<IGroupBox> groupBoxes = getModel().getGroupBoxes();
    if (tabIndex >= 0 && tabIndex < groupBoxes.size()) {
      IGroupBox groupBox = groupBoxes.get(tabIndex);
      addPropertyEventFilterCondition(ITabBox.PROP_SELECTED_TAB, groupBox);
      getModel().getUIFacade().setSelectedTabFromUI(groupBox);
    }
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
