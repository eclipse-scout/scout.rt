/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.desktop.viewarea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.scout.rt.ui.rap.window.desktop.viewarea.ViewArea.SashKey;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;

public class ViewAreaLayout extends Layout {
  private static final long serialVersionUID = 1L;

  private static int SASH_WIDTH = 3;
  private static int MIN_SIZE = 30;

  Rectangle boundsLeft;
  Rectangle boundsSashLeft;
  Rectangle boundsCenter;
  Rectangle boundsSashRight;
  Rectangle boundsRight;
  private EventListenerList m_eventListeners;

  public ViewAreaLayout() {
    m_eventListeners = new EventListenerList();
  }

  public void addLayoutListener(ILayoutListener listener) {
    m_eventListeners.add(ILayoutListener.class, listener);
  }

  public void removeLayoutListener(ILayoutListener listener) {
    m_eventListeners.remove(ILayoutListener.class, listener);
  }

  private void fireCompositeLayouted() {
    for (ILayoutListener l : m_eventListeners.getListeners(ILayoutListener.class)) {
      l.handleCompositeLayouted();
    }
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    Assert.isTrue(composite instanceof ViewArea);
    ViewArea viewArea = (ViewArea) composite;
    Rectangle clientArea = viewArea.getClientArea();
    cache(viewArea, clientArea.x, clientArea.y, clientArea.width, clientArea.height, flushCache);
    // layout
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        RwtScoutViewStack view = viewArea.m_viewStacks[x][y];
        if (view.getVisible()) {
          view.setBounds(m_bounds.get(view));
        }
      }
    }
    ArrayList<SashKey> keys = new ArrayList<ViewArea.SashKey>(Arrays.asList(SashKey.values()));
    keys.remove(SashKey.VERTICAL_LEFT);
    keys.remove(SashKey.VERTICAL_RIGHT);
    for (SashKey k : keys) {
      Sash s = viewArea.getSash(k);
      if (s.getVisible()) {
        Rectangle bounds = m_bounds.get(s);
        s.setBounds(bounds);
      }
    }
    // sashes
    Sash verticalLeft = viewArea.getSash(SashKey.VERTICAL_LEFT);
    if (verticalLeft.getVisible()) {
      Rectangle bounds = new Rectangle(boundsSashLeft.x, boundsSashLeft.y, boundsSashLeft.width, boundsSashLeft.height);
      bounds.height = clientArea.height;
      verticalLeft.setBounds(bounds);
    }

    Sash verticalRight = viewArea.getSash(SashKey.VERTICAL_RIGHT);
    if (verticalRight.getVisible()) {
      Rectangle bounds = new Rectangle(boundsSashRight.x, boundsSashRight.y, boundsSashRight.width, boundsSashRight.height);
      bounds.height = clientArea.height;
      verticalRight.setBounds(bounds);
    }
    fireCompositeLayouted();
  }

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    Assert.isTrue(composite instanceof ViewArea);
    ViewArea viewArea = (ViewArea) composite;
    Point minSize = new Point(0, 0);
    Point prefSize = new Point(0, 0);
    for (int x = 0; x < 3; x++) {
      int height = 0;
      int minHeight = 0;
      int width = 0;
      int minWidth = 0;
      for (int y = 0; y < 3; y++) {
        RwtScoutViewStack view = viewArea.m_viewStacks[x][y];
        if (view.getVisible()) {
          if (minHeight > 0) {
            minHeight += SASH_WIDTH;
          }
          minHeight += MIN_SIZE;
          Point computedSize = view.computeSize(SWT.DEFAULT, SWT.DEFAULT);
          if (height > 0) {
            height += SASH_WIDTH;
          }
          height += computedSize.y;
          // width
          minWidth = Math.max(minWidth, MIN_SIZE);
          width = Math.max(width, computedSize.x);
        }
      }

      minSize.y = Math.max(minSize.y, minHeight);
      prefSize.y = Math.max(prefSize.y, height);
      // width
      if (minSize.x > 0) {
        minSize.x += SASH_WIDTH;
      }
      minSize.x += minWidth;
      if (prefSize.x > 0) {
        prefSize.x += SASH_WIDTH;
      }
      prefSize.x += width;
    }
    int w = prefSize.x;
    if (wHint >= 0) {
      w = Math.max(minSize.x, wHint);
    }
    int h = prefSize.y;
    if (hHint >= 0) {
      h = Math.max(minSize.y, hHint);
    }
    return new Point(w, h);
  }

  private Rectangle getBounds(ViewArea viewArea, int x, int y) {
    Rectangle bounds = new Rectangle(0, 0, 0, 0);
    switch (x) {
      case 0:
        bounds.x = boundsLeft.x;
        bounds.y = boundsLeft.y;
        bounds.width = boundsLeft.width;
        break;
      case 1:
        bounds.x = boundsCenter.x;
        bounds.y = boundsCenter.y;
        bounds.width = boundsCenter.width;
        break;
      case 2:
        bounds.x = boundsRight.x;
        bounds.y = boundsRight.y;
        bounds.width = boundsRight.width;
        break;
    }
    bounds.height = 300;
    return bounds;
  }

  private void cache(ViewArea viewArea, int x, int y, int width, int height, boolean flushCache) {
    try {
      boundsLeft = new Rectangle(x, y, 0, 0);
      int w = computeWidth(viewArea.m_viewStacks[0]);
      boundsLeft.width = w;

      boundsCenter = new Rectangle(x, y, 0, 0);
      w = computeWidth(viewArea.m_viewStacks[1]);
      boundsCenter.width = w;

      boundsRight = new Rectangle(x, y, 0, 0);
      w = computeWidth(viewArea.m_viewStacks[2]);
      boundsRight.width = w;
      // sashes
      Sash leftSash = viewArea.getSash(SashKey.VERTICAL_LEFT);
      Sash rightSash = viewArea.getSash(SashKey.VERTICAL_RIGHT);
      leftSash.setVisible(false);
      rightSash.setVisible(false);
      boundsSashLeft = new Rectangle(x, y, 0, 0);
      boundsSashRight = new Rectangle(x, y, 0, 0);

      // all 3 parts visible
      if (boundsLeft.width > 0 && boundsCenter.width > 0 && boundsRight.width > 0) {
        leftSash.setVisible(true);
        rightSash.setVisible(true);
        boundsSashLeft.width = SASH_WIDTH;
        boundsSashRight.width = SASH_WIDTH;
      }
      // left and center visible
      else if (boundsLeft.width > 0 && boundsCenter.width > 0) {
        leftSash.setVisible(true);
        rightSash.setVisible(false);
        boundsSashLeft.width = SASH_WIDTH;
      }
      else if (boundsLeft.width > 0 && boundsRight.width > 0) {
        leftSash.setVisible(true);
        rightSash.setVisible(false);
        boundsSashLeft.width = SASH_WIDTH;
      }
      else if (boundsCenter.width > 0 && boundsRight.width > 0) {
        leftSash.setVisible(false);
        rightSash.setVisible(true);
        boundsSashRight.width = SASH_WIDTH;
      }
      int pos = viewArea.getSashPosition(SashKey.VERTICAL_LEFT);
      if (pos > 0) {
        boundsLeft.width = pos;
      }
      pos = viewArea.getSashPosition(SashKey.VERTICAL_RIGHT);
      if (pos > 0 && width > 0 && boundsRight.width > 0) {
        boundsRight.width = pos;
      }
      // correct if larger/smaller than hint
      int totalSize = boundsLeft.width + boundsSashLeft.width + boundsCenter.width + boundsSashRight.width + boundsRight.width;
      if (width > 0 && width != totalSize) {
        // shrink/expand
        int[] widths = new int[]{boundsLeft.width, boundsSashLeft.width, boundsCenter.width, boundsSashRight.width, boundsRight.width};
        int delta = adjust(width, new float[]{0, 0, 1, 0, 0}, widths, new int[]{-1, -1, -1, -1, -1});
        if (delta != 0) {
          adjust(width, new float[]{(float) boundsLeft.width / (float) totalSize, 0, 0, 0, (float) boundsRight.width / (float) totalSize}, widths, new int[]{-1, -1, -1, -1, -1});
        }
        boundsLeft.width = widths[0];
        boundsCenter.width = widths[2];
        boundsRight.width = widths[4];
      }

      // compute bounds
      int xLocal = x;
      boundsLeft.x = xLocal;
      xLocal += boundsLeft.width;
      boundsSashLeft.x = xLocal;
      xLocal += boundsSashLeft.width;
      boundsCenter.x = xLocal;
      xLocal += boundsCenter.width;
      boundsSashRight.x = xLocal;
      xLocal += boundsSashRight.width;
      boundsRight.x = xLocal;

      // vertical
      if (height > 0) {
        boundsLeft.height = height;
        boundsCenter.height = height;
        boundsRight.height = height;
        boundsSashLeft.height = height;
        boundsSashRight.height = height;
      }
      RwtScoutViewStack[] views = new RwtScoutViewStack[3];
      for (int i = 0; i < 3; i++) {
        views[i] = viewArea.m_viewStacks[0][i];
      }
      layoutVertical(viewArea, views, boundsLeft, SashKey.HORIZONTAL_LEFT_TOP, SashKey.HORIZONTAL_LEFT_BOTTOM);
      views = new RwtScoutViewStack[3];
      for (int i = 0; i < 3; i++) {
        views[i] = viewArea.m_viewStacks[1][i];
      }
      layoutVertical(viewArea, views, boundsCenter, SashKey.HORIZONTAL_CENTER_TOP, SashKey.HORIZONTAL_CENTER_BOTTOM);
      views = new RwtScoutViewStack[3];
      for (int i = 0; i < 3; i++) {
        views[i] = viewArea.m_viewStacks[2][i];
      }
      layoutVertical(viewArea, views, boundsRight, SashKey.HORIZONTAL_RIGHT_TOP, SashKey.HORIZONTAL_RIGHT_BOTTOM);

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void layoutVertical(ViewArea viewArea, RwtScoutViewStack[] views, Rectangle clientArea, SashKey keyTop, SashKey keyBottom) {
    Rectangle[] bounds = new Rectangle[3];
    for (int i = 0; i < 3; i++) {
      bounds[i] = new Rectangle(clientArea.x, 0, clientArea.width, 0);
      // compute
      if (views[i].getVisible()) {
        Point size = views[i].computeSize(bounds[i].width, SWT.DEFAULT);
        bounds[i].height = size.y;
      }
    }
    // sashes
    Rectangle sashTop = new Rectangle(0, 0, clientArea.width, 0);
    Rectangle sashBottom = new Rectangle(0, 0, clientArea.width, 0);
    int pos = viewArea.getSashPosition(keyTop);
    if (pos > 0 && bounds[0].height > 0) {
      bounds[0].height = pos - clientArea.y;
    }
    pos = viewArea.getSashPosition(keyBottom);
    if (pos > 0 && bounds[2].height > 0) {
      bounds[2].height = pos - clientArea.y;
    }
    // visibility of sashes
    if (bounds[0].height > 0 && bounds[1].height > 0 && bounds[2].height > 0) {
      sashTop.height = SASH_WIDTH;
      sashBottom.height = SASH_WIDTH;
    }
    // left and center visible
    else if (bounds[0].height > 0 && bounds[1].height > 0) {
      sashTop.height = SASH_WIDTH;
      sashBottom.height = 0;
    }
    else if (bounds[0].height > 0 && bounds[2].height > 0) {
      sashTop.height = SASH_WIDTH;
      sashBottom.height = 0;
    }
    else if (bounds[1].height > 0 && bounds[2].height > 0) {
      sashTop.height = 0;
      sashBottom.height = SASH_WIDTH;
    }
    // resize
    int totalHeight = bounds[0].height + bounds[1].height + bounds[2].height + sashTop.height + sashBottom.height;
    if (clientArea.height > 0 && totalHeight != clientArea.height) {
      // shrink/expand
      int[] heights = new int[]{bounds[0].height, sashTop.height, bounds[1].height, sashBottom.height, bounds[2].height};
      int delta = adjust(clientArea.height, new float[]{0, 0, 1, 0, 0}, heights, new int[]{0, 0, Integer.MAX_VALUE, 0, 0});
      if (delta != 0) {
        adjust(clientArea.height, new float[]{(float) bounds[0].height / (float) totalHeight, 0, 0, 0, (float) bounds[2].height / (float) totalHeight}, heights, new int[]{Integer.MAX_VALUE, 0, 0, 0, Integer.MAX_VALUE});
      }

      bounds[0].height = heights[0];
      bounds[1].height = heights[2];
      bounds[2].height = heights[4];

    }
    // fill bounds
    int y = clientArea.y;
    bounds[0].y = y;
    y += bounds[0].height;
    sashTop.y = y;
    y += sashTop.height;
    bounds[1].y = y;
    y += bounds[1].height;
    sashBottom.y = y;
    y += sashBottom.height;
    bounds[2].y = y;
    // cache
    for (int i = 0; i < 3; i++) {
      m_bounds.put(views[i], bounds[i]);
    }
    Sash sash = viewArea.getSash(keyTop);
    m_bounds.put(sash, sashTop);
    m_bounds.put(viewArea.getSash(keyBottom), sashBottom);
  }

  private int adjust(int target, float[] weights, int[] pixels, int[] maxWidth) {
    for (int i = 0; i < maxWidth.length; i++) {
      if (maxWidth[i] < 0) {
        maxWidth[i] = Integer.MAX_VALUE;
      }
    }
    int delta = 0;
    for (int pixel : pixels) {
      delta += pixel;
    }
    delta = target - delta;
    float[] accWeight = new float[weights.length];
    boolean hasTargets = true;
    while (Math.abs(delta) > 0 && hasTargets) {
      hasTargets = false;
      for (int i = 0; i < pixels.length && Math.abs(delta) > 0; i++) {
        if (weights[i] > 0 && pixels[i] > MIN_SIZE && pixels[i] < maxWidth[i]) {
          hasTargets = true;
          accWeight[i] += Math.abs(weights[i]);
          if (accWeight[i] >= 1) {
            accWeight[i] -= 1;
            pixels[i] += (delta / Math.abs(delta)); // -+1
            delta -= (delta / Math.abs(delta));
          }
        }
      }
    }
    return delta;
  }

  private WeakHashMap<Control, Rectangle> m_bounds = new WeakHashMap<Control, Rectangle>();

  private int computeWidth(RwtScoutViewStack[] views) {
    int w = 0;
    for (RwtScoutViewStack v : views) {
      if (v.getVisible()) {
        Point size = v.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        w = Math.max(size.x, w);
      }
    }
    return w;
  }
}
