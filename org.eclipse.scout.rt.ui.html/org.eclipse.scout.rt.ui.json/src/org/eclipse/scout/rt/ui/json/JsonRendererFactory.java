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
package org.eclipse.scout.rt.ui.json;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.checkbox.ICheckBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.json.desktop.JsonDesktopTree;
import org.eclipse.scout.rt.ui.json.desktop.JsonViewButton;
import org.eclipse.scout.rt.ui.json.form.JsonForm;
import org.eclipse.scout.rt.ui.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.json.form.fields.checkbox.JsonCheckBoxField;
import org.eclipse.scout.rt.ui.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.json.form.fields.rangebox.JsonSequenceBox;
import org.eclipse.scout.rt.ui.json.form.fields.tablefield.JsonTableField;
import org.eclipse.scout.rt.ui.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.json.table.JsonTable;

public class JsonRendererFactory {

  public JsonClientSession createJsonClientSession(IClientSession model, IJsonSession session, String id) {
    return new JsonClientSession(model, session, id);
  }

  @SuppressWarnings("unchecked")
  public IJsonRenderer createJsonRenderer(Object modelObject, IJsonSession session) {
    // form fields
    if (modelObject instanceof IGroupBox) {
      return init(new JsonGroupBox((IGroupBox) modelObject, session));
    }
    else if (modelObject instanceof ISequenceBox) {
      return init(new JsonSequenceBox((ISequenceBox) modelObject, session));
    }
    else if (modelObject instanceof ICheckBox) {
      return init(new JsonCheckBoxField((IBooleanField) modelObject, session));
    }
    else if (modelObject instanceof ITableField<?>) {
      return init(new JsonTableField((ITableField) modelObject, session));
    }
    else if (modelObject instanceof IFormField) {
      // TODO AWE: direktes instanzieren von form-field verbieten
      return init(new JsonFormField((IFormField) modelObject, session));
    }
    // other model objects
    else if (modelObject instanceof IMenu) {
      return init(new JsonMenu((IMenu) modelObject, session));
    }
    else if (modelObject instanceof IForm) {
      return init(new JsonForm((IForm) modelObject, session));
    }
    else if (modelObject instanceof IViewButton) {
      return init(new JsonViewButton((IViewButton) modelObject, session));
    }
    else if (modelObject instanceof IOutline) {
      return init(new JsonDesktopTree((IOutline) modelObject, session));
    }
    else if (modelObject instanceof ITable) {
      return init(new JsonTable((ITable) modelObject, session));
    }
    else if (modelObject instanceof IDesktop) {
      return init(new JsonDesktop((IDesktop) modelObject, session));
    }
    throw new IllegalArgumentException("Cannot create JSON-renderer for model-object " + modelObject);
  }

  private <T extends IJsonRenderer> T init(T jsonRenderer) {
    jsonRenderer.init();
    return jsonRenderer;
  }

}
