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

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class DoEntityHolderTest {

  @Test
  public void testSetGetValue() {
    DoEntityHolder<IDoEntity> holder = new DoEntityHolder<>();
    assertNull(holder.getValue());
    IDoEntity entity = BEANS.get(DoEntity.class);
    entity.put("foo", "bar");
    holder.setValue(entity);
    assertEquals(entity, holder.getValue());
  }

  static class P_FixtureDoEntityHolder extends DoEntityHolder<EntityFixtureDo> {
    private static final long serialVersionUID = 1L;
  }

  @Test
  public void testGetHolderType() {
    DoEntityHolder<DoEntity> holder = new DoEntityHolder<>(DoEntity.class);
    assertEquals(DoEntity.class, holder.getHolderType());

    DoEntityHolder<EntityFixtureDo> holder2 = new P_FixtureDoEntityHolder();
    assertEquals(EntityFixtureDo.class, holder2.getHolderType());

    DoEntityHolder<EntityFixtureDo> holder3 = new P_FixtureDoEntityHolder();
    holder3.setValue(new EntityFixtureDo());
    assertEquals(EntityFixtureDo.class, holder3.getHolderType());
  }
}
