/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.dataobject;

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

  protected Consumer<DoNode<String>> m_lazyCreate = attribute -> {
    /* nop */ };

  @Test
  public void testCreateExists() {
    DoValue<String> value = new DoValue<>(m_lazyCreate);
    assertFalse(value.exists());
    value.create();
    assertTrue(value.exists());

    value = new DoValue<>(m_lazyCreate);
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
}
