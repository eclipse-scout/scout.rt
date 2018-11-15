/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.group;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;

public class JsonGroup<T extends IGroup> extends AbstractJsonWidget<T> {

  public JsonGroup(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Group";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_COLLAPSED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCollapsed();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_COLLAPSIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCollapsible();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_COLLAPSE_STYLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getCollapseStyle();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_TITLE_SUFFIX, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitleSuffix();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });
    putJsonProperty(new JsonAdapterProperty<T>(IGroup.PROP_HEADER, model, getUiSession()) {
      @Override
      protected IWidget modelValue() {
        return getModel().getHeader();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_HEADER_FOCUSABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHeaderFocusable();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_HEADER_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHeaderVisible();
      }
    });
    putJsonProperty(new JsonAdapterProperty<T>(IGroup.PROP_BODY, model, getUiSession()) {
      @Override
      protected IWidget modelValue() {
        return getModel().getBody();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_BODY_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBodyVisible();
      }
    });
    putJsonProperty(new JsonProperty<T>(IGroup.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IGroup.PROP_COLLAPSED.equals(propertyName)) {
      boolean collapsed = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, collapsed);
      getModel().getUIFacade().setCollapsedFromUI(collapsed);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }
}
