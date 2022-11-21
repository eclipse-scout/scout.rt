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
package org.eclipse.scout.rt.dataobject.id;

import static org.junit.Assert.*;

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.Test;

/**
 * Test cases for {@link IdExternalFormatter}.
 */
@SuppressWarnings("deprecation")
public class IdExternalFormatterTest {

  protected static final UUID TEST_UUID = UUID.fromString("5833aae1-c813-4d7c-a342-56a53772a3ea");
  protected static final String TEST_STRING = "foobar";

  @Test
  public void testToExternalForm() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    String ext1 = BEANS.get(IdExternalFormatter.class).toExternalForm(id1);
    assertEquals("scout.FixtureUuId:" + TEST_UUID, ext1);
  }

  @Test
  public void testToExternalFormComposite() {
    FixtureCompositeId id = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    String ext = BEANS.get(IdExternalFormatter.class).toExternalForm(id);
    assertEquals("scout.FixtureCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea", ext);
  }

  @Test
  public void testFromExternalForm() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    IId id2 = BEANS.get(IdExternalFormatter.class).fromExternalForm("scout.FixtureUuId:" + TEST_UUID);
    assertEquals(id1, id2);
  }

  @Test
  public void testFromExternalFormComposite() {
    FixtureCompositeId id1 = IIds.create(FixtureCompositeId.class, TEST_STRING, TEST_UUID);
    IId id2 = BEANS.get(IdExternalFormatter.class).fromExternalForm("scout.FixtureCompositeId:foobar;5833aae1-c813-4d7c-a342-56a53772a3ea");
    assertEquals(id1, id2);
  }

  @Test(expected = PlatformException.class)
  public void testFromExternalForm_InvalidType() {
    IId id = BEANS.get(IdExternalFormatter.class).fromExternalForm("scout.FixtureUuId:Other:" + TEST_UUID);
    assertEquals(id, id);
  }

  @Test(expected = PlatformException.class)
  public void testFromExternalForm_UnknownType() {
    IId id = BEANS.get(IdExternalFormatter.class).fromExternalForm("DoesNotExist:" + TEST_UUID);
    assertEquals(id, id);
  }

  @Test
  public void testFromExternalFormLenient() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    IId id2 = BEANS.get(IdExternalFormatter.class).fromExternalFormLenient("scout.FixtureUuId:" + TEST_UUID);
    assertEquals(id1, id2);
  }

  @Test
  public void testFromExternalFormLenient_UnknownType() {
    IId id = BEANS.get(IdExternalFormatter.class).fromExternalFormLenient("DoesNotExist:" + TEST_UUID);
    assertNull(id);
  }

  @Test
  public void testGetIdClass() {
    assertEquals(FixtureUuId.class, BEANS.get(IdExternalFormatter.class).getIdClass("scout.FixtureUuId"));
    assertNull(BEANS.get(IdExternalFormatter.class).getIdClass("scout.FixtureUuIdUnknown"));
    assertNull(BEANS.get(IdExternalFormatter.class).getIdClass(null));
  }

  @Test
  public void testGetTypeName() {
    assertEquals("scout.FixtureUuId", BEANS.get(IdExternalFormatter.class).getTypeName(FixtureUuId.class));
    assertNull(BEANS.get(IdExternalFormatter.class).getTypeName(FixtureLongId.class));
    assertNull(BEANS.get(IdExternalFormatter.class).getTypeName((Class<? extends IId>) null));
  }
}
