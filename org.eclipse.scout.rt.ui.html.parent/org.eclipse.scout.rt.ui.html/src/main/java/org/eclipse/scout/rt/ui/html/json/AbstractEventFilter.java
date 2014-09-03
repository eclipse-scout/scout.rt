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
package org.eclipse.scout.rt.ui.html.json;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;

public abstract class AbstractEventFilter<EVENT extends EventObject, CONDITION> {
  private List<CONDITION> m_ignorableModelEvents;

  public AbstractEventFilter() {
    m_ignorableModelEvents = new LinkedList<>();
  }

  public abstract EVENT filterIgnorableModelEvent(EVENT event);

  public List<CONDITION> getIgnorableModelEvents() {
    return CollectionUtility.arrayList(m_ignorableModelEvents);
  }

  public void addIgnorableModelEvent(CONDITION event) {
    m_ignorableModelEvents.add(event);
  }

  public void removeIgnorableModelEvent(CONDITION event) {
    m_ignorableModelEvents.remove(event);
  }
}
