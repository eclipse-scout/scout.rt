package org.eclipse.scout.rt.mom.api;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
 * Represents a {@link IMom} and delegates all invocations to the delegate {@link IMom}.
 *
 * @see IMom
 * @since 6.1
 */
public abstract class AbstractMomDelegate implements IMom {

  private final FinalValue<IMom> m_delegate = new FinalValue<>();

  /**
   * @return the MOM delegate. The delegate is initialized <i>synchronously</i> when this method is first called (i.e.
   *         this call may block until the delegate is initialized).
   */
  protected IMom getDelegate() {
    if (!m_delegate.isSet()) {
      synchronized (m_delegate) {
        m_delegate.setIfAbsent(new Callable<IMom>() {
          @Override
          public IMom call() throws Exception {
            return initDelegate();
          }
        });
      }
    }
    return m_delegate.get();
  }

  /**
   * Initializes this {@link IMom} delegate.
   * <p>
   * <h3>Warning: risk of deadlocks!</h3>
   * <p>
   * This method is called from within a <i>synchronized</i> block inside {@link #getDelegate()}. The implementation
   * must <b>not</b> call (directly or indirectly) any other methods on {@link AbstractMomDelegate}. Be very cautious
   * when calling other code.
   */
  protected abstract IMom initDelegate() throws Exception; // NOSONAR

  @Override
  public <DTO> void publish(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    getDelegate().publish(destination, transferObject, input);
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext, final int acknowledgementMode) {
    return getDelegate().subscribe(destination, listener, runContext, acknowledgementMode);
  }

  @Override
  public <REQUEST, REPLY> REPLY request(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    return getDelegate().request(destination, requestObject, input);
  }

  @Override
  public <REQUEST, REPLY> ISubscription reply(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final RunContext runContext) {
    return getDelegate().reply(destination, listener, runContext);
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination<?> destination, final IMarshaller marshaller) {
    return getDelegate().registerMarshaller(destination, marshaller);
  }

  @Override
  public IRegistrationHandle registerEncrypter(final IDestination<?> destination, final IEncrypter encrypter) {
    return getDelegate().registerEncrypter(destination, encrypter);
  }

  @Override
  public void destroy() {
    if (!m_delegate.isSet()) {
      return; // don't trigger unnecessary delegate initialization
    }
    getDelegate().destroy();
  }
}
