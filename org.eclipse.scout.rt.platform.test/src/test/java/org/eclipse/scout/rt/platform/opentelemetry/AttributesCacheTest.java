/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

public class AttributesCacheTest {

  private static final AttributeKey<String> KEY1 = AttributeKey.stringKey("key1");
  private static final AttributeKey<String> KEY2 = AttributeKey.stringKey("key2");

  @Test
  public void testGetOrCreate() {
    @SuppressWarnings("unchecked")
    BiFunction<String, String, Attributes> createAttributesFunctionMock = Mockito.mock(BiFunction.class);
    Attributes attributes = Attributes.of(KEY1, "foo", KEY2, "bar");
    Mockito.when(createAttributesFunctionMock.apply(Mockito.any(), Mockito.any())).thenReturn(attributes);
    AttributesCache<String, String> cache = AttributesCache.of(1, 1, createAttributesFunctionMock);

    Assert.assertEquals(attributes, cache.getOrCreate("a", "b"));
    Mockito.verify(createAttributesFunctionMock).apply("a", "b");

    Assert.assertEquals(attributes, cache.getOrCreate("a", "b"));
    Mockito.verifyNoMoreInteractions(createAttributesFunctionMock);
  }

  @Test
  public void testPut() {
    @SuppressWarnings("unchecked")
    BiFunction<String, String, Attributes> createAttributesFunctionMock = Mockito.mock(BiFunction.class);
    Attributes attributes = Attributes.of(KEY1, "asdf", KEY2, "qwer");
    Mockito.when(createAttributesFunctionMock.apply(Mockito.any(), Mockito.any())).thenReturn(attributes);
    AttributesCache<String, String> cache = AttributesCache.of(1, 1, createAttributesFunctionMock);

    cache.put("a", "b");
    Mockito.verify(createAttributesFunctionMock).apply("a", "b");
    Assert.assertEquals(attributes, cache.getOrCreate("a", "b"));
    Mockito.verifyNoMoreInteractions(createAttributesFunctionMock);
  }
}
