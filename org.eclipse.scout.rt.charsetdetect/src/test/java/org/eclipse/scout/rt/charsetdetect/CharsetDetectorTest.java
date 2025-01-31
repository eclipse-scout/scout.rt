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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class CharsetDetectorTest {

  /**
   * Tests that the {@link CharsetDetector} recognizes some charsets. Ensures the detection keeps on working on a
   * 3rd-party library update/change.
   */
  @Test
  public void testWithStream() {
    Map.of(
            "iso88591.txt", "ISO-8859-1",
            "iso88595.txt", "ISO-8859-5",
            "utf8.txt", "UTF-8",
            "utf16-be-bom.txt", "UTF-16BE",
            "utf16-le-bom.txt", "UTF-16LE",
            "win1252.txt", "windows-1252")
        .forEach((fileName, expectedEncodingName) -> assertEquals(expectedEncodingName, guess(fileName).name()));
  }

  @Test
  public void testWithArray() {
    String text = "Test ¨$äü text";
    assertEquals(StandardCharsets.UTF_8, BEANS.get(CharsetDetector.class).guessCharset(text.getBytes(StandardCharsets.UTF_8)));
    assertEquals(StandardCharsets.UTF_16BE, BEANS.get(CharsetDetector.class).guessCharset(text.getBytes(StandardCharsets.UTF_16BE)));
    assertEquals(StandardCharsets.UTF_16LE, BEANS.get(CharsetDetector.class).guessCharset(text.getBytes(StandardCharsets.UTF_16LE)));
  }

  protected Charset guess(String fileName) {
    try (InputStream in = CharsetDetectorTest.class.getResourceAsStream(fileName)) {
      return BEANS.get(CharsetDetector.class).guessCharset(in, fileName.length() % 2 == 0);
    }
    catch (IOException e) {
      throw new PlatformException("Error reading '{}'.", fileName, e);
    }
  }
}
