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
package org.eclipse.scout.rt.rest.jersey.client.multipart;

import static org.junit.Assert.*;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.rest.client.multipart.MultipartMessage;
import org.eclipse.scout.rt.rest.client.multipart.MultipartPart;
import org.junit.Test;

/**
 * Test for {@link MultipartMessageBodyWriter}, additional tests indirectly using this class are in
 * {@link MultipartRestClientTest}.
 */
public class MultipartMessageBodyWriterTest {

  @Test
  public void testIsWritable() {
    MultipartMessageBodyWriter reader = new MultipartMessageBodyWriter();
    assertFalse(reader.isWriteable(String.class, null, null, MediaType.MULTIPART_FORM_DATA_TYPE)); // wrong class
    assertFalse(reader.isWriteable(MultipartPart.class, null, null, MediaType.MULTIPART_FORM_DATA_TYPE)); // wrong class
    assertFalse(reader.isWriteable(MultipartMessage.class, null, null, MediaType.TEXT_PLAIN_TYPE)); // wrong media type
    assertTrue(reader.isWriteable(MultipartMessage.class, null, null, MediaType.MULTIPART_FORM_DATA_TYPE)); // correct
  }
}
