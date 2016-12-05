/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.EventListenerList;

public abstract class AbstractConfigProperty<DATA_TYPE, RAW_TYPE> implements IConfigProperty<DATA_TYPE> {

  private DATA_TYPE m_value;
  private boolean m_valueInitialized;
  private PlatformException m_error;
  private final EventListenerList m_listeners = new EventListenerList();

  protected DATA_TYPE getDefaultValue() {
    return null;
  }

  protected abstract DATA_TYPE parse(RAW_TYPE value);

  @Override
  public DATA_TYPE getValue(String namespace) {
    @SuppressWarnings("unchecked")
    RAW_TYPE value = (RAW_TYPE) ConfigUtility.getProperty(getKey(), null, namespace);
    if (value == null) {
      return getDefaultValue();
    }
    return parse(value);
  }

  @Override
  public DATA_TYPE getValue() {
    if (!m_valueInitialized) {
      initValue();
    }

    if (m_error != null) {
      throw m_error;
    }
    return m_value;
  }

  protected void initValue() {
    DATA_TYPE oldValue = m_value;
    try {
      m_value = getValue(null);
    }
    catch (PlatformException t) {
      m_error = t;
    }
    catch (Exception e) {
      m_error = new PlatformException(e.getMessage(), e);
    }
    finally {
      m_valueInitialized = true;
    }

    fireConfigChangedEvent(new ConfigPropertyChangeEvent(this, oldValue, m_value, ConfigPropertyChangeEvent.TYPE_VALUE_INITIALIZED));
  }

  @Override
  public void invalidate() {
    DATA_TYPE oldValue = m_value;
    m_value = null;
    m_valueInitialized = false;
    m_error = null;

    fireConfigChangedEvent(new ConfigPropertyChangeEvent(this, oldValue, m_value, ConfigPropertyChangeEvent.TYPE_INVALIDATE));
  }

  @Override
  public void setValue(DATA_TYPE newValue) {
    DATA_TYPE oldValue = m_value;
    m_value = newValue;
    m_valueInitialized = true;
    m_error = null;

    fireConfigChangedEvent(new ConfigPropertyChangeEvent(this, oldValue, m_value, ConfigPropertyChangeEvent.TYPE_VALUE_CHANGED));
  }

  protected void fireConfigChangedEvent(ConfigPropertyChangeEvent e) {
    IConfigChangedListener[] listeners = m_listeners.getListeners(IConfigChangedListener.class);
    if (listeners == null || listeners.length < 1) {
      return;
    }

    for (int i = 0; i < listeners.length; i++) {
      listeners[i].configPropertyChanged(e);
    }
  }

  public PlatformException getError() {
    return m_error;
  }

  @Override
  public void addListener(IConfigChangedListener listener) {
    m_listeners.add(IConfigChangedListener.class, listener);
  }

  @Override
  public void removeListener(IConfigChangedListener listener) {
    m_listeners.remove(IConfigChangedListener.class, listener);
  }
}
