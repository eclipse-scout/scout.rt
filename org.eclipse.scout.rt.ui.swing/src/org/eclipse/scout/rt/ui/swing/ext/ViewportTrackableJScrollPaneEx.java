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
import java.awt.LayoutManager;
import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.ViewportLayout;

/**
 * {@link JScrollPane} with a {@link ViewportLayout} to have a scrolling policy to shrink components
 * horizontally and vertically to their minimum size and to stretch components by tracking the viewport's dimension.<br/>
 * Please note that the behavior of having a {@link Scrollable} view is very similar but is too rigorous if configured
 * to track the viewport's width because then, the minimal dimension is not respected and scrolling is completely
 * disabled.
 */
public class ViewportTrackableJScrollPaneEx extends JScrollPaneEx {
  private static final long serialVersionUID = 1L;

  public ViewportTrackableJScrollPaneEx(Component view) {
    super(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  @Override
  protected JViewport createViewport() {
    return new P_Viewport();
  }

  /**
   * Viewport with a different {@link LayoutManager} installed to support a scrolling policy to shrink/stretch
   * components horizontally and vertically.
   */
  private class P_Viewport extends JViewport {

    private static final long serialVersionUID = 1L;

    @Override
    protected LayoutManager createLayoutManager() {
      return new P_ViewportLayout(); // register layout manager to control the scrolling behavior.
    }
  }

  private class P_ViewportLayout extends ViewportLayout {

    private static final long serialVersionUID = 1L;

    private int m_defaultHScrollPolicy = ViewportTrackableJScrollPaneEx.this.getHorizontalScrollBarPolicy();
    private int m_defaultVScrollPolicy = ViewportTrackableJScrollPaneEx.this.getVerticalScrollBarPolicy();

    @Override
    public void layoutContainer(Container parent) {
      JViewport vp = (JViewport) parent;
      Component view = vp.getView();

      if (view == null) {
        return;
      }

      // Cache the current viewport position to be set after layouting the viewport.
      Point viewPosition = vp.getViewPosition();

      Dimension viewPrefSize = view.getPreferredSize();
      Dimension viewMinSize = view.getMinimumSize();

      Dimension vpSize = vp.getSize();
      Dimension viewSize = new Dimension(viewPrefSize);

      // VIEW-WIDTH
      // Make the view adapt the viewport's width as long as it is bigger then its minimum width.
      if (viewPosition.x == 0 && vpSize.width > viewMinSize.width) {
        viewSize.width = vpSize.width; // stretch the view to fit the viewport.
        ViewportTrackableJScrollPaneEx.this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Fully disable horizontal scrolling to prevent flickering
      }
      else {
        ViewportTrackableJScrollPaneEx.this.setHorizontalScrollBarPolicy(m_defaultHScrollPolicy); // Reset the scrolling policy because the view does not fit into the viewport anymore.
      }

      // VIEW-HEIGHT
      // Make the view adapt the viewport's height as long as it is bigger then its minimum height.
      if (viewPosition.y == 0 && vpSize.height > viewMinSize.height) {
        ViewportTrackableJScrollPaneEx.this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER); // Fully disable vertical scrolling to prevent flickering
        viewSize.height = vpSize.height; // stretch the view to fit the viewport.
      }
      else {
        ViewportTrackableJScrollPaneEx.this.setVerticalScrollBarPolicy(m_defaultVScrollPolicy); // Reset the scrolling policy because the view does not fit into the viewport anymore.
      }

      // Ensure the minimal dimension of the view to be respected.
      viewSize.width = Math.max(viewMinSize.width, viewSize.width);
      viewSize.height = Math.max(viewMinSize.height, viewSize.height);

      // Set the position and the size of the view.
      vp.setViewPosition(viewPosition);
      vp.setViewSize(viewSize);
    }
  }
}
