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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Before;
import org.junit.Test;

public class DoMapEntityTest {

  protected DoMapEntity<String> m_entity;

  @Before
  @SuppressWarnings("unchecked")
  public void before() {
    m_entity = BEANS.get(DoMapEntity.class);
    m_entity.put("one", "foo");
    m_entity.put("two", "bar");
    m_entity.put("three", "baz");
  }

  @Test
  public void testGet() {
    String one = m_entity.get("one");
    assertEquals("foo", one);
  }

  @Test(expected = ClassCastException.class)
  public void testGet_WrongType() {
    m_entity.put("four", 42);
    @SuppressWarnings("unused")
    String four = m_entity.get("four");
  }

  @Test
  public void testAll() {
    Map<String, String> all = m_entity.all();
    assertEquals("foo", all.get("one"));
    assertEquals("bar", all.get("two"));
    assertEquals("baz", all.get("three"));
  }

  @Test(expected = ClassCastException.class)
  public void testAll_WrongType() {
    m_entity.put("four", 42);
    Map<String, String> all = m_entity.all();
    @SuppressWarnings("unused")
    String four = all.get("four");
  }
}
