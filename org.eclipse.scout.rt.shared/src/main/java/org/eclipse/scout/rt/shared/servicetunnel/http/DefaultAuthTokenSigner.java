/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPrivateKeyProperty;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenTimeToLiveProperty;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;

/**
 * Used to sign authentication tokens and may fill default values.
 */
@ApplicationScoped
public class DefaultAuthTokenSigner {

  protected long getTokenTimeToLive() {
    return CONFIG.getPropertyValue(AuthTokenTimeToLiveProperty.class);
  }

  protected byte[] getPrivateKey() {
    return CONFIG.getPropertyValue(AuthTokenPrivateKeyProperty.class);
  }

  protected String getDefaultUserId() {
    return BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
  }

  /**
   * @return true if enabled
   */
  public boolean isEnabled() {
    return getPrivateKey() != null;
  }

  /**
   * Creates new token, sets {@link DefaultAuthToken#getUserId()} to user id of current subject and signs token.
   *
   * @param tokenClass
   *          not null
   * @return null if not enabled or no userId could be determined else serialized token
   */
  public <T extends DefaultAuthToken> T createDefaultSignedToken(Class<T> tokenClazz) {
    if (!isEnabled()) {
      return null;
    }
    String userId = getDefaultUserId();
    if (StringUtility.isNullOrEmpty(userId)) {
      return null;
    }
    T token = BEANS.get(tokenClazz);
    token.withUserId(userId);
    return sign(token);
  }

  /**
   * Sets {@link DefaultAuthToken#getValidUntil()} and signs token.
   *
   * @param non
   *          null token to sign
   * @return token for method chaining
   */
  public <T extends DefaultAuthToken> T sign(T token) {
    token.withValidUntil(System.currentTimeMillis() + getTokenTimeToLive());

    if (isEnabled()) {
      token.withSignature(signature(token));
    }
    return token;
  }

  protected byte[] signature(DefaultAuthToken token) {
    return SecurityUtility.createSignature(getPrivateKey(), token.write(false).getBytes(StandardCharsets.UTF_8));
  }
}
