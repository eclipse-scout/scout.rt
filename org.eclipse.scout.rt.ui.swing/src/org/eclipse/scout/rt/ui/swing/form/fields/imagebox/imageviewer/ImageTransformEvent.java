/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields.imagebox.imageviewer;

import java.util.EventObject;

public class ImageTransformEvent extends EventObject {

  private static final long serialVersionUID = 1L;
  private double m_dx;
  private double m_dy;
  private double m_sx;
  private double m_sy;
  private double m_angle;

  public ImageTransformEvent(Object source, double dx, double dy, double sx, double sy, double angle) {
    super(source);
    m_dx = dx;
    m_dy = dy;
    m_sx = sx;
    m_sy = sy;
    m_angle = angle;
  }

  public SwingImageViewer getSwingImageViewer() {
    return (SwingImageViewer) getSource();
  }

  public double getAngle() {
    return m_angle;
  }

  public void setAngle(double angle) {
    m_angle = angle;
  }

  public double getDx() {
    return m_dx;
  }

  public void setDx(double dx) {
    m_dx = dx;
  }

  public double getDy() {
    return m_dy;
  }

  public void setDy(double dy) {
    m_dy = dy;
  }

  public double getSx() {
    return m_sx;
  }

  public void setSx(double sx) {
    m_sx = sx;
  }

  public double getSy() {
    return m_sy;
  }

  public void setSy(double sy) {
    m_sy = sy;
  }

}
