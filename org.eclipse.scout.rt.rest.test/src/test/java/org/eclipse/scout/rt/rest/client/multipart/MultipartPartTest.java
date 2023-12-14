/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client.multipart;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.junit.Test;

/**
 * Test for {@link MultipartPart}.
 */
public class MultipartPartTest {

  @Test
  public void testOf() {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
    assertThrows(AssertionException.class, () -> MultipartPart.of(null, "filename", MediaType.APPLICATION_JSON, inputStream)); // mandatory part name
    assertThrows(AssertionException.class, () -> MultipartPart.of("partName", "filename", MediaType.APPLICATION_JSON, null)); // mandatory input stream

    // full
    MultipartPart multipartPart = MultipartPart.of("partName", "filename.json", MediaType.APPLICATION_JSON, inputStream);
    assertEquals("partName", multipartPart.getPartName());
    assertEquals("filename.json", multipartPart.getFilename());
    assertEquals(MediaType.APPLICATION_JSON, multipartPart.getContentType());
    assertSame(inputStream, multipartPart.getInputStream());

    // mandatory only
    multipartPart = MultipartPart.of("partName", null, null, inputStream);
    assertEquals("partName", multipartPart.getPartName());
    assertNull(multipartPart.getFilename());
    assertNull(multipartPart.getContentType());
    assertSame(inputStream, multipartPart.getInputStream());
  }

  @Test
  public void testOfFile() {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
    assertThrows(AssertionException.class, () -> MultipartPart.ofFile(null, "filename", inputStream)); // mandatory part name
    assertThrows(AssertionException.class, () -> MultipartPart.ofFile("partName", "filename", null)); // mandatory input stream

    // full
    MultipartPart multipartPart = MultipartPart.ofFile("partName", "filename.json", inputStream);
    assertEquals("partName", multipartPart.getPartName());
    assertEquals("filename.json", multipartPart.getFilename());
    assertEquals(MediaType.APPLICATION_JSON, multipartPart.getContentType());
    assertSame(inputStream, multipartPart.getInputStream());

    // mandatory only
    multipartPart = MultipartPart.ofFile("partName", null, inputStream);
    assertEquals("partName", multipartPart.getPartName());
    assertNull(multipartPart.getFilename());
    assertEquals(MediaType.APPLICATION_OCTET_STREAM, multipartPart.getContentType()); // due to missing filename
    assertSame(inputStream, multipartPart.getInputStream());
  }

  @Test
  public void testOfField() {
    assertThrows(AssertionException.class, () -> MultipartPart.ofField(null, "filename")); // mandatory part name
    assertThrows(AssertionException.class, () -> MultipartPart.ofField("partName", null)); // mandatory value

    MultipartPart multipartPart = MultipartPart.ofField("partName", "value");
    assertEquals("partName", multipartPart.getPartName());
    assertNull(multipartPart.getFilename());
    assertNull(multipartPart.getContentType());
    assertEquals("value", IOUtility.readStringUTF8(multipartPart.getInputStream()));
  }
}
