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

public class SnapBoxLayout extends Layout {

  @Override
  protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
    Point p = new Point(0, 0);
    for (Control c : composite.getChildren()) {
      Point bounds = c.computeSize(hint, SWT.DEFAULT);
      p.x = Math.max(p.x, bounds.x);
      p.y += bounds.y;
    }

    return p;
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {

    Rectangle clientArea = composite.getClientArea();
    Control[] children = composite.getChildren();

    children[0].setBounds(clientArea.x, clientArea.y, clientArea.width, clientArea.height - 24);
    ((Composite) children[0]).layout(true);
    children[1].setBounds(clientArea.x, clientArea.height - 24, clientArea.width, 24);
    ((Composite) children[1]).layout(true);

  }

}
