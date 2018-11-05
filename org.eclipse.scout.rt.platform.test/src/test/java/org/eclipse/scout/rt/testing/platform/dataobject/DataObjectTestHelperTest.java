/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.dataobject;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.platform.dataobject.fixture.OtherEntityFixtureDo;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

public class DataObjectTestHelperTest {

  protected static DataObjectTestHelper s_dataObjectTestHelper = BEANS.get(DataObjectTestHelper.class);

  protected DoEntity m_expectedDoEntity;
  protected EntityFixtureDo m_expectedEntityFixture;

  @Before
  public void before() {
    m_expectedDoEntity = BEANS.get(DoEntity.class);
    m_expectedDoEntity.put("foo", "bar");
    m_expectedDoEntity.put("baz", 42);
    m_expectedEntityFixture = createEntityFixture();
  }

  protected EntityFixtureDo createEntityFixture() {
    return BEANS.get(EntityFixtureDo.class)
        .withId(null)
        .withOtherEntities(
            BEANS.get(OtherEntityFixtureDo.class)
                .withId("10")
                .withActive(true),
            BEANS.get(OtherEntityFixtureDo.class)
                .withId("12")
                .withActive(false));
  }

  protected DoEntity createEntityFixtureRaw() {
    DoEntity other1 = BEANS.get(DoEntity.class);
    other1.put("id", "10");
    other1.put("active", true);
    DoEntity other2 = BEANS.get(DoEntity.class);
    other2.put("id", "12");
    other2.put("active", false);
    DoEntity entity = BEANS.get(DoEntity.class);
    entity.put("id", null);
    entity.putList("otherEntities", Arrays.asList(other1, other2));
    return entity;
  }

  @Test
  public void testAssertDoEntityEquals() {
    DoEntity actual = new DoEntity();
    actual.put("foo", "bar");
    actual.put("baz", 42);
    s_dataObjectTestHelper.assertDoEntityEquals(actual, m_expectedDoEntity);

    EntityFixtureDo actualFixture = createEntityFixture();
    s_dataObjectTestHelper.assertDoEntityEquals(actualFixture, m_expectedEntityFixture);
  }

  @Test(expected = ComparisonFailure.class)
  public void testAssertDoEntityEquals_missingAttribute() {
    DoEntity actual = new DoEntity();
    actual.put("foo", "bar");
    s_dataObjectTestHelper.assertDoEntityEquals(actual, m_expectedDoEntity);
  }

  @Test(expected = ComparisonFailure.class)
  public void testAssertDoEntityEquals_wrongAttribute() {
    DoEntity actual = new DoEntity();
    actual.put("foo", "bar");
    actual.put("baz", 43);
    s_dataObjectTestHelper.assertDoEntityEquals(actual, m_expectedDoEntity);
  }

  @Test(expected = ComparisonFailure.class)
  public void testAssertDoEntityEquals_wrongListItem() {
    EntityFixtureDo actual = createEntityFixture();
    actual.getOtherEntities().add(BEANS.get(OtherEntityFixtureDo.class));
    s_dataObjectTestHelper.assertDoEntityEquals(actual, m_expectedEntityFixture);
  }

  @Test
  public void testAssertDoEntityEquals_classesMustNotBeEquals() {
    DoEntity entity = createEntityFixtureRaw();
    s_dataObjectTestHelper.assertDoEntityEquals(entity, m_expectedEntityFixture, false);
  }

  @Test(expected = ComparisonFailure.class)
  public void testAssertDoEntityEquals_classesMustBeEquals() {
    DoEntity entity = createEntityFixtureRaw();
    s_dataObjectTestHelper.assertDoEntityEquals(entity, m_expectedEntityFixture, true);
  }
}
