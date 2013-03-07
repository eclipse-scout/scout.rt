/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.prefs;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Test the behaviour of a default implementation of {@link AbstractPreferences}
 */
public class PreferencesTest {

  @Test
  public void test() throws BackingStoreException {
    Prefs prefs = new Prefs("X");
    prefs.put("any", "Any");
    prefs.putBoolean("bool", true);
    prefs.putByteArray("byte", new byte[]{(byte) 1, (byte) 2, (byte) 3});
    prefs.putDouble("double", 1.23);
    prefs.putFloat("float", 1.23f);
    prefs.putInt("int", 123);
    prefs.putLong("long", 123L);
    //
    Assert.assertEquals(true, prefs.isDirty());
    Assert.assertEquals("X", prefs.name());
    Assert.assertEquals(7, prefs.keys().length);
    Assert.assertEquals("Any", prefs.get("any", null));
    Assert.assertEquals("Xyz", prefs.get("xyz", "Xyz"));
    Assert.assertEquals(true, prefs.getBoolean("bool", false));
    Assert.assertEquals(true, prefs.getBoolean("xyz", true));
    Assert.assertArrayEquals(new byte[]{(byte) 1, (byte) 2, (byte) 3}, prefs.getByteArray("byte", null));
    Assert.assertArrayEquals(new byte[]{(byte) 9,}, prefs.getByteArray("xyz", new byte[]{(byte) 9,}));
    Assert.assertEquals(1.23, prefs.getDouble("double", 0), 0.0);
    Assert.assertEquals(9.0, prefs.getDouble("xyz", 9.0), 0.0);
    Assert.assertEquals(1.23f, prefs.getFloat("float", 0), 0.0);
    Assert.assertEquals(9.0f, prefs.getFloat("xyz", 9.0f), 0.0);
    Assert.assertEquals(123, prefs.getInt("int", 0));
    Assert.assertEquals(9, prefs.getInt("xyz", 9));
    Assert.assertEquals(123L, prefs.getLong("long", 0));
    Assert.assertEquals(9L, prefs.getLong("xyz", 9L));
    //
    prefs.put("double", "123");
    prefs.put("float", "123");
    prefs.put("int", "123");
    prefs.put("long", "123");
    Assert.assertEquals(123.0, prefs.getDouble("double", 0), 0.0);
    Assert.assertEquals(123f, prefs.getFloat("float", 0), 0.0);
    Assert.assertEquals(123, prefs.getInt("int", 0));
    Assert.assertEquals(123L, prefs.getLong("long", 0));
    //
    prefs.flush();
    Assert.assertEquals(false, prefs.isDirty());
  }

  private static class Prefs extends AbstractPreferences {
    private Properties p = new Properties();
    private boolean dirty;

    public Prefs(String name) {
      super(name);
    }

    public boolean isDirty() {
      return dirty;
    }

    @Override
    public String[] keys() throws BackingStoreException {
      return p.keySet().toArray(new String[p.size()]);
    }

    @Override
    protected void putImpl(String key, String value) {
      dirty = true;
      p.setProperty(key, value);
    }

    @Override
    protected String getImpl(String key) {
      return p.getProperty(key);
    }

    @Override
    protected void removeImpl(String key) {
      dirty = true;
      p.remove(key);
    }

    @Override
    protected void clearImpl() {
      dirty = true;
      p.clear();
    }

    @Override
    public void flush() throws BackingStoreException {
      dirty = false;
    }
  }
}
