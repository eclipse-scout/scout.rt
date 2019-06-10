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

import java.util.Map;

import org.eclipse.scout.rt.dataobject.DoMapEntity;
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
