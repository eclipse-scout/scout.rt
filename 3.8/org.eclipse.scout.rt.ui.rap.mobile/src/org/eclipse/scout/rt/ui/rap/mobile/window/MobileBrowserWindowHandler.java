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
package org.eclipse.scout.rt.ui.rap.mobile.window;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.rap.window.BrowserWindowHandler;

/**
 * @since 3.9.0
 */
public class MobileBrowserWindowHandler extends BrowserWindowHandler {

  @Override
  public void openLink(String link) {
    if (link == null) {
      return;
    }

    if (isMapsLink(link)) {
      //Open the link in the same browser window to open the maps app. Otherwise the popup gets blocked without notice.
      openLinkInSameBrowserWindow(link);
    }
    else {
      super.openLink(link);
    }
  }

  public boolean isMapsLink(String link) {
    if ((StringUtility.find(link, "http://maps.google.com") >= 0) || (StringUtility.find(link, "https://maps.google.com") >= 0)) {
      return true;
    }

    return false;
  }
}
