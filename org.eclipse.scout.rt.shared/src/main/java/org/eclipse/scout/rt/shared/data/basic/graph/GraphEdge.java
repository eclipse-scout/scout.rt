/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;

public class GraphEdge implements Serializable {
  private static final long serialVersionUID = 1L;

  private GraphNode m_source;
  private GraphNode m_target;
  private String m_label;
  private GraphLineStyle m_style;
  private boolean m_directed;

  protected GraphEdge() {
  }

  public static GraphEdge create() {
    return new GraphEdge();
  }

  public static GraphEdge create(GraphNode source, GraphNode target, String label, GraphLineStyle style, boolean directed) {
    return create()
        .withSource(source)
        .withTarget(target)
        .withLabel(label)
        .withStyle(style)
        .withDirected(directed);
  }

  public GraphEdge withSource(GraphNode source) {
    setSource(source);
    return this;
  }

  public GraphEdge withTarget(GraphNode target) {
    setTarget(target);
    return this;
  }

  public GraphEdge withLabel(String label) {
    setLabel(label);
    return this;
  }

  public GraphEdge withStyle(GraphLineStyle style) {
    setStyle(style);
    return this;
  }

  public GraphEdge withDirected(boolean directed) {
    setDirected(directed);
    return this;
  }

  public GraphNode getSource() {
    return m_source;
  }

  protected void setSource(GraphNode source) {
    m_source = source;
  }

  public GraphNode getTarget() {
    return m_target;
  }

  protected void setTarget(GraphNode target) {
    m_target = target;
  }

  public String getLabel() {
    return m_label;
  }

  protected void setLabel(String label) {
    m_label = label;
  }

  public GraphLineStyle getStyle() {
    return m_style;
  }

  protected void setStyle(GraphLineStyle style) {
    m_style = style;
  }

  public boolean isDirected() {
    return m_directed;
  }

  protected void setDirected(boolean directed) {
    m_directed = directed;
  }
}
