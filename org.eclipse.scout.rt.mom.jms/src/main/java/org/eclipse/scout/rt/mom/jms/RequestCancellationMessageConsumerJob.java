/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.JMS_PROP_REPLY_ID;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.platform.job.Jobs;

public class RequestCancellationMessageConsumerJob<DTO> extends AbstractMessageConsumerJob<DTO> {

  public RequestCancellationMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, SubscribeInput input) {
    super(mom, sessionProvider, destination, input);
  }

  @Override
  protected void onJmsMessage(Message jmsMessage) throws JMSException {
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jmsMessage.getStringProperty(JMS_PROP_REPLY_ID))
        .toFilter(), true);
  }
}
