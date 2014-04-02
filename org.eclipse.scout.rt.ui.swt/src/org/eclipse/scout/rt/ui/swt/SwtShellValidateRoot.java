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
package org.eclipse.scout.rt.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Responsible for resizing the height of a SWT Shell to its preferred size.
 */
public class SwtShellValidateRoot extends DefaultValidateRoot {

  ISwtEnvironment m_env;

  public SwtShellValidateRoot(Shell root, ISwtEnvironment env) {
    super(root);
    if (env == null) {
      throw new IllegalArgumentException("environment cannot be null.");
    }
    m_env = env;
  }

  private Shell getShell() {
    return (Shell) getComposite();
  }

  protected ISwtEnvironment getEnvironment() {
    return m_env;
  }

  protected boolean isActive() {
    return getShell() == getEnvironment().getDisplay().getActiveShell();
  }

  @Override
  public void validate() {
    if (getShell() == null || getShell().isDisposed() || !getShell().isVisible() || !isActive()) {
      return;
    }
    super.validate();
    Rectangle curShellBounds = getShell().getBounds();
    Point prefSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

    if (curShellBounds != null && prefSize != null) {
      // if perfSize is higher than the displays current height area reduce the height to the parent height to keep forms scrollable
      if (prefSize.y > Display.getCurrent().getBounds().height) {
        prefSize.y = Display.getCurrent().getBounds().height;
      }
      int dhPref = 0;
      dhPref = prefSize.y - curShellBounds.height;
      if (dhPref > 0) {
        getShell().setBounds(new Rectangle(curShellBounds.x, curShellBounds.y, curShellBounds.width, prefSize.y));
      }
    }
  }
}
