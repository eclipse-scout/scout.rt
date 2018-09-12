/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import javax.jms.Message;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

public class MessageConsumerJob<DTO> extends AbstractMessageConsumerJob<DTO> {
  protected final IMessageListener<DTO> m_listener;

  public MessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input) throws JMSException {
    super(mom, sessionProvider, destination, input);
    m_listener = listener;
  }

  @Override
  protected void onJmsMessage(final Message jmsMessage) throws JMSException {
    if (isSingleThreaded() || isTransacted()) {
      handleMessageInRunContext(jmsMessage);
    }
    else {
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          handleMessageInRunContext(jmsMessage);
        }
      }, m_mom.newJobInput().withName("Receiving JMS message [dest={}]", m_destination));
    }
  }

  protected void handleMessageInRunContext(final Message jmsMessage) throws JMSException {
    final JmsMessageReader<DTO> messageReader = JmsMessageReader.newInstance(jmsMessage, m_marshaller);
    final IMessage<DTO> message = messageReader.readMessage();

    createRunContext()
        .withCorrelationId(messageReader.readCorrelationId())
        .withThreadLocal(IMessage.CURRENT, message)
        .run(() -> m_listener.onMessage(message));
  }
}
