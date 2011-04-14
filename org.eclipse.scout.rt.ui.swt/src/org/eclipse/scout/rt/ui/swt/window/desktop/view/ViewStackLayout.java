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
package org.eclipse.scout.rt.ui.swt.window.desktop.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.ILayoutExtension;

public class ViewStackLayout extends Layout implements ILayoutExtension {

  public ViewStackLayout() {
  }

  @Override
  public int computeMinimumWidth(Composite parent, boolean changed) {
    Control lastChild = getLastChild(parent);
    if (lastChild != null) {
      if (lastChild instanceof Composite) {
        Layout cLayout = ((Composite) lastChild).getLayout();
        if (cLayout instanceof ILayoutExtension) {
          return ((ILayoutExtension) cLayout).computeMinimumWidth((Composite) lastChild, changed);
        }
      }
    }
    return computeSize(parent, 0, SWT.DEFAULT, changed).x;
  }

  @Override
  public int computeMaximumWidth(Composite parent, boolean changed) {
    Control lastChild = getLastChild(parent);
    if (lastChild != null) {
      if (lastChild instanceof Composite) {
        Layout cLayout = ((Composite) lastChild).getLayout();
        if (cLayout instanceof ILayoutExtension) {
          return ((ILayoutExtension) cLayout).computeMinimumWidth((Composite) lastChild, changed);
        }
      }
    }
    return computeSize(parent, 1240000, SWT.DEFAULT, changed).y;
  }

  @Override
  protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
    Control lastChild = getLastChild(composite);
    if (lastChild != null) {
      return lastChild.computeSize(hint, hint2, flushCache);
    }
    return new Point(0, 0);
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    Rectangle clientArea = composite.getClientArea();
    Control[] children = composite.getChildren();
    for (int i = 0; i < children.length; i++) {
      if (i == children.length - 1) {
        children[i].setBounds(clientArea);
      }
      else {
        children[i].setBounds(0, 0, 0, 0);
      }
    }
  }

  private Control getLastChild(Composite composite) {
    Control[] children = composite.getChildren();
    if (children.length > 0) {
      return children[children.length - 1];
    }
    return null;
  }

}
