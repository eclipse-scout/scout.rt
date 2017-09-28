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

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a {@link MessageConsumer} in the JMS messaging standard.
 *
 * @see IMom
 * @since 6.1
 */
public class JmsSubscription implements ISubscription {

  private static final Logger LOG = LoggerFactory.getLogger(JmsSubscription.class);

  private final Session m_session;
  private final IDestination<?> m_destination;

  public JmsSubscription(final Session session, final IDestination<?> destination) {
    m_session = session;
    m_destination = destination;
  }

  @Override
  public IDestination<?> getDestination() {
    return m_destination;
  }

  @Override
  public void dispose() {
    try {
      m_session.close();
    }
    catch (final JMSException e) {
      LOG.warn("Failed to close session", m_session, e);
    }
  }
}
