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
package org.eclipse.scout.rt.ui.swing.ext.decoration;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 *
 */
public interface IDecoration {

  /**
   * @return
   */
  JComponent getOwner();

  /**
   * @return
   */
  ISwingEnvironment getEnvironment();

  /**
   * @return
   */
  Rectangle getBounds();

  void paint(Component c, Graphics g, int x, int y);

  int getWidth();

  int getHeight();

  /**
   * @param e
   */
  void handleMouseChlicked(MouseEvent e);

  void handleMouseMoved(DecorationMouseEvent e);

  void handleMouseExit(DecorationMouseEvent e);

}
