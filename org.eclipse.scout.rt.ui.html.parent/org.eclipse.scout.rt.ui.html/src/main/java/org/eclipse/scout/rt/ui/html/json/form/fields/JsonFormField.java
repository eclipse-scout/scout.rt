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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonGridData;
import org.eclipse.scout.rt.ui.html.json.JsonProcessingStatus;

// TODO AWE: make JsonFormField abstract (later), direktes instanzieren soll nicht mehr möglich sein
public class JsonFormField<T extends IFormField> extends AbstractJsonPropertyObserver<T> implements IJsonFormField<T> {

  public JsonFormField(T model, IJsonSession session, String id) {
    super(model, session, id);
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_LABEL_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLabelVisible();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_MANDATORY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMandatory();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_ERROR_STATUS, model) {
      @Override
      protected IProcessingStatus modelValue() {
        return getModel().getErrorStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return "";
        }
        else {
          return new JsonProcessingStatus((IProcessingStatus) value).toJson();
        }
      }
    });
    putJsonProperty(new JsonProperty<T>(IJsonFormField.PROP_GRID_DATA, model) {
      @Override
      protected GridData modelValue() {
        return getModel().getGridData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonGridData((GridData) value).toJson();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "FormField";
  }
}
