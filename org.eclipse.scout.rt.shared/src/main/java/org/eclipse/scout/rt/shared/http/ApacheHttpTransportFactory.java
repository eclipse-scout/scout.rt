package org.eclipse.scout.rt.shared.http;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.scout.rt.shared.http.transport.ApacheHttpTransport;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStore;

import com.google.api.client.http.HttpTransport;

/**
 * Factory to create the {@link ApacheHttpTransport} instances.
 */
public class ApacheHttpTransportFactory implements IHttpTransportFactory {

  @Override
  public HttpTransport newHttpTransport(IHttpTransportManager manager) {
    HttpClientBuilder builder = HttpClients.custom();

    installMultiSessionCookieStore(builder);
    interceptNewHttpTransport(builder, manager);

    return new ApacheHttpTransport(builder.build());
  }

  /**
   * Install a {@link MultiSessionCookieStore} to store cookies by session.
   */
  protected void installMultiSessionCookieStore(HttpClientBuilder builder) {
    builder.setDefaultCookieStore(new ApacheMultiSessionCookieStore());
  }

  /**
   * Intercept the building of the new {@link HttpTransport}.
   */
  protected void interceptNewHttpTransport(HttpClientBuilder builder, IHttpTransportManager manager) {
    // nop
  }

}
