package org.eclipse.scout.rt.server.jms;

import javax.jms.Message;

import org.eclipse.scout.rt.mom.api.MOM;

/**
 * Interface to hold information about an ongoing JMS request.
 *
 * @since 6.1
 * @deprecated will be removed in 7.1.x; use {@link MOM} instead.
 */
@Deprecated
public interface IJmsMessage {

  /**
   * The JMS {@link Message} which is currently associated with the current thread.
   */
  ThreadLocal<Message> CURRENT = new ThreadLocal<>();
}
