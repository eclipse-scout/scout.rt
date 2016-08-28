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
  public <DTO> void publish(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    m_delegate.publish(destination, transferObject, input);
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext, final int acknowledgementMode) {
    return m_delegate.subscribe(destination, listener, runContext, acknowledgementMode);
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
