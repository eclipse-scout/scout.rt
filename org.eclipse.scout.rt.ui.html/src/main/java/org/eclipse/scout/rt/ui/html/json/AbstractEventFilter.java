/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

public abstract class AbstractEventFilter<EVENT extends EventObject, CONDITION> {

  private List<CONDITION> m_conditions;

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
