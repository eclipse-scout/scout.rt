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

public class GraphCoordinate implements Serializable {
  private static final long serialVersionUID = 1L;

  private int m_x;
  private int m_y;

  protected GraphCoordinate() {
  }

  public static GraphCoordinate create() {
    return new GraphCoordinate();
  }

  public static GraphCoordinate create(int x, int y) {
    return create()
        .withX(x)
        .withY(y);
  }

  public GraphCoordinate withX(int x) {
    setX(x);
    return this;
  }

  public GraphCoordinate withY(int y) {
    setY(y);
    return this;
  }

  public int getX() {
    return m_x;
  }

  protected void setX(int x) {
    m_x = x;
  }

  public int getY() {
    return m_y;
  }

  protected void setY(int y) {
    m_y = y;
  }
}
