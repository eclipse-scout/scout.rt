/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class ObjectTypeAnnotationProviderTest {

  private final ObjectTypeAnnotationProvider m_provider = new ObjectTypeAnnotationProvider();

  @Test
  public void testObjectTypeAnnotation() {
    assertEquals("test", m_provider.objectTypeOf(ObjectTypeFixture.class));
  }

  @Test
  public void testEmptyObjectTypeAnnotation() {
    assertNull(m_provider.objectTypeOf(EmptyObjectTypeFixture.class));
  }

  @Test
  public void testWithoutObjectTypeAnnotation() {
    assertNull(m_provider.objectTypeOf(NoObjectTypeFixture.class));
  }

  @ObjectType("test")
  public static class ObjectTypeFixture {
  }

  @ObjectType("")
  public static class EmptyObjectTypeFixture {
  }

  public static class NoObjectTypeFixture {
  }
}
