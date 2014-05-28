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
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigation;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
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
import org.eclipse.scout.rt.ui.html.json.desktop.JsonBreadCrumbNavigation;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktopTree;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonViewButton;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.form.fields.checkbox.JsonCheckBoxField;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.rangebox.JsonSequenceBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.JsonTableField;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;

/**
 * This factory creates IJsonAdapter instances for a given model object. You must call the <code>init()</code> method
 * on the return value from <code>createJsonAdapter()</code>.
 */
public class JsonAdapterFactory {

  @SuppressWarnings("unchecked")
  public IJsonAdapter createJsonAdapter(Object modelObject, IJsonSession session, String id) {
    // form fields
    if (modelObject instanceof IGroupBox) {
      return new JsonGroupBox((IGroupBox) modelObject, session, id);
    }
    else if (modelObject instanceof ISequenceBox) {
      return new JsonSequenceBox((ISequenceBox) modelObject, session, id);
    }
    else if (modelObject instanceof ICheckBox) {
      return new JsonCheckBoxField((IBooleanField) modelObject, session, id);
    }
    else if (modelObject instanceof ITableField<?>) {
      return new JsonTableField((ITableField) modelObject, session, id);
    }
    else if (modelObject instanceof IFormField) {
      // TODO AWE: direktes instanzieren von form-field verbieten
      return new JsonFormField((IFormField) modelObject, session, id);
    }
    // other model objects
    else if (modelObject instanceof IContextMenu) {
      return new JsonContextMenu((IContextMenu) modelObject, session, id);
    }
    else if (modelObject instanceof IMenu) {
      return new JsonMenu((IMenu) modelObject, session, id);
    }
    else if (modelObject instanceof IForm) {
      return new JsonForm((IForm) modelObject, session, id);
    }
    else if (modelObject instanceof IViewButton) {
      return new JsonViewButton((IViewButton) modelObject, session, id);
    }
    else if (modelObject instanceof IOutline) {
      return new JsonDesktopTree((IOutline) modelObject, session, id);
    }
    else if (modelObject instanceof ITable) {
      return new JsonTable((ITable) modelObject, session, id);
    }
    else if (modelObject instanceof IDesktop) {
      return new JsonDesktop((IDesktop) modelObject, session, id);
    }
    else if (modelObject instanceof IClientSession) {
      return new JsonClientSession((IClientSession) modelObject, session, id);
    }
    else if (modelObject instanceof IBreadCrumbsNavigation) {
      return new JsonBreadCrumbNavigation((IBreadCrumbsNavigation) modelObject, session, id);
    }
    throw new IllegalArgumentException("Cannot create JSON-adapter for model-object " + modelObject);
  }

}
