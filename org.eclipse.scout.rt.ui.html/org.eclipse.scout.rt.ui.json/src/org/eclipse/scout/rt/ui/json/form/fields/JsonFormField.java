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

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.json.JSONObject;

public class JsonFormField<T extends IFormField> extends AbstractJsonPropertyObserverRenderer<T> implements IJsonFormField<T> {

  public JsonFormField(T model, IJsonSession session) {
    super(model, session);
  }

  @Override
  public String getObjectType() {
    return "FormField";
  }

  @Override
  public JSONObject toJson() {
    return putProperty(super.toJson(), "label", getModelObject().getLabel());
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
  }

}
