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
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Color;

public final class ColorUtility {

  private ColorUtility() {
  }

  public static Color mergeColors(Color a, float fa, Color b, float fb) {
    return new Color(
        (fa * a.getRed() + fb * b.getRed()) / (fa + fb) / 255f,
        (fa * a.getGreen() + fb * b.getGreen()) / (fa + fb) / 255f,
        (fa * a.getBlue() + fb * b.getBlue()) / (fa + fb) / 255f);
  }

}
