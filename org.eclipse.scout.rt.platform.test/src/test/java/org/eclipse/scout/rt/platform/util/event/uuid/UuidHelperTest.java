/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.event.uuid;

import static org.junit.Assert.*;

import java.util.UUID;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.uuid.UuidHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class UuidHelperTest {

  protected static final UUID TEST_UUID = UUID.fromString("0932d205-5828-4830-8007-725295a1c5e1");
  protected static final byte[] BYTES = new byte[]{9, 50, -46, 5, 88, 40, 72, 48, -128, 7, 114, 82, -107, -95, -59, -31};

  protected static UuidHelper s_helper;

  @BeforeClass
  public static void beforeClass() {
    s_helper = BEANS.get(UuidHelper.class);
  }

  @Test
  public void testToByteArray() {
    assertArrayEquals(BYTES, s_helper.toByteArray(TEST_UUID));
  }

  @Test
  public void testFromByteArray() {
    assertEquals(TEST_UUID, s_helper.fromByteArray(BYTES));
    Assert.assertThrows(AssertionException.class, () -> s_helper.fromByteArray(new byte[]{1, 2, 3}));
  }

  @Test
  public void testEncodeDecode() {
    UUID uuid = UUID.randomUUID();
    String encoded = s_helper.encodeUrlSafe(uuid);
    UUID decoded = s_helper.decodeUrlSafe(encoded);
    assertEquals(uuid, decoded);
  }
}
