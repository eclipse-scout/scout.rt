package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;

/**
 * <p>
 * Configuration property to define the default {@link IHttpTransportFactory}.
 * </p>
 * <p>
 * If property is not set, the default is {@link ApacheHttpTransportFactory}.
 * </p>
 */
public class HttpTransportFactoryProperty extends AbstractClassConfigProperty<IHttpTransportFactory> {

  @Override
  protected Class<? extends IHttpTransportFactory> getDefaultValue() {
    return ApacheHttpTransportFactory.class;
  }

  @Override
  public String getKey() {
    return "scout.http.transport_factory";
  }

}
