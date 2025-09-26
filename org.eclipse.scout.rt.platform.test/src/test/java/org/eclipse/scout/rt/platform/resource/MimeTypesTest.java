/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import static org.eclipse.scout.rt.platform.resource.MimeTypes.verifyMagic;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for {@link MimeTypes} and {@link IMimeMagic}.
 */
@RunWith(PlatformTestRunner.class)
public class MimeTypesTest {

  @Test
  public void testVerifyMagic() {
    // correct jpg -> valid
    assertTrue(verifyMagic(BinaryResources.create().withContent(HexUtility.decode("ffd8ff")).withFilename("valid.jpg").build()));

    // unknown mime-type -> valid
    assertTrue(verifyMagic(BinaryResources.create().withFilename("valid.not-existing").build()));

    // jpg content but ics extension -> invalid
    assertFalse(verifyMagic(BinaryResources.create().withContent(HexUtility.decode("ffd8ff")).withFilename("calendar.ics").build()));

    // invalid jpg
    assertFalse(verifyMagic(BinaryResources.create().withContent(HexUtility.decode("ffaaaa")).withFilename("invalid.jpg").build()));
  }

  @Test
  public void testMatches() throws IOException {
    assertFalse(MimeType.JAR.matches((Path) null));
    assertFalse(MimeType.JAR.matches(Paths.get("not-existing")));
    testWithTmpFile("test.xyz", "504b0304", file -> {
      assertFalse(MimeType.JAR.matches(file)); // extension does not match
    });
    testWithTmpFile("test.txt", "aaaa", file -> {
      assertTrue(MimeType.TXT.matches(file)); // extension matches
    });
    testWithTmpFile("test.JAR", "504b0708", file -> {
      assertTrue(MimeType.JAR.matches(file)); // extension and magic bytes match
    });
  }

  private void testWithTmpFile(String fileName, String content, Consumer<Path> test) throws IOException {
    Path tempFile = IOUtility.createTempFile(fileName, HexUtility.decode(content)).toPath();
    try {
      test.accept(tempFile);
    }
    finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testJpgJpeg() {
    // without markers
    runJpegTest("ffd8ff");
    runJpegTest("FFD8FF");

    // with some markers
    runJpegTest("ffd8ffdb");
    runJpegTest("ffd8ffe0");
    runJpegTest("ffd8ffe1");
    runJpegTest("ffd8ffe2");
    runJpegTest("ffd8ffee");
  }

  protected void runJpegTest(String content) {
    byte[] bytes = HexUtility.decode(content);
    assertTrue(MimeType.JPG.getMagic().matches(bytes));
    assertTrue(MimeType.JPEG.getMagic().matches(bytes));

    BinaryResource binRes = BinaryResources.create().withContent(bytes).build();
    assertTrue(verifyMagic(binRes));
  }
}
