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
package org.eclipse.scout.rt.ui.rap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * Responsible for resizing the height of a RWT Shell to its preferred size.
 */
public class RwtShellValidateRoot extends DefaultValidateRoot {

  private final IRwtEnvironment m_env;

  public RwtShellValidateRoot(Shell root, IRwtEnvironment env) {
    super(root);
    if (env == null) {
      throw new IllegalArgumentException("environment cannot be null.");
    }
    m_env = env;
  }

  private Shell getShell() {
    return (Shell) getUiComposite();
  }

  protected IRwtEnvironment getEnvironment() {
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

    int dhPref = 0;
    if (curShellBounds != null && prefSize != null) {
      dhPref = prefSize.y - curShellBounds.height;
      if (dhPref > 0) {
        getShell().setBounds(new Rectangle(curShellBounds.x, curShellBounds.y, curShellBounds.width, prefSize.y));
      }
    }
  }
}
