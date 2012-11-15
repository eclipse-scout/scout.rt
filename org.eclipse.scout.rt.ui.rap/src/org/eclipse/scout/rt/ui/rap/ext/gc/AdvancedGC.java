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

import java.util.Arrays;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>AdvancedGC</h3> rwt does not yet support Tranform and advanced gc.
 * This helper supports for a limited set of transform functionality (no shearing).
 * 
 * @author imo
 * @since 3.7.0 June 2011
 */
public class AdvancedGC {
  private GC m_gc;
  private Transform m_txRef;
  /**
   * u11, u12, u21, u22, dx, dy
   */
  private final float[] m_matrix = new float[]{1, 0, 0, 1, 0, 0};

  public AdvancedGC(GC gc) {
    m_gc = gc;
  }

  public void setTransform(Transform t) {
    m_txRef = t;
    if (m_txRef != null) {
      m_txRef.getElements(m_matrix);
    }
    else {
      Arrays.fill(m_matrix, 0f);
      m_matrix[0] = 1;
      m_matrix[3] = 1;
    }
  }

  public Transform getTransform() {
    return m_txRef;
  }

  public boolean isDisposed() {
    return m_gc.isDisposed();
  }

  public void dispose() {
    m_gc.dispose();
  }

  public void setBackground(Color c) {
    m_gc.setBackground(c);
  }

  public void setForeground(Color c) {
    m_gc.setForeground(c);
  }

  public void setFont(Font f) {
    m_gc.setFont(f);
  }

  public void setAlpha(int alpha) {
    m_gc.setAlpha(alpha);
  }

  public void drawOval(int x, int y, int w, int h) {
    m_gc.drawOval(tx(x), ty(y), tw(w), th(h));
  }

  public void drawRectangle(int x, int y, int w, int h) {
    m_gc.drawRectangle(tx(x), ty(y), tw(w), th(h));
  }

  public void drawPolygon(int[] pointArray) {
    int[] p = new int[pointArray.length];
    for (int i = 0; i < p.length; i = i + 2) {
      p[i] = tx(pointArray[i]);
      p[i + 1] = ty(pointArray[i + 1]);
    }
    m_gc.drawPolygon(p);
  }

  public void drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight) {
    m_gc.drawImage(image, srcX, srcY, srcWidth, srcHeight, tx(destX), ty(destY), tw(destWidth), th(destHeight));
  }

  public void drawString(String s, int x, int y, boolean transparent) {
    m_gc.drawString(s, tx(x), ty(y), transparent);
  }

  public void fillOval(int x, int y, int w, int h) {
    m_gc.fillOval(tx(x), ty(y), tw(w), th(h));
  }

  public void fillRectangle(int x, int y, int w, int h) {
    m_gc.fillRectangle(tx(x), ty(y), tw(w), th(h));
  }

  public void fillPolygon(int[] pointArray) {
    int[] p = new int[pointArray.length];
    for (int i = 0; i < p.length; i = i + 2) {
      p[i] = tx(pointArray[i]);
      p[i + 1] = ty(pointArray[i + 1]);
    }
    m_gc.fillPolygon(p);
  }

  private int tx(int x) {
    return (int) (m_matrix[0] * x + m_matrix[4]);
  }

  private int ty(int y) {
    return (int) (m_matrix[3] * y + m_matrix[5]);
  }

  private int tw(int w) {
    return (int) (m_matrix[0] * w);
  }

  private int th(int h) {
    return (int) (m_matrix[3] * h);
  }

}
