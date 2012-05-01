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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.button;

import org.eclipse.scout.rt.ui.rap.form.fields.button.ButtonFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.button.IRwtScoutButton;
import org.eclipse.scout.rt.ui.rap.util.DeviceUtility;

public class MobileButtonFieldFactory extends ButtonFieldFactory {

  @Override
  protected IRwtScoutButton createRwtScoutButton() {
    if (DeviceUtility.isMobileOrTabletDevice()) {
      return new RwtScoutMobileButton();
    }

    return super.createRwtScoutButton();
  }
}
