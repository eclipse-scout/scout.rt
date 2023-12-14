/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import java.util.Map;

import jakarta.jms.Message;

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
