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

import static org.eclipse.scout.rt.dataobject.DoPredicates.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.OtherEntityFixtureDo;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;

public class DoPredicatesTest {

  private EntityFixtureDo m_entity1;
  private EntityFixtureDo m_entity2;

  @Before
  public void before() {
    m_entity1 = new EntityFixtureDo()
        .withId(null)
        .withOtherEntities(
            new OtherEntityFixtureDo()
                .withId("10")
                .withActive(true));

    m_entity2 = new EntityFixtureDo()
        .withId("1")
        .withOtherEntities(
            new OtherEntityFixtureDo()
                .withId("20")
                .withActive(false),

            new OtherEntityFixtureDo()
                .withId("21")
                .withActive(true));
  }

  /* **************************************************************************
   * eq
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testEqNullAccessor() {
    eq(null, null);
  }

  @Test
  public void testEq() {

    assertTrue(eq(EntityFixtureDo::id, null).test(m_entity1));
    assertFalse(eq(EntityFixtureDo::id, null).test(m_entity2));

    assertFalse(eq(EntityFixtureDo::id, "1").test(m_entity1));
    assertTrue(eq(EntityFixtureDo::id, "1").test(m_entity2));

    assertFalse(eq(EntityFixtureDo::id, "2").test(m_entity1));
    assertFalse(eq(EntityFixtureDo::id, "2").test(m_entity2));
  }

  /* **************************************************************************
   * ne
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testNeNullAccessor() {
    ne(null, null);
  }

  @Test
  public void testNe() {
    assertFalse(ne(EntityFixtureDo::id, null).test(m_entity1));
    assertTrue(ne(EntityFixtureDo::id, null).test(m_entity2));

    assertTrue(ne(EntityFixtureDo::id, "1").test(m_entity1));
    assertFalse(ne(EntityFixtureDo::id, "1").test(m_entity2));

    assertTrue(ne(EntityFixtureDo::id, "2").test(m_entity1));
    assertTrue(ne(EntityFixtureDo::id, "2").test(m_entity2));
  }

  /* **************************************************************************
   * in
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testInNullAccessor() {
    in(null, Collections.emptySet());
  }

  @Test(expected = AssertionException.class)
  public void testInNullCollection() {
    in(EntityFixtureDo::id, null);
  }

  @Test
  public void testIn() {
    Collection<String> inValues = Collections.emptySet();
    assertFalse(in(EntityFixtureDo::id, inValues).test(m_entity1));
    assertFalse(in(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Collections.singleton(null);
    assertTrue(in(EntityFixtureDo::id, inValues).test(m_entity1));
    assertFalse(in(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Arrays.asList("1");
    assertFalse(in(EntityFixtureDo::id, inValues).test(m_entity1));
    assertTrue(in(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Arrays.asList("1", null);
    assertTrue(in(EntityFixtureDo::id, inValues).test(m_entity1));
    assertTrue(in(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Arrays.asList("2", "3");
    assertFalse(in(EntityFixtureDo::id, inValues).test(m_entity1));
    assertFalse(in(EntityFixtureDo::id, inValues).test(m_entity2));
  }

  /* **************************************************************************
   * not in
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testNotInNullAccessor() {
    notIn(null, Collections.emptySet());
  }

  @Test(expected = AssertionException.class)
  public void testNotInNullCollection() {
    notIn(EntityFixtureDo::id, null);
  }

  @Test
  public void testNotIn() {
    Collection<String> inValues = Collections.emptySet();
    assertTrue(notIn(EntityFixtureDo::id, inValues).test(m_entity1));
    assertTrue(notIn(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Collections.singleton(null);
    assertFalse(notIn(EntityFixtureDo::id, inValues).test(m_entity1));
    assertTrue(notIn(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Arrays.asList("1");
    assertTrue(notIn(EntityFixtureDo::id, inValues).test(m_entity1));
    assertFalse(notIn(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Arrays.asList("1", null);
    assertFalse(notIn(EntityFixtureDo::id, inValues).test(m_entity1));
    assertFalse(notIn(EntityFixtureDo::id, inValues).test(m_entity2));

    inValues = Arrays.asList("2", "3");
    assertTrue(notIn(EntityFixtureDo::id, inValues).test(m_entity1));
    assertTrue(notIn(EntityFixtureDo::id, inValues).test(m_entity2));
  }

  /* **************************************************************************
   * exists
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testExistsNullListAccessor() {
    exists(null, n -> true);
  }

  @Test(expected = AssertionException.class)
  public void testExistsNullPredicate() {
    exists(EntityFixtureDo::otherEntities, null);
  }

  @Test
  public void testExists() {
    final EntityFixtureDo entityWithoutOtherEntities = new EntityFixtureDo().withId("3");

    Predicate<OtherEntityFixtureDo> predicate = n -> true;
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> false;
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> n.isActive();
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> !n.isActive();
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));
  }

  /* **************************************************************************
   * not exists
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testNotExistsNullListAccessor() {
    exists(null, n -> true);
  }

  @Test(expected = AssertionException.class)
  public void testNotExistsNullPredicate() {
    exists(EntityFixtureDo::otherEntities, null);
  }

  @Test
  public void testNotExists() {
    final EntityFixtureDo entityWithoutOtherEntities = new EntityFixtureDo().withId("3");

    Predicate<OtherEntityFixtureDo> predicate = n -> true;
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> false;
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> n.isActive();
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> !n.isActive();
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));
  }

  /* **************************************************************************
   * empty
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testEmptyNullListAccessor() {
    empty(null);
  }

  @Test
  public void testEmpty() {
    final EntityFixtureDo entityWithoutOtherEntities = new EntityFixtureDo().withId("3");

    assertFalse(empty(EntityFixtureDo::otherEntities).test(m_entity1));
    assertFalse(empty(EntityFixtureDo::otherEntities).test(m_entity2));
    assertTrue(empty(EntityFixtureDo::otherEntities).test(entityWithoutOtherEntities));
  }

  /* **************************************************************************
   * not empty
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testNotEmptyNullListAccessor() {
    notEmpty(null);
  }

  @Test
  public void testNotEmpty() {
    final EntityFixtureDo entityWithoutOtherEntities = new EntityFixtureDo().withId("3");

    assertTrue(notEmpty(EntityFixtureDo::otherEntities).test(m_entity1));
    assertTrue(notEmpty(EntityFixtureDo::otherEntities).test(m_entity2));
    assertFalse(notEmpty(EntityFixtureDo::otherEntities).test(entityWithoutOtherEntities));
  }
}
