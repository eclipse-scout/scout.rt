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
package org.eclipse.scout.rt.ui.swing.window.desktop.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JInternalFrame;

import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;

class ViewElement {
  public ViewSplit top;
  public ViewSplit bottom;
  public ViewSplit left;
  public ViewSplit right;
  //
  private JInternalFrame m_frame;

  public ViewElement(JInternalFrame frame) {
    m_frame = frame;
  }

  public JInternalFrame getFrame() {
    return m_frame;
  }

  /**
   * resize frame as to support min / max sizes
   */
  void validateMinMaxSizes() {
    Dimension[] sizes = SwingLayoutUtility.getValidatedSizes(m_frame);
    Dimension d = new Dimension(right.getLocation() - left.getLocation(), bottom.getLocation() - top.getLocation());
    //only check vertically
    int dy = sizes[0].height - d.height;
    if (dy > 0) {
      // expand
      if (!top.isFixed()) {
        top.move(-dy, true);
      }
      else {
        bottom.move(dy, true);
      }
    }
    dy = d.height - sizes[2].height;
    if (dy > 0) {
      // shrink
      if (!top.isFixed()) {
        top.move(dy, true);
      }
      else {
        bottom.move(-dy, true);
      }
    }
  }

  /**
   * resize frame as it is requested, but allow opposite expanding/shrinking
   * <p>
   * Example: when expanding to bottom, but bottom is fixed expand to top if possible
   */
  public void fitSize(Dimension size) {
    Dimension d = new Dimension(right.getLocation() - left.getLocation(), bottom.getLocation() - top.getLocation());
    //only fit vertically
    int dy = size.height - d.height;
    if (dy > 0) {
      // expand
      dy -= bottom.move(dy, false);
      top.move(-dy, false);
    }
    dy = d.height - size.height;
    if (dy > 0) {
      // shrink
      dy -= top.move(dy, false);
      bottom.move(-dy, false);
    }
  }

  /**
   * resize frame exactly as it is requested
   * <p>
   * Example: when expanding to bottom, but bottom is fixed then nothing is done
   */
  public void resize(Rectangle newR) {
    Rectangle oldR = m_frame.getBounds();
    if (oldR.x != newR.x) {
      int dx = newR.x - oldR.x;
      left.move(dx, false);
    }
    else if (oldR.x + oldR.width != newR.x + newR.width) {
      int dx = newR.x + newR.width - oldR.x - oldR.width;
      right.move(dx, false);
    }
    //
    if (oldR.y != newR.y) {
      int dy = newR.y - oldR.y;
      top.move(dy, false);
    }
    else if (oldR.y + oldR.height != newR.y + newR.height) {
      int dy = newR.y + newR.height - oldR.y - oldR.height;
      bottom.move(dy, false);
    }
  }
}
