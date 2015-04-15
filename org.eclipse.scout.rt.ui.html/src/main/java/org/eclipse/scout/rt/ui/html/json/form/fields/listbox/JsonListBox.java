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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonListBox<V, T extends IListBox<V>> extends JsonFormField<T> {

  public JsonListBox(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ListBox";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<IListBox<V>>("table", model, getUiSession()) {
      @Override
      protected ITable modelValue() {
        return getModel().getTable();
      }
    });

    if (getModel() instanceof AbstractListBox) {
      putJsonProperty(new JsonAdapterProperty<IListBox<V>>("filterBox", model, getUiSession()) {
        @Override
        protected AbstractListBoxFilterBox modelValue() {
          return ((AbstractListBox) getModel()).getListBoxFilterBox();
        }
      });
    }
  }
}
