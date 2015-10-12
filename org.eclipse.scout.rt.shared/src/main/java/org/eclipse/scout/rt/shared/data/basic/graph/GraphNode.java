/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;

public class GraphNode implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_label;
  private final String m_url;
  private final GraphShape m_shape;
  private final GraphColor m_color;
  private final GraphNodeSize m_size;
  private final GraphCoordinate m_location;
  private final Long m_order;
  private final String m_cssClass;

  public GraphNode(String label, String url, GraphShape shape, GraphColor color, String cssClass, GraphNodeSize size, GraphCoordinate location, Long order) {
    m_label = label;
    m_url = url;
    m_shape = shape;
    m_color = color;
    m_cssClass = cssClass;
    m_size = size;
    m_location = location;
    m_order = order;
  }

  public String getLabel() {
    return m_label;
  }

  public String getUrl() {
    return m_url;
  }

  public GraphShape getShape() {
    return m_shape;
  }

  public GraphColor getColor() {
    return m_color;
  }

  public GraphNodeSize getSize() {
    return m_size;
  }

  public GraphCoordinate getLocation() {
    return m_location;
  }

  public Long getOrder() {
    return m_order;
  }

  public String getCssClass() {
    return m_cssClass;
  }
}
