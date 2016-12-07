package org.eclipse.scout.rt.mom.api;

import java.util.Map;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
 * MOM which does nothing. This is useful to disable a MOM on certain environments, e.g. in development mode.
 *
 * @since 6.1
 */
@ApplicationScoped
public final class NullMomImplementor implements IMomImplementor {

  @Override
  public void init(final Map<Object, Object> properties) throws Exception {
    // NOOP
  }

  @Override
  public <DTO> void publish(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    // NOOP
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext, final int acknowledgementMode) {
    return new P_NullSubscription(destination);
  }

  @Override
  public <REQUEST, REPLY> REPLY request(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    return null;
  }

  @Override
  public <REQUEST, REPLY> ISubscription reply(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final RunContext runContext) {
    return new P_NullSubscription(destination);
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination<?> destination, final IMarshaller marshaller) {
    return IRegistrationHandle.NULL_HANDLE;
  }

  @Override
  public IRegistrationHandle registerEncrypter(final IDestination<?> destination, final IEncrypter encrypter) {
    return IRegistrationHandle.NULL_HANDLE;
  }

  @Override
  public void setDefaultMarshaller(final IMarshaller marshaller) {
    // NOOP
  }

  @Override
  public void setDefaultEncrypter(final IEncrypter encrypter) {
    // NOOP
  }

  @Override
  public void destroy() {
    // NOOP
  }

  private class P_NullSubscription implements ISubscription {

    private final IDestination<?> m_destination;

    P_NullSubscription(final IDestination<?> destination) {
      m_destination = destination;
    }

    @Override
    public void dispose() {
      // NOOP
    }

    @Override
    public IDestination<?> getDestination() {
      return m_destination;
    }
  }
}
