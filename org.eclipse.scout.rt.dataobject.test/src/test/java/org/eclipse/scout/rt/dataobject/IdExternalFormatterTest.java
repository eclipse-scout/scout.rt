/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
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

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.IdExternalFormatter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.junit.Test;

/**
 * Test cases for {@link IdExternalFormatter}.
 */
public class IdExternalFormatterTest {

  protected static final UUID TEST_UUID = UUID.randomUUID();

  @Test
  public void testToExternalForm() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    String ext1 = BEANS.get(IdExternalFormatter.class).toExternalForm(id1);
    assertEquals("FixtureUuId:" + TEST_UUID.toString(), ext1);
  }

  @Test
  public void testFromExternalForm() {
    FixtureUuId id1 = IIds.create(FixtureUuId.class, TEST_UUID);
    FixtureUuId id2 = BEANS.get(IdExternalFormatter.class).fromExternalForm("FixtureUuId:" + TEST_UUID.toString());
    assertEquals(id1, id2);
  }

  @Test(expected = ClassCastException.class)
  public void testFromExternalForm_InvalidType() {
    FixtureStringId id = BEANS.get(IdExternalFormatter.class).fromExternalForm("FixtureUuId:" + TEST_UUID.toString());
    assertEquals(id, id);
  }

  @Test(expected = ProcessingException.class)
  public void testFromExternalForm_UnknownType() {
    FixtureUuId id = BEANS.get(IdExternalFormatter.class).fromExternalForm("DoesNotExist:" + TEST_UUID.toString());
    assertEquals(id, id);
  }
}
