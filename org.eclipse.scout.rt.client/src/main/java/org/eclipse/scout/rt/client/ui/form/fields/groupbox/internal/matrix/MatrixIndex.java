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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public class MatrixIndex {

  public int x = 0;
  public int y = 0;

  public MatrixIndex(int x, int y) {
    this.x = x;
    this.y = y;
  }

  MatrixIndex(MatrixIndex index) {
    this(index.x, index.y);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + x;
    result = prime * result + y;
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
    MatrixIndex other = (MatrixIndex) obj;
    if (x != other.x) {
      return false;
    }
    if (y != other.y) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Index [" + x + "," + y + "]";
  }
}
