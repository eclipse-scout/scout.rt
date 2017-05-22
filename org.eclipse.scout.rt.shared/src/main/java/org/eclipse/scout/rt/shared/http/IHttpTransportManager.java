package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;

/**
 * Interface to manager {@link HttpTransport} instances for other application classes.
 */
@ApplicationScoped
public interface IHttpTransportManager {

  /**
   * Get the {@link HttpTransport} instance. This method may create new instances or return a previously created one.
   */
  HttpTransport getHttpTransport();

  /**
   * Get the {@link HttpRequestFactory} for the specific {@link HttpTransport}.
   */
  HttpRequestFactory getHttpRequestFactory();
}
