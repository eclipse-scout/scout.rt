/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.eclipse.scout.rt.mom.jms.JmsMomImplementor.MomExceptionHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener to receive JMS messages.
 *
 * @since 6.1
 */
public abstract class JmsMessageListener implements MessageListener { // NOSONAR

  private static final Logger LOG = LoggerFactory.getLogger(JmsMessageListener.class);

  @Override
  public final void onMessage(final Message message) {
    try {
      LOG.debug("Receiving JMS message [message={}]", message);
      onJmsMessage(message);
    }
    catch (final Exception e) {
      BEANS.get(MomExceptionHandler.class).handle(e);
    }
  }

  /**
   * Invoked upon the receipt of a message. If this method throws an exception, the exception is given to
   * {@link MomExceptionHandler}.
   */
  protected abstract void onJmsMessage(Message message) throws Exception; // NOSONAR
}
