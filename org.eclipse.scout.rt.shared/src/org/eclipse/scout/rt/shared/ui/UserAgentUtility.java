/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.ui;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.services.common.session.ISessionService;
import org.eclipse.scout.service.SERVICES;

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

  public static boolean isWebClient() {
    return getCurrentUiLayer().isWebUi();
  }

  public static boolean isRichClient() {
    return !isWebClient();
  }

  public static boolean isSwingUi() {
    return UiLayer.SWING.equals(getCurrentUiLayer());
  }

  public static boolean isSwtUi() {
    return UiLayer.SWT.equals(getCurrentUiLayer());
  }

  public static boolean isRapUi() {
    return UiLayer.RAP.equals(getCurrentUiLayer());
  }

  public static IUiDeviceType getCurrentUiDeviceType() {
    return getCurrentUserAgent().getUiDeviceType();
  }

  public static IUiLayer getCurrentUiLayer() {
    return getCurrentUserAgent().getUiLayer();
  }

  public static UserAgent getCurrentUserAgent() {
    ISessionService service = SERVICES.getService(ISessionService.class);
    if (service == null) {
      LOG.warn("No session service found! Returning default user agent object.");
      return UserAgent.createDefault();
    }

    ISession session = service.getCurrentSession();
    if (service.getCurrentSession() == null) {
      LOG.warn("No session found! Returning default user agent object.");
      return UserAgent.createDefault();
    }

    return session.getUserAgent();
  }

  public static String getFontSizeUnit() {
    if (isWebClient()) {
      return "px";
    }

    return "pt";
  }

}
