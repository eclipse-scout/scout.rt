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
public abstract class AbstractMomDelegate implements IMom {

  protected IMom m_delegate;

  @PostConstruct
  public final void init() throws Exception {
    m_delegate = initDelegate();
  }

  @Override
  public void publish(final IDestination destination, final Object transferObject) {
    m_delegate.publish(destination, transferObject);
  }

  @Override
  public void publish(final IDestination destination, final Object transferObject, final PublishInput input) {
    m_delegate.publish(destination, transferObject, input);
  }

  @Override
  public <TRANSFER_OBJECT> ISubscription subscribe(final IDestination destination, final IMessageListener<TRANSFER_OBJECT> listener, final RunContext runContext) {
    return m_delegate.subscribe(destination, listener, runContext);
  }

  @Override
  public <TRANSFER_OBJECT> ISubscription subscribe(final IDestination destination, final IMessageListener<TRANSFER_OBJECT> listener, final RunContext runContext, final int acknowledgementMode) {
    return m_delegate.subscribe(destination, listener, runContext, acknowledgementMode);
  }

  @Override
  public <REPLY_OBJECT, REQUEST_OBJECT> REPLY_OBJECT request(final IDestination destination, final REQUEST_OBJECT transferObject) {
    return m_delegate.request(destination, transferObject);
  }

  @Override
  public <REPLY_OBJECT> REPLY_OBJECT request(final IDestination destination, final Object transferObject, final PublishInput input) {
    return m_delegate.request(destination, transferObject, input);
  }

  @Override
  public <REQUEST_TRANSFER_OBJECT, REPLY_TRANSFER_OBJECT> ISubscription reply(final IDestination destination, final IRequestListener<REQUEST_TRANSFER_OBJECT, REPLY_TRANSFER_OBJECT> listener, final RunContext runContext) {
    return m_delegate.reply(destination, listener, runContext);
  }

  @Override
  public IDestination newTopic(final String topic) {
    return m_delegate.newTopic(topic);
  }

  @Override
  public IDestination newQueue(final String queue) {
    return m_delegate.newQueue(queue);
  }

  @Override
  public IDestination lookupDestination(final String destination) {
    return m_delegate.lookupDestination(destination);
  }

  @Override
  public PublishInput newPublishInput() {
    return m_delegate.newPublishInput();
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination destination, final IMarshaller marshaller) {
    return m_delegate.registerMarshaller(destination, marshaller);
  }

  @Override
  public IRegistrationHandle registerEncrypter(final IDestination destination, final IEncrypter encrypter) {
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
