package org.eclipse.scout.rt.server.commons.healthcheck;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.UriUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.RemoteHealthCheckUrlsProperty;

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
        URL remoteUrl = UriUtility.toUrl(remote);
        HttpURLConnection conn = (HttpURLConnection) remoteUrl.openConnection();
        conn.setRequestMethod("HEAD");
        conn.setUseCaches(false);
        int statusCode = conn.getResponseCode();
        if (statusCode < 200 || statusCode >= 400) {
          status = false;
          break;
        }
      }
    }
    return status;
  }

}
