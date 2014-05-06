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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 *
 */
public class DecorationGroup extends AbstractDecoration implements IDecorationGroup {

  private final List<IDecoration> m_decorations;

  /**
   * @param owner
   * @param environment
   */
  public DecorationGroup(JComponent owner, ISwingEnvironment environment) {
    super(owner, environment, true);
    m_decorations = new ArrayList<IDecoration>(3);
  }

  @Override
  public void addDecoration(IDecoration icon) {
    m_decorations.add(icon);
  }

  @Override
  public boolean removeDecoration(IDecoration icon) {
    return m_decorations.remove(icon);
  }

  @Override
  public List<IDecoration> getDecoration() {
    return CollectionUtility.arrayList(m_decorations);
  }

  @Override
  protected Rectangle paintInternal(Component c, Graphics g, int x, int y) {
    int height = 0;
    int width = 0;
    int iconX = x;
    for (IDecoration child : m_decorations) {
      int childWidth = child.getWidth();
      child.paint(c, g, iconX, y);
      iconX += childWidth;
      width += childWidth;
      height = Math.max(child.getHeight(), height);
    }
    return new Rectangle(x, y, width, height);
  }

  @Override
  public int getWidth() {
    int width = 0;
    for (IDecoration child : m_decorations) {
      width += child.getWidth();
    }
    return width;
  }

  @Override
  public int getHeight() {
    int height = 0;
    for (IDecoration child : m_decorations) {
      height = Math.max(child.getHeight(), height);
    }
    return height;
  }

  @Override
  public void handleMouseChlicked(MouseEvent e) {
    for (IDecoration d : getDecoration()) {
      if (d.getBounds() != null && d.getBounds().contains(e.getPoint())) {
        d.handleMouseChlicked(e);
        break;
      }
    }
  }

  @Override
  public void handleMouseMoved(DecorationMouseEvent e) {
    for (IDecoration d : getDecoration()) {
      d.handleMouseMoved(e);
    }
  }

  @Override
  public void handleMouseExit(DecorationMouseEvent e) {
    for (IDecoration d : getDecoration()) {
      d.handleMouseExit(e);
    }
  }
}
