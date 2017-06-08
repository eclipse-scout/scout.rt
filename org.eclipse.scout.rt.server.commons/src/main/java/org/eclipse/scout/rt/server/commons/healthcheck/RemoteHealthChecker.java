package org.eclipse.scout.rt.server.commons.healthcheck;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.RemoteHealthCheckUrlsProperty;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

/**
 * The <code>RemoteHealthChecker</code> provides a simple way to daisy-chain multiple applications' status (e.g. HTML UI
 * -> Server).
 *
 * @see RemoteHealthCheckUrlsProperty
 * @since 6.1
 */
public class RemoteHealthChecker extends AbstractHealthChecker {

  private final List<String> m_remoteUrls;

  public RemoteHealthChecker() {
    m_remoteUrls = BEANS.get(RemoteHealthCheckUrlsProperty.class).getValue();
  }

  @Override
  protected long getConfiguredTimeoutMillis() {
    return TimeUnit.SECONDS.toMillis(30);
  }

  @Override
  public boolean isActive() {
    return !CollectionUtility.isEmpty(m_remoteUrls);
  }

  @Override
  protected boolean execCheckHealth() throws Exception {
    boolean status = true;
    if (m_remoteUrls != null) {
      for (String remote : m_remoteUrls) {
        GenericUrl remoteUrl = remote != null ? new GenericUrl(remote) : null;
        HttpRequest req = BEANS.get(DefaultHttpTransportManager.class).getHttpRequestFactory().buildHeadRequest(remoteUrl);
        req.getHeaders().setCacheControl("no-cache");
        HttpResponse resp = req.execute();
        int statusCode = resp.getStatusCode();
        if (statusCode < 200 || statusCode >= 400) {
          status = false;
          break;
        }
      }
    }
    return status;
  }

}
