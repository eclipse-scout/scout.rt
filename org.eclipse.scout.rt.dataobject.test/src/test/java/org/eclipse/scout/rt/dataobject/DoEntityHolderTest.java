/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityHolder;
import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureDo;
import org.junit.Test;

public class DoEntityHolderTest {

  @Test
  public void testSetGetValue() {
    DoEntityHolder<DoEntity> holder = new DoEntityHolder<>();
    assertNull(holder.getValue());
    DoEntity entity = new DoEntity();
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
