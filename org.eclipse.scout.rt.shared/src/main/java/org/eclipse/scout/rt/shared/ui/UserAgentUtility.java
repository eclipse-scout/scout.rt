/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.8.0
 */
public final class UserAgentUtility {
  private static final Logger LOG = LoggerFactory.getLogger(UserAgentUtility.class);

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
    return getCurrentUserAgent().isTouch();
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
      LOG.info("No UserAgent in calling context found; using default UserAgent");
      return UserAgents.createDefault();
    }
  }
}
