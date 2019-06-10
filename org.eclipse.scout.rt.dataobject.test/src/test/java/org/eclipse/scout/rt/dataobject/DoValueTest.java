/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.DoValue;
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
    DoValue<String> value = new DoValue<>(null, m_lazyCreate);
    assertFalse(value.exists());
    value.create();
    assertTrue(value.exists());

    value = new DoValue<>(null, m_lazyCreate);
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
    DoValue<String> value = DoValue.of("foo");
    assertTrue(value.exists());
    assertEquals("foo", value.get());
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
    assertEquals("foo", new DoValue<>("foo", null).getAttributeName());
  }
}
