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

import javax.swing.JComponent;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public abstract class AbstractDecoration implements IDecoration {
  public static final Rectangle NULL_RECTANGLE = new Rectangle(0, 0, 0, 0);

  private final JComponent m_owner;
  private final ISwingEnvironment m_environment;
  private Rectangle m_bounds;

  public AbstractDecoration(JComponent owner, ISwingEnvironment environment, boolean callInitializer) {
    m_owner = owner;
    m_environment = environment;
    if (callInitializer) {
      init();
    }
  }

  protected void init() {
  }

  @Override
  public final void paint(Component c, Graphics g, int x, int y) {
    m_bounds = paintInternal(c, g, x, y);
  }

  /**
   * @param c
   * @param g
   * @param x
   * @param y
   * @return the bounds used of this decoration
   */
  protected abstract Rectangle paintInternal(Component c, Graphics g, int x, int y);

  @Override
  public JComponent getOwner() {
    return m_owner;
  }

  @Override
  public ISwingEnvironment getEnvironment() {
    return m_environment;
  }

  @Override
  public Rectangle getBounds() {
    return m_bounds;
  }

}
