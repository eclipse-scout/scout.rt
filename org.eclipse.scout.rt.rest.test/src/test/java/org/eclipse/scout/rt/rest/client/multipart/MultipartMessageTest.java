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

import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

/**
 * Test for {@link MultipartMessage}.
 */
public class MultipartMessageTest {

  @Test
  public void testEmptyPart() {
    assertThrows(AssertionException.class, () -> BEANS.get(MultipartMessage.class).toEntity());
  }

  @Test
  public void testCachedBoundary() {
    MultipartMessage multipartMessage = BEANS.get(MultipartMessage.class).addPart(MultipartPart.ofField("lorem", "ipsum"));
    MediaType expectedMediaType = multipartMessage.toEntity().getMediaType();
    MediaType actualMediaType = multipartMessage.toEntity().getMediaType();
    assertEquals("Expected to have the same boundary when #toEntity is called twice", expectedMediaType, actualMediaType);

    MultipartMessage otherMultipartMessage = BEANS.get(MultipartMessage.class).addPart(MultipartPart.ofField("lorem", "ipsum"));
    MediaType otherMediaType = otherMultipartMessage.toEntity().getMediaType();
    assertNotEquals("Expected to have a different boundary when #toEntity is called on a different multipart message", expectedMediaType, otherMediaType);
  }
}
