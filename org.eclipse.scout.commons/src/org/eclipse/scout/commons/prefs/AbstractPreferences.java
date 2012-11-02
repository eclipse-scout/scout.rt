/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.prefs;

import org.eclipse.scout.commons.Base64Utility;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Basic empty implementation of {@link Preferences}
 */
public abstract class AbstractPreferences implements Preferences {

  private final String m_name;

  public AbstractPreferences(String name) {
    m_name = name;
  }

  protected abstract void putImpl(String key, String value);

  protected abstract String getImpl(String key);

  protected abstract void removeImpl(String key);

  protected abstract void clearImpl();

  @Override
  public String name() {
    return m_name;
  }

  @Override
  public String[] childrenNames() throws BackingStoreException {
    return new String[0];
  }

  @Override
  public Preferences parent() {
    return null;
  }

  @Override
  public Preferences node(String pathName) {
    return null;
  }

  @Override
  public boolean nodeExists(String pathName) throws BackingStoreException {
    return false;
  }

  @Override
  public void removeNode() throws BackingStoreException {
  }

  @Override
  public String absolutePath() {
    return "/" + name();
  }

  @Override
  public void flush() throws BackingStoreException {
  }

  @Override
  public void sync() throws BackingStoreException {
  }

  @Override
  public void clear() throws BackingStoreException {
    clearImpl();
  }

  @Override
  public void remove(String key) {
    removeImpl(key);
  }

  /*
   * @see org.osgi.service.prefs.Preferences#put(java.lang.String, java.lang.String)
   */
  @Override
  public void put(String key, String newValue) {
    internalPut(key, newValue);
  }

  private static final String TRUE = "true";
  private static final String FALSE = "false";

  /*
   * @see org.osgi.service.prefs.Preferences#putBoolean(java.lang.String, boolean)
   */
  @Override
  public void putBoolean(String key, boolean value) {
    String newValue = value ? TRUE : FALSE;
    internalPut(key, newValue);
  }

  /*
   * @see org.osgi.service.prefs.Preferences#putByteArray(java.lang.String, byte[])
   */
  @Override
  public void putByteArray(String key, byte[] value) {
    String newValue = new String(Base64Utility.encode(value));
    internalPut(key, newValue);
  }

  /*
   * @see org.osgi.service.prefs.Preferences#putDouble(java.lang.String, double)
   */
  @Override
  public void putDouble(String key, double value) {
    String newValue = Double.toString(value);
    internalPut(key, newValue);
  }

  /*
   * @see org.osgi.service.prefs.Preferences#putFloat(java.lang.String, float)
   */
  @Override
  public void putFloat(String key, float value) {
    String newValue = Float.toString(value);
    internalPut(key, newValue);
  }

  /*
   * @see org.osgi.service.prefs.Preferences#putInt(java.lang.String, int)
   */
  @Override
  public void putInt(String key, int value) {
    String newValue = Integer.toString(value);
    internalPut(key, newValue);
  }

  /*
   * @see org.osgi.service.prefs.Preferences#putLong(java.lang.String, long)
   */
  @Override
  public void putLong(String key, long value) {
    String newValue = Long.toString(value);
    internalPut(key, newValue);
  }

  /*
   * @see org.osgi.service.prefs.Preferences#get(java.lang.String, java.lang.String)
   */
  @Override
  public String get(String key, String def) {
    String value = internalGet(key);
    return value != null ? value : def;
  }

  /*
   * @see org.osgi.service.prefs.Preferences#getBoolean(java.lang.String, boolean)
   */
  @Override
  public boolean getBoolean(String key, boolean def) {
    String value = internalGet(key);
    return value != null ? TRUE.equalsIgnoreCase(value) : def;
  }

  /*
   * @see org.osgi.service.prefs.Preferences#getByteArray(java.lang.String, byte[])
   *
   */
  @Override
  public byte[] getByteArray(String key, byte[] def) {
    String value = internalGet(key);
    return value != null ? Base64Utility.decode(value) : def;
  }

  /*
   * @see org.osgi.service.prefs.Preferences#getDouble(java.lang.String, double)
   */
  @Override
  public double getDouble(String key, double def) {
    String value = internalGet(key);
    if (value != null) {
      try {
        return Double.parseDouble(value);
      }
      catch (NumberFormatException e) {
        //nop
      }
    }
    return def;
  }

  /*
   * @see org.osgi.service.prefs.Preferences#getFloat(java.lang.String, float)
   */
  @Override
  public float getFloat(String key, float def) {
    String value = internalGet(key);
    if (value != null) {
      try {
        return Float.parseFloat(value);
      }
      catch (NumberFormatException e) {
        // nop
      }
    }
    return def;
  }

  /*
   * @see org.osgi.service.prefs.Preferences#getInt(java.lang.String, int)
   */
  @Override
  public int getInt(String key, int def) {
    String value = internalGet(key);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      }
      catch (NumberFormatException e) {
        //nop
      }
    }
    return def;
  }

  /*
   * @see org.osgi.service.prefs.Preferences#getLong(java.lang.String, long)
   */
  @Override
  public long getLong(String key, long def) {
    String value = internalGet(key);
    if (value != null) {
      try {
        return Long.parseLong(value);
      }
      catch (NumberFormatException e) {
        //nop
      }
    }
    return def;
  }

  /**
   * @return old value
   */
  protected String internalPut(String key, String newValue) {
    if (key == null) {
      throw new NullPointerException();
    }
    String oldValue = getImpl(key);
    if (newValue == oldValue || (newValue != null && newValue.equals(oldValue))) {
      //not changed
    }
    else {
      putImpl(key, newValue);
    }
    return oldValue;
  }

  /**
   * @return the value for the key or null when the key does not exist
   */
  protected String internalGet(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return getImpl(key);
  }

}
