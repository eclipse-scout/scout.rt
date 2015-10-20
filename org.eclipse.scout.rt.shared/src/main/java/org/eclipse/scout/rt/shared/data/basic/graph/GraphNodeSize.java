/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;

public class GraphNodeSize implements Serializable {
  private static final long serialVersionUID = 1L;

  private int m_width;
  private int m_height;

  protected GraphNodeSize() {
  }

  public static GraphNodeSize create() {
    return new GraphNodeSize();
  }

  public static GraphNodeSize create(int width, int height) {
    return create();
  }

  public GraphNodeSize withWidth(int width) {
    setWidth(width);
    return this;
  }

  public GraphNodeSize withHeight(int height) {
    setHeight(height);
    return this;
  }

  public int getWidth() {
    return m_width;
  }

  protected void setWidth(int width) {
    m_width = width;
  }

  public int getHeight() {
    return m_height;
  }

  protected void setHeight(int height) {
    m_height = height;
  }
}
