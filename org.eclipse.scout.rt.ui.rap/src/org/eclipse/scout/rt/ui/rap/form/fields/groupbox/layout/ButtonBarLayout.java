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
package org.eclipse.scout.rt.ui.rap.form.fields.groupbox.layout;

import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * <h3>ButtonBarLayout</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class ButtonBarLayout extends Layout {
  private static final long serialVersionUID = 1L;

  private final int m_style;
  public int verticalGap;
  public int horizontalGap;

  public ButtonBarLayout(int style) {
    m_style = style;
    verticalGap = 4;
    horizontalGap = 4;
  }

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    Point size = new Point(0, 0);
    for (Control c : composite.getChildren()) {
      if (c.getVisible()) {
        Point cSize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
        cSize.x += 15;
        cSize.x = Math.max(cSize.x, UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonMinWidth());
        cSize.y = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonHeight(), cSize.y);
        cSize = addInsets(c, cSize);
        size.x += (cSize.x + horizontalGap);
        size.y = Math.max(cSize.y, size.y);
      }
    }
    //WORKAROUND for bug in swt composite setting size to 64x64 when 0,0
    if (size.x <= 0) {
      size.x = 1;
    }
    if (size.y <= 0) {
      size.y = 1;
    }
    //end
    return size;
  }

  private Point addInsets(Control c, Point size) {
    Object data = c.getLayoutData();
    if (data instanceof ButtonBarLayoutData) {
      ButtonBarLayoutData ld = (ButtonBarLayoutData) data;
      size.x += (ld.insetLeft + ld.insetRight);
      size.y += (ld.insetTop + ld.insetBottom);
    }
    return size;
  }

  private ButtonBarLayoutData getLayoutData(Control c) {
    Object data = c.getLayoutData();
    if (!(data instanceof ButtonBarLayoutData)) {
      data = new ButtonBarLayoutData();
    }
    return (ButtonBarLayoutData) data;
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    if ((m_style & SWT.RIGHT) != 0) {
      layoutRight(composite);
    }
    else {
      layoutLeft(composite);
    }
  }

  protected void layoutRight(Composite composite) {
    Control[] children = composite.getChildren();
    Rectangle clientArea = composite.getClientArea();
    int fillCount = 0;
    int excessWidth = clientArea.width;
    for (int i = 0; i < children.length; i++) {
      if (children[i].getVisible()) {
        ButtonBarLayoutData layoutData = getLayoutData(children[i]);
        if (layoutData.fillHorizontal) {
          fillCount++;
        }
        Point prefSize = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
        prefSize.x += 15;
        int width = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonMinWidth(), prefSize.x);
        if (i > 0) {
          excessWidth -= horizontalGap;
        }
        excessWidth -= (width + layoutData.insetLeft + layoutData.insetRight);
      }
    }
    //
    int x = clientArea.x + clientArea.width;
    int y = clientArea.y;
    for (int i = children.length - 1; i >= 0; i--) {
      if (children[i].getVisible()) {
        ButtonBarLayoutData layoutData = getLayoutData(children[i]);
        Point prefSize = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
        prefSize.x += 15;
        int width = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonMinWidth(), prefSize.x);
        int height = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonHeight(), prefSize.y);
        if (layoutData.fillHorizontal) {
          if (fillCount > 0 && excessWidth > 0) {
            int delta = excessWidth / fillCount;
            width += delta;
            excessWidth -= delta;
            fillCount--;
          }
        }
        Rectangle bounds = new Rectangle(x - (width + layoutData.insetRight), y + layoutData.insetTop, width, height);
        children[i].setBounds(bounds);
        x -= (width + horizontalGap + layoutData.insetLeft + layoutData.insetRight);
      }
    }
  }

  protected void layoutLeft(Composite composite) {
    Control[] children = composite.getChildren();
    Rectangle clientArea = composite.getClientArea();
    int fillCount = 0;
    int excessWidth = clientArea.width;
    for (int i = 0; i < children.length; i++) {
      if (children[i].getVisible()) {
        ButtonBarLayoutData layoutData = getLayoutData(children[i]);
        if (layoutData.fillHorizontal) {
          fillCount++;
        }
        Point prefSize = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
        prefSize.x += 15;
        int width = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonMinWidth(), prefSize.x);
        if (i > 0) {
          excessWidth -= horizontalGap;
        }
        excessWidth -= (width + layoutData.insetLeft + layoutData.insetRight);
      }
    }
    //
    int x = clientArea.x;
    int y = clientArea.y;
    for (Control element : children) {
      if (element.getVisible()) {
        ButtonBarLayoutData layoutData = getLayoutData(element);
        Point prefSize = element.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
        prefSize.x += 15;
        int width = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonMinWidth(), prefSize.x);
        int height = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getProcessButtonHeight(), prefSize.y);
        if (layoutData.fillHorizontal) {
          if (fillCount > 0 && excessWidth > 0) {
            int delta = excessWidth / fillCount;
            width += delta;
            excessWidth -= delta;
            fillCount--;
          }
        }
        element.setBounds(x + layoutData.insetLeft, y + layoutData.insetTop, width, height);
        x += (width + horizontalGap + layoutData.insetLeft + layoutData.insetRight);
      }
    }
  }
}
