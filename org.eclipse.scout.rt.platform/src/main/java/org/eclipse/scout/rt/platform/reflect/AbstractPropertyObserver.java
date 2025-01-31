/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.reflect;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.LazyValue;

public abstract class AbstractPropertyObserver implements IPropertyObserver {

  private static final LazyValue<Boolean> STORE_CONFIG_VALUES = new LazyValue<>(() -> CONFIG.getPropertyValue(StoreConfigValuesConfigProperty.class));

  @SuppressWarnings("squid:S00116")
  protected final BasicPropertySupport propertySupport = new BasicPropertySupport(this);

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  @Override
  public List<PropertyChangeListener> getPropertyChangeListeners() {
    return propertySupport.getPropertyChangeListeners();
  }

  @Override
  public Map<String, List<PropertyChangeListener>> getSpecificPropertyChangeListeners() {
    return propertySupport.getSpecificPropertyChangeListeners();
  }

  /**
   * @return {@code true} if config values are stored in {@link BasicPropertySupport}, this is the current default.
   * Otherwise, a supplier should be used to return the config value see
   * {@link BasicPropertySupport#getProperty(String, Supplier)}
   */
  protected boolean isStoreConfigValues() {
    return STORE_CONFIG_VALUES.get();
  }

  public static class StoreConfigValuesConfigProperty extends AbstractBooleanConfigProperty {

    public static final String KEY = "scout.propertySupport.storeConfigValues";

    @Override
    public String getKey() {
      return KEY;
    }

    @Override
    public Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String description() {
      return "Defines if config values should be stored using BasicPropertySupport, "
          + "this is the current default and may be changed > 22.0 in order to reduce global memory consumption."
          + "Default: true";
    }
  }
}
