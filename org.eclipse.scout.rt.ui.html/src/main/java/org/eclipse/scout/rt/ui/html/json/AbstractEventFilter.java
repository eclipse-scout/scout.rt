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

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

public abstract class AbstractEventFilter<EVENT extends EventObject, CONDITION> {

  private final List<CONDITION> m_conditions;

  public AbstractEventFilter() {
    m_conditions = new LinkedList<>();
  }

  public abstract EVENT filter(EVENT event);

  public List<CONDITION> getConditions() {
    return CollectionUtility.arrayList(m_conditions);
  }

  public void addCondition(CONDITION event) {
    m_conditions.add(event);
  }

  public void removeCondition(CONDITION event) {
    m_conditions.remove(event);
  }

  /**
   * Removes all conditions from this filter.
   */
  public void removeAllConditions() {
    m_conditions.clear();
  }
}
