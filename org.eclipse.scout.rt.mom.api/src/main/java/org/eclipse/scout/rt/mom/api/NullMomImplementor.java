/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
 * MOM which does nothing. This is useful to disable a MOM on certain environments, e.g. in development mode.
 *
 * @since 6.1
 */
public class NullMomImplementor implements IMomImplementor {

  @Override
  public void init(final Map<Object, Object> properties) throws Exception {
    // NOOP
  }

  @Override
  public String getId() {
    return NullMomImplementor.class.getSimpleName();
  }

  @Override
  public String getName() {
    return getId();
  }

  @Override
  public List<ISubscription> getSubscriptions() {
    return new ArrayList<>();
  }

  @Override
  public <DTO> void publish(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    // NOOP
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final SubscribeInput input) {
    return new P_NullSubscription(destination, listener, null, input);
  }

  @Override
  public <REQUEST, REPLY> REPLY request(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    return null;
  }

  @Override
  public <REQUEST, REPLY> ISubscription reply(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final SubscribeInput input) {
    return new P_NullSubscription(destination, null, listener, input);
  }

  @Override
  public void cancelDurableSubscription(String durableSubscriptionName) {
    // NOOP
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination<?> destination, final IMarshaller marshaller) {
    return IRegistrationHandle.NULL_HANDLE;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  private class P_NullSubscription implements ISubscription {

    private final IDestination<?> m_destination;
    private final IMessageListener<?> m_messageListener;
    private final IRequestListener<?, ?> m_requestListener;
    private final SubscribeInput m_input;

    P_NullSubscription(IDestination<?> destination, IMessageListener<?> messageListener, IRequestListener<?, ?> requestListener, SubscribeInput input) {
      m_destination = destination;
      m_messageListener = messageListener;
      m_requestListener = requestListener;
      m_input = input;
    }

    @Override
    public IDestination<?> getDestination() {
      return m_destination;
    }

    @Override
    public IMessageListener<?> getMessageListener() {
      return m_messageListener;
    }

    @Override
    public IRequestListener<?, ?> getRequestListener() {
      return m_requestListener;
    }

    @Override
    public SubscribeInput getSubscribeInput() {
      return m_input;
    }

    @Override
    public void dispose() {
      // NOOP
    }

    @Override
    public boolean isDisposed() {
      return false;
    }

    @Override
    public ISubscriptionStats getStats() {
      return null;
    }
  }
}
