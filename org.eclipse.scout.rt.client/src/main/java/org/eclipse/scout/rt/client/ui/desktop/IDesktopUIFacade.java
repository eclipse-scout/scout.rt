/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.shared.deeplink.DeepLinkUrlParameter;

/**
 * The desktop model (may) consist of
 * <ul>
 * <li>set of available outline
 * <li>active outline
 * <li>active tableview
 * <li>active detail form
 * <li>active search form
 * <li>form stack (swing: dialogs on desktop as JInternalFrames; eclipse: editors or views)
 * <li>dialog stack of model and non-modal dialogs (swing: dialogs as JDialog, JFrame; eclipse: dialogs in a new Shell)
 * <li>active message box stack
 * </ul>
 */
public interface IDesktopUIFacade {

  /**
   * GUI fires this event as soon as the desktop was completely setup and displayed
   */
  void openFromUI();

  /**
   * GUI fires this event when it is in the process of closing the workbench / application
   * <p>
   * The default case is to pass <code>false</code> as parameter.
   *
   * @param forcedClosing
   *          If set to <code>true</code> all vetos to stop the closing process (see
   *          {@link AbstractDesktop#doBeforeClosingInternal()} will be ignored. Otherwise if set to <code>false</code>
   *          vetos are accepted.
   */
  void closeFromUI(boolean forcedClosing);

  /**
   * GUI fires this event when a UiSession has been attached to the desktop. The desktop is open at this point. The
   * event occurs after openFromUI().
   */
  void fireGuiAttached();

  /**
   * @deprecated Do not use this method anymore, use {@link #fireGuiAttached()} instead. The argument
   *             <code>deepLinkPath</code> is ignored! It's value is available in {@link PropertyMap#CURRENT} under the
   *             key {@link DeepLinkUrlParameter#DEEP_LINK}.
   *             <p>
   *             This method will be removed in "P" release.
   */
  @Deprecated
  void fireGuiAttached(String deepLinkPath);

  /**
   * GUI fires this event when the application/workbench is closing
   */
  void fireGuiDetached();

  /**
   * GUI fires this event when user clicks on back/forward button in the navigation history of the browser.
   *
   * @param deepLinkPath
   *          The deep-link path which belongs to the activated history entry. This parameter may be null.
   */
  void historyEntryActivateFromUI(String deepLinkPath);

  void setNavigationVisibleFromUI(boolean visible);

  void setHeaderVisibleFromUI(boolean visible);

  void setBenchVisibleFromUI(boolean visible);

  void setGeoLocationServiceAvailableFromUI(boolean available);

  void fireGeolocationDetermined(String latitude, String longitude);

  void fireGeolocationFailed(String errorCode, String errorMessage);

  /**
   * Called after DesktopNotification was removed from UI
   */
  void removedNotificationFromUI(IDesktopNotification desktopNotification);
}
