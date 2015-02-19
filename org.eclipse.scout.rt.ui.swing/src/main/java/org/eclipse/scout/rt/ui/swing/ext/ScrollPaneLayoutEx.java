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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.Scrollable;
import javax.swing.border.Border;

/**
 * Bug fix in preferredSize: extentSize and viewSize where swapped.
 */
public class ScrollPaneLayoutEx extends ScrollPaneLayout {
  private static final long serialVersionUID = 1L;

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    super.preferredLayoutSize(parent);
    JScrollPane spParent = (JScrollPane) parent;
    Insets insets = parent.getInsets();
    int prefW = insets.left + insets.right;
    int prefH = insets.top + insets.bottom;
    Dimension sizeOfExtent = null;
    Dimension sizeOfView = null;
    Component viewComponent = null;
    if (viewport != null) {
      //WORKAROUND imo, 16.08.2010
      sizeOfExtent = viewport.getExtentSize();
      sizeOfView = viewport.getPreferredSize();
      viewComponent = viewport.getView();
      //end
    }
    //WORKAROUND imo, 16.08.2010
    if (sizeOfView != null) {
      prefW += sizeOfView.width;
      prefH += sizeOfView.height;
    }
    //end
    Border borderOfViewport = spParent.getViewportBorder();
    if (borderOfViewport != null) {
      Insets localInsets = borderOfViewport.getBorderInsets(parent);
      prefW += localInsets.left + localInsets.right;
      prefH += localInsets.top + localInsets.bottom;
    }
    if (rowHead != null && rowHead.isVisible()) {
      prefW += rowHead.getPreferredSize().width;
    }
    if (colHead != null && colHead.isVisible()) {
      prefH += colHead.getPreferredSize().height;
    }
    if (vsb != null && vsbPolicy != VERTICAL_SCROLLBAR_NEVER) {
      if (vsbPolicy == VERTICAL_SCROLLBAR_ALWAYS) {
        prefW += vsb.getPreferredSize().width;
      }
      else if (sizeOfView != null && sizeOfExtent != null) {
        boolean scrollEnabled = true;
        if (viewComponent instanceof Scrollable) {
          scrollEnabled = !((Scrollable) viewComponent).getScrollableTracksViewportHeight();
        }
        if (scrollEnabled && (sizeOfView.height > sizeOfExtent.height)) {
          prefW += vsb.getPreferredSize().width;
        }
      }
    }
    if (hsb != null && hsbPolicy != HORIZONTAL_SCROLLBAR_NEVER) {
      if (hsbPolicy == HORIZONTAL_SCROLLBAR_ALWAYS) {
        prefH += hsb.getPreferredSize().height;
      }
      else if (sizeOfView != null && sizeOfExtent != null) {
        boolean scrollEnabled = true;
        if (viewComponent instanceof Scrollable) {
          scrollEnabled = !((Scrollable) viewComponent).getScrollableTracksViewportWidth();
        }
        if (scrollEnabled && sizeOfView.width > sizeOfExtent.width) {
          prefH += hsb.getPreferredSize().height;
        }
      }
    }
    return new Dimension(prefW, prefH);
  }

}
