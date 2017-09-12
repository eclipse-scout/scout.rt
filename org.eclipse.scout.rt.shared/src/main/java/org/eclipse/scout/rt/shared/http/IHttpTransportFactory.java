package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.google.api.client.http.HttpTransport;

/**
 * Factory (helper) for {@link IHttpTransportManager} (especially {@link AbstractHttpTransportManager}) classes to
 * create new instances of {@link HttpTransport}.
 */
@FunctionalInterface
@ApplicationScoped
public interface IHttpTransportFactory {

  /**
   * Create a new {@link HttpTransport} for the specific {@link IHttpTransportManager}.
   */
  HttpTransport newHttpTransport(IHttpTransportManager manager);

}
