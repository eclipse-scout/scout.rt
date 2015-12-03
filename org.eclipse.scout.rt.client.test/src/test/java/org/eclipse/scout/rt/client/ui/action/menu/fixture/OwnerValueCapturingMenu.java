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
package org.eclipse.scout.rt.client.ui.action.menu.fixture;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;

public class OwnerValueCapturingMenu extends AbstractMenu {
  private int m_count = 0;
  private Object m_lastOwnerValue;

  public OwnerValueCapturingMenu(IMenuType... types) {
    final Set<IMenuType> s = new HashSet<>();
    for (IMenuType t : types) {
      s.add(t);
    }
    setMenuTypes(s);
  }

  @Override
  protected void execOwnerValueChanged(Object ownerValue) {
    m_count++;
    m_lastOwnerValue = ownerValue;
  }

  public int getCount() {
    return m_count;
  }

  public Object getLastOwnerValue() {
    return m_lastOwnerValue;
  }
}
