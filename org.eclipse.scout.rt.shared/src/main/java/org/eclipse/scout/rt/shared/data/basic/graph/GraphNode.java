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

public class GraphNode implements Serializable {
  private static final long serialVersionUID = 1L;

  private String m_label;
  private String m_url;
  private GraphShape m_shape;
  private GraphColor m_color;
  private String m_cssClass;
  private GraphNodeSize m_size;
  private GraphCoordinate m_location;
  private Long m_order;

  protected GraphNode() {
  }

  public static GraphNode create() {
    return new GraphNode();
  }

  @SuppressWarnings("squid:S00107")
  public static GraphNode create(String label, String url, GraphShape shape, GraphColor color, String cssClass, GraphNodeSize size, GraphCoordinate location, Long order) {
    return create()
        .withLabel(label)
        .withUrl(url)
        .withShape(shape)
        .withColor(color)
        .withCssClass(cssClass)
        .withSize(size)
        .withLocation(location)
        .withOrder(order);
  }

  public GraphNode withLabel(String label) {
    setLabel(label);
    return this;
  }

  public GraphNode withUrl(String url) {
    setUrl(url);
    return this;
  }

  public GraphNode withShape(GraphShape shape) {
    setShape(shape);
    return this;
  }

  public GraphNode withColor(GraphColor color) {
    setColor(color);
    return this;
  }

  public GraphNode withCssClass(String cssClass) {
    setCssClass(cssClass);
    return this;
  }

  public GraphNode withSize(GraphNodeSize size) {
    setSize(size);
    return this;
  }

  public GraphNode withLocation(GraphCoordinate location) {
    setLocation(location);
    return this;
  }

  public GraphNode withOrder(Long order) {
    setOrder(order);
    return this;
  }

  public String getLabel() {
    return m_label;
  }

  protected void setLabel(String label) {
    m_label = label;
  }

  public String getUrl() {
    return m_url;
  }

  protected void setUrl(String url) {
    m_url = url;
  }

  public GraphShape getShape() {
    return m_shape;
  }

  protected void setShape(GraphShape shape) {
    m_shape = shape;
  }

  public GraphColor getColor() {
    return m_color;
  }

  protected void setColor(GraphColor color) {
    m_color = color;
  }

  public String getCssClass() {
    return m_cssClass;
  }

  protected void setCssClass(String cssClass) {
    m_cssClass = cssClass;
  }

  public GraphNodeSize getSize() {
    return m_size;
  }

  protected void setSize(GraphNodeSize size) {
    m_size = size;
  }

  public GraphCoordinate getLocation() {
    return m_location;
  }

  protected void setLocation(GraphCoordinate location) {
    m_location = location;
  }

  public Long getOrder() {
    return m_order;
  }

  protected void setOrder(Long order) {
    m_order = order;
  }
}
