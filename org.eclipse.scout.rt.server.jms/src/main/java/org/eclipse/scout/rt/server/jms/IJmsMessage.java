package org.eclipse.scout.rt.server.jms;

import javax.jms.Message;

/**
 * Interface to hold information about an ongoing JMS request.
 *
 * @since 6.1
 */
public interface IJmsMessage {

  /**
   * The JMS {@link Message} which is currently associated with the current thread.
   */
  ThreadLocal<Message> CURRENT = new ThreadLocal<>();
}
