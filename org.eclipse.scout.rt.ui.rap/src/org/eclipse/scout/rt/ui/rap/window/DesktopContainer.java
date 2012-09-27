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
package org.eclipse.scout.rt.ui.rap.window;

import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * <h3>DesktopLayout</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class DesktopContainer extends Composite {
  private static final long serialVersionUID = 1L;

  private RwtScoutViewStack[][] m_viewStacks;

  public DesktopContainer(Composite parent) {
    super(parent, SWT.NONE);
    m_viewStacks = new RwtScoutViewStack[3][3];
  }

  private class P_DesktopLayout extends Layout {
    private static final long serialVersionUID = 1L;

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
      // TODO Auto-generated method stub
    }
  }
}
