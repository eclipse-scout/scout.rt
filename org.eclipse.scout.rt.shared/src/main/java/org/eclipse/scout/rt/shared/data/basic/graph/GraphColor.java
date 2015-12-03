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

public class GraphColor implements Serializable {
  private static final long serialVersionUID = 1L;

  private String m_foreground;
  private String m_background;

  protected GraphColor() {
  }

  public static GraphColor create() {
    return new GraphColor();
  }

  public static GraphColor create(String foreground, String background) {
    return create()
        .withForeground(foreground)
        .withBackground(background);
  }

  public GraphColor withForeground(String foreground) {
    setForeground(foreground);
    return this;
  }

  public GraphColor withBackground(String background) {
    setBackground(background);
    return this;
  }

  public String getForeground() {
    return m_foreground;
  }

  protected void setForeground(String foreground) {
    m_foreground = foreground;
  }

  public String getBackground() {
    return m_background;
  }

  protected void setBackground(String background) {
    m_background = background;
  }
}
