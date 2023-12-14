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

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.JMS_PROP_REPLY_ID;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.platform.job.Jobs;

public class RequestCancellationMessageConsumerJob<DTO> extends AbstractMessageConsumerJob<DTO> {

  public RequestCancellationMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, SubscribeInput input) {
    this(mom, sessionProvider, destination, input, 0L);
  }

  public RequestCancellationMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, SubscribeInput input, long receiveTimeout) {
    super(mom, sessionProvider, destination, input, receiveTimeout);
  }

  @Override
  protected void onJmsMessage(Message jmsMessage) throws JMSException {
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jmsMessage.getStringProperty(JMS_PROP_REPLY_ID))
        .toFilter(), true);
    onMessageConsumptionComplete();
  }
}
