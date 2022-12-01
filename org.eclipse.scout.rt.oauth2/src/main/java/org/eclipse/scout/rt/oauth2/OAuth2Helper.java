/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.oauth2;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

@ApplicationScoped
public class OAuth2Helper {

  private Lock m_cacheAccessLock = new ReentrantLock();
  private Map<String, TokenEntry> m_tokenCache = new HashMap<>();

  /**
   * Callers of this method must ensure, that oAuth2Config.getId() gives a unique String for each OAuth2 configuration
   * and that {@link #invalidateCacheEntry(String)} is called whenever that configuration changes.
   *
   * @param oAuth2Config
   * @return OAuth2 access token
   */
  public String getToken(OAuth2Config oAuth2Config) {
    TokenEntry tokenEntry = null;
    m_cacheAccessLock.lock();
    try {
      tokenEntry = m_tokenCache.get(oAuth2Config.getId());
      if (tokenEntry == null
          || tokenEntry.getAccessToken() == null
          || tokenEntry.getTokenReceivedDate() == null
          || DateUtility.addSeconds(
              tokenEntry.getTokenReceivedDate(),
              tokenEntry.getAccessToken().getExpiresIn() - 60).before(BEANS.get(IDateProvider.class).currentSeconds())) {
        try {
          OAuth20Service service = new ServiceBuilder(oAuth2Config.getClientId())
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
              });
          OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrant();
          tokenEntry = new TokenEntry(token, new Date());
          m_tokenCache.put(oAuth2Config.getId(), tokenEntry);
        }
        catch (IOException | InterruptedException | ExecutionException e) {
          throw new ProcessingException("Exception while retrieving OAuth2 access token.", e);
        }
      }
    }
    finally {
      m_cacheAccessLock.unlock();
    }
    if (tokenEntry.getAccessToken() != null) {
      return tokenEntry.getAccessToken().getAccessToken();
    }
    return null;
  }

  public void invalidateCacheEntry(String id) {
    m_cacheAccessLock.lock();
    try {
      m_tokenCache.remove(id);
    }
    finally {
      m_cacheAccessLock.unlock();
    }
  }

  protected static class TokenEntry {
    private final OAuth2AccessToken m_accessToken;
    private final Date m_tokenReceivedDate;

    public TokenEntry(OAuth2AccessToken accessToken, Date tokenReceivedDate) {
      m_accessToken = accessToken;
      m_tokenReceivedDate = tokenReceivedDate;
    }

    public OAuth2AccessToken getAccessToken() {
      return m_accessToken;
    }

    public Date getTokenReceivedDate() {
      return m_tokenReceivedDate;
    }
  }

}
