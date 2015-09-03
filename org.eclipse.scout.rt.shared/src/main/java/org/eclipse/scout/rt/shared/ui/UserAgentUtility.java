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
package org.eclipse.scout.rt.shared.ui;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * @since 3.8.0
 */
public final class UserAgentUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UserAgentUtility.class);

  private UserAgentUtility() {
  }

  public static boolean isMobileDevice() {
    return UiDeviceType.MOBILE.equals(getCurrentUiDeviceType());
  }

  public static boolean isTabletDevice() {
    return UiDeviceType.TABLET.equals(getCurrentUiDeviceType());
  }

  public static boolean isDesktopDevice() {
    return UiDeviceType.DESKTOP.equals(getCurrentUiDeviceType());
  }

  public static boolean isTouchDevice() {
    return getCurrentUiDeviceType().isTouchDevice();
  }

  public static IUiDeviceType getCurrentUiDeviceType() {
    return getCurrentUserAgent().getUiDeviceType();
  }

  public static IUiLayer getCurrentUiLayer() {
    return getCurrentUserAgent().getUiLayer();
  }

  public static UserAgent getCurrentUserAgent() {
    UserAgent userAgent = UserAgent.CURRENT.get();
    if (userAgent != null) {
      return userAgent;
    }
    else {
      LOG.warn("No UserAgent in calling context found; using default UserAgent");
      return UserAgent.createDefault();
    }
  }

}
