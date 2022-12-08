/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.scout.rt.dataobject.fixture.BiCompositeFixtureObject;
import org.eclipse.scout.rt.dataobject.fixture.BiCompositeFixtureObjectDataObjectVisitorExtension;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectWithCompositeFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectWithCompositeIdDo;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectWithTypedIdDo;
import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureWrapperCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.IdAsStringFixtureDataObjectVisitorExtension;
import org.eclipse.scout.rt.dataobject.fixture.IdAsStringFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.TriCompositeFixtureObject;
import org.eclipse.scout.rt.dataobject.fixture.TriCompositeFixtureObjectDataObjectVisitorExtension;
import org.eclipse.scout.rt.dataobject.id.ICompositeId;
import org.eclipse.scout.rt.dataobject.id.IRootId;
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * Tests for {@link DataObjectVisitors#forEachRec(Object, Class, Consumer)} and
 * {@link DataObjectVisitors#replaceEach(Object, Class, UnaryOperator)} with implementations of
 * {@link IDataObjectVisitorExtension}.
 */
public class DataObjectVisitorExtensionTest {

  @Test
  public void testForEachRecCompositeObjectId() {
    DataObjectWithCompositeFixtureDo doEntity = BEANS.get(DataObjectWithCompositeFixtureDo.class)
        .withId(FixtureStringId.of("single-id"))
        .withComposite(BiCompositeFixtureObject.of(FixtureStringId.of("composite-1"), FixtureStringId.of("composite-2")))
        .withCompositeList(
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.1"), FixtureStringId.of("composite-list-1.2")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.1"), FixtureStringId.of("composite-list-2.2")));

    List<FixtureStringId> ids = new ArrayList<>();
    DataObjectVisitors.forEachRec(doEntity, FixtureStringId.class, ids::add);

    assertEquals(
        CollectionUtility.arrayList(
            FixtureStringId.of("single-id"),
            FixtureStringId.of("composite-1"),
            FixtureStringId.of("composite-2"),
            FixtureStringId.of("composite-list-1.1"),
            FixtureStringId.of("composite-list-1.2"),
            FixtureStringId.of("composite-list-2.1"),
            FixtureStringId.of("composite-list-2.2")),
        ids);
  }

  /**
   * Test with {@link TriCompositeFixtureObject} instead of {@link BiCompositeFixtureObject} to check if
   * {@link TriCompositeFixtureObjectDataObjectVisitorExtension} is called instead of
   * {@link BiCompositeFixtureObjectDataObjectVisitorExtension}.
   * <p>
   * Verification just for 'for each rec' is enough because inventory returns correct visitor extension, thus no need to
   * check the 'replace' case too.
   */
  @Test
  public void testForEachRecTriCompositeObject() {
    DataObjectWithCompositeFixtureDo doEntity = BEANS.get(DataObjectWithCompositeFixtureDo.class)
        .withId(FixtureStringId.of("single-id"))
        .withComposite(TriCompositeFixtureObject.of(FixtureStringId.of("composite-1"), FixtureStringId.of("composite-2"), FixtureStringId.of("composite-3")))
        .withCompositeList(
            TriCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.1"), FixtureStringId.of("composite-list-1.2"), FixtureStringId.of("composite-list-1.3")),
            TriCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.1"), FixtureStringId.of("composite-list-2.2"), FixtureStringId.of("composite-list-2.3")));

    List<FixtureStringId> ids = new ArrayList<>();
    DataObjectVisitors.forEachRec(doEntity, FixtureStringId.class, ids::add);

    assertEquals(
        CollectionUtility.arrayList(
            FixtureStringId.of("single-id"),
            FixtureStringId.of("composite-1"),
            FixtureStringId.of("composite-2"),
            FixtureStringId.of("composite-3"),
            FixtureStringId.of("composite-list-1.1"),
            FixtureStringId.of("composite-list-1.2"),
            FixtureStringId.of("composite-list-1.3"),
            FixtureStringId.of("composite-list-2.1"),
            FixtureStringId.of("composite-list-2.2"),
            FixtureStringId.of("composite-list-2.3")),
        ids);
  }

  /**
   * Make sure that despite having an own {@link IDataObjectVisitorExtension}, main type
   * ({@link BiCompositeFixtureObject}) is still processed.
   */
  @Test
  public void testForEachRecCompositeObject() {
    DataObjectWithCompositeFixtureDo doEntity = BEANS.get(DataObjectWithCompositeFixtureDo.class)
        .withId(FixtureStringId.of("single-id"))
        .withComposite(BiCompositeFixtureObject.of(FixtureStringId.of("composite-1"), FixtureStringId.of("composite-2")))
        .withCompositeList(
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.1"), FixtureStringId.of("composite-list-1.2")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.1"), FixtureStringId.of("composite-list-2.2")));

    List<BiCompositeFixtureObject> compositeObjects = new ArrayList<>();
    DataObjectVisitors.forEachRec(doEntity, BiCompositeFixtureObject.class, compositeObjects::add);

    assertEquals(
        CollectionUtility.arrayList(
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-1"), FixtureStringId.of("composite-2")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.1"), FixtureStringId.of("composite-list-1.2")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.1"), FixtureStringId.of("composite-list-2.2"))),
        compositeObjects);
  }

  @Test
  public void testReplaceEachCompositeObjectId() {
    DataObjectWithCompositeFixtureDo actual = BEANS.get(DataObjectWithCompositeFixtureDo.class)
        .withId(FixtureStringId.of("single-id"))
        .withComposite(BiCompositeFixtureObject.of(FixtureStringId.of("composite-1"), FixtureStringId.of("composite-2")))
        .withCompositeList(
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.1"), FixtureStringId.of("composite-list-1.2")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.1"), FixtureStringId.of("composite-list-2.2")));

    DataObjectVisitors.replaceEach(actual, FixtureStringId.class, id -> FixtureStringId.of(id.unwrapAsString() + "-replaced"));

    DataObjectWithCompositeFixtureDo expected = BEANS.get(DataObjectWithCompositeFixtureDo.class)
        .withId(FixtureStringId.of("single-id-replaced"))
        .withComposite(BiCompositeFixtureObject.of(FixtureStringId.of("composite-1-replaced"), FixtureStringId.of("composite-2-replaced")))
        .withCompositeList(
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.1-replaced"), FixtureStringId.of("composite-list-1.2-replaced")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.1-replaced"), FixtureStringId.of("composite-list-2.2-replaced")));

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Make sure that despite having an own {@link IDataObjectVisitorExtension}, main type
   * ({@link BiCompositeFixtureObject}) is still replaced.
   */
  @Test
  public void testReplaceEachCompositeObject() {
    DataObjectWithCompositeFixtureDo actual = BEANS.get(DataObjectWithCompositeFixtureDo.class)
        .withId(FixtureStringId.of("single-id"))
        .withComposite(BiCompositeFixtureObject.of(FixtureStringId.of("composite-1"), FixtureStringId.of("composite-2")))
        .withCompositeList(
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.1"), FixtureStringId.of("composite-list-1.2")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.1"), FixtureStringId.of("composite-list-2.2")));

    DataObjectVisitors.replaceEach(actual, BiCompositeFixtureObject.class, id -> BiCompositeFixtureObject.of(id.getId2(), id.getId1())); // switch 1 <-> 2

    DataObjectWithCompositeFixtureDo expected = BEANS.get(DataObjectWithCompositeFixtureDo.class)
        .withId(FixtureStringId.of("single-id"))
        .withComposite(BiCompositeFixtureObject.of(FixtureStringId.of("composite-2"), FixtureStringId.of("composite-1")))
        .withCompositeList(
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-1.2"), FixtureStringId.of("composite-list-1.1")),
            BiCompositeFixtureObject.of(FixtureStringId.of("composite-list-2.2"), FixtureStringId.of("composite-list-2.1")));

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Test for {@link TypedIdDataObjectVisitorExtension}.
   */
  @Test
  public void testForEachTypedId() {
    DataObjectWithTypedIdDo doEntity = BEANS.get(DataObjectWithTypedIdDo.class)
        .withId(TypedId.of(FixtureStringId.of("single-id")))
        .withIds(Arrays.asList(
            TypedId.of(FixtureStringId.of("list-1")),
            TypedId.of(FixtureStringId.of("list-2")),
            TypedId.of(FixtureStringId.of("list-3"))));

    List<FixtureStringId> ids = new ArrayList<>();
    DataObjectVisitors.forEachRec(doEntity, FixtureStringId.class, ids::add);

    assertEquals(
        CollectionUtility.arrayList(
            FixtureStringId.of("single-id"),
            FixtureStringId.of("list-1"),
            FixtureStringId.of("list-2"),
            FixtureStringId.of("list-3")),
        ids);
  }

  /**
   * Test for {@link TypedIdDataObjectVisitorExtension}.
   */
  @Test
  public void testReplaceTypedId() {
    DataObjectWithTypedIdDo actual = BEANS.get(DataObjectWithTypedIdDo.class)
        .withId(TypedId.of(FixtureStringId.of("single-id")))
        .withIds(Arrays.asList(
            TypedId.of(FixtureStringId.of("list-1")),
            TypedId.of(FixtureStringId.of("list-3")),
            TypedId.of(FixtureStringId.of("list-3"))));

    DataObjectVisitors.replaceEach(actual, FixtureStringId.class, id -> FixtureStringId.of(id.unwrapAsString() + "-replaced"));

    DataObjectWithTypedIdDo expected = BEANS.get(DataObjectWithTypedIdDo.class)
        .withId(TypedId.of(FixtureStringId.of("single-id-replaced")))
        .withIds(Arrays.asList(
            TypedId.of(FixtureStringId.of("list-1-replaced")),
            TypedId.of(FixtureStringId.of("list-3-replaced")),
            TypedId.of(FixtureStringId.of("list-3-replaced"))));

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Test for {@link CompositeIdDataObjectVisitorExtension}.
   */
  @Test
  public void testForEachCompositeId() {
    DataObjectWithCompositeIdDo doEntity = BEANS.get(DataObjectWithCompositeIdDo.class)
        .withId(FixtureCompositeId.of(FixtureStringId.of("single-id"), FixtureUuId.of("353d9bd3-b082-4346-97b6-8c7f08377495")))
        .withIds(
            FixtureCompositeId.of(FixtureStringId.of("list-1"), FixtureUuId.of("0c3b57e0-14e3-4980-b81c-3bcec11794f8")),
            FixtureCompositeId.of(FixtureStringId.of("list-2"), FixtureUuId.of("395d7a06-8141-4966-9bc5-458515b1fa75")),
            FixtureCompositeId.of(FixtureStringId.of("list-3"), FixtureUuId.of("48e83968-6411-473d-98d5-2041536eb97f")))
        .withWrappedCompositeId(FixtureWrapperCompositeId.of(
            FixtureCompositeId.of(FixtureStringId.of("wrapped-inner-composite-id"), FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0")),
            FixtureStringId.of("wrapped-composite-id")));

    List<IRootId> rootIds = new ArrayList<>();
    DataObjectVisitors.forEachRec(doEntity, IRootId.class, rootIds::add);

    assertEquals(
        CollectionUtility.arrayList(
            FixtureStringId.of("single-id"),
            FixtureUuId.of("353d9bd3-b082-4346-97b6-8c7f08377495"),
            FixtureStringId.of("list-1"),
            FixtureUuId.of("0c3b57e0-14e3-4980-b81c-3bcec11794f8"),
            FixtureStringId.of("list-2"),
            FixtureUuId.of("395d7a06-8141-4966-9bc5-458515b1fa75"),
            FixtureStringId.of("list-3"),
            FixtureUuId.of("48e83968-6411-473d-98d5-2041536eb97f"),
            FixtureStringId.of("wrapped-inner-composite-id"),
            FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0"),
            FixtureStringId.of("wrapped-composite-id")),
        rootIds);

    List<ICompositeId> compositeIds = new ArrayList<>();
    DataObjectVisitors.forEachRec(doEntity, ICompositeId.class, compositeIds::add);

    assertEquals(
        CollectionUtility.arrayList(
            FixtureCompositeId.of(FixtureStringId.of("single-id"), FixtureUuId.of("353d9bd3-b082-4346-97b6-8c7f08377495")),
            FixtureCompositeId.of(FixtureStringId.of("list-1"), FixtureUuId.of("0c3b57e0-14e3-4980-b81c-3bcec11794f8")),
            FixtureCompositeId.of(FixtureStringId.of("list-2"), FixtureUuId.of("395d7a06-8141-4966-9bc5-458515b1fa75")),
            FixtureCompositeId.of(FixtureStringId.of("list-3"), FixtureUuId.of("48e83968-6411-473d-98d5-2041536eb97f")),
            // first the wrapper composite
            FixtureWrapperCompositeId.of(
                FixtureCompositeId.of(FixtureStringId.of("wrapped-inner-composite-id"), FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0")),
                FixtureStringId.of("wrapped-composite-id")),
            // before the inner composite
            FixtureCompositeId.of(FixtureStringId.of("wrapped-inner-composite-id"), FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0"))),
        compositeIds);
  }

  /**
   * Test for {@link CompositeIdDataObjectVisitorExtension} with replace of {@link FixtureStringId}.
   */
  @Test
  public void testReplaceCompositeIdInner() {
    DataObjectWithCompositeIdDo actual = BEANS.get(DataObjectWithCompositeIdDo.class)
        .withId(FixtureCompositeId.of(FixtureStringId.of("single-id"), FixtureUuId.of("353d9bd3-b082-4346-97b6-8c7f08377495")))
        .withIds(
            FixtureCompositeId.of(FixtureStringId.of("list-1"), FixtureUuId.of("0c3b57e0-14e3-4980-b81c-3bcec11794f8")),
            FixtureCompositeId.of(FixtureStringId.of("list-2"), FixtureUuId.of("395d7a06-8141-4966-9bc5-458515b1fa75")),
            FixtureCompositeId.of(FixtureStringId.of("list-3"), FixtureUuId.of("48e83968-6411-473d-98d5-2041536eb97f")))
        .withWrappedCompositeId(FixtureWrapperCompositeId.of(
            FixtureCompositeId.of(FixtureStringId.of("wrapped-inner-composite-id"), FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0")),
            FixtureStringId.of("wrapped-composite-id")));

    DataObjectVisitors.replaceEach(actual, FixtureStringId.class, id -> FixtureStringId.of(id.unwrapAsString() + "-replaced"));

    DataObjectWithCompositeIdDo expected = BEANS.get(DataObjectWithCompositeIdDo.class)
        .withId(FixtureCompositeId.of(FixtureStringId.of("single-id-replaced"), FixtureUuId.of("353d9bd3-b082-4346-97b6-8c7f08377495")))
        .withIds(
            FixtureCompositeId.of(FixtureStringId.of("list-1-replaced"), FixtureUuId.of("0c3b57e0-14e3-4980-b81c-3bcec11794f8")),
            FixtureCompositeId.of(FixtureStringId.of("list-2-replaced"), FixtureUuId.of("395d7a06-8141-4966-9bc5-458515b1fa75")),
            FixtureCompositeId.of(FixtureStringId.of("list-3-replaced"), FixtureUuId.of("48e83968-6411-473d-98d5-2041536eb97f")))
        .withWrappedCompositeId(FixtureWrapperCompositeId.of(
            FixtureCompositeId.of(FixtureStringId.of("wrapped-inner-composite-id-replaced"), FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0")),
            FixtureStringId.of("wrapped-composite-id-replaced")));

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Test for {@link CompositeIdDataObjectVisitorExtension} with replace of {@link FixtureCompositeId}.
   */
  @Test
  public void testReplaceCompositeIdFull() {
    DataObjectWithCompositeIdDo actual = BEANS.get(DataObjectWithCompositeIdDo.class)
        .withId(FixtureCompositeId.of(FixtureStringId.of("single-id"), FixtureUuId.of("353d9bd3-b082-4346-97b6-8c7f08377495")))
        .withIds(
            FixtureCompositeId.of(FixtureStringId.of("list-1"), FixtureUuId.of("0c3b57e0-14e3-4980-b81c-3bcec11794f8")),
            FixtureCompositeId.of(FixtureStringId.of("list-2"), FixtureUuId.of("395d7a06-8141-4966-9bc5-458515b1fa75")),
            FixtureCompositeId.of(FixtureStringId.of("list-3"), FixtureUuId.of("48e83968-6411-473d-98d5-2041536eb97f")))
        .withWrappedCompositeId(FixtureWrapperCompositeId.of(
            FixtureCompositeId.of(FixtureStringId.of("wrapped-inner-composite-id"), FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0")),
            FixtureStringId.of("wrapped-composite-id")));

    DataObjectVisitors.replaceEach(actual, FixtureCompositeId.class, id -> {
      FixtureStringId stringId = (FixtureStringId) id.unwrap().get(0);
      FixtureUuId uuId = (FixtureUuId) id.unwrap().get(1);
      return FixtureCompositeId.of(FixtureStringId.of(stringId.unwrap() + "-replaced"), uuId);
    });

    DataObjectWithCompositeIdDo expected = BEANS.get(DataObjectWithCompositeIdDo.class)
        .withId(FixtureCompositeId.of(FixtureStringId.of("single-id-replaced"), FixtureUuId.of("353d9bd3-b082-4346-97b6-8c7f08377495")))
        .withIds(
            FixtureCompositeId.of(FixtureStringId.of("list-1-replaced"), FixtureUuId.of("0c3b57e0-14e3-4980-b81c-3bcec11794f8")),
            FixtureCompositeId.of(FixtureStringId.of("list-2-replaced"), FixtureUuId.of("395d7a06-8141-4966-9bc5-458515b1fa75")),
            FixtureCompositeId.of(FixtureStringId.of("list-3-replaced"), FixtureUuId.of("48e83968-6411-473d-98d5-2041536eb97f")))
        .withWrappedCompositeId(FixtureWrapperCompositeId.of(
            FixtureCompositeId.of(FixtureStringId.of("wrapped-inner-composite-id-replaced"), FixtureUuId.of("4480f56b-70f5-48cc-ab29-431cd3cdc0d0")),
            FixtureStringId.of("wrapped-composite-id")));

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Test for {@link IdAsStringFixtureDataObjectVisitorExtension}.
   */
  @Test
  public void testForEachIdAsStringDataObject() {
    IdAsStringFixtureDo doEntity = BEANS.get(IdAsStringFixtureDo.class)
        .withIdAsString("single-id");

    List<FixtureStringId> ids = new ArrayList<>();
    DataObjectVisitors.forEachRec(doEntity, FixtureStringId.class, ids::add);

    assertEquals(
        CollectionUtility.arrayList(FixtureStringId.of("single-id")),
        ids);
  }

  /**
   * Test for {@link IdAsStringFixtureDataObjectVisitorExtension}.
   * <p>
   * Replacing a top level data object doesn't work the way the visitor and the corresponding method
   * ({@link DataObjectVisitors#replaceEach(Object, Class, UnaryOperator)}) was designed, because there is no return
   * value. That's why a dummy root data object is used for the test.
   */
  @Test
  public void testReplaceIdAsStringDataObject() {
    IDoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("dummy", BEANS.get(IdAsStringFixtureDo.class)
            .withIdAsString("single-id"))
        .build();

    DataObjectVisitors.replaceEach(actual, FixtureStringId.class, id -> FixtureStringId.of(id.unwrapAsString() + "-replaced"));

    IDoEntity expected = BEANS.get(DoEntityBuilder.class)
        .put("dummy", BEANS.get(IdAsStringFixtureDo.class)
            .withIdAsString("single-id-replaced"))
        .build();

    assertEqualsWithComparisonFailure(expected, actual);
  }

  /**
   * Tests that {@link DataObjectVisitors#forEachRec(Object, Class, Consumer)} and
   * {@link DataObjectVisitors#replaceEach(Object, Class, UnaryOperator)} will work for <code>null</code> node values
   * too regarding visitor extensions.
   */
  @Test
  public void testReplaceEachNullSafe() {
    DataObjectWithTypedIdDo actual = BEANS.get(DataObjectWithTypedIdDo.class)
        .withId(null);

    List<FixtureStringId> ids = new ArrayList<>();
    DataObjectVisitors.forEachRec(actual, FixtureStringId.class, ids::add);
    assertTrue(ids.isEmpty());

    DataObjectVisitors.replaceEach(actual, FixtureStringId.class, id -> FixtureStringId.of(id.unwrapAsString() + "-replaced"));

    DataObjectWithTypedIdDo expected = BEANS.get(DataObjectWithTypedIdDo.class)
        .withId(null);

    assertEqualsWithComparisonFailure(expected, actual);
  }
}
