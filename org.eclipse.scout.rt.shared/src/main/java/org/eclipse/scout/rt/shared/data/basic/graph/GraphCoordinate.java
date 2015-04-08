/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;

public class GraphCoordinate implements Serializable {
  private static final long serialVersionUID = 1L;

  private final int m_x;
  private final int m_y;

  public GraphCoordinate(int x, int y) {
    m_x = x;
    m_y = y;
  }

  public int getX() {
    return m_x;
  }

  public int getY() {
    return m_y;
  }
}
