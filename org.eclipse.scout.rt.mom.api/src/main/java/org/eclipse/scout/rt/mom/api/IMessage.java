package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Represents a message received via {@link IMom}.
 *
 * @since 6.1
 */
public interface IMessage<TRANSFER_OBJECT> extends IAdaptable {

  /**
   * The {@link IMessage} which is currently associated with the current thread.
   */
  ThreadLocal<IMessage<?>> CURRENT = new ThreadLocal<>();

  /**
   * Returns the transfer object sent with this message.
   */
  TRANSFER_OBJECT getTransferObject();

  /**
   * Returns the property associated with this message.
   */
  String getProperty(String property);
}
