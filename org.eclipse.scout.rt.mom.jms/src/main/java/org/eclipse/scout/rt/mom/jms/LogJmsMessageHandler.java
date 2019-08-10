/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.jms;

import java.util.Map;

import javax.jms.Message;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IJmsMessageHandler} implementation logging each JMS message interaction on <i>DEBUG</i> level.
 */
public class LogJmsMessageHandler implements IJmsMessageHandler {

  private static final Logger LOG = LoggerFactory.getLogger(LogJmsMessageHandler.class);

  @Override
  public void init(Map<Object, Object> properties) {
    // NOP
  }

  @Override
  public void handleIncoming(IDestination<?> destination, Message message, IMarshaller marshaller) {
    LOG.debug("Receiving JMS message [destination={}, message={}]", destination, message);
  }

  @Override
  public void handleOutgoing(IDestination<?> destination, Message message, IMarshaller marshaller) {
    LOG.debug("Sending JMS message [destination={}, message={}]", destination, message);
  }
}
