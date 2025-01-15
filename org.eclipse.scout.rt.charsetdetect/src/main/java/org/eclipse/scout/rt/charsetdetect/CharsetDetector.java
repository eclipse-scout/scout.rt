/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.charsetdetect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.tika.parser.txt.CharsetMatch;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Helper bean to guess a charset for raw text byte data.
 *
 * @since 22.0
 */
@Bean
public class CharsetDetector {

  /**
   * Default read limit (16KB)
   */
  public static final int DEFAULT_READ_LIMIT = 1024 * 16;

  /**
   * Prevent accidental manual instance creation. Use BEANS.get() instead.
   */
  protected CharsetDetector() {
  }

  /**
   * Tries to detect the best {@link Charset} to convert the bytes from the given {@link InputStream} to text. For this
   * a certain amount of data is read from the given {@link InputStream} and the most plausible {@link Charset} is
   * returned. This {@link Charset} must not be correct. Therefore, it should only be used if no explicit
   * {@link Charset} is known or the user has the possibility to correct the encoding if necessary.
   *
   * @param in
   *     The {@link InputStream} to get the raw bytes for which the {@link Charset} should be detected. Must not be
   *     {@code null}. The stream is reset to its original position after the {@link Charset} detection. Therefore,
   *     it must support mark and reset (check* {@link InputStream#markSupported()}).
   * @return The detected {@link Charset}. Never returns {@code null}. If no {@link Charset} could be detected at all,
   * UTF-8 is returned.
   * @throws IOException
   *     when reading from the {@link InputStream}.
   */
  public Charset guessCharset(InputStream in) throws IOException {
    return guessCharset(in, true);
  }

  /**
   * Tries to detect the best {@link Charset} to convert the bytes from the given {@link InputStream} to text. For this
   * a certain amount of data is read from the given {@link InputStream} and the most plausible {@link Charset} is
   * returned. This {@link Charset} must not be correct. Therefore, it should only be used if no explicit
   * {@link Charset} is known or the user has the possibility to correct the encoding if necessary.
   *
   * @param in
   *     The {@link InputStream} to get the raw bytes for which the {@link Charset} should be detected. Must not be
   *     {@code null}.
   * @param resetStream
   *     Specifies if the {@link InputStream} given should be reset to the original position after the charset
   *     detection. If {@code true} this allows to reuse the stream to read the content after it was already used
   *     to detect the {@link Charset}. In this case the {@link InputStream} must support mark and reset (check
   *     {@link InputStream#markSupported()}). If {@code false}, the stream is not reset and will continue to read
   *     from the last position read to detect the {@link Charset}. This may be handy if the stream is not needed
   *     after {@link Charset} detection anymore.
   * @return The detected {@link Charset}. Never returns {@code null}. If no {@link Charset} could be detected at all,
   * UTF-8 is returned.
   * @throws IOException
   *     when reading from the {@link InputStream}.
   */
  public Charset guessCharset(InputStream in, boolean resetStream) throws IOException {
    return guessCharset(in, resetStream, DEFAULT_READ_LIMIT);
  }

  /**
   * Tries to detect the best {@link Charset} to convert the bytes from the given {@link InputStream} to text. For this
   * a certain amount of data is read from the given {@link InputStream} and the most plausible {@link Charset} is
   * returned. This {@link Charset} must not be correct. Therefore, it should only be used if no explicit
   * {@link Charset} is known or the user has the possibility to correct the encoding if necessary.
   *
   * @param in
   *     The {@link InputStream} to get the raw bytes for which the {@link Charset} should be detected. Must not be
   *     {@code null}.
   * @param resetStream
   *     Specifies if the {@link InputStream} given should be reset to the original position after the charset
   *     detection. If {@code true} this allows to reuse the stream to read the content after it was already used
   *     to detect the {@link Charset}. In this case the {@link InputStream} must support mark and reset (check
   *     {@link InputStream#markSupported()}). If {@code false}, the stream is not reset and will continue to read
   *     from the last position read to detect the {@link Charset}. This may be handy if the stream is not needed
   *     after {@link Charset} detection anymore.
   * @param limit
   *     The number of bytes to read from the given {@link InputStream} to detect the {@link Charset}. More bytes
   *     give better results, but is slower.
   * @return The detected {@link Charset}. Never returns {@code null}. If no {@link Charset} could be detected at all,
   * UTF-8 is returned.
   * @throws IOException
   *     when reading from the {@link InputStream}.
   */
  public Charset guessCharset(InputStream in, boolean resetStream, int limit) throws IOException {
    byte[] data = readNBytes(in, resetStream, limit);
    return guessCharset(data, limit);
  }

  /**
   * Tries to detect the best {@link Charset} to convert the bytes given to text. For this a certain number of bytes
   * from the given array is used and the most plausible {@link Charset} is returned. This {@link Charset} must not be
   * correct. Therefore, it should only be used if no explicit {@link Charset} is known or the user has the possibility
   * to correct the encoding if necessary.
   *
   * @param data
   *     The raw bytes to use to detect the {@link Charset}. Must not be {@code null}.
   * @return The detected {@link Charset}. Never returns {@code null}. If no {@link Charset} could be detected at all,
   * UTF-8 is returned.
   */
  public Charset guessCharset(byte[] data) {
    return guessCharset(data, DEFAULT_READ_LIMIT);
  }

  /**
   * Tries to detect the best {@link Charset} to convert the bytes given to text. For this a certain number of bytes
   * from the given array is used and the most plausible {@link Charset} is returned. This {@link Charset} must not be
   * correct. Therefore, it should only be used if no explicit {@link Charset} is known or the user has the possibility
   * to correct the encoding if necessary.
   *
   * @param data
   *     The raw bytes to use to detect the {@link Charset}. Must not be {@code null}.
   * @param limit
   *     Specifies the number of bytes from the given array to use for the detection. More bytes give better
   *     results, but is slower.
   * @return The detected {@link Charset}. Never returns {@code null}. If no {@link Charset} could be detected at all,
   * UTF-8 is returned.
   */
  public Charset guessCharset(byte[] data, int limit) {
    org.apache.tika.parser.txt.CharsetDetector detector = new org.apache.tika.parser.txt.CharsetDetector(limit);
    detector.setText(data);
    return Arrays.stream(detector.detectAll())
        .map(CharsetMatch::getName)
        .filter(Charset::isSupported)
        .map(Charset::forName)
        .findFirst()
        .orElse(StandardCharsets.UTF_8);
  }

  protected byte[] readNBytes(InputStream in, boolean resetStream, int readLimit) throws IOException {
    if (!resetStream) {
      return in.readNBytes(readLimit);
    }

    if (!in.markSupported()) {
      throw new IOException("mark/reset not supported");
    }
    in.mark(readLimit);
    try {
      return in.readNBytes(readLimit);
    }
    finally {
      in.reset();
    }
  }
}
