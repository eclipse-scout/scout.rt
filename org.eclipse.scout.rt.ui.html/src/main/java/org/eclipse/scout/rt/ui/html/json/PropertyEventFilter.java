/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.beans.PropertyChangeEvent;

public class PropertyEventFilter extends AbstractEventFilter<PropertyChangeEvent, IPropertyChangeEventFilterCondition> {

  @Override
  public PropertyChangeEvent filter(PropertyChangeEvent event) {
    for (IPropertyChangeEventFilterCondition condition : getConditions()) {
      if (condition.getPropertyName().equals(event.getPropertyName()) && !condition.test(event)) {
        return null;
      }
    }
    return event;
  }
}
