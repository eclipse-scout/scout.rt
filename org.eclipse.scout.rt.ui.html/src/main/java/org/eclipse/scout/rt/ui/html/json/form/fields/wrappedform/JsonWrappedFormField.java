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
package org.eclipse.scout.rt.ui.html.json.form.fields.wrappedform;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonGlobalAdapterProperty;

public class JsonWrappedFormField<T extends IWrappedFormField<? extends IForm>> extends JsonFormField<T> {

  public JsonWrappedFormField(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WrappedFormField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonGlobalAdapterProperty<T>(IWrappedFormField.PROP_INNER_FORM, model, getJsonSession()) {
      @Override
      protected IForm modelValue() {
        return getModel().getInnerForm();
      }
    });
  }
}
