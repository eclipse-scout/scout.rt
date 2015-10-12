/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;

public class GraphEdge implements Serializable {
  private static final long serialVersionUID = 1L;

  private final GraphNode m_source;
  private final GraphNode m_target;
  private final String m_label;
  private final GraphLineStyle m_style;
  private final boolean m_directed;

  public GraphEdge(GraphNode start, GraphNode end, String label, GraphLineStyle style, boolean directed) {
    m_source = start;
    m_target = end;
    m_label = label;
    m_style = style;
    m_directed = directed;
  }

  public GraphNode getSource() {
    return m_source;
  }

  public GraphNode getTarget() {
    return m_target;
  }

  public String getLabel() {
    return m_label;
  }

  public GraphLineStyle getStyle() {
    return m_style;
  }

  public boolean isDirected() {
    return m_directed;
  }
}
