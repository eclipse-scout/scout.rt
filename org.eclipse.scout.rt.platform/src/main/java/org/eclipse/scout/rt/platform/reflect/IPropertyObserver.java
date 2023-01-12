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

public interface IPropertyObserver {

  void addPropertyChangeListener(PropertyChangeListener listener);

  void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

  List<PropertyChangeListener> getPropertyChangeListeners();

  Map<String, List<PropertyChangeListener>> getSpecificPropertyChangeListeners();
}
