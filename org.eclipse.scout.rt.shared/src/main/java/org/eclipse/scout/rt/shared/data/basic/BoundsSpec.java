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
