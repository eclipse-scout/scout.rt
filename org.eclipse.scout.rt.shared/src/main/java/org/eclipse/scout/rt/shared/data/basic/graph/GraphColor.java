/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.data.basic.graph;

import java.io.Serializable;

public class GraphColor implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_foreground;
  private final String m_background;

  public GraphColor(String foreground, String background) {
    m_foreground = foreground;
    m_background = background;
  }

  public String getForeground() {
    return m_foreground;
  }

  public String getBackground() {
    return m_background;
  }
}
