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
package org.eclipse.scout.rt.ui.json.form.fields.groupbox;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.json.form.fields.JsonProperty;
import org.json.JSONObject;

/**
 * This class creates JSON output for an <code>IGroupBox</code>.
 */
public class JsonGroupBox extends JsonFormField<IGroupBox> {

  public JsonGroupBox(IGroupBox aGroupBox, IJsonSession session, String id) {
    super(aGroupBox, session, id);
    putJsonProperty(new JsonProperty<IGroupBox, String>(IGroupBox.PROP_BORDER_DECORATION, aGroupBox) {
      @Override
      protected String getValueImpl(IGroupBox groupBox) {
        return groupBox.getBorderDecoration();
      }
    });
    putJsonProperty(new JsonProperty<IGroupBox, Boolean>(IGroupBox.PROP_BORDER_VISIBLE, aGroupBox) {
      @Override
      protected Boolean getValueImpl(IGroupBox groupBox) {
        return groupBox.isBorderVisible();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "GroupBox";
  }

  @Override
  public JSONObject toJson() {
    return putProperty(super.toJson(), "formFields", modelObjectsToJson(getModelObject().getControlFields()));
  }

}
