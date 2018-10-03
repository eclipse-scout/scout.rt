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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.HexUtility.HexOutputStream;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication token which can be used within a chain of trust.
 */
@Bean
public class DefaultAuthToken {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthToken.class);

  private String m_userId;
  private long m_validUntil;
  private List<String> m_customArgs;

  private byte[] m_signature;

  public String getUserId() {
    return m_userId;
  }

  public String getUserIdOrAnonymous() {
    return StringUtility.hasText(m_userId) ? m_userId : "anonymous";
  }

  public DefaultAuthToken withUserId(String userId) {
    m_userId = userId;
    return this;
  }

  public long getValidUntil() {
    return m_validUntil;
  }

  public DefaultAuthToken withValidUntil(long validUntil) {
    m_validUntil = validUntil;
    return this;
  }

  protected boolean verifyUser(DefaultAuthToken token) {
    return StringUtility.hasText(token.getUserId());
  }

  protected boolean verifyValidUntil(DefaultAuthToken token) {
    return System.currentTimeMillis() < token.getValidUntil();
  }

  public List<String> getCustomArgs() {
    return m_customArgs;
  }

  public DefaultAuthToken withCustomArgs(List<String> customArgs) {
    m_customArgs = customArgs;
    return this;
  }

  public DefaultAuthToken withCustomArgs(String... customArgs) {
    if (customArgs != null && customArgs.length > 0) {
      m_customArgs = new ArrayList<>(Arrays.asList(customArgs));
    }
    return this;
  }

  public byte[] getSignature() {
    return m_signature;
  }

  public DefaultAuthToken withSignature(byte[] signature) {
    m_signature = signature;
    return this;
  }

  /**
   * According to HTTP spec
   *
   * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">rfc2616</a>
   */
  protected char partsDelimiter() {
    return ';';
  }

  /**
   * @param token
   *          possible null token to be parsed
   * @return this
   */
  public DefaultAuthToken read(String token) {
    if (!StringUtility.hasText(token)) {
      return this;
    }
    String[] parts = token.split(Pattern.quote("" + partsDelimiter()));
    if (parts == null || parts.length < 3) {
      return this;
    }

    try {
      withUserId(new String(HexUtility.decode(parts[0]), StandardCharsets.UTF_8));
      withValidUntil(Long.parseLong(parts[1], 16));
      if (parts.length > 3) {
        int numberOfCustomArgs = parts.length - 1;
        List<String> customArgs = new ArrayList<String>(numberOfCustomArgs);
        for (int i = 2; i < numberOfCustomArgs; i++) {
          customArgs.add(new String(HexUtility.decode(parts[i]), StandardCharsets.UTF_8));
        }
        withCustomArgs(customArgs);
      }
      try { // NOSONAR
        withSignature(HexUtility.decode(parts[parts.length - 1]));
      }
      catch (RuntimeException e) {
        LOG.debug("Could not decode hex string", e);
      }
    }
    catch (Exception ex) {
      throw new PlatformException("unexpected behaviour", ex);
    }
    return this;
  }

  public String write(boolean withSignature) {
    char partsDelimiter = partsDelimiter();
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); HexOutputStream hex = new HexOutputStream(bytes)) {
      hex.write(getUserIdOrAnonymous().getBytes(StandardCharsets.UTF_8));
      bytes.write(partsDelimiter);
      bytes.write(Long.toHexString(getValidUntil()).getBytes());
      if (getCustomArgs() != null) {
        for (String arg : getCustomArgs()) {
          bytes.write(partsDelimiter);
          hex.write(arg.getBytes(StandardCharsets.UTF_8));
        }
      }
      byte[] signature = getSignature();
      if (withSignature && signature != null && signature.length > 0) {
        bytes.write(partsDelimiter);
        hex.write(signature);
      }
      return bytes.toString(StandardCharsets.UTF_8.name());
    }
    catch (IOException ex) {
      throw new PlatformException("unexpected behaviour", ex);
    }
  }

  @Override
  public String toString() {
    return write(true);
  }
}
