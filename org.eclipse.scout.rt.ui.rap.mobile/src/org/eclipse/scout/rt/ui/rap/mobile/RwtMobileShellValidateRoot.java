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
package org.eclipse.scout.rt.ui.rap.mobile;

import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.RwtShellValidateRoot;
import org.eclipse.scout.rt.ui.rap.mobile.window.dialog.RwtScoutMobileDialog;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class RwtMobileShellValidateRoot extends RwtShellValidateRoot {

  /**
   * @param root
   * @param env
   */
  public RwtMobileShellValidateRoot(Shell root, IRwtEnvironment env) {
    super(root, env);
  }

  @Override
  protected void setBoundsIfResizeIsNeeded(Rectangle curShellBounds, Point prefSize) {
    int dhPref = 0;
    if (curShellBounds != null && prefSize != null) {
      dhPref = prefSize.y - curShellBounds.height;

      for (IRwtScoutPart rwtScoutPart : getEnvironment().getOpenFormParts()) {
        if (rwtScoutPart.isActive() && rwtScoutPart instanceof RwtScoutMobileDialog) {
          // If the header of mobile forms is visible subtract the header height from the perSize
          if (AbstractMobileForm.isHeaderVisible(rwtScoutPart.getScoutObject()) && prefSize.y >= AbstractMobileStandaloneRwtEnvironment.FORM_HEADER_HEIGHT) {
            dhPref -= AbstractMobileStandaloneRwtEnvironment.FORM_HEADER_HEIGHT;
            // if perfSize is higher than the parents client area reduce the height to the parent height (because mobile forms are always scrollable)
            if (getShell().getParent() != null && prefSize.y > getShell().getParent().getClientArea().height) {
              prefSize.y = getShell().getParent().getClientArea().height;
            }
            break;
          }
        }
      }

      if (dhPref != 0) {
        getShell().setBounds(new Rectangle(curShellBounds.x, curShellBounds.y, curShellBounds.width, prefSize.y));
      }
    }
  }
}
