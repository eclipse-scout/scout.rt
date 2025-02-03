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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication token which can be used within a chain of trust.
 */
@Bean
public class DefaultAuthToken {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthToken.class);
  private static final Set<Integer> DIRECT_WRITE_BYTES =
      IntStream
          .range(0, 128)
          .filter(i -> (i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z') || (i >= '0' && i <= '9') || i == '+' || i == '-' || i == '_' || i == '.' || i == '/')
          .boxed()
          .collect(Collectors.toSet());

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
   * All bytes a-z A-Z 0-9 and +-_./ are written directly, other bytes are escaped using 3 bytes '%nn' where nn is the
   * hex byte value
   */
  protected void writeByte(ByteArrayOutputStream out, byte b) {
    int i = ((int) b) & 0xff;
    if (DIRECT_WRITE_BYTES.contains(i)) {
      out.write(i);
    }
    else {
      int hi = (i / 0x10);
      int lo = (i % 0x10);
      out.write('$');
      out.write((char) (hi <= 9 ? '0' + hi : 'a' + (hi - 10)));
      out.write((char) (lo <= 9 ? '0' + lo : 'a' + (lo - 10)));
    }
  }

  protected int readByte(ByteArrayInputStream in) {
    int b = in.read();
    if (b != '$' || b < 0) {
      return b;
    }
    int hi = in.read() - '0';
    int lo = in.read() - '0';
    if (hi > 9) {
      hi = hi + '0' - 'a' + 10;
    }
    if (lo > 9) {
      lo = lo + '0' - 'a' + 10;
    }
    return (hi << 4) | lo;
  }

  /**
   * @param token
   *     possible null token to be parsed
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
    Function<String, byte[]> bytesDecoder = s -> {
      ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII));
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      int b;
      while (true) {
        b = readByte(in);
        if (b < 0) {
          break;
        }
        buf.write(b);
      }
      return buf.toByteArray();
    };
    try {
      withUserId(new String(bytesDecoder.apply(parts[0]), StandardCharsets.UTF_8));
      withValidUntil(Long.parseLong(parts[1], 16));
      if (parts.length > 3) {
        int numberOfCustomArgs = parts.length - 1;
        List<String> customArgs = new ArrayList<>(numberOfCustomArgs);
        for (int i = 2; i < numberOfCustomArgs; i++) {
          String arg = new String(bytesDecoder.apply(parts[i]), StandardCharsets.UTF_8);
          if (arg.isEmpty()) {
            arg = null;
          }
          customArgs.add(arg);
        }
        withCustomArgs(customArgs);
      }
      try { // NOSONAR
        withSignature(bytesDecoder.apply(parts[parts.length - 1]));
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

  /**
   * @return a text representation. The token text is first UTF-8 byte encoded, then all bytes are written with
   * {@link #writeByte(ByteArrayOutputStream, byte)}
   */
  public String write(boolean withSignature) {
    byte partsDelimiter = (byte) partsDelimiter();
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Consumer<byte[]> bytesEncoder = bytes -> {
        for (byte b : bytes) {
          writeByte(out, b);
        }
      };
      bytesEncoder.accept(getUserIdOrAnonymous().getBytes(StandardCharsets.UTF_8));
      out.write(partsDelimiter);
      out.write(Long.toHexString(getValidUntil()).getBytes());
      if (getCustomArgs() != null) {
        for (String arg : getCustomArgs()) {
          out.write(partsDelimiter);
          if (arg != null) {
            bytesEncoder.accept(arg.getBytes(StandardCharsets.UTF_8));
          }
        }
      }
      byte[] signature = getSignature();
      if (withSignature && signature != null && signature.length > 0) {
        out.write(partsDelimiter);
        bytesEncoder.accept(signature);
      }
      return out.toString(StandardCharsets.US_ASCII);
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
