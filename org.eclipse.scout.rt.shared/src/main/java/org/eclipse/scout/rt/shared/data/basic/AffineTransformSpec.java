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
public class AffineTransformSpec implements Serializable {
  private static final long serialVersionUID = 1L;

  public double dx = 0;
  public double dy = 0;
  public double sx = 1;
  public double sy = 1;
  public double angle = 0;

  public AffineTransformSpec() {
    super();
  }

  public AffineTransformSpec(double dx, double dy, double sx, double sy, double angle) {
    this.dx = dx;
    this.dy = dy;
    this.sx = sx;
    this.sy = sy;
    this.angle = angle;
  }

  public AffineTransformSpec(AffineTransformSpec o) {
    if (o != null) {
      this.dx = o.dx;
      this.dy = o.dy;
      this.sx = o.sx;
      this.sy = o.sy;
      this.angle = o.angle;
    }
  }
}
