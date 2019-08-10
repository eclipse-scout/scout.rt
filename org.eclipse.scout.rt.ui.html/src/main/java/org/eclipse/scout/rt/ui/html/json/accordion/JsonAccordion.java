/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.accordion;

import java.util.List;

import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

public class JsonAccordion<T extends IAccordion> extends AbstractJsonWidget<T> {

  public JsonAccordion(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Accordion";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<T>(IAccordion.PROP_GROUPS, model, getUiSession()) {
      @Override
      protected List<? extends IGroup> modelValue() {
        return getModel().getGroups();
      }
    });
    putJsonProperty(new JsonProperty<T>(IAccordion.PROP_EXCLUSIVE_EXPAND, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isExclusiveExpand();
      }
    });
    putJsonProperty(new JsonProperty<T>(IAccordion.PROP_SCROLLABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollable();
      }
    });
  }
}
