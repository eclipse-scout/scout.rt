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
package org.eclipse.scout.rt.platform.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

/**
 * Various low-level tests for {@link DoNode}
 */
public class DoNodeTest {

  protected static class FixtureDoNode extends DoNode<String> {
    FixtureDoNode() {
      super(null, (foo) -> {
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
    FixtureDoNode node = new FixtureDoNode();
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
}
