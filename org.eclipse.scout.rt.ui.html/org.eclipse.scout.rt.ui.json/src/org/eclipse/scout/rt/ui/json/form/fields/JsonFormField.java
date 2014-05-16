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
import org.json.JSONObject;

public class JsonFormField<T extends IFormField> extends AbstractJsonPropertyObserverRenderer<T> implements IJsonFormField<T> {

  public JsonFormField(T model, IJsonSession session, String id) {
    super(model, session, id);
    delegateProperty(IFormField.PROP_LABEL);
    delegateProperty(IFormField.PROP_ENABLED);
    delegateProperty(IFormField.PROP_VISIBLE);
    delegateProperty(IFormField.PROP_MANDATORY);
  }

  @Override
  public String getObjectType() {
    return "FormField";
  }

  @Override
  public JSONObject toJson() {
    // TODO AWE: (ask C.GU) wollen wir das nicht mit delegateProperty zusammenlegen? sind vermutlich immer die gleichen properties.
    JSONObject json = super.toJson();
    putProperty(json, IFormField.PROP_LABEL, getModelObject().getLabel());
    putProperty(json, IFormField.PROP_ENABLED, getModelObject().isEnabled());
    putProperty(json, IFormField.PROP_VISIBLE, getModelObject().isVisible());
    putProperty(json, IFormField.PROP_MANDATORY, getModelObject().isMandatory());
    putProperty(json, IFormField.PROP_ERROR_STATUS, toJson(getModelObject().getErrorStatus()));
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
  }

  @Override
  protected void handleModelPropertyChange(String name, Object newValue) {
    super.handleModelPropertyChange(name, newValue);
    if (IFormField.PROP_ERROR_STATUS.equals(name)) {
      handleErrorStatusChanged((IProcessingStatus) newValue);
    }
  }

  // TODO AWE: (ask C.GU) eigentlich wäre es hübsch wenn wir das auch über delegateProperty machen könnten, aber weil das ein komplexes
  // object ist müssten wir noch eine Art Transformation mitgeben können. Das Interface für JsonPropertyDelegate könnte etwa so aussehen
  // -String getPropertyName()
  // -JSONObject getValue() // default impl. gibt z.B. isVisible() zurück, spezielle impls. können noch eine transformation machen
  //    wie hier unten
  private void handleErrorStatusChanged(IProcessingStatus scoutStatus) {
    getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), IFormField.PROP_ERROR_STATUS, toJson(scoutStatus));
  }

  private Object toJson(IProcessingStatus scoutStatus) {
    // TODO AWE: (ask C.GU) wie "löschen" wir eine property? Siehe fix-me in addPropertyChangeEvent()
    if (scoutStatus == null) {
      return "";
    }
    else {
      return new JsonProcessingStatus(scoutStatus).toJson();
    }
  }

}
