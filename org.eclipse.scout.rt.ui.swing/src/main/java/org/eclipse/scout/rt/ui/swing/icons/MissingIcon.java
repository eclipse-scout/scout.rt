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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class MissingIcon implements Icon {

  public MissingIcon() {
  }

  @Override
  public int getIconWidth() {
    return 16;
  }

  @Override
  public int getIconHeight() {
    return 16;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.setColor(Color.lightGray);
  }

  @Override
  public String toString() {
    return "MissingIcon";
  }
}// end class
