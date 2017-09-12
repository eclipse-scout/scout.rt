package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Notification handler that is capable of dispaching based on the {@link IClientNotificationAddress}.
 */
@FunctionalInterface
@ApplicationScoped
public interface IDispatchingNotificationHandler<T extends Serializable> {

  /**
   * @param notification
   * @param address
   */
  void handleNotification(T notification, IClientNotificationAddress address);

}
