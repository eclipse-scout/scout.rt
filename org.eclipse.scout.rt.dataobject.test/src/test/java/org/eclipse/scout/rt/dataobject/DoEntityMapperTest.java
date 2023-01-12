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

import org.eclipse.scout.rt.dataobject.fixture.EntityMapperFixture;
import org.eclipse.scout.rt.dataobject.fixture.EntityMapperFixtureDo;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for testing a simple implementation of {@link AbstractDoEntityMapper}.
 *
 * @see DoEntityMappingsTest for a more detailed test of mapping functionality
 */
@RunWith(PlatformTestRunner.class)
public class DoEntityMapperTest {

  @Test
  public void testFromDo() {
    EntityMapperFixtureDo source = BEANS.get(EntityMapperFixtureDo.class).withId("test");
    EntityMapperFixture target = BEANS.get(EntityMapperFixture.class);

    BEANS.get(EntityMapperFixtureDoEntityMapper.class).fromDo(source, target);
    assertEquals(source.getId(), target.getId());
  }

  @Test
  public void testToDo() {
    EntityMapperFixture source = BEANS.get(EntityMapperFixture.class);
    source.getIdHolder().setValue("test");
    EntityMapperFixtureDo target = BEANS.get(EntityMapperFixtureDo.class);

    BEANS.get(EntityMapperFixtureDoEntityMapper.class).toDo(source, target);
    assertEquals(source.getId(), target.getId());
  }

  @ApplicationScoped
  public static class EntityMapperFixtureDoEntityMapper extends AbstractDoEntityMapper<EntityMapperFixtureDo, EntityMapperFixture> {

    @Override
    protected void initMappings(DoEntityMappings<EntityMapperFixtureDo, EntityMapperFixture> mappings) {
      mappings
          .withHolder(EntityMapperFixtureDo::id, EntityMapperFixture::getIdHolder);
    }
  }
}
