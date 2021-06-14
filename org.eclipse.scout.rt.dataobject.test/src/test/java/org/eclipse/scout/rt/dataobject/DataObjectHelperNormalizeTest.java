/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.dataobject.fixture.AnotherCollectionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.CollectionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.ObjectCollectionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.SimpleFixtureDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Separated from {@link DataObjectHelperTest} because requiring a real implementation of {@link IDataObjectMapper} and
 * not the mocked one.
 */
@RunWith(PlatformTestRunner.class)
public class DataObjectHelperNormalizeTest {

  protected DataObjectHelper m_helper;

  @Before
  public void before() {
    m_helper = BEANS.get(DataObjectHelper.class);
  }

  @Test
  public void testNormalize() {
    CollectionFixtureDo doEntity = BEANS.get(CollectionFixtureDo.class)
        .withStringDoList(
            "string-doList-charlie",
            "string-doList-alfa",
            "string-doList-bravo")
        .withStringDoSet(
            "string-doSet-charlie",
            "string-doSet-alfa",
            "string-doSet-bravo")
        .withStringDoCollection(
            "string-doCollection-charlie",
            "string-doCollection-alfa",
            "string-doCollection-bravo")
        .withSimpleDoList(
            simpleFixture("simple-doList-charlie"),
            simpleFixture("simple-doList-alfa"),
            simpleFixture("simple-doList-bravo"))
        .withSimpleDoSet(
            simpleFixture("simple-doSet-charlie"),
            simpleFixture("simple-doSet-alfa"),
            simpleFixture("simple-doSet-bravo"))
        .withSimpleDoCollection(
            simpleFixture("simple-doCollection-charlie"),
            simpleFixture("simple-doCollection-alfa"),
            simpleFixture("simple-doCollection-bravo"));

    // Unchanged order before normalization
    assertEquals(Arrays.asList("string-doList-charlie", "string-doList-alfa", "string-doList-bravo"), new ArrayList<>(doEntity.getStringDoList()));
    assertEquals(Arrays.asList("string-doSet-charlie", "string-doSet-alfa", "string-doSet-bravo"), new ArrayList<>(doEntity.getStringDoSet()));
    assertEquals(Arrays.asList("string-doCollection-charlie", "string-doCollection-alfa", "string-doCollection-bravo"), new ArrayList<>(doEntity.getStringDoCollection()));

    assertEquals(Arrays.asList(simpleFixture("simple-doList-charlie"), simpleFixture("simple-doList-alfa"), simpleFixture("simple-doList-bravo")), new ArrayList<>(doEntity.getSimpleDoList()));
    assertEquals(Arrays.asList(simpleFixture("simple-doSet-charlie"), simpleFixture("simple-doSet-alfa"), simpleFixture("simple-doSet-bravo")), new ArrayList<>(doEntity.getSimpleDoSet()));
    assertEquals(Arrays.asList(simpleFixture("simple-doCollection-charlie"), simpleFixture("simple-doCollection-alfa"), simpleFixture("simple-doCollection-bravo")), new ArrayList<>(doEntity.getSimpleDoCollection()));

    m_helper.normalize(doEntity);

    // DoSet and DoCollection are sorted by comparable String
    assertEquals(Arrays.asList("string-doList-charlie", "string-doList-alfa", "string-doList-bravo"), new ArrayList<>(doEntity.getStringDoList())); // DoList keeps order
    assertEquals(Arrays.asList("string-doSet-alfa", "string-doSet-bravo", "string-doSet-charlie"), new ArrayList<>(doEntity.getStringDoSet()));
    assertEquals(Arrays.asList("string-doCollection-alfa", "string-doCollection-bravo", "string-doCollection-charlie"), new ArrayList<>(doEntity.getStringDoCollection()));

    // DoSet and DoCollection are sorted by serialized output
    assertEquals(Arrays.asList(simpleFixture("simple-doList-charlie"), simpleFixture("simple-doList-alfa"), simpleFixture("simple-doList-bravo")), new ArrayList<>(doEntity.getSimpleDoList())); // DoList keeps order
    assertEquals(Arrays.asList(simpleFixture("simple-doSet-alfa"), simpleFixture("simple-doSet-bravo"), simpleFixture("simple-doSet-charlie")), new ArrayList<>(doEntity.getSimpleDoSet()));
    assertEquals(Arrays.asList(simpleFixture("simple-doCollection-alfa"), simpleFixture("simple-doCollection-bravo"), simpleFixture("simple-doCollection-charlie")), new ArrayList<>(doEntity.getSimpleDoCollection()));
  }

  /**
   * Verify that for normalization, inner {@link DoSet} and {@link DoCollection} are normalized first before sorting
   * other collections.
   * <p>
   * Only validated based on {@link DoCollection} here.
   */
  @Test
  public void testNormalizeDeepFirst() {
    CollectionFixtureDo doEntity = BEANS.get(CollectionFixtureDo.class)
        .withAnotherDoCollection(
            BEANS.get(AnotherCollectionFixtureDo.class)
                .withAnotherDoCollection(
                    "bravo",
                    "alfa",
                    "charlie",
                    "delta-1"),
            BEANS.get(AnotherCollectionFixtureDo.class)
                .withAnotherDoCollection(
                    "charlie",
                    "bravo",
                    "alfa",
                    "delta-2"),
            BEANS.get(AnotherCollectionFixtureDo.class)
                .withAnotherDoCollection(
                    "alfa",
                    "charlie",
                    "bravo",
                    "delta-3"));

    m_helper.normalize(doEntity);

    List<AnotherCollectionFixtureDo> list = new ArrayList<>(doEntity.getAnotherDoCollection());
    assertEquals(3, list.size());
    // If normalization wouldn't normalize deep first, the DoCollection would be ordered delta-3 (starting with alfa), delta-1 (starting with bravo) and then delta-2 (starting with charlie)
    assertEquals(Arrays.asList("alfa", "bravo", "charlie", "delta-1"), new ArrayList<>(list.get(0).getAnotherDoCollection()));
    assertEquals(Arrays.asList("alfa", "bravo", "charlie", "delta-2"), new ArrayList<>(list.get(1).getAnotherDoCollection()));
    assertEquals(Arrays.asList("alfa", "bravo", "charlie", "delta-3"), new ArrayList<>(list.get(2).getAnotherDoCollection()));
  }

  @Test
  public void testNormalizeNumbers() {
    ObjectCollectionFixtureDo doEntity = BEANS.get(ObjectCollectionFixtureDo.class)
        .withObjectSet(3, 2, 1);

    m_helper.normalize(doEntity);

    assertEquals(Arrays.asList(1, 2, 3), new ArrayList<>(doEntity.getObjectSet()));
  }

  @Test
  public void testNormalizeMixed() {
    ObjectCollectionFixtureDo doEntity = BEANS.get(ObjectCollectionFixtureDo.class)
        .withObjectSet(3, "two", 1); // types are not comparable with each other

    // ClassCastException in comparison of Integer/String
    assertThrows(ClassCastException.class, () -> m_helper.normalize(doEntity));
  }

  protected SimpleFixtureDo simpleFixture(String name) {
    return BEANS.get(SimpleFixtureDo.class).withName1(name);
  }
}
