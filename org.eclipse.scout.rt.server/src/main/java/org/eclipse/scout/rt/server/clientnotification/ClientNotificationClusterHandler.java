package org.eclipse.scout.rt.server.clientnotification;

import java.util.Collection;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;

/**
 * Publishes {@link ClientNotificationClusterNotification}s received via cluster synchronization into local registry.
 * <h3>{@link ClientNotificationClusterHandler}</h3>
 *
 * @author jgu
 */
public class ClientNotificationClusterHandler implements INotificationHandler<ClientNotificationClusterNotification> {

  @Override
  public void handleNotification(ClientNotificationClusterNotification notification) {
    Collection<? extends ClientNotificationMessage> messages = notification.getClientNotificationMessages();
    BEANS.get(ClientNotificationRegistry.class).publishWithoutClusterNotification(messages);
  }

}
