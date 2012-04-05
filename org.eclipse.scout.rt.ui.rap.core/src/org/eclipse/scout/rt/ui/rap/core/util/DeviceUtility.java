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
package org.eclipse.scout.rt.ui.rap.core.util;

import org.eclipse.rwt.RWT;

/**
 * @since 3.8.0
 */
public class DeviceUtility {
  public static final String SESSION_ATTR_DEVICE_TYPE = "device-type";

  public static DeviceType getDeviceType() {
    return (DeviceType) RWT.getSessionStore().getAttribute(SESSION_ATTR_DEVICE_TYPE);
  }

  public static void setDeviceType(DeviceType deviceType) {
    RWT.getSessionStore().setAttribute(SESSION_ATTR_DEVICE_TYPE, deviceType);
  }

  public static boolean isMobileOrTabletDevice() {
    DeviceType deviceType = getDeviceType();
    if (DeviceType.MOBILE.equals(deviceType) || DeviceType.TABLET.equals(deviceType)) {
      return true;
    }

    return false;
  }

  public static boolean isMobileDevice() {
    DeviceType deviceType = getDeviceType();
    if (DeviceType.MOBILE.equals(deviceType)) {
      return true;
    }

    return false;
  }

  public static boolean isTabletDevice() {
    DeviceType deviceType = getDeviceType();
    if (DeviceType.TABLET.equals(deviceType)) {
      return true;
    }

    return false;
  }
}
