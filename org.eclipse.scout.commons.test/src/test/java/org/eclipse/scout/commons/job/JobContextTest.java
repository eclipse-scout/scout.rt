/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link JobContext}
 */
public class JobContextTest {

  @Test
  public void testGetAndSet() {
    JobContext ctx = new JobContext();

    assertNull(ctx.get("key"));
    assertNull(ctx.get(null));

    // Put null-value
    ctx.set("NullValue", null);
    assertNull(ctx.get(null));
    assertFalse(ctx.iterator().hasNext());

    // Put value
    ctx.set("key1", "value1");
    ctx.set("key2", "value2");
    assertEquals("value1", ctx.get("key1"));
    assertEquals(CollectionUtility.hashSet("key1", "key2"), toKeySet(ctx.iterator()));
    assertEquals(CollectionUtility.hashSet("value1", "value2"), toValueSet(ctx.iterator()));

    // Get null value
    assertNull(ctx.get(null));

    // Overwrite value
    ctx.set("key1", "value1 (b)");
    assertEquals("value1 (b)", ctx.get("key1"));
    assertEquals(CollectionUtility.hashSet("key1", "key2"), toKeySet(ctx.iterator()));
    assertEquals(CollectionUtility.hashSet("value1 (b)", "value2"), toValueSet(ctx.iterator()));

    // Clear value
    ctx.set("key1", null);
    assertNull(ctx.get("key1"));

    ctx.set("key2", null);
    assertNull(ctx.get("key2"));

    assertFalse(ctx.iterator().hasNext());
  }

  @Test
  public void testCopy() {
    JobContext ctx = new JobContext();

    ctx.set("key", "value1");
    JobContext copy = JobContext.copy(ctx);

    assertEquals("value1", copy.get("key"));
    assertEquals(CollectionUtility.hashSet("key"), toKeySet(copy.iterator()));
    assertEquals(CollectionUtility.hashSet("value1"), toValueSet(copy.iterator()));

    // Overwrite value
    copy.set("key", "value2");
    assertEquals("value2", copy.get("key"));
    assertEquals(CollectionUtility.hashSet("key"), toKeySet(copy.iterator()));
    assertEquals(CollectionUtility.hashSet("value2"), toValueSet(copy.iterator()));

    // expect no changes in original context
    assertEquals("value1", ctx.get("key"));
    assertEquals(CollectionUtility.hashSet("key"), toKeySet(ctx.iterator()));
    assertEquals(CollectionUtility.hashSet("value1"), toValueSet(ctx.iterator()));

    // Clear values
    copy.clear();
    assertNull(copy.get("key"));
    assertFalse(copy.iterator().hasNext());

    // expect no changes in original context
    assertEquals("value1", ctx.get("key"));
    assertEquals(CollectionUtility.hashSet("key"), toKeySet(ctx.iterator()));
    assertEquals(CollectionUtility.hashSet("value1"), toValueSet(ctx.iterator()));
  }

  @Test
  public void testCopyNullContext() {
    JobContext copy = JobContext.copy(null);

    assertNull(copy.get("key"));
    assertFalse(copy.iterator().hasNext());

    // Put value
    copy.set("key", "value2");
    assertEquals("value2", copy.get("key"));
    assertEquals(CollectionUtility.hashSet("key"), toKeySet(copy.iterator()));
    assertEquals(CollectionUtility.hashSet("value2"), toValueSet(copy.iterator()));

    // Clear values
    copy.clear();
    assertNull(copy.get("key"));
    assertFalse(copy.iterator().hasNext());
  }

  @Test
  public void testCopyEmptyContext() {
    JobContext ctx = new JobContext();

    JobContext copy = JobContext.copy(ctx);

    assertNull(copy.get("key"));
    assertFalse(copy.iterator().hasNext());

    // Put value
    copy.set("key", "value2");
    assertEquals("value2", copy.get("key"));
    assertEquals(CollectionUtility.hashSet("key"), toKeySet(copy.iterator()));
    assertEquals(CollectionUtility.hashSet("value2"), toValueSet(copy.iterator()));

    // expect no changes in original context
    assertNull(ctx.get("key"));
    assertFalse(ctx.iterator().hasNext());

    // Clear values
    copy.clear();
    assertNull(copy.get("key"));
    assertFalse(copy.iterator().hasNext());

    // expect no changes in original context
    assertNull(ctx.get("key"));
    assertFalse(ctx.iterator().hasNext());
  }

  private static Set<Object> toKeySet(Iterator<Entry<Object, Object>> iterator) {
    Set<Object> keys = new HashSet<>();
    while (iterator.hasNext()) {
      keys.add(iterator.next().getKey());
    }
    return keys;
  }

  private static Set<Object> toValueSet(Iterator<Entry<Object, Object>> iterator) {
    Set<Object> keys = new HashSet<>();
    while (iterator.hasNext()) {
      keys.add(iterator.next().getValue());
    }
    return keys;
  }

  @Test
  public void testIterator() {
    Map<String, String> props = new HashMap<>();
    props.put("key1", "value1");
    props.put("key2", "value2");

    JobContext ctx = new JobContext();
    assertFalse(ctx.iterator().hasNext());

    ctx.set("key1", props.get("key1"));
    ctx.set("key2", props.get("key2"));

    for (Entry<Object, Object> entry : ctx) {
      assertTrue(props.containsKey(entry.getKey()));
      assertEquals(props.get(entry.getKey()), entry.getValue());

      props.remove(entry.getKey());
    }

    assertTrue(props.isEmpty());
  }
}
