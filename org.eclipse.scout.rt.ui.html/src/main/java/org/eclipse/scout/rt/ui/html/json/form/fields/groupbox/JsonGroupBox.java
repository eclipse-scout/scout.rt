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
package org.eclipse.scout.rt.ui.html.json.form.fields.groupbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonCompositeField;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONObject;

/**
 * This class creates JSON output for an <code>IGroupBox</code>.
 */
public class JsonGroupBox<GROUP_BOX extends IGroupBox> extends JsonCompositeField<GROUP_BOX, IFormField> implements IJsonContextMenuOwner {

  // from UI
  public static final String EVENT_EXPANDED = "expanded";

  public static final String PROP_MAIN_BOX = "mainBox";
  public static final String PROP_SCROLLABLE = "scrollable";

  public JsonGroupBox(GROUP_BOX model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "GroupBox";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
  }

  @Override
  protected void initJsonProperties(GROUP_BOX model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<IGroupBox>(IGroupBox.PROP_BORDER_DECORATION, model) {
      @Override
      protected String modelValue() {
        return getModel().getBorderDecoration();
      }
    });
    putJsonProperty(new JsonProperty<IGroupBox>(IGroupBox.PROP_BORDER_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBorderVisible();
      }
    });
    putJsonProperty(new JsonProperty<IGroupBox>(PROP_MAIN_BOX, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMainBox();
      }
    });
    putJsonProperty(new JsonProperty<IGroupBox>(PROP_SCROLLABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollable().isTrue();
      }
    });
    putJsonProperty(new JsonProperty<IGroupBox>(IGroupBox.PROP_EXPANDABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isExpandable();
      }
    });
    putJsonProperty(new JsonProperty<IGroupBox>(IGroupBox.PROP_EXPANDED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isExpanded();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      json.put(PROP_MENUS, jsonContextMenu.childActionsToJson());
    }
    return json;
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IGroupBox.PROP_EXPANDED.equals(propertyName)) {
      boolean expanded = data.getBoolean(IGroupBox.PROP_EXPANDED);
      addPropertyEventFilterCondition(IGroupBox.PROP_EXPANDED, expanded);
      getModel().getUIFacade().setExpandedFromUI(expanded);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }
}
