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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;

/**
 * Base class used to create JSON output for Scout form-fields with a value. When a sub-class need to provide a custom
 * <code>valueToJson()</code> method for the value property, it should replace the default JsonProperty for PROP_VALUE ,
 * with it's own implementation by calling <code>putJsonProperty()</code>.
 * 
 * @param <T>
 */
public class JsonValueField<T extends IValueField<?>> extends JsonFormField<T> {

  public JsonValueField(T model, IJsonSession session, String id) {
    super(model, session, id);

    putJsonProperty(new JsonProperty<T>(IValueField.PROP_DISPLAY_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayText();
      }
    });
    // TODO AWE: vermutlich sollten wir den value nicht schicken, für text-felder wird immer der display-text
    // benötigt. Für checkbox / radio könnten wir eine eigene implementierung für value machen
    putJsonProperty(new JsonProperty<T>(IValueField.PROP_VALUE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getValue();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "ValueField";
  }

}
