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
package org.eclipse.scout.rt.ui.rap.form.fields.snapbox.layout;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class SnapBoxMinimizedLayout extends Layout {
  private static final long serialVersionUID = 1L;

  private int m_iconSize = 20;
  private int m_gap = 2;
  private int m_insets = 2;

  @Override
  protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
    Point size = new Point(0, 0);
    Control[] children = composite.getChildren();
    int visibleChildSize = 0;
    for (Control child : children) {
      SnapBoxLayoutData layoutData = (SnapBoxLayoutData) child.getLayoutData();
      if (!layoutData.exclude && !layoutData.maximized) {
        size.y = Math.max(size.y, m_iconSize);
        size.x += m_iconSize;
        visibleChildSize++;

      }
    }
    size.x += ((visibleChildSize - 1) * m_gap);
    size.x += 2 * m_insets;
    size.y += 2 * m_insets;
    return size;
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    Control[] children = composite.getChildren();
    Rectangle clientArea = composite.getClientArea();
    int x = clientArea.x + clientArea.width - m_iconSize - m_insets;
    int height = clientArea.height - 2 * m_insets;
    // start from the end
    for (int i = children.length - 1; i >= 0; i--) {
      Control child = children[i];
      SnapBoxLayoutData layoutData = (SnapBoxLayoutData) child.getLayoutData();
      boolean layouted = !layoutData.exclude && !layoutData.maximized;
      if (layouted) {
        child.setBounds(x, clientArea.y + m_insets, m_iconSize, height);

        x -= (m_iconSize + m_gap);
      }
      else {
        child.setBounds(0, 0, 0, 0);
      }
    }
  }

}
