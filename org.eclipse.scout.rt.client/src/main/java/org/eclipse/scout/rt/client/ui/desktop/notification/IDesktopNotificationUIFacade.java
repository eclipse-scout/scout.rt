package org.eclipse.scout.rt.client.ui.desktop.notification;

@FunctionalInterface
public interface IDesktopNotificationUIFacade {

  /**
   * Notification that the DesktopNotification was closed in the UI
   */
  void fireClosedFromUI();
}
