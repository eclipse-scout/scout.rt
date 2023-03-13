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

import static org.eclipse.scout.rt.dataobject.DoPredicates.*;
import static org.eclipse.scout.rt.platform.util.Assertions.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.OtherEntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.ProjectFixtureDo;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;

public class DoPredicatesTest {

  private EntityFixtureDo m_entity1;
  private EntityFixtureDo m_entity2;

  private ProjectFixtureDo m_project1;
  private ProjectFixtureDo m_project2;

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

    m_project1 = new ProjectFixtureDo()
        .withName("project one")
        .withCount(50);

    m_project2 = new ProjectFixtureDo()
        .withName("project two");
  }

  /* **************************************************************************
   * exists
   * **************************************************************************/

  @Test(expected = AssertionException.class)
  public void testExistsDoValueNullAccessor() {
    exists(null);
  }

  @Test
  public void testExistsDoValue() {
    assertTrue(exists(EntityFixtureDo::id).test(m_entity1));
    assertTrue(exists(EntityFixtureDo::id).test(m_entity2));

    assertFalse(exists(EntityFixtureDo::otherEntity).test(m_entity1));
    assertFalse(exists(EntityFixtureDo::otherEntity).test(m_entity2));
  }

  /* **************************************************************************
   * not exists
   * **************************************************************************/

  @Test(expected = AssertionException.class)
  public void testNotExistsDoValueNullAccessor() {
    notExists(null);
  }

  @Test
  public void testNotExistsDoValue() {
    assertFalse(notExists(EntityFixtureDo::id).test(m_entity1));
    assertFalse(notExists(EntityFixtureDo::id).test(m_entity2));

    assertTrue(notExists(EntityFixtureDo::otherEntity).test(m_entity1));
    assertTrue(notExists(EntityFixtureDo::otherEntity).test(m_entity2));
  }

  /* **************************************************************************
   * is null
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testIsNullNullAccessor() {
    isNull(null);
  }

  @Test
  public void testIsNull() {
    assertTrue(isNull(EntityFixtureDo::id).test(m_entity1));
    assertFalse(isNull(EntityFixtureDo::id).test(m_entity2));

    assertTrue(isNull(EntityFixtureDo::otherEntity).test(m_entity1));
    assertTrue(isNull(EntityFixtureDo::otherEntity).test(m_entity2));
  }

  /* **************************************************************************
   * is not null
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testIsNotNullNullAccessor() {
    isNotNull(null);
  }

  @Test
  public void testIsNotNull() {
    assertFalse(isNotNull(EntityFixtureDo::id).test(m_entity1));
    assertTrue(isNotNull(EntityFixtureDo::id).test(m_entity2));

    assertFalse(isNotNull(EntityFixtureDo::otherEntity).test(m_entity1));
    assertFalse(isNotNull(EntityFixtureDo::otherEntity).test(m_entity2));
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
   * le
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testLeNullAccessor() {
    le(null, 42);
  }

  @Test(expected = AssertionException.class)
  public void testLeNullValue() {
    le(ProjectFixtureDo::count, null);
  }

  @Test
  public void testLe() {
    assertFalse(le(ProjectFixtureDo::count, -100).test(m_project1));
    assertFalse(le(ProjectFixtureDo::count, -100).test(m_project2));

    assertFalse(le(ProjectFixtureDo::count, 49).test(m_project1));
    assertFalse(le(ProjectFixtureDo::count, 49).test(m_project2));

    assertTrue(le(ProjectFixtureDo::count, 50).test(m_project1));
    assertFalse(le(ProjectFixtureDo::count, 50).test(m_project2));

    assertTrue(le(ProjectFixtureDo::count, 51).test(m_project1));
    assertFalse(le(ProjectFixtureDo::count, 51).test(m_project2));

    assertTrue(le(ProjectFixtureDo::count, 100).test(m_project1));
    assertFalse(le(ProjectFixtureDo::count, 100).test(m_project2));
  }

  /* **************************************************************************
   * lt
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testLtNullAccessor() {
    lt(null, 42);
  }

  @Test(expected = AssertionException.class)
  public void testLtNullValue() {
    lt(ProjectFixtureDo::count, null);
  }

  @Test
  public void testLt() {
    assertFalse(lt(ProjectFixtureDo::count, -100).test(m_project1));
    assertFalse(lt(ProjectFixtureDo::count, -100).test(m_project2));

    assertFalse(lt(ProjectFixtureDo::count, 49).test(m_project1));
    assertFalse(lt(ProjectFixtureDo::count, 49).test(m_project2));

    assertFalse(lt(ProjectFixtureDo::count, 50).test(m_project1));
    assertFalse(lt(ProjectFixtureDo::count, 50).test(m_project2));

    assertTrue(lt(ProjectFixtureDo::count, 51).test(m_project1));
    assertFalse(lt(ProjectFixtureDo::count, 51).test(m_project2));

    assertTrue(lt(ProjectFixtureDo::count, 100).test(m_project1));
    assertFalse(lt(ProjectFixtureDo::count, 100).test(m_project2));
  }

  /* **************************************************************************
   * ge
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testGeNullAccessor() {
    ge(null, 42);
  }

  @Test(expected = AssertionException.class)
  public void testGeNullValue() {
    ge(ProjectFixtureDo::count, null);
  }

  @Test
  public void testGe() {
    assertTrue(ge(ProjectFixtureDo::count, -100).test(m_project1));
    assertFalse(ge(ProjectFixtureDo::count, -100).test(m_project2));

    assertTrue(ge(ProjectFixtureDo::count, 49).test(m_project1));
    assertFalse(ge(ProjectFixtureDo::count, 49).test(m_project2));

    assertTrue(ge(ProjectFixtureDo::count, 50).test(m_project1));
    assertFalse(ge(ProjectFixtureDo::count, 50).test(m_project2));

    assertFalse(ge(ProjectFixtureDo::count, 51).test(m_project1));
    assertFalse(ge(ProjectFixtureDo::count, 51).test(m_project2));

    assertFalse(ge(ProjectFixtureDo::count, 100).test(m_project1));
    assertFalse(ge(ProjectFixtureDo::count, 100).test(m_project2));
  }

  /* **************************************************************************
   * gt
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testGtNullAccessor() {
    gt(null, 42);
  }

  @Test(expected = AssertionException.class)
  public void testGtNullValue() {
    gt(ProjectFixtureDo::count, null);
  }

  @Test
  public void testGt() {
    assertTrue(gt(ProjectFixtureDo::count, -100).test(m_project1));
    assertFalse(gt(ProjectFixtureDo::count, -100).test(m_project2));

    assertTrue(gt(ProjectFixtureDo::count, 49).test(m_project1));
    assertFalse(gt(ProjectFixtureDo::count, 49).test(m_project2));

    assertFalse(gt(ProjectFixtureDo::count, 50).test(m_project1));
    assertFalse(gt(ProjectFixtureDo::count, 50).test(m_project2));

    assertFalse(gt(ProjectFixtureDo::count, 51).test(m_project1));
    assertFalse(gt(ProjectFixtureDo::count, 51).test(m_project2));

    assertFalse(gt(ProjectFixtureDo::count, 100).test(m_project1));
    assertFalse(gt(ProjectFixtureDo::count, 100).test(m_project2));
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
  public void testExistsDoCollectionNullListAccessor() {
    exists(null, n -> true);
  }

  @Test(expected = AssertionException.class)
  public void testExistsDoCollectionNullPredicate() {
    exists(EntityFixtureDo::otherEntities, null);
  }

  @Test
  public void testExistsDoCollection() {
    final EntityFixtureDo entityWithoutOtherEntities = new EntityFixtureDo().withId("3");

    Predicate<OtherEntityFixtureDo> predicate = n -> true;
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> false;
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> n.getActive();
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> !n.getActive();
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(exists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertFalse(exists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));
  }

  /* **************************************************************************
   * not exists
   * **************************************************************************/
  @Test(expected = AssertionException.class)
  public void testNotExistsDoCollectionNullListAccessor() {
    exists(null, n -> true);
  }

  @Test(expected = AssertionException.class)
  public void testNotExistsDoCollectionNullPredicate() {
    exists(EntityFixtureDo::otherEntities, null);
  }

  @Test
  public void testNotExistsDoCollection() {
    final EntityFixtureDo entityWithoutOtherEntities = new EntityFixtureDo().withId("3");

    Predicate<OtherEntityFixtureDo> predicate = n -> true;
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> false;
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> n.getActive();
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity1));
    assertFalse(notExists(EntityFixtureDo::otherEntities, predicate).test(m_entity2));
    assertTrue(notExists(EntityFixtureDo::otherEntities, predicate).test(entityWithoutOtherEntities));

    predicate = n -> !n.getActive();
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
