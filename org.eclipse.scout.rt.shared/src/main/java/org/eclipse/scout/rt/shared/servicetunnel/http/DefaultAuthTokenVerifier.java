/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPublicKeyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies authentication tokens.
 */
@ApplicationScoped
public class DefaultAuthTokenVerifier {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthTokenVerifier.class);

  /**
   * @return true if enabled
   */
  public boolean isEnabled() {
    return getPublicKey() != null;
  }

  protected byte[] getPublicKey() {
    return CONFIG.getPropertyValue(AuthTokenPublicKeyProperty.class);
  }

  /**
   * @return true if token is valid
   */
  public boolean verify(DefaultAuthToken token) {
    try {
      return token != null && verifyUser(token) && verifyValidUntil(token) && verifySignature(token);
    }
    catch (RuntimeException e) {
      LOG.info("Failed verifying signature of token {}", token, e);
      return false;
    }
  }

  protected boolean verifyUser(DefaultAuthToken token) {
    return StringUtility.hasText(token.getUserId());
  }

  protected boolean verifyValidUntil(DefaultAuthToken token) {
    return System.currentTimeMillis() < token.getValidUntil();
  }

  protected boolean verifySignature(DefaultAuthToken token) {
    byte[] signature = token.getSignature();
    if (signature == null) {
      return false;
    }
    byte[] publicKey = getPublicKey();
    if (publicKey == null) {
      return false;
    }
    return SecurityUtility.verifySignature(publicKey, token.write(false).getBytes(StandardCharsets.UTF_8), signature);
  }
}
