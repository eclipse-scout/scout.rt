/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
