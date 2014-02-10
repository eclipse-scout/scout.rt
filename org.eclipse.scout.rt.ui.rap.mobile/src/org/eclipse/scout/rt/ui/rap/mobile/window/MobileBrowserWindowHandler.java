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

import org.eclipse.scout.rt.client.ui.desktop.IUrlTarget;
import org.eclipse.scout.rt.client.ui.desktop.UrlTarget;
import org.eclipse.scout.rt.ui.rap.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.util.BrowserInfo.System;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.BrowserWindowHandler;

/**
 * @since 3.9.0
 */
public class MobileBrowserWindowHandler extends BrowserWindowHandler {

  @Override
  protected IUrlTarget computeTargetAuto(String link) {
    if (link == null) {
      return super.computeTargetAuto(link);
    }

    BrowserInfo browserInfo = RwtUtility.getBrowserInfo();
    if (System.IOS.equals(browserInfo.getSystem()) && browserInfo.isStandalone()) {
      //Home screen mode blocks popups, even though popups are enabled, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=427802
      return UrlTarget.SELF;
    }
    if (isMapsLink(link)) {
      //Open the link in the same browser window to open the maps app without using a popup.
      return UrlTarget.SELF;
    }

    return super.computeTargetAuto(link);
  }

}
