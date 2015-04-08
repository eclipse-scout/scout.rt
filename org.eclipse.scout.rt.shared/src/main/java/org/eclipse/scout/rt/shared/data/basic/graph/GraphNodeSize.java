/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;

public class GraphNodeSize implements Serializable {
  private static final long serialVersionUID = 1L;

  private final int m_width;
  private final int m_height;

  public GraphNodeSize(int width, int height) {
    m_width = width;
    m_height = height;
  }

  public int getWidth() {
    return m_width;
  }

  public int getHeight() {
    return m_height;
  }
}
