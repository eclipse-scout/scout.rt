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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.SecurityUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenHashContentProperty;
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
  protected static final boolean USE_CONTENT_HASH = CONFIG.getPropertyValue(AuthTokenHashContentProperty.class);

  private final String m_userId;
  private final long m_validUntil;
  private final byte[] m_contentHash;
  private final String[] m_customTokens;
  private byte[] m_signature;

  public DefaultAuthToken(ISession session) throws ProcessingException {
    this(session, null);
  }

  public DefaultAuthToken(ISession session, byte[] data) throws ProcessingException {
    this(session, data, null);
  }

  public DefaultAuthToken(ISession session, byte[] data, String[] customTokens) throws ProcessingException {
    this(CollectionUtility.firstElement(session.getSubject().getPrincipals()).getName(), data, customTokens);
  }

  protected DefaultAuthToken(String userId, long validUntil, byte[] contentHash, byte[] signature, String[] customTokens) {
    m_userId = userId;
    m_validUntil = validUntil;
    if (customTokens == null) {
      m_customTokens = null;
    }
    else {
      m_customTokens = Arrays.copyOf(customTokens, customTokens.length);
    }
    m_contentHash = contentHash;
    m_signature = signature;
  }

  public DefaultAuthToken(String userId) throws ProcessingException {
    this(userId, null);
  }

  public DefaultAuthToken(String userId, byte[] data) throws ProcessingException {
    this(userId, data, null);
  }

  public DefaultAuthToken(String userId, byte[] data, String[] customTokens) throws ProcessingException {
    m_userId = userId;
    m_validUntil = System.currentTimeMillis() + TOKEN_TTL;
    if (customTokens == null) {
      m_customTokens = null;
    }
    else {
      m_customTokens = Arrays.copyOf(customTokens, customTokens.length);
    }
    m_contentHash = calcContentHash(data);
  }

  protected byte[] calcContentHash(byte[] content) throws ProcessingException {
    if (content == null) {
      return null;
    }

    if (!USE_CONTENT_HASH) {
      return null;
    }

    return SecurityUtility.hash(content, SALT);
  }

  public static boolean isActive() {
    return PRIVATE_KEY != null || PUBLIC_KEY != null;
  }

  protected void calcSignature(String clearTextPart) throws ProcessingException {
    try {
      m_signature = SecurityUtility.createSignature(PRIVATE_KEY, clearTextPart.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
      throw new ProcessingException("Could not find utf-8 encoding.", e);
    }
  }

  public static DefaultAuthToken fromSignedString(String token) {
    if (!StringUtility.hasText(token)) {
      return null;
    }

    String[] parts = TOKEN_SPLIT_REGEX.split(token);
    if (parts == null || parts.length < 4) {
      return null;
    }

    long validUntil = Long.parseLong(parts[1]);
    String hashPart = parts[2];
    byte[] hash = null;
    if (StringUtility.hasText(hashPart)) {
      hash = Base64Utility.decode(hashPart);
    }
    String[] customParts = null;
    if (parts.length > 4) {
      customParts = Arrays.copyOfRange(parts, 3, parts.length - 1);
    }
    byte[] signature = Base64Utility.decode(parts[parts.length - 1]);

    return new DefaultAuthToken(parts[0], validUntil, hash, signature, customParts);
  }

  public String getUserId() {
    return m_userId;
  }

  public long getValidUntil() {
    return m_validUntil;
  }

  public byte[] getContentHash() {
    return m_contentHash;
  }

  public String getCustomToken(int customIndex) {
    return m_customTokens[customIndex];
  }

  protected byte[] getSignature() {
    return m_signature;
  }

  public boolean isContentHashValid(byte[] data) throws ProcessingException {
    if (m_contentHash == null) {
      return true;
    }

    byte[] calculatedHash = SecurityUtility.hash(data, SALT);
    return Arrays.equals(calculatedHash, m_contentHash);
  }

  public boolean isSignatureValid() throws ProcessingException {
    if (!isActive()) {
      return false;
    }

    try {
      return SecurityUtility.verifySignature(PUBLIC_KEY, toString().getBytes("UTF-8"), m_signature);
    }
    catch (UnsupportedEncodingException e) {
      throw new ProcessingException("Could not find utf-8 encoding.", e);
    }
  }

  public String toSignedString() throws ProcessingException {
    if (!isActive()) {
      return null;
    }

    String clearTextPart = toString();
    StringBuilder sb = new StringBuilder(clearTextPart);

    // sign it
    calcSignature(clearTextPart);
    sb.append(TOKEN_DELIM);
    sb.append(Base64Utility.encode(m_signature));

    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(m_userId).append(TOKEN_DELIM).append(m_validUntil).append(TOKEN_DELIM);
    if (getContentHash() != null) {
      sb.append(Base64Utility.encode(m_contentHash));
    }
    if (m_customTokens != null) {
      for (String token : m_customTokens) {
        sb.append(TOKEN_DELIM).append(token);
      }
    }
    return sb.toString();
  }
}
