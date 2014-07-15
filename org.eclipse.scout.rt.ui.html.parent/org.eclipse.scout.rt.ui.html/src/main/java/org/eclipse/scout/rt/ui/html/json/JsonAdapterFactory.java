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
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.control.IAnalysisTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.IChartTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.IGraphTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.IMapTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton5;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.checkbox.ICheckBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.ui.html.json.action.keystroke.JsonKeyStroke;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonBreadCrumbNavigation;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktopTree;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonFormToolButton;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonViewButton;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.checkbox.JsonCheckBoxField;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.numberfield.JsonNumberField;
import org.eclipse.scout.rt.ui.html.json.form.fields.sequencebox.JsonSequenceBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.stringfield.JsonStringField;
import org.eclipse.scout.rt.ui.html.json.form.fields.tabbox.JsonTabBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.JsonTableField;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonAnalysisTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonChartTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonGraphTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonMapTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonTableControl;

/**
 * This factory creates IJsonAdapter instances for a given model object. You must call the <code>init()</code> method
 * on the return value from <code>createJsonAdapter()</code>.
 */
public class JsonAdapterFactory {

  @SuppressWarnings("unchecked")
  public IJsonAdapter createJsonAdapter(Object model, IJsonSession session, String id) {
    // form fields
    if (model instanceof IGroupBox) {
      return new JsonGroupBox((IGroupBox) model, session, id);
    }
    else if (model instanceof ISequenceBox) {
      return new JsonSequenceBox((ISequenceBox) model, session, id);
    }
    else if (model instanceof ITabBox) {
      return new JsonTabBox((ITabBox) model, session, id);
    }
    else if (model instanceof ICheckBox) {
      return new JsonCheckBoxField((IBooleanField) model, session, id);
    }
    else if (model instanceof ITableField<?>) {
      return new JsonTableField((ITableField) model, session, id);
    }
    else if (model instanceof IButton) {
      return new JsonButton((IButton) model, session, id);
    }
    else if (model instanceof IStringField) {
      return new JsonStringField((IStringField) model, session, id);
    }
    else if (model instanceof INumberField) {
      return new JsonNumberField((INumberField) model, session, id);
    }
    else if (model instanceof IFormField) {
      return new JsonFormField((IFormField) model, session, id);
    }
    // other model objects
    else if (model instanceof IContextMenu) {
      return new JsonContextMenu((IContextMenu) model, session, id);
    }
    else if (model instanceof IMenu) {
      return new JsonMenu((IMenu) model, session, id);
    }
    else if (model instanceof IKeyStroke) {
      return new JsonKeyStroke((IKeyStroke) model, session, id);
    }
    else if (model instanceof IForm) {
      return new JsonForm((IForm) model, session, id);
    }
    else if (model instanceof IViewButton) {
      return new JsonViewButton((IViewButton) model, session, id);
    }
    else if (model instanceof IFormToolButton) {
      return new NullAdapter(model, session, id);
    }
    else if (model instanceof IFormToolButton5) {
      return new JsonFormToolButton((IFormToolButton5) model, session, id);
    }
    else if (model instanceof IOutline) {
      return new JsonDesktopTree((IOutline) model, session, id);
    }
    else if (model instanceof ITable) {
      return new JsonTable((ITable) model, session, id);
    }
    else if (model instanceof IDesktop) {
      return new JsonDesktop((IDesktop) model, session, id);
    }
    else if (model instanceof IChartTableControl) {
      return new JsonChartTableControl((IChartTableControl) model, session, id);
    }
    else if (model instanceof IGraphTableControl) {
      return new JsonGraphTableControl((IGraphTableControl) model, session, id);
    }
    else if (model instanceof IMapTableControl) {
      return new JsonMapTableControl((IMapTableControl) model, session, id);
    }
    else if (model instanceof IAnalysisTableControl) {
      return new JsonAnalysisTableControl((IAnalysisTableControl) model, session, id);
    }
    else if (model instanceof ITableControl) {
      return new JsonTableControl((ITableControl) model, session, id);
    }
    else if (model instanceof IClientSession) {
      return new JsonClientSession((IClientSession) model, session, id);
    }
    else if (model instanceof IDataModel) {
      return new JsonDataModel((IDataModel) model, session, id);
    }
    else if (model instanceof IBreadCrumbsNavigation) {
      return new JsonBreadCrumbNavigation((IBreadCrumbsNavigation) model, session, id);
    }
    throw new IllegalArgumentException("Cannot create JSON-adapter for model-object " + model);
  }

  //FIXME CGU only needed temporarily, remove when switched to FormToolButton2
  public static class NullAdapter extends AbstractJsonAdapter<Object> {

    public NullAdapter(Object model, IJsonSession jsonSession, String id) {
      super(model, jsonSession, id);
    }

    @Override
    public String getObjectType() {
      return null;
    }

    @Override
    public void handleUiEvent(JsonEvent event, JsonResponse res) {
    }

  }
}
