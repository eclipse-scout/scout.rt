/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.OtherEntityFixtureDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Various tests for {@link DataObjectVisitors} and its {@link AbstractDataObjectVisitor} implementations.
 */
@RunWith(PlatformTestRunner.class)
public class DataObjectVisitorsTest {

  protected EntityFixtureDo fixtureEntity;

  @Before
  public void before() {
    Map<String, OtherEntityFixtureDo> map = new LinkedHashMap<>();
    map.put("map1", BEANS.get(OtherEntityFixtureDo.class)
        .withId("m1")
        .withActive(true));
    map.put("map2", BEANS.get(OtherEntityFixtureDo.class)
        .withId("m2")
        .withActive(false));

    List<OtherEntityFixtureDo> list = new ArrayList<>();
    list.add(BEANS.get(OtherEntityFixtureDo.class)
        .withId("l1")
        .withActive(true));
    list.add(BEANS.get(OtherEntityFixtureDo.class)
        .withId("l2")
        .withActive(false));

    // setup fixtureEntity used as test entity for various test methods
    fixtureEntity = BEANS.get(EntityFixtureDo.class)
        .withId("myId")
        .withOtherEntities(
            BEANS.get(OtherEntityFixtureDo.class)
                .withId("o1")
                .withActive(true)
                .withNestedOtherEntity(
                    BEANS.get(OtherEntityFixtureDo.class)
                        .withId("n1")),
            BEANS.get(OtherEntityFixtureDo.class)
                .withId("o2")
                .withActive(false)
                .withNestedOtherEntity(
                    BEANS.get(OtherEntityFixtureDo.class)
                        .withId("n2")))
        .withOtherEntitiesList(list)
        .withOtherEntitiesMap(map);
  }

  @Test
  public void testForEach() {
    List<EntityFixtureDo> visited = new ArrayList<>();
    DataObjectVisitors.forEach(fixtureEntity, EntityFixtureDo.class, visited::add);
    assertEquals(1, visited.size());
    assertEquals(fixtureEntity, visited.get(0));
  }

  @Test
  public void testForEachRec_Entity() {
    List<OtherEntityFixtureDo> visited = new ArrayList<>();
    DataObjectVisitors.forEachRec(fixtureEntity, OtherEntityFixtureDo.class, visited::add);
    assertEquals(8, visited.size());

    List<OtherEntityFixtureDo> expected = new ArrayList<>();
    addAllRec(expected, fixtureEntity.getOtherEntities());
    addAllRec(expected, fixtureEntity.getOtherEntitiesList());
    addAllRec(expected, fixtureEntity.getOtherEntitiesMap().values());
    assertEquals(expected.size(), visited.size());
    CollectionUtility.containsAll(visited, expected);
  }

  protected void addAllRec(List<OtherEntityFixtureDo> entityList, Collection<OtherEntityFixtureDo> entities) {
    addAllRecIf(entityList, entities, o -> true);
  }

  @Test
  public void testForEachRec_SimpleType() {
    List<Boolean> visited = new ArrayList<>();
    DataObjectVisitors.forEachRec(fixtureEntity, Boolean.class, visited::add);
    assertEquals(6, visited.size());

    List<String> visitedStrings = new ArrayList<>();
    DataObjectVisitors.forEachRec(fixtureEntity, String.class, visitedStrings::add);
    assertEquals(11, visitedStrings.size());
  }

  @Test
  public void testForEachRec_List() {
    DoList<OtherEntityFixtureDo> list = new DoList<>();
    list.addAll(fixtureEntity.getOtherEntitiesList());

    List<OtherEntityFixtureDo> visited = new ArrayList<>();
    DataObjectVisitors.forEachRec(list, OtherEntityFixtureDo.class, visited::add);
    assertEquals(2, visited.size());

    List<OtherEntityFixtureDo> expected = new ArrayList<>();
    expected.addAll(fixtureEntity.getOtherEntitiesList());
    CollectionUtility.containsAll(visited, expected);
  }

  @Test
  public void testForEachRec_DoEntity() {
    OtherEntityFixtureDo otherEntity = BEANS.get(OtherEntityFixtureDo.class);
    DoEntity entity = BEANS.get(EntityFixtureDo.class)
        .withId("myId")
        .withOtherEntity(otherEntity);

    List<DoEntity> visited = new ArrayList<>();
    DataObjectVisitors.forEachRec(entity, DoEntity.class, visited::add);
    assertEquals(2, visited.size());
    assertEquals(entity, visited.get(0));
    assertEquals(otherEntity, visited.get(1));
  }

  @Test
  public void testForEachRec_DoEntityNullDoValue() {
    DoEntity entity = BEANS.get(EntityFixtureDo.class)
        .withId("myId")
        .withOtherEntity(null);

    List<DoEntity> visited = new ArrayList<>();
    DataObjectVisitors.forEachRec(entity, DoEntity.class, visited::add);
    assertEquals(1, visited.size());
    assertEquals(entity, visited.get(0));
  }

  @Test
  public void testForEachRecIf() {
    List<DoEntity> visited = new ArrayList<>();
    DataObjectVisitors.forEachRecIf(fixtureEntity, OtherEntityFixtureDo.class, o -> {
      visited.add(o);
      return BooleanUtility.nvl(o.isActive());
    });
    assertEquals(7, visited.size());

    List<OtherEntityFixtureDo> expected = new ArrayList<>();
    addAllRecIf(expected, fixtureEntity.getOtherEntities(), OtherEntityFixtureDo::isActive);
    addAllRecIf(expected, fixtureEntity.getOtherEntitiesList(), OtherEntityFixtureDo::isActive);
    addAllRecIf(expected, fixtureEntity.getOtherEntitiesMap().values(), OtherEntityFixtureDo::isActive);
    assertEquals(expected.size(), visited.size());
    CollectionUtility.containsAll(visited, expected);
  }

  @Test
  public void testReplaceEach_identity() {
    OtherEntityFixtureDo otherEntity = BEANS.get(OtherEntityFixtureDo.class)
        .withId("otherId");

    EntityFixtureDo entity = BEANS.get(EntityFixtureDo.class)
        .withId("myId")
        .withOtherEntity(otherEntity);

    DataObjectVisitors.replaceEach(entity, OtherEntityFixtureDo.class, o -> o);
    assertEquals(otherEntity, entity.getOtherEntity());
  }

  @Test
  public void testReplaceEach_updateElement() {
    OtherEntityFixtureDo otherEntity = BEANS.get(OtherEntityFixtureDo.class)
        .withId("otherId");

    EntityFixtureDo entity = BEANS.get(EntityFixtureDo.class)
        .withId("myId")
        .withOtherEntity(otherEntity);

    DataObjectVisitors.replaceEach(entity, OtherEntityFixtureDo.class, o -> o.withId("otherId2"));
    assertEquals(otherEntity, entity.getOtherEntity());
    assertEquals("otherId2", entity.getOtherEntity().getId());
    assertEquals("otherId2", otherEntity.getId());
  }

  @Test
  public void testReplaceEach_replaceAll_DoEntity() {
    OtherEntityFixtureDo otherEntity = BEANS.get(OtherEntityFixtureDo.class).withId("otherId");
    DataObjectVisitors.replaceEach(fixtureEntity, OtherEntityFixtureDo.class, o -> otherEntity);

    List<OtherEntityFixtureDo> visited = new ArrayList<>();
    DataObjectVisitors.forEachRec(fixtureEntity, OtherEntityFixtureDo.class, visited::add);
    assertEquals(6, visited.size());
    visited.forEach(o -> assertEquals(otherEntity, o));
  }

  @Test
  public void testReplaceEach_replaceAll_DoList() {
    DoList<OtherEntityFixtureDo> entites = new DoList<>();
    entites.add(BEANS.get(OtherEntityFixtureDo.class).withId("id1"));
    entites.add(BEANS.get(OtherEntityFixtureDo.class).withId("id2"));

    OtherEntityFixtureDo otherEntity = BEANS.get(OtherEntityFixtureDo.class).withId("otherId");
    DataObjectVisitors.replaceEach(entites, OtherEntityFixtureDo.class, o -> otherEntity);
    entites.forEach(o -> assertEquals(otherEntity, o));
  }

  protected void addAllRecIf(List<OtherEntityFixtureDo> entityList, Collection<OtherEntityFixtureDo> entities, Predicate<OtherEntityFixtureDo> nestedEntityPredicate) {
    entities.stream()
        .filter(Objects::nonNull)
        .forEach(entity -> {
          entityList.add(entity);
          if (nestedEntityPredicate.test(entity)) {
            addAllRec(entityList, Arrays.asList(entity.getNestedOtherEntity()));
          }
        });
  }
}
