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
package org.eclipse.scout.rt.ui.swing.icons;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class EmptyIcon implements Icon {
  private int w, h;

  public EmptyIcon(int w, int h) {
    this.w = w;
    this.h = h;
  }

  @Override
  public int getIconWidth() {
    return w;
  }

  @Override
  public int getIconHeight() {
    return h;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
  }

}// end class
