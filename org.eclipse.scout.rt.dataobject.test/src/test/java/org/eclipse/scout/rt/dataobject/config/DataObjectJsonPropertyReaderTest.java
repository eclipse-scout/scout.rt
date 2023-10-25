/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.config;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link DataObjectJsonPropertyReader}.
 */
@RunWith(PlatformTestRunner.class)
public class DataObjectJsonPropertyReaderTest {

  @Test
  public void testNullArgument() {
    assertNull(read(null));
  }

  @Test
  public void testEmptyArgument() {
    assertEquals(Collections.emptyMap(), read(""));
  }

  @Test
  public void testSingleStringArgument() {
    assertThrows(RuntimeException.class, () -> read("a"));
  }

  @Test
  public void testSingleBooleanArgument() {
    assertThrows(RuntimeException.class, () -> read("true"));
  }

  @Test
  public void testSingleIntegerArgument() {
    assertThrows(RuntimeException.class, () -> read("1"));
  }

  @Test
  public void testSingleQuotedStringArgument() {
    assertThrows(RuntimeException.class, () -> read("\"a\""));
  }

  @Test
  public void testReadEmptyJsonObjectString() {
    assertEquals(Collections.emptyMap(), read("{}"));
  }

  @Test
  public void testReadJsonObjectWithSingleEmptyStringProperty() {
    assertEquals(Map.of("testKey", ""), read("{\"testKey\": \"\"}"));
  }

  @Test
  public void testReadJsonObjectWithSingleStringProperty() {
    assertEquals(Map.of("testKey", "testValue"), read("{\"testKey\": \"testValue\"}"));
  }

  @Test
  public void testReadJsonObjectWithMultipleStringProperties() {
    assertEquals(Map.of(
        "testKey1", "testValue1",
        "testKey2", "testValue2"),
        read("{\"testKey1\": \"testValue1\", \"testKey2\": \"testValue2\"}"));
  }

  @Test
  public void testReadJsonObjectWithPropertyWithNullValue() {
    assertEquals(CollectionUtility.hashMap( // Map.of cannot be used with a null value
        ImmutablePair.of("testKey1", null),
        ImmutablePair.of("testKey2", "testValue2")),
        read("{\"testKey1\": null, \"testKey2\": \"testValue2\"}"));
  }

  @Test
  public void testReadJsonObjectWithIntegerProperty() {
    assertEquals(Map.of(
        "testKey1", "1",
        "testKey2", "testValue2"),
        read("{\"testKey1\": 1, \"testKey2\": \"testValue2\"}"));
  }

  @Test
  public void testReadJsonObjectWithBooleanProperty() {
    assertEquals(Map.of(
        "testKey1", "true",
        "testKey2", "testValue2"),
        read("{\"testKey1\": true, \"testKey2\": \"testValue2\"}"));
  }

  @Test
  public void testReadJsonObjectWithDoubleProperty() {
    assertEquals(Map.of(
        "testKey1", "1.2",
        "testKey2", "testValue2"),
        read("{\"testKey1\": 1.2, \"testKey2\": \"testValue2\"}"));
  }

  @Test
  @Ignore // unsupported, Objects#toString is applied on all map values, which will result in an unwanted representation if map value are data objects again
  public void testReadJsonObjectWithJsonStringProperty() {
    assertEquals(Map.of(
        "testKey1", "{\"subKey1\": \"subValue1\",\"subKey2\": \"subValue2\"}",
        "testKey2", "[\"testValue2Sub1\", \"testValue2Sub2\", \"testValue2Sub3\"]"),
        read("{\"testKey1\": {\"subKey1\": \"subValue1\", \"subKey2\": \"subValue2\"}, \"testKey2\": [\"testValue2Sub1\", \"testValue2Sub2\", \"testValue2Sub3\"]}"));
  }

  protected Map<String, String> read(String propertyValue) {
    return new DataObjectJsonPropertyReader().readJsonPropertyValue(propertyValue);
  }
}
