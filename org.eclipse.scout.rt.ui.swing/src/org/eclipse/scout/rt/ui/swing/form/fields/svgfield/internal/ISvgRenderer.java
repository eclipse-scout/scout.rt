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
package org.eclipse.scout.rt.ui.swing.form.fields.svgfield.internal;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;

/**
 * Renderer for scout svg {@link ScoutSVGModel} pictures
 */
public interface ISvgRenderer {

  double getWidth();

  double getHeight();

  void paint(Graphics g, int offsetX, int offsetY, double scalingFactor);

  /**
   * @param modelX
   * @param modelY
   * @param interactive
   *          if null then search any element; if true or false then only search element with correspsonding
   *          {@link IScoutSVGElement#isInteractive()}
   * @return the element at the location in svg original coordinates
   */
  IScoutSVGElement elementAtModelLocation(double modelX, double modelY, Boolean interactive);

  public static interface ISvgElementRenderer {

    IScoutSVGElement getModel();

    /**
     * paint is called with no transform applied to g, set either the plain or the scaled (default) transform
     */
    void paint(Graphics2D g, AffineTransform plainTx, AffineTransform scaledTx);

    double getRawWidth();

    double getRawHeight();

    /**
     * @param modelX
     *          in original svg coordinates
     * @param modelY
     *          in original svg coordinates
     */
    boolean contains(double modelX, double modelY);
  }

}
