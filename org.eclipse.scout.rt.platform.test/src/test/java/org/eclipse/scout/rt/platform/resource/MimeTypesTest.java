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

import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for {@link MimeTypes} and {@link IMimeMagic}.
 */
@RunWith(PlatformTestRunner.class)
public class MimeTypesTest {

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
    BinaryResource binRes = BinaryResources.create().withContent(HexUtility.decode(content)).build();
    assertTrue(MimeType.JPG.getMagic().matches(binRes));
    assertTrue(MimeType.JPEG.getMagic().matches(binRes));
    assertTrue(MimeTypes.verifyMagic(binRes));
  }
}
