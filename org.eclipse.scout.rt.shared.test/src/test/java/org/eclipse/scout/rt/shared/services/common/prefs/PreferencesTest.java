/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.prefs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class PreferencesTest {

  @Test
  public void test() throws Exception {
    Preferences prefs = new Preferences("X", null);

    prefs.put("any", "Any");
    prefs.putBoolean("bool", true);
    prefs.putByteArray("byte", new byte[]{(byte) 1, (byte) 2, (byte) 3});
    prefs.putDouble("double", 1.23);
    prefs.putFloat("float", 1.23f);
    prefs.putInt("int", 123);
    prefs.putLong("long", 123L);

    assertEquals(true, prefs.isDirty());
    assertEquals("X", prefs.name());
    assertEquals(7, prefs.keys().size());
    assertEquals("Any", prefs.get("any", null));
    assertEquals("Xyz", prefs.get("xyz", "Xyz"));
    assertEquals(true, prefs.getBoolean("bool", false));
    assertEquals(true, prefs.getBoolean("xyz", true));
    assertArrayEquals(new byte[]{(byte) 1, (byte) 2, (byte) 3}, prefs.getByteArray("byte", null));
    assertArrayEquals(new byte[]{(byte) 9,}, prefs.getByteArray("xyz", new byte[]{(byte) 9,}));
    assertEquals(1.23, prefs.getDouble("double", 0), 0.0);
    assertEquals(9.0, prefs.getDouble("xyz", 9.0), 0.0);
    assertEquals(1.23f, prefs.getFloat("float", 0), 0.0);
    assertEquals(9.0f, prefs.getFloat("xyz", 9.0f), 0.0);
    assertEquals(123, prefs.getInt("int", 0));
    assertEquals(9, prefs.getInt("xyz", 9));
    assertEquals(123L, prefs.getLong("long", 0));
    assertEquals(9L, prefs.getLong("xyz", 9L));

    prefs.put("double", "123");
    prefs.put("float", "123");
    prefs.put("int", "123");
    prefs.put("long", "123");
    assertEquals(123.0, prefs.getDouble("double", 0), 0.0);
    assertEquals(123f, prefs.getFloat("float", 0), 0.0);
    assertEquals(123, prefs.getInt("int", 0));
    assertEquals(123L, prefs.getLong("long", 0));

    TestingUserPreferencesStorageService svc = new TestingUserPreferencesStorageService();

    List<IBean<?>> registerServices = TestingUtility.registerBeans(
        new BeanMetaData(IUserPreferencesStorageService.class).
            initialInstance(svc).
            applicationScoped(true));
    try {
      prefs.flush();
    }
    finally {
      TestingUtility.unregisterBeans(registerServices);
    }
    assertFalse(prefs.isDirty());
    assertTrue(svc.m_flushed);
  }

  private static final class TestingUserPreferencesStorageService extends AbstractService implements IUserPreferencesStorageService {

    private boolean m_flushed = false;

    @Override
    public IPreferences getPreferences(ISession userScope, String nodeId) throws ProcessingException {
      return null;
    }

    @Override
    public void flush(IPreferences prefs) throws ProcessingException {
      m_flushed = true;
    }

  }
}
