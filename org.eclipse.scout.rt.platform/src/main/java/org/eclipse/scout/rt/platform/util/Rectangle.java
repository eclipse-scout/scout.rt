/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import java.io.Serializable;

public class Rectangle implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int m_x;
  private final int m_y;
  private final int m_width;
  private final int m_height;

  public Rectangle(int x, int y, int width, int height) {
    m_x = x;
    m_y = y;
    m_width = width;
    m_height = height;
  }

  public int getX() {
    return m_x;
  }

  public int getY() {
    return m_y;
  }

  public int getWidth() {
    return m_width;
  }

  public int getHeight() {
    return m_height;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_height;
    result = prime * result + m_width;
    result = prime * result + m_x;
    result = prime * result + m_y;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Rectangle other = (Rectangle) obj;
    if (m_height != other.m_height) {
      return false;
    }
    if (m_width != other.m_width) {
      return false;
    }
    if (m_x != other.m_x) {
      return false;
    }
    if (m_y != other.m_y) {
      return false;
    }
    return true;
  }
}
