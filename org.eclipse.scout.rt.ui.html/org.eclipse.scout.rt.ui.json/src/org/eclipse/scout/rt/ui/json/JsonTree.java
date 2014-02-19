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

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.json.JSONObject;

public class JsonTree<T extends ITree> extends AbstractJsonRenderer<T> {
  private P_ModelTreeListener m_modelTreeListener;

  public JsonTree(T scoutObject, IJsonSession jsonSession) {
    super(scoutObject, jsonSession);
  }

  @Override
  protected void attachModel() throws JsonUIException {
    super.attachModel();
    if (m_modelTreeListener == null) {
      m_modelTreeListener = new P_ModelTreeListener();
      getModelObject().addUITreeListener(m_modelTreeListener);
    }
  }

  @Override
  protected void detachModel() throws JsonUIException {
    super.detachModel();
    if (m_modelTreeListener != null) {
      getModelObject().removeTreeListener(m_modelTreeListener);
      m_modelTreeListener = null;
    }
  }

  @Override
  public JSONObject toJson() throws JsonUIException {
    return null;
  }

  @Override
  public void handleUiEvent(JsonRequest req, JsonResponse res) throws JsonUIException {
  }

  protected void handleModelTreeEvent(TreeEvent event) {
    switch (event.getType()) {
      case TreeEvent.TYPE_NODES_INSERTED: {

        break;
      }
    }
  }

  protected void handleModelTreeEventBatch(List<? extends TreeEvent> events) {

  }

  private class P_ModelTreeListener implements TreeListener {
    @Override
    public void treeChanged(final TreeEvent e) {
      handleModelTreeEvent(e);
    }

    @Override
    public void treeChangedBatch(List<? extends TreeEvent> events) {
      handleModelTreeEventBatch(events);
    }
  }
}
