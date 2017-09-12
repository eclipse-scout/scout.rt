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
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class PlannerEventFilterCondition {

  private final int m_type;
  private List<Resource> m_resources;
  private boolean m_checkResources;

  public PlannerEventFilterCondition(int type) {
    m_type = type;
    m_resources = new ArrayList<>();
  }

  public int getType() {
    return m_type;
  }

  public List<Resource> getResources() {
    return CollectionUtility.arrayList(m_resources);
  }

  public void setResources(List<? extends Resource> resources) {
    m_resources = CollectionUtility.arrayList(resources);
    m_checkResources = true;
  }

  public boolean checkResources() {
    return m_checkResources;
  }
}
