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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.button;

import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.form.fields.button.RwtScoutButton;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileButton extends RwtScoutButton {
  public static final int BUTTON_HEIGHT = 28;

  @Override
  protected void adaptButtonLayoutData(LogicalGridData gd) {
    // Buttons on mobile devices have to be bigger
    if (getScoutObject().getDisplayStyle() == IButton.DISPLAY_STYLE_DEFAULT
        || getScoutObject().getDisplayStyle() == IButton.DISPLAY_STYLE_TOGGLE) {
      gd.useUiHeight = true;
      gd.heightHint = BUTTON_HEIGHT;
    }
  }
}
