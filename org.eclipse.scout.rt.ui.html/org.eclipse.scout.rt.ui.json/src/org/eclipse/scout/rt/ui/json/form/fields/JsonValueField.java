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
package org.eclipse.scout.rt.ui.json.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonException;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonValueField<T extends IValueField<?>> extends JsonFormField<T> {
  public static final String PROP_VALUE = IValueField.PROP_VALUE;
  public static final String PROP_DISPLAY_TEXT = IValueField.PROP_DISPLAY_TEXT;

  public JsonValueField(T model, IJsonSession session) {
    super(model, session);
  }

  @Override
  public String getObjectType() {
    return "ValueField";
  }

  @Override
  public JSONObject toJson() throws JsonException {
    JSONObject json = super.toJson();
    try {
//      json.put(PROP_VALUE, valueToJson()); //FIXME
      json.put(PROP_DISPLAY_TEXT, getModelObject().getDisplayText());
      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

}
