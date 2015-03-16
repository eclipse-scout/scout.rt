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
package org.eclipse.scout.rt.ui.html.json.form.fields.groupbox;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonCompositeField;

/**
 * This class creates JSON output for an <code>IGroupBox</code>.
 */
public class JsonGroupBox<T extends IGroupBox> extends JsonCompositeField<T, IFormField> {

  // from UI
  public static final String EVENT_EXPANDED = "expanded";

  public static final String PROP_MAIN_BOX = "mainBox";
  public static final String PROP_SCROLLABLE = "scrollable";

  public JsonGroupBox(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "GroupBox";
  }

  @Override
  protected void initJsonProperties(T model) {
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
        return getModel().isScrollable();
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
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_EXPANDED.equals(event.getType())) {
      handleUiExpanded(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  protected void handleUiExpanded(JsonEvent event) {
    boolean expanded = event.getData().optBoolean("expanded");
    addPropertyEventFilterCondition(IGroupBox.PROP_EXPANDED, expanded);
    getModel().getUIFacade().setExpandedFromUI(expanded);
  }
}
