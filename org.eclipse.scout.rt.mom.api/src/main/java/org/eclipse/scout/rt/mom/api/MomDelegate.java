package org.eclipse.scout.rt.mom.api;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
 * Represents a {@link IMom} and delegates all invocations to the delegate {@link IMom}.
 *
 * @see IMom
 * @since 6.1
 */
public abstract class MomDelegate implements IMom {

  protected IMom m_delegate;

  @PostConstruct
  public final void init() throws Exception {
    m_delegate = initDelegate();
  }

  @Override
  public <TYPE> void publish(final IDestination<TYPE> destination, final TYPE transferObject) {
    m_delegate.publish(destination, transferObject);
  }

  @Override
  public <TYPE> void publish(final IDestination<TYPE> destination, final TYPE transferObject, final PublishInput input) {
    m_delegate.publish(destination, transferObject, input);
  }

  @Override
  public <TYPE> ISubscription subscribe(final IDestination<TYPE> destination, final IMessageListener<TYPE> listener, final RunContext runContext) {
    return m_delegate.subscribe(destination, listener, runContext);
  }

  @Override
  public <TYPE> ISubscription subscribe(final IDestination<TYPE> destination, final IMessageListener<TYPE> listener, final RunContext runContext, final int acknowledgementMode) {
    return m_delegate.subscribe(destination, listener, runContext, acknowledgementMode);
  }

  @Override
  public <REQUEST, REPLY> REPLY request(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject) {
    return m_delegate.request(destination, requestObject);
  }

  @Override
  public <REQUEST, REPLY> REPLY request(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    return m_delegate.request(destination, requestObject, input);
  }

  @Override
  public <REQUEST, REPLY> ISubscription reply(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final RunContext runContext) {
    return m_delegate.reply(destination, listener, runContext);
  }

  @Override
  public <TYPE> IDestination<TYPE> newDestination(final String name, final int destinationType) {
    return m_delegate.newDestination(name, destinationType);
  }

  @Override
  public <REQUEST, REPLY> IBiDestination<REQUEST, REPLY> newBiDestination(final String name, final int destinationType) {
    return m_delegate.newBiDestination(name, destinationType);
  }

  @Override
  public PublishInput newPublishInput() {
    return m_delegate.newPublishInput();
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination<?> destination, final IMarshaller marshaller) {
    return m_delegate.registerMarshaller(destination, marshaller);
  }

  @Override
  public IRegistrationHandle registerEncrypter(final IDestination<?> destination, final IEncrypter encrypter) {
    return m_delegate.registerEncrypter(destination, encrypter);
  }

  @Override
  public void destroy() {
    m_delegate.destroy();
  }

  /**
   * Initializes this {@link IMom} delegate.
   */
  protected abstract IMom initDelegate() throws Exception;
}
