package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStoreInstaller;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport.Builder;

/**
 * <p>
 * Factory to create the {@link NetHttpTransport} instances.
 * </p>
 * <p>
 * Unfortunately these transports do not support cookie handling per instance. Several settings must be set VM wide. If
 * cookies per session should be used it might be helpful to activate {@link MultiSessionCookieStoreInstaller}.
 * </p>
 */
public class NetHttpTransportFactory implements IHttpTransportFactory {

  @Override
  public HttpTransport newHttpTransport(IHttpTransportManager manager) {
    NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
    interceptNewHttpTransport(builder, manager);
    return builder.build();
  }

  /**
   * Intercept the building of the new {@link HttpTransport}.
   */
  protected void interceptNewHttpTransport(Builder builder, IHttpTransportManager manager) {
    // nop
  }

}
