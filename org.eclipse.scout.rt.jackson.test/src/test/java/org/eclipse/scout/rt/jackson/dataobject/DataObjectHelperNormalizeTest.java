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
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.jackson.dataobject.fixture.AnotherCollectionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.CollectionAlfaContributionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.CollectionBravoContributionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.CollectionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.ObjectCollectionFixtureDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample1Do;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * In .jackson. module because requiring a real implementation of {@link IDataObjectMapper}.
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
        .withExampleDoList(
            exampleFixture("example-doList-charlie"),
            exampleFixture("example-doList-alfa"),
            exampleFixture("example-doList-bravo"))
        .withExampleDoSet(
            exampleFixture("example-doSet-charlie"),
            exampleFixture("example-doSet-alfa"),
            exampleFixture("example-doSet-bravo"))
        .withExampleDoCollection(
            exampleFixture("example-doCollection-charlie"),
            exampleFixture("example-doCollection-alfa"),
            exampleFixture("example-doCollection-bravo"));

    doEntity.contribution(CollectionBravoContributionFixtureDo.class).withValue("bravo");
    doEntity.contribution(CollectionAlfaContributionFixtureDo.class).withValue("alfa");

    // Unchanged order before normalization
    assertEquals(Arrays.asList("string-doList-charlie", "string-doList-alfa", "string-doList-bravo"), new ArrayList<>(doEntity.getStringDoList()));
    assertEquals(Arrays.asList("string-doSet-charlie", "string-doSet-alfa", "string-doSet-bravo"), new ArrayList<>(doEntity.getStringDoSet()));
    assertEquals(Arrays.asList("string-doCollection-charlie", "string-doCollection-alfa", "string-doCollection-bravo"), new ArrayList<>(doEntity.getStringDoCollection()));

    assertEquals(Arrays.asList(exampleFixture("example-doList-charlie"), exampleFixture("example-doList-alfa"), exampleFixture("example-doList-bravo")), new ArrayList<>(doEntity.getExampleDoList()));
    assertEquals(Arrays.asList(exampleFixture("example-doSet-charlie"), exampleFixture("example-doSet-alfa"), exampleFixture("example-doSet-bravo")), new ArrayList<>(doEntity.getExampleDoSet()));
    assertEquals(Arrays.asList(exampleFixture("example-doCollection-charlie"), exampleFixture("example-doCollection-alfa"), exampleFixture("example-doCollection-bravo")), new ArrayList<>(doEntity.getExampleDoCollection()));

    // Contribution order is by insertion order (not API though)
    List<IDoEntityContribution> contributions = new ArrayList<>(doEntity.getContributions());
    assertTrue(contributions.get(0) instanceof CollectionBravoContributionFixtureDo);
    assertTrue(contributions.get(1) instanceof CollectionAlfaContributionFixtureDo);

    m_helper.normalize(doEntity);

    // DoSet and DoCollection are sorted by comparable String
    assertEquals(Arrays.asList("string-doList-charlie", "string-doList-alfa", "string-doList-bravo"), new ArrayList<>(doEntity.getStringDoList())); // DoList keeps order
    assertEquals(Arrays.asList("string-doSet-alfa", "string-doSet-bravo", "string-doSet-charlie"), new ArrayList<>(doEntity.getStringDoSet()));
    assertEquals(Arrays.asList("string-doCollection-alfa", "string-doCollection-bravo", "string-doCollection-charlie"), new ArrayList<>(doEntity.getStringDoCollection()));

    // DoSet and DoCollection are sorted by serialized output
    assertEquals(Arrays.asList(exampleFixture("example-doList-charlie"), exampleFixture("example-doList-alfa"), exampleFixture("example-doList-bravo")), new ArrayList<>(doEntity.getExampleDoList())); // DoList keeps order
    assertEquals(Arrays.asList(exampleFixture("example-doSet-alfa"), exampleFixture("example-doSet-bravo"), exampleFixture("example-doSet-charlie")), new ArrayList<>(doEntity.getExampleDoSet()));
    assertEquals(Arrays.asList(exampleFixture("example-doCollection-alfa"), exampleFixture("example-doCollection-bravo"), exampleFixture("example-doCollection-charlie")), new ArrayList<>(doEntity.getExampleDoCollection()));

    // Contributions are normalized the same way as DoCollection
    contributions = new ArrayList<>(doEntity.getContributions());
    assertTrue(contributions.get(0) instanceof CollectionAlfaContributionFixtureDo);
    assertTrue(contributions.get(1) instanceof CollectionBravoContributionFixtureDo);
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

  protected TestCoreExample1Do exampleFixture(String name) {
    return BEANS.get(TestCoreExample1Do.class).withName(name);
  }
}
