/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.junit.Test;

/**
 * Various low-level tests for {@link DoNode}
 */
public class DoNodeTest {

  protected static class FixtureDoNode<T> extends DoNode<T> {
    FixtureDoNode() {
      super(null, (DoNode<T> foo) -> {
      }, null);
    }
  }

  @Test
  public void testAttributeName() {
    FixtureDoNode node = new FixtureDoNode();
    assertNull(node.getAttributeName());
    node.setAttributeName("nodeAttributeName");
    assertEquals("nodeAttributeName", node.getAttributeName());
    node.setAttributeName(null);
    assertNull(node.getAttributeName());
  }

  @Test
  public void testToOptional() {
    FixtureDoNode<String> node = new FixtureDoNode<>();
    assertFalse(node.exists());
    Optional<String> optValue = node.toOptional();
    assertFalse(optValue.isPresent());
    assertEquals("else-value", optValue.orElseGet(() -> "else-value"));

    node.create(); // node with null value
    optValue = node.toOptional();
    assertFalse(optValue.isPresent());
    assertEquals("else-value", optValue.orElseGet(() -> "else-value"));

    node.set("foo"); // node was created and contains a value
    optValue = node.toOptional();
    assertTrue(optValue.isPresent());
    assertEquals("foo", optValue.orElseGet(() -> "else-value"));

    node.set(null); // node with null value
    optValue = node.toOptional();
    assertFalse(optValue.isPresent());
    assertEquals("else-value", optValue.orElseGet(() -> "else-value"));
  }

  @Test
  public void testIfPresent() {
    FixtureDoNode<String> node = new FixtureDoNode<>();
    assertFalse(node.exists());
    node.ifPresent(value -> fail("node exists"));

    node.create(); // node with null value
    StringHolder holder = new StringHolder("other");
    node.ifPresent(holder::setValue);
    assertNull(holder.getValue()); // value was set

    node.set("foo"); // node was created and contains a value
    holder = new StringHolder("other");
    node.ifPresent(holder::setValue);
    assertEquals("foo", holder.getValue());
  }

  @Test
  public void testEqualsHashCode() {
    FixtureDoNode<String> node1 = new FixtureDoNode<>();
    FixtureDoNode<String> node2 = new FixtureDoNode<>();
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());

    node1.create();
    assertNotEquals(node1, node2);

    node2.create();
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());

    node1.set("foo");
    assertNotEquals(node1, node2);

    node2.set("foo");
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());

    node1.setAttributeName("name");
    assertNotEquals(node1, node2);

    node2.setAttributeName("name");
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());

    assertTrue(node1.equals(node1));
    assertFalse(node1.equals(null));
    assertFalse(node1.equals(new Object()));

    node1.set(null);
    assertFalse(node1.equals(node2));
    assertFalse(node2.equals(node1));

    node1.setAttributeName(null);
    assertFalse(node1.equals(node2));
    assertFalse(node2.equals(node1));
  }

  @Test
  public void testEqualsHashCode_ArrayValue() {
    FixtureDoNode<byte[]> node1 = new FixtureDoNode<>();
    FixtureDoNode<byte[]> node2 = new FixtureDoNode<>();

    assertEquals(node1, node2);
    node1.set(new byte[]{1, 2, 3});
    assertNotEquals(node1, node2);
    assertNotEquals(node2, node1);

    node2.set(new byte[]{1, 2, 3});
    assertEquals(node1, node2);
  }

  @Test
  public void testEqualsHashCode_BigDecimalValue() {
    FixtureDoNode<BigDecimal> node1 = new FixtureDoNode<>();
    FixtureDoNode<BigDecimal> node2 = new FixtureDoNode<>();

    assertEquals(node1, node2);
    node1.set(new BigDecimal(100));
    assertNotEquals(node1, node2);
    assertNotEquals(node2, node1);

    node2.set(new BigDecimal(100));
    assertEquals(node1, node2);

    // BigDecimal is not equals if scale does not match
    node2.set(new BigDecimal(100).setScale(100));
    assertEquals(0, node1.get().compareTo(node2.get()));
    assertNotEquals(node1, node2);
  }
}
