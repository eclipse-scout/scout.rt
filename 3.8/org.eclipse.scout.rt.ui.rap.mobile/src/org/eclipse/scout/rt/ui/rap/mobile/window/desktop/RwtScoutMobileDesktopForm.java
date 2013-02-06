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
package org.eclipse.scout.rt.ui.rap.mobile.window.desktop;

import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktopForm;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileDesktopForm extends RwtScoutDesktopForm {

  @Override
  public boolean isEclipseFormUsed() {
    //Eclipse forms are too heavyweight and may even crash chrome on android.
    //Since the mobile forms don't use any feature of the eclipse forms there is no need to create them.
    return false;
  }

}
