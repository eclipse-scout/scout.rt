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
package org.eclipse.scout.rt.ui.html.json.table.control;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.graph.IGraph;
import org.eclipse.scout.rt.client.ui.basic.table.control.IGraphTableControl;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

public class JsonGraphTableControl<GRAPH_TABLE_CONTROL extends IGraphTableControl> extends JsonTableControl<GRAPH_TABLE_CONTROL> {

  public JsonGraphTableControl(GRAPH_TABLE_CONTROL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "GraphTableControl";
  }

  @Override
  protected void initJsonProperties(GRAPH_TABLE_CONTROL model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<GRAPH_TABLE_CONTROL>(IGraphTableControl.PROP_GRAPH, model, getUiSession()) {
      @Override
      protected IGraph modelValue() {
        return getModel().getGraph();
      }

      @Override
      public boolean accept() {
        return getModel().isSelected();
      }
    });
    getJsonProperty(IAction.PROP_SELECTED).addLazyProperty(getJsonProperty(IGraphTableControl.PROP_GRAPH));
  }
}
