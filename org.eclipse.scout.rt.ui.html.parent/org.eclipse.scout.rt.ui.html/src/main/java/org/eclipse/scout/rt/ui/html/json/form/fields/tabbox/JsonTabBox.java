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
package org.eclipse.scout.rt.ui.html.json.form.fields.tabbox;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.json.JSONObject;

public class JsonTabBox extends JsonFormField<ITabBox> {

  public JsonTabBox(ITabBox model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  protected void initProperties(ITabBox model) {
    super.initProperties(model);

    putJsonProperty(new JsonAdapterProperty<ITabBox>(ITabBox.PROP_SELECTED_TAB, model, getJsonSession()) {
      @Override
      protected IGroupBox modelValue() {
        return getModel().getSelectedTab();
      }
    });

    //FIXME CGU really needed?
    putJsonProperty(new JsonProperty<ITabBox>(ITabBox.PROP_MARK_STRATEGY, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMarkStrategy();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "TabBox";
  }

  @Override
  protected void createChildAdapters() {
    super.createChildAdapters();
    attachAdapters(getModel().getGroupBoxes());
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeAdapters(getModel().getGroupBoxes());
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), "groupBoxes", getModel().getGroupBoxes());
  }

}
