/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;

/**
 * Various low-level tests for {@link DoValue}
 */
public class DoValueTest {

  @Test
  public void testDoValueConstructor() {
    DoValue<String> value = new DoValue<>();
    assertTrue(value.exists());
  }

  @Test
  public void testDoValueDetailedConstructor() {
    String value = "foo";
    DoValue<String> doValue = new DoValue<>("attributeName", m_lazyCreate, value);
    assertFalse(doValue.exists());
    assertSame(value, doValue.get());
  }

  protected Consumer<DoNode<String>> m_lazyCreate = attribute -> {
    /* nop */
  };

  @Test
  public void testCreateExists() {
    DoValue<String> value = new DoValue<>(null, m_lazyCreate, null);
    assertFalse(value.exists());
    value.create();
    assertTrue(value.exists());

    value = new DoValue<>(null, m_lazyCreate, null);
    assertFalse(value.exists());
    value.set("foo");
    assertTrue(value.exists());
  }

  @Test
  public void testGetSet() {
    DoValue<String> value = new DoValue<>();
    assertNull(value.get());
    value.set("foo");
    assertEquals("foo", value.get());
    value.set(null);
    assertNull(value.get());
  }

  @Test
  public void testOf() {
    String value = "foo";
    DoValue<String> doValue = DoValue.of("foo");
    assertTrue(doValue.exists());
    assertEquals("foo", doValue.get());
    assertSame(value, doValue.get());
  }

  @Test
  public void testOfNull() {
    DoValue<String> value = DoValue.of(null);
    assertTrue(value.exists());
    assertNull(value.get());
  }

  @Test
  public void testAttributeName() {
    assertNull(new DoValue<>().getAttributeName());
    assertNull(DoValue.of("").getAttributeName());
    assertEquals("foo", new DoValue<>("foo", null, null).getAttributeName());
  }
}
