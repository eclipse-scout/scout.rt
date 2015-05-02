/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.SecurityUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenNumHashCyclesProperty;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPrivateKeyProperty;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPublicKeyProperty;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenTimeToLifeProperty;

public class DefaultAuthToken {
  /**
   * According to HTTP spec
   *
   * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">rfc2616</a>
   */
  protected static final char TOKEN_DELIM = ';';
  protected static final Pattern TOKEN_SPLIT_REGEX = Pattern.compile(Character.toString(TOKEN_DELIM));

  protected static final byte[] SALT = Base64Utility.decode("tIJdVyLxYDCoXZOAFlZ8Xw==");
  protected static final byte[] PRIVATE_KEY = CONFIG.getPropertyValue(AuthTokenPrivateKeyProperty.class);
  protected static final byte[] PUBLIC_KEY = CONFIG.getPropertyValue(AuthTokenPublicKeyProperty.class);
  protected static final long TOKEN_TTL = CONFIG.getPropertyValue(AuthTokenTimeToLifeProperty.class);
  protected static final int NUM_HASH_CYCLES = CONFIG.getPropertyValue(AuthTokenNumHashCyclesProperty.class);

  public static boolean isActive() {
    return PRIVATE_KEY != null || PUBLIC_KEY != null;
  }

  public static DefaultAuthToken parse(String token) {
    if (!StringUtility.hasText(token)) {
      return null;
    }

    String[] parts = TOKEN_SPLIT_REGEX.split(token);
    if (parts == null || parts.length < 3) {
      return null;
    }

    String userId = parts[0];
    long validUntil = Long.parseLong(parts[1]);
    String[] customTokens = null;
    if (parts.length > 3) {
      customTokens = Arrays.copyOfRange(parts, 2, parts.length - 1);
    }
    byte[] signature = Base64Utility.decode(parts[parts.length - 1]);

    DefaultAuthToken instance = new DefaultAuthToken(userId, validUntil, customTokens);
    instance.setSignature(signature);
    return instance;
  }

  private final String m_userId;
  private final long m_validUntil;
  private final String[] m_customTokens;
  private byte[] m_signature;

  public DefaultAuthToken(String userId, String... customTokens) throws ProcessingException {
    this(userId, System.currentTimeMillis() + TOKEN_TTL, customTokens);
    try {
      setSignature(SecurityUtility.createSignature(PRIVATE_KEY, createUnsignedText().getBytes("UTF-8")));
    }
    catch (Exception e) {
      throw new PlatformException("Invalid signature setup", e);
    }
  }

  protected DefaultAuthToken(String userId, long validUntil, String... customTokens) {
    m_userId = userId;
    m_validUntil = validUntil;
    if (customTokens != null && customTokens.length == 0) {
      customTokens = null;
    }
    m_customTokens = (customTokens == null ? null : Arrays.copyOf(customTokens, customTokens.length));
  }

  public byte[] getSignature() {
    return m_signature;
  }

  protected void setSignature(byte[] signature) {
    m_signature = signature;
  }

  public String getUserId() {
    return m_userId;
  }

  public long getValidUntil() {
    return m_validUntil;
  }

  public int getCustomTokenCount() {
    return m_customTokens == null ? 0 : m_customTokens.length;
  }

  public String getCustomToken(int customIndex) {
    return m_customTokens[customIndex];
  }

  protected String createUnsignedText() throws PlatformException {
    StringBuilder sb = new StringBuilder();
    sb.append(m_userId).append(TOKEN_DELIM).append(m_validUntil);
    if (m_customTokens != null) {
      for (String token : m_customTokens) {
        sb.append(TOKEN_DELIM).append(token);
      }
    }
    return sb.toString();
  }

  public boolean isValid() {
    if (!isActive()) {
      return false;
    }
    if (getSignature() == null) {
      return false;
    }
    try {
      return SecurityUtility.verifySignature(PUBLIC_KEY, createUnsignedText().getBytes("UTF-8"), getSignature());
    }
    catch (Exception e) {
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(createUnsignedText());
    sb.append(TOKEN_DELIM);
    sb.append(Base64Utility.encode(m_signature));
    return sb.toString();
  }
}
