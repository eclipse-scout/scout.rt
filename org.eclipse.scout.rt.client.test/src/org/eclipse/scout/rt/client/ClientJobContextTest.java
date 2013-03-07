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
package org.eclipse.scout.rt.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link ClientJobContext}
 */
public class ClientJobContextTest {
  @Test
  public void testGetAndSet() {
    String key1 = "key1";
    String value1 = "value1";
    String key2 = "key2";
    String value2 = "value2";

    ClientJobContext context = new ClientJobContext();
    context.set(key1, value1);
    context.set(key2, value2);

    Assert.assertEquals(value1, context.get(key1));
    Assert.assertEquals(value2, context.get(key2));

    context.set(key1, value2);
    Assert.assertEquals(value2, context.get(key1));

    context.set(key1, null);
    Assert.assertEquals(null, context.get(key1));

    context.clear();
    Assert.assertEquals(null, context.get(key2));
  }

  @Test
  public void testIterator() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("key1", "value1");
    props.put("key2", "value2");

    ClientJobContext context = new ClientJobContext();
    Assert.assertFalse(context.iterator().hasNext());

    context.set("key1", props.get("key1"));
    context.set("key2", props.get("key2"));

    for (Entry<Object, Object> entry : context) {
      Assert.assertTrue(props.containsKey(entry.getKey()));
      Assert.assertEquals(props.get(entry.getKey()), entry.getValue());

      props.remove(entry.getKey());
    }

    Assert.assertTrue(props.isEmpty());
  }

  @Test
  public void testClone() {
    String key1 = "key1";
    String value1 = "value1";
    String key2 = "key2";
    String value2 = "value2";

    ClientJobContext context = new ClientJobContext();
    context.set(key1, value1);
    context.set(key2, value2);

    ClientJobContext anotherContext = new ClientJobContext(context);
    Assert.assertEquals(context.get(key1), anotherContext.get(key1));
    Assert.assertEquals(context.get(key2), anotherContext.get(key2));

    context.clear();

    Assert.assertEquals(value1, anotherContext.get(key1));
    Assert.assertEquals(value2, anotherContext.get(key2));
  }
}
