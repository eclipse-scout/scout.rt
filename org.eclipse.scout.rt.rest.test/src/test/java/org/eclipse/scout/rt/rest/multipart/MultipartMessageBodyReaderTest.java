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
package org.eclipse.scout.rt.rest.multipart;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.junit.Test;

/**
 * Test for {@link MultipartMessageBodyReader}.
 */
public class MultipartMessageBodyReaderTest {

  @Test
  public void testIsReadable() {
    MultipartMessageBodyReader reader = new MultipartMessageBodyReader();
    assertFalse(reader.isReadable(String.class, null, null, MediaType.MULTIPART_FORM_DATA_TYPE)); // wrong class
    assertFalse(reader.isReadable(IMultipartPart.class, null, null, MediaType.MULTIPART_FORM_DATA_TYPE)); // wrong class
    assertFalse(reader.isReadable(IMultipartMessage.class, null, null, MediaType.TEXT_PLAIN_TYPE)); // wrong media type
    assertTrue(reader.isReadable(IMultipartMessage.class, null, null, MediaType.MULTIPART_FORM_DATA_TYPE)); // correct
  }

  @Test
  public void testReadFromNoBoundaryInMediaType() {
    MultipartMessageBodyReader reader = new MultipartMessageBodyReader();
    MediaType mediaType = MediaType.MULTIPART_FORM_DATA_TYPE;
    assertThrows(AssertionException.class, () -> reader.readFrom(null, null, null, mediaType, null, getMultipartRequestInputStream()));
  }

  @Test
  public void testReadFromInvalidBoundaryInMediaType() {
    MultipartMessageBodyReader reader = new MultipartMessageBodyReader();
    MediaType mediaType = new MediaType(MediaType.MULTIPART_FORM_DATA_TYPE.getType(), MediaType.MULTIPART_FORM_DATA_TYPE.getSubtype(), CollectionUtility.hashMap(ImmutablePair.of("boundary", "loremipsum")));
    assertThrows(AssertionException.class, () -> reader.readFrom(null, null, null, mediaType, null, getMultipartRequestInputStream()));
  }

  @Test
  public void testReadFromValid() throws Exception {
    MultipartMessageBodyReader reader = new MultipartMessageBodyReader();

    String boundary = "3372ccc6dada4847b1893ff57a81e553"; // boundary as used in .txt file
    MediaType mediaType = new MediaType(MediaType.MULTIPART_FORM_DATA_TYPE.getType(), MediaType.MULTIPART_FORM_DATA_TYPE.getSubtype(), CollectionUtility.hashMap(ImmutablePair.of("boundary", boundary)));
    IMultipartMessage multipartMessage = reader.readFrom(null, null, null, mediaType, null, getMultipartRequestInputStream());
    assertNotNull(multipartMessage);

    assertTrue(multipartMessage.hasNext());
    try (IMultipartPart part = multipartMessage.next()) {
      assertEquals("plaintext", part.getPartName());
      assertEquals("text.txt", part.getFilename());
      assertEquals("text/plain", part.getContentType());
      assertArrayEquals("lorem ipsum dolor\nsit amet".getBytes(StandardCharsets.UTF_8), IOUtility.readBytes(part.getInputStream()));
    }

    assertTrue(multipartMessage.hasNext());
    try (IMultipartPart part = multipartMessage.next()) {
      assertEquals("json", part.getPartName());
      assertEquals("json.json", part.getFilename());
      assertEquals("application/json", part.getContentType());
      assertArrayEquals("{ \"lorem\": \"ipsum\"}".getBytes(StandardCharsets.UTF_8), IOUtility.readBytes(part.getInputStream()));
    }

    assertTrue(multipartMessage.hasNext());
    try (IMultipartPart part = multipartMessage.next()) {
      assertEquals("umlaute-öäü", part.getPartName());
      assertEquals("äpfel\uD83D\uDE00.txt", part.getFilename());
      assertEquals("text/plain", part.getContentType());
      assertArrayEquals("Äpfel".getBytes(StandardCharsets.UTF_8), IOUtility.readBytes(part.getInputStream()));
    }

    assertTrue(multipartMessage.hasNext());
    try (IMultipartPart part = multipartMessage.next()) {
      assertEquals("lorem", part.getPartName());
      assertNull(part.getFilename());
      assertNull(part.getContentType());
      assertEquals("lorem value", IOUtility.readStringUTF8(part.getInputStream()));
    }

    assertTrue(multipartMessage.hasNext());
    try (IMultipartPart part = multipartMessage.next()) {
      assertEquals("ipsum", part.getPartName());
      assertNull(part.getFilename());
      assertNull(part.getContentType());
      assertEquals("ipsum välüe", IOUtility.readStringUTF8(part.getInputStream()));
    }

    assertTrue(multipartMessage.hasNext());
    try (IMultipartPart part = multipartMessage.next()) {
      assertEquals("dol\"or", part.getPartName());
      assertNull(part.getFilename());
      assertNull(part.getContentType());
      assertEquals("dolor\"value", IOUtility.readStringUTF8(part.getInputStream()));
    }

    assertFalse(multipartMessage.hasNext());
    assertThrows(AssertionException.class, multipartMessage::next);
  }

  protected ByteArrayInputStream getMultipartRequestInputStream() {
    // file contains \n only, replace by \r\n as the HTTP line delimiter is using \r\n too (except the plain text bytes)
    String multipartRequest = IOUtility.readStringUTF8(MultipartMessageBodyReaderTest.class.getResourceAsStream("MultipartRequest.txt"))
        .replaceAll("\n", "\r\n")
        .replaceFirst("lorem ipsum dolor\r\nsit amet", "lorem ipsum dolor\nsit amet");

    return new ByteArrayInputStream(multipartRequest.getBytes(StandardCharsets.UTF_8));
  }
}
