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
package org.eclipse.scout.rt.ui.html.json.form.fields.listbox;

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonListBox<V, T extends IListBox<V>> extends JsonFormField<T> {

  public JsonListBox(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    //TODO nbu check update of table
    putJsonProperty(new JsonAdapterProperty<IListBox<V>>("table", model, getJsonSession()) {
      @Override
      protected ITable modelValue() {
        return getModel().getTable();
      }
    });

    if (getModel() instanceof AbstractListBox) {
      putJsonProperty(new JsonAdapterProperty<IListBox<V>>("filterBox", model, getJsonSession()) {
        @Override
        protected AbstractListBoxFilterBox modelValue() {
          return ((AbstractListBox) getModel()).getListBoxFilterBox();
        }
      });
    }
    //TODO NBU implement client side processing
    putJsonProperty(new JsonProperty<IListBox<V>>(IListBox.PROP_FILTER_ACTIVE_ROWS_VALUE, model) {
      @Override
      protected TriState modelValue() {
        return getModel().getFilterActiveRowsValue();
      }
    });
    //TODO NBU implement client side processing
    putJsonProperty(new JsonProperty<IListBox<V>>(IListBox.PROP_FILTER_CHECKED_ROWS_VALUE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getFilterCheckedRowsValue();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "ListBox";
  }

}
