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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonProcessingStatus;
import org.eclipse.scout.rt.ui.json.JsonResponse;

public class JsonFormField<T extends IFormField> extends AbstractJsonPropertyObserverRenderer<T> implements IJsonFormField<T> {

  public JsonFormField(T model, IJsonSession session, String id) {
    super(model, session, id);
    putJsonProperty(new JsonProperty<T, String>(IFormField.PROP_LABEL, model) {
      @Override
      protected String getValueImpl(T modelObject) {
        return modelObject.getLabel();
      }
    });
    putJsonProperty(new JsonProperty<T, Boolean>(IFormField.PROP_ENABLED, model) {
      @Override
      protected Boolean getValueImpl(T modelObject) {
        return modelObject.isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<T, Boolean>(IFormField.PROP_VISIBLE, model) {
      @Override
      protected Boolean getValueImpl(T modelObject) {
        return modelObject.isVisible();
      }
    });
    putJsonProperty(new JsonProperty<T, Boolean>(IFormField.PROP_MANDATORY, model) {
      @Override
      protected Boolean getValueImpl(T modelObject) {
        return modelObject.isMandatory();
      }
    });
    putJsonProperty(new JsonProperty<T, IProcessingStatus>(IFormField.PROP_ERROR_STATUS, model) {
      @Override
      protected IProcessingStatus getValueImpl(T modelObject) {
        return modelObject.getErrorStatus();
      }

      @Override
      public Object valueToJson(Object value) {
        if (value == null) {
          return "";
        }
        else {
          return new JsonProcessingStatus((IProcessingStatus) value).toJson();
        }
      }
    });
  }

  @Override
  public String getObjectType() {
    return "FormField";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
  }

}
