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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an {@link IMom} that delegates all invocations to a delegate {@link IMom}. The delegate implementation is
 * defined by two configuration methods ({@link #getConfiguredImplementor()} and {@link #getConfiguredEnvironment()}).
 * To further customize the created {@link IMomImplementor}, override {@link #initDelegate()}.
 *
 * @see IMom
 * @since 6.1
 */
public abstract class AbstractMomTransport implements IMomTransport {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractMomTransport.class);

  protected final String m_momUid = UUID.randomUUID().toString();

  private final Object m_delegateLock = new Object();
  private volatile IMomImplementor m_delegate;

  @Override
  public String getId() {
    return m_momUid;
  }

  @Override
  public String getName() {
    return getDelegate().getName();
  }

  @Override
  public List<ISubscription> getSubscriptions() {
    return getDelegate().getSubscriptions();
  }

  /**
   * @return the {@link IMomImplementor} class to use in this MOM. If <code>null</code> is returned, the
   *         {@link NullMomImplementor} is used.
   */
  protected abstract Class<? extends IMomImplementor> getConfiguredImplementor();

  /**
   * @return the environment required to initialize the MOM implementor (not <code>null</code>).
   */
  protected abstract Map<String, String> getConfiguredEnvironment();

  /**
   * @return the default {@link IMarshaller} to be used if no specific marshaller is configured for this MOM or is
   *         registered for a {@link IDestination}. The default value is <code>null</code>, which means that the default
   *         marshaller is chosen by the implementor.
   */
  protected IMarshaller getConfiguredDefaultMarshaller() {
    return null;
  }

  @Override
  public boolean isNullTransport() {
    final Class<? extends IMomImplementor> implementorClass = getConfiguredImplementor();
    return implementorClass == null || NullMomImplementor.class.isAssignableFrom(implementorClass);
  }

  /**
   * Initializes this {@link IMom} delegate.
   * <p>
   * <h3>Warning: risk of deadlocks!</h3>
   * <p>
   * This method is called from within a <i>synchronized</i> block inside {@link #getDelegate()}. The implementation
   * must <b>not</b> call (directly or indirectly) any other methods on {@link AbstractMomTransport}. Be very cautious
   * when calling other code.
   */
  protected IMomImplementor initDelegate() throws Exception {
    final Class<? extends IMomImplementor> implementorClass = ObjectUtility.nvl(getConfiguredImplementor(), NullMomImplementor.class);
    final IMomImplementor implementor = BEANS.get(implementorClass);

    if (NullMomImplementor.class.isAssignableFrom(implementorClass)) {
      LOG.info("+++ Using '{}' for transport '{}'. No messages are published and received.", implementorClass.getSimpleName(), getClass().getSimpleName());
    }
    else {
      implementor.init(lookupEnvironment());
    }

    return implementor;
  }

  /**
   * @return the environment required to initialize the MOM implementor.
   */
  protected Map<Object, Object> lookupEnvironment() {
    final Map<String, String> configuredEnv = Assertions.assertNotNull(getConfiguredEnvironment(), "Environment for {} not specified", getClass().getSimpleName());
    final Map<Object, Object> env = new HashMap<>(configuredEnv);
    // Use the class name as default symbolic name
    if (!env.containsKey(IMomImplementor.SYMBOLIC_NAME)) {
      env.put(IMomImplementor.SYMBOLIC_NAME, getClass().getSimpleName());
    }
    // Use MOM-specific default marshaller
    if (!env.containsKey(IMomImplementor.MARSHALLER)) {
      IMarshaller defaultMarshaller = getConfiguredDefaultMarshaller();
      if (defaultMarshaller != null) {
        env.put(IMomImplementor.MARSHALLER, defaultMarshaller);
      }
    }
    return env;
  }

  // --- Delegated methods ---

  /**
   * @return the MOM delegate. The delegate is initialized <i>synchronously</i> when this method is first called (i.e.
   *         this call may block until the delegate is initialized).
   */
  protected IMomImplementor getDelegate() {
    if (m_delegate == null) {
      synchronized (m_delegateLock) {
        if (m_delegate == null) {
          try {
            m_delegate = initDelegate();
          }
          catch (Exception e) {
            m_delegate = BEANS.get(NullMomImplementor.class);
            throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
          }
        }
      }
    }
    return m_delegate;
  }

  /**
   * @return {@link IMomImplementor} which may not have been initialized yet. The implementor does not get initialized
   *         when this method is called. Use {@link #getDelegate()} to safely access the initialized
   *         {@link IMomImplementor} instance.
   * @see #getDelegate()
   */
  public IMomImplementor getImplementor() {
    return m_delegate;
  }

  @Override
  public <DTO> void publish(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    getDelegate().publish(destination, transferObject, input);
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final SubscribeInput input) {
    return getDelegate().subscribe(destination, listener, input);
  }

  @Override
  public <REQUEST, REPLY> REPLY request(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    return getDelegate().request(destination, requestObject, input);
  }

  @Override
  public <REQUEST, REPLY> ISubscription reply(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final SubscribeInput input) {
    return getDelegate().reply(destination, listener, input);
  }

  @Override
  public void cancelDurableSubscription(String durableSubscriptionName) {
    getDelegate().cancelDurableSubscription(durableSubscriptionName);
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination<?> destination, final IMarshaller marshaller) {
    return getDelegate().registerMarshaller(destination, marshaller);
  }

  @Override
  public void destroy() {
    IMomImplementor delegate;
    synchronized (m_delegateLock) {
      delegate = m_delegate;
      m_delegate = null;
    }
    if (delegate == null) {
      return; // don't trigger unnecessary delegate initialization
    }
    delegate.destroy();
  }
}
