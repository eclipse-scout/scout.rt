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
import org.json.JSONObject;

/**
 * Base class used to create JSON output for Scout form-fields with a value. Sub classes should overwrite the toJson()
 * method and add their own properties to the JSON string. Only properties required to render the UI should be added.
 * 
 * @param <T>
 */
public class JsonValueField<T extends IValueField<?>> extends JsonFormField<T> {

  public static final String PROP_VALUE = IValueField.PROP_VALUE;
  public static final String PROP_DISPLAY_TEXT = IValueField.PROP_DISPLAY_TEXT;

  public JsonValueField(T model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "ValueField";
  }

  @Override
  public JSONObject toJson() {
    return putProperty(super.toJson(), PROP_DISPLAY_TEXT, getModelObject().getDisplayText());
  }

}
