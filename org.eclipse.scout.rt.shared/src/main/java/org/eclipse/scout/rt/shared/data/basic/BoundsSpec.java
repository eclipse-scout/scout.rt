/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.basic;

import java.io.Serializable;

@SuppressWarnings({"squid:S00116", "squid:ClassVariableVisibilityCheck"})
public class BoundsSpec implements Serializable {
  private static final long serialVersionUID = 1L;

  public int x;
  public int y;
  public int width;
  public int height;

  public BoundsSpec() {
    super();
  }

  public BoundsSpec(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
}
