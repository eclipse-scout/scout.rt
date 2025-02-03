/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * JUnit tests for {@link MimeTypeDetector} and {@link FileUtility#getMimeType(java.nio.file.Path)}
 *
 * @since 5.2
 */
public class MimeTypeDetectorTest {

  @Test
  public void testGetMimeType_Xml() {
    assertEquals("text/xml", FileUtility.getContentTypeForExtension("xml"));
  }

  @Test
  public void testGetMimeType_XML() {
    assertEquals("text/xml", FileUtility.getContentTypeForExtension("XML"));
  }

  @Test
  public void testGetMimeType_DotXml() {
    assertEquals("text/xml", FileUtility.getContentTypeForExtension(".Xml"));
  }

  @Test
  public void testGetMimeType_Null() {
    assertEquals("application/octet-stream", FileUtility.getContentTypeForExtension(null));
  }

  @Test
  public void testGetMimeType_Empty() {
    assertEquals("application/octet-stream", FileUtility.getContentTypeForExtension(""));
  }

  @Test
  public void testGetMimeType_Space() {
    assertEquals("application/octet-stream", FileUtility.getContentTypeForExtension(" "));
  }
}
