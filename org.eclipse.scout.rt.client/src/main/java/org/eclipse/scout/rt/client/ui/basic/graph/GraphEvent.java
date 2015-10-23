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
package org.eclipse.scout.rt.client.ui.basic.graph;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphNode;

/**
 * @since 5.2
 */
public class GraphEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_NODE_ACTION = 10;

  private final int m_type;

  private GraphNode m_node;

  public GraphEvent(IGraph source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public IGraph getSource() {
    return (IGraph) super.getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  protected GraphNode getNode() {
    return m_node;
  }

  protected void setNode(GraphNode node) {
    m_node = node;
  }
}
