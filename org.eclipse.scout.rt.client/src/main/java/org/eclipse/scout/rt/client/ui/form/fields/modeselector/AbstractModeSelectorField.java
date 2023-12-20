/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.modeselector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.mode.IMode;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

@ClassId("0b8931d2-e694-437b-bf31-94ad5a44ad71")
public abstract class AbstractModeSelectorField<T> extends AbstractValueField<T> implements IModeSelectorField<T> {

  private boolean m_valueAndSelectionMediatorActive;

  public AbstractModeSelectorField() {
    this(true);
  }

  public AbstractModeSelectorField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    List<Class<? extends IMode<T>>> configuredModes = getConfiguredModes();
    OrderedCollection<IMode<T>> modes = new OrderedCollection<>();
    for (Class<? extends IMode<T>> modeClazz : configuredModes) {
      IMode<T> mode = ConfigurationUtility.newInnerInstance(this, modeClazz);
      modes.addOrdered(mode);
    }
    injectModesInternal(modes);
    for (IMode<T> mode : modes) {
      initMode(mode);
    }
    setModesInternal(modes.getOrderedList());
  }

  protected void injectModesInternal(OrderedCollection<IMode<T>> modes) {
  }

  protected void initMode(IMode<T> mode) {
    mode.init();
    mode.addPropertyChangeListener(new P_ModePropertyChangeListener());
  }

  @SuppressWarnings("unchecked")
  protected List<Class<? extends IMode<T>>> getConfiguredModes() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMode>> fca = ConfigurationUtility.filterClasses(dca, IMode.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  protected void setModesInternal(List<IMode<T>> modes) {
    propertySupport.setPropertyList(PROP_MODES, modes);
  }

  public List<IMode<T>> getModesInternal() {
    return propertySupport.getPropertyList(PROP_MODES);
  }

  @Override
  public List<IMode<T>> getModes() {
    return CollectionUtility.arrayList(getModesInternal());
  }

  @Override
  public IMode<T> getModeFor(T value) {
    for (IMode<T> b : getModes()) {
      T modeValue = b.getRef();
      if (ObjectUtility.equals(modeValue, value)) {
        return b;
      }
    }
    return null;
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getModes());
  }

  @Override
  protected void valueChangedInternal() {
    super.valueChangedInternal();
    syncValueToModes();
  }

  @Override
  protected T validateValueInternal(T rawValue) {
    T validValue;
    if (rawValue == null) {
      validValue = null;
    }
    else {
      IMode<T> m = getModeFor(rawValue);
      if (m != null) {
        validValue = rawValue;
      }
      else {
        throw new ProcessingException("Illegal mode value: " + rawValue);
      }
    }
    return validValue;
  }

  private void syncValueToModes() {
    if (m_valueAndSelectionMediatorActive) {
      return;
    }
    try {
      m_valueAndSelectionMediatorActive = true;
      T selectedKey = getValue();
      IMode<T> selectedMode = getModeFor(selectedKey);
      for (IMode<T> m : getModes()) {
        m.setSelected(m == selectedMode);
      }
    }
    finally {
      m_valueAndSelectionMediatorActive = false;
    }
  }

  private void syncModesToValue(IMode<T> selectedMode) {
    if (m_valueAndSelectionMediatorActive) {
      return;
    }
    try {
      m_valueAndSelectionMediatorActive = true;
      for (IMode<T> b : getModes()) {
        b.setSelected(b == selectedMode);
      }
      setValue(selectedMode.getRef());
    }
    finally {
      m_valueAndSelectionMediatorActive = false;
    }
  }

  private final class P_ModePropertyChangeListener implements PropertyChangeListener {

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IAction.PROP_SELECTED) && (Boolean) e.getNewValue()) {
        syncModesToValue(((IMode<T>) e.getSource()));
      }
    }
  }
}
