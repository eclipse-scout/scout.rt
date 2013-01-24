/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext.gc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;

/**
 * <h3>Transform</h3> rwt does not yet support Tranform and advanced gc.
 * This helper supports for a limited set of transform functionality (no shearing).
 * <p>
 * 
 * <pre>
 * [x']   [u11 u12 | tx]    [x]
 * [y'] = [u21 u22 | ty]  * [y]
 *                          [1]
 * </pre>
 * 
 * @author imo
 * @since 3.7.0 June 2011
 */
public class Transform {
  /**
   * u11, u12, u21, u22, dx, dy
   */
  private final float[] m_matrix;

  public Transform(Device dummy) {
    m_matrix = new float[]{0, 0, 0, 0, 0, 0};
    identity();
  }

  public boolean isDisposed() {
    return true;
  }

  public void dispose() {
  }

  public void identity() {
    m_matrix[0] = 1;
    m_matrix[1] = 0;
    m_matrix[2] = 0;
    m_matrix[3] = 1;
    m_matrix[4] = 0;
    m_matrix[5] = 0;
  }

  public void scale(float scaleX, float scaleY) {
    m_matrix[0] *= scaleX;
    m_matrix[3] *= scaleY;
  }

  public void translate(float offsetX, float offsetY) {
    m_matrix[4] += offsetX;
    m_matrix[5] += offsetY;
  }

  /**
   * return the 2x3 matix to the out array in the order u11, u12, u21, u22, dx, dy
   */
  public void getElements(float[] out) {
    if (out == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
      return;
    }
    if (out.length < 6) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
      return;
    }
    System.arraycopy(m_matrix, 0, out, 0, 6);
  }
}
