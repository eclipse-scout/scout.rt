/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.oauth2;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

@ApplicationScoped
public class OAuth2Helper {

  protected final ConcurrentHashMap<OAuth2Config, TokenEntry> m_tokenCache = new ConcurrentHashMap<>();
  protected long m_latestEviction = BEANS.get(IDateProvider.class).currentUTCMillis();
  // Clean map after 30 minutes (or longer, if #getToken() isn't called)
  protected static final long EVICTION_INTERVALL = 30 * 60 * 1000;

  /**
   * Supports the OAuth2 client credentials flow.
   *
   * @return OAuth2 access token
   */
  public String getToken(OAuth2Config oAuth2Config) {
    try {
      TokenEntry tokenEntry = null;
      tokenEntry = m_tokenCache.get(oAuth2Config);
      IDateProvider dateProvider = BEANS.get(IDateProvider.class);
      if (tokenEntry == null
          || tokenEntry.getAccessToken() == null
          || tokenEntry.getTokenReceivedTime() == null
          // If expired or when expiring within the next minute: Get a new token
          || (tokenEntry.getTokenReceivedTime() + tokenEntry.getAccessToken().getExpiresIn() * 1000 - 60000) < dateProvider.currentUTCMillis()) {
        OAuth2AccessToken token;
        try (OAuth20Service service = new ServiceBuilder(oAuth2Config.getClientId())
            .apiSecret(oAuth2Config.getClientSecret())
            .defaultScope(oAuth2Config.getScope())
            .build(new DefaultApi20() {

              @Override
              public String getAccessTokenEndpoint() {
                return oAuth2Config.getTokenEndpoint();
              }

              @Override
              protected String getAuthorizationBaseUrl() {
                return oAuth2Config.getAuthorizationEndpoint();
              }
            })) {
          token = service.getAccessTokenClientCredentialsGrant();

          tokenEntry = new TokenEntry(token, dateProvider.currentUTCMillis());
          m_tokenCache.put(oAuth2Config, tokenEntry);
        }
        catch (IOException | InterruptedException | ExecutionException e) {
          throw new ProcessingException("Exception while retrieving OAuth2 access token.", e);
        }
      }
      if (tokenEntry.getAccessToken() != null) {
        return tokenEntry.getAccessToken().getAccessToken();
      }
      return null;
    }
    finally {
      evictExpiredEntries();
    }
  }

  /**
   * Evict all expired entries when at least EVICTION_INTERVALL milliseconds have elapsed since last eviction
   */
  protected void evictExpiredEntries() {
    IDateProvider dateProvider = BEANS.get(IDateProvider.class);
    if (m_latestEviction + EVICTION_INTERVALL > dateProvider.currentUTCMillis()) {
      Iterator<Entry<OAuth2Config, TokenEntry>> iterator = m_tokenCache.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<OAuth2Config, TokenEntry> entry = iterator.next();
        if (entry.getValue().m_tokenReceivedTime + entry.getValue().getAccessToken().getExpiresIn() * 1000 < dateProvider.currentUTCMillis()) {
          m_tokenCache.remove(entry.getKey());
        }
      }
      m_latestEviction = dateProvider.currentUTCMillis();
    }
  }

  protected static class TokenEntry {

    private final OAuth2AccessToken m_accessToken;
    private final Long m_tokenReceivedTime;

    public TokenEntry(OAuth2AccessToken accessToken, Long tokenReceivedTime) {
      m_accessToken = accessToken;
      m_tokenReceivedTime = tokenReceivedTime;
    }

    public OAuth2AccessToken getAccessToken() {
      return m_accessToken;
    }

    public Long getTokenReceivedTime() {
      return m_tokenReceivedTime;
    }
  }
}
