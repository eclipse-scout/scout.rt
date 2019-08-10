/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
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

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuIdWithCustomFromString;
import org.eclipse.scout.rt.dataobject.id.AbstractId;
import org.eclipse.scout.rt.dataobject.id.AbstractUuId;
import org.eclipse.scout.rt.dataobject.id.IdFactory;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.Test;

/**
 * Test cases for {@link IdFactory}.
 */
public class IdFactoryTest {

  /**
   * Missing of()
   */
  static final class IllegalUuId1 extends AbstractUuId {
    private static final long serialVersionUID = 1L;

    private IllegalUuId1(UUID id) {
      super(id);
    }
  }

  /**
   * Wrong of() signature
   */
  static final class IllegalUuId2 extends AbstractUuId {
    private static final long serialVersionUID = 1L;

    private IllegalUuId2(UUID id) {
      super(id);
    }

    public static IllegalUuId2 of(UUID id, String anotherParam) {
      return new IllegalUuId2(id);
    }
  }

  /**
   * Wrong of() signature
   */
  static final class IllegalUuId3 extends AbstractUuId {
    private static final long serialVersionUID = 1L;

    private IllegalUuId3(UUID id) {
      super(id);
    }

    public static IllegalUuId3 of(UUID id) {
      return new IllegalUuId3(id);
    }

    public static IllegalUuId3 of(String id) {
      return new IllegalUuId3(UUID.fromString(id));
    }
  }

  /**
   * Missing fromString()
   */
  static final class IllegalUuId4 extends AbstractId<Long> {
    private static final long serialVersionUID = 1L;

    private IllegalUuId4(Long id) {
      super(id);
    }

    public static IllegalUuId4 of(String id) {
      return new IllegalUuId4(Long.parseLong(id));
    }
  }

  @Test
  public void testCreateUuIdFromString() {
    final String goodUuuid = "a7a44470-7bb5-4d9a-884a-c1f8f7714412";
    final String badUuid = "a7a44470.7bb5.4d9a.884a.c1f8f7714412";

    FixtureUuId id1 = BEANS.get(IdFactory.class).createFromString(FixtureUuId.class, goodUuuid);
    assertEquals(goodUuuid, id1.unwrapAsString());

    ScoutAssert.assertThrows(PlatformException.class, () -> BEANS.get(IdFactory.class).createFromString(FixtureUuId.class, badUuid));

    FixtureUuIdWithCustomFromString id2 = BEANS.get(IdFactory.class).createFromString(FixtureUuIdWithCustomFromString.class, badUuid);
    assertEquals(goodUuuid, id2.unwrapAsString());
  }

  @Test
  public void testCreateStringId() {
    String str = "aaabbbccc";
    FixtureStringId id = BEANS.get(IdFactory.class).createInternal(FixtureStringId.class, str);
    assertEquals(str, id.unwrap());
  }

  @Test(expected = PlatformException.class)
  public void testCreateInvalidType4() {
    BEANS.get(IdFactory.class).createFromString(IllegalUuId4.class, "123456789");
  }
}
