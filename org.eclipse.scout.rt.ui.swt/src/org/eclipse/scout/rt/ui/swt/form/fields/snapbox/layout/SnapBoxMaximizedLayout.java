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
package org.eclipse.scout.rt.ui.swt.form.fields.snapbox.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class SnapBoxMaximizedLayout extends Layout {

  private int m_cachedItemHeight = 0;

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    return new Point(1, 1);
    // return computeMaximizedSize(composite, wHint, hHint);
    // Point size = new Point(wHint, hHint);
    // if(hHint == SWT.DEFAULT){
    // Control[] children = composite.getChildren();
    // for (Control child : children) {
    // SnapBoxLayoutData layoutData = (SnapBoxLayoutData) child.getLayoutData();
    // if (!layoutData.exclude & child.isVisible()) {
    // size.y += m_cachedItemHeight;
    // }
    // }
    // }
    // return new Point(1,1);
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    computeMaximizedSize(composite, 0, 0);
    Control[] children = composite.getChildren();
    Rectangle clientArea = composite.getClientArea();
    int y = clientArea.y;
    for (Control child : children) {
      SnapBoxLayoutData layoutData = (SnapBoxLayoutData) child.getLayoutData();
      if (!layoutData.exclude && child.getVisible()) {
        boolean maximized = ((y + m_cachedItemHeight) < clientArea.height);
        layoutData.maximized = maximized;
        if (maximized) {
          child.setBounds(clientArea.x, y, clientArea.width, m_cachedItemHeight);
          y += m_cachedItemHeight;
        }
        else {
          child.setBounds(0, 0, 0, 0);
        }
      }
    }

  }

  private Point computeMaximizedSize(Composite composite, int wHint, int hHint) {
    int maxChildHeight = 0;
    int maxChildWidth = 0;
    int visibleChildren = 0;
    Control[] children = composite.getChildren();
    for (Control child : children) {
      SnapBoxLayoutData layoutData = (SnapBoxLayoutData) child.getLayoutData();
      if (!layoutData.exclude && child.getVisible()) {
        visibleChildren++;
        Point prefSize = child.computeSize(wHint, SWT.DEFAULT);
        maxChildWidth = Math.max(prefSize.x, maxChildWidth);
        maxChildHeight = Math.max(prefSize.y, maxChildHeight);
      }
    }
    m_cachedItemHeight = maxChildHeight;
    return new Point(maxChildWidth, visibleChildren * maxChildHeight);
  }
}
