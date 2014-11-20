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

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.PropertyChangeEventFilterCondition;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonTabBox<T extends ITabBox> extends JsonFormField<T> {

  public JsonTabBox(T model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    // instead of returning a whole adapter here, we simply return the index of the group-box (=tab)
    putJsonProperty(new JsonProperty<ITabBox>(ITabBox.PROP_SELECTED_TAB, model) {

      // TODO AWE: (tab-box) feuert als folge von getModel().getUIFacade().setSelectedTabFromUI(groupBox);
      // einen PC event, den wir gerne filtern würden - wie machen wir das am elegantesten?

      @Override
      public Integer prepareValueForToJson(Object value) {
        IGroupBox selectedTab = (IGroupBox) value;
        return getIndexForGroupBox(selectedTab);
      }

      private int getIndexForGroupBox(IGroupBox groupBox) {
        List<IGroupBox> groupBoxes = getModel().getGroupBoxes();
        for (int i = 0; i < groupBoxes.size(); i++) {
          if (groupBox == groupBoxes.get(i)) {
            return i;
          }
        }
        throw new IllegalStateException("selected tab not found in group-boxes list");
      }

      @Override
      protected Object modelValue() {
        return getModel().getSelectedTab();
      }
    });

    // FIXME CGU really needed?
    putJsonProperty(new JsonProperty<ITabBox>(ITabBox.PROP_MARK_STRATEGY, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMarkStrategy();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "TabBox";
  }

  @Override
  protected void createChildAdapters() {
    super.createChildAdapters();
    attachAdapters(getModel().getGroupBoxes());
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeAdapters(getModel().getGroupBoxes());
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), "tabItems", getModel().getGroupBoxes());
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (JsonEventType.SELECT.matches(event)) {
      handleUiTabSelected(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  private void handleUiTabSelected(JsonEvent event) {
    int tabIndex = event.getData().optInt("tabIndex");
    List<IGroupBox> groupBoxes = getModel().getGroupBoxes();
    if (tabIndex >= 0 && tabIndex < groupBoxes.size()) {
      IGroupBox groupBox = groupBoxes.get(tabIndex);
      // TODO AWE: (filter) anschauen --> evtl. filter am ende vom request abräumen
      PropertyChangeEventFilterCondition condition = new PropertyChangeEventFilterCondition(ITabBox.PROP_SELECTED_TAB, groupBox);
      getPropertyEventFilter().addCondition(condition);
      try {
        getModel().getUIFacade().setSelectedTabFromUI(groupBox);
      }
      finally {
        getPropertyEventFilter().removeCondition(condition);
      }
    }
  }
}
