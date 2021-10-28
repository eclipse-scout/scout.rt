/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
