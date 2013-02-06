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
package org.eclipse.scout.rt.ui.rap.util;

import org.eclipse.rwt.RWT;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;

/**
 * @since 3.8.0
 */
public class DeviceUtility {
  public static final String SESSION_ATTR_DEVICE_TYPE = "device-type";

  public static IUiDeviceType getCurrentDeviceType() {
    return (IUiDeviceType) RWT.getSessionStore().getAttribute(SESSION_ATTR_DEVICE_TYPE);
  }

  public static void setCurrentDeviceType(IUiDeviceType uiDeviceType) {
    RWT.getSessionStore().setAttribute(SESSION_ATTR_DEVICE_TYPE, uiDeviceType);
  }

  public static boolean isMobileOrTabletDevice() {
    IUiDeviceType currentDeviceType = getCurrentDeviceType();
    if (UiDeviceType.MOBILE.equals(currentDeviceType) || UiDeviceType.TABLET.equals(currentDeviceType)) {
      return true;
    }

    return false;
  }

  public static boolean isMobileDevice() {
    IUiDeviceType currentDeviceType = getCurrentDeviceType();
    if (UiDeviceType.MOBILE.equals(currentDeviceType)) {
      return true;
    }

    return false;
  }

  public static boolean isTabletDevice() {
    IUiDeviceType currentDeviceType = getCurrentDeviceType();
    if (UiDeviceType.TABLET.equals(currentDeviceType)) {
      return true;
    }

    return false;
  }
}
