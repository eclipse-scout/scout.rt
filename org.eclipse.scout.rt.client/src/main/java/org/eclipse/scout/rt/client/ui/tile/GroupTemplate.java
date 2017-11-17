/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Used by implementations of {@link ITilesAccordionGroupManager}. This is a template to create instances of
 * {@link IGroup} within the {@link AbstractTilesAccordion}.
 */
@ClassId("66075cfe-6ef2-4a0c-9932-097038d16838")
public class GroupTemplate {

  private String m_title;
  private Object m_groupId;
  private boolean m_collapsed;
  private String m_cssClass;
  private boolean m_headerVisible = true;

  public GroupTemplate(Object groupId, String title) {
    m_groupId = groupId;
    m_title = title;
  }

  public String getTitle() {
    return m_title;
  }

  public Object getGroupId() {
    return m_groupId;
  }

  public boolean isCollapsed() {
    return m_collapsed;
  }

  public String getCssClass() {
    return m_cssClass;
  }

  public boolean isHeaderVisible() {
    return m_headerVisible;
  }

  public GroupTemplate withCollapsed(boolean collapsed) {
    m_collapsed = collapsed;
    return this;
  }

  public GroupTemplate withCssClass(String cssClass) {
    m_cssClass = cssClass;
    return this;
  }

  public GroupTemplate withHeaderVisible(boolean headerVisible) {
    m_headerVisible = headerVisible;
    return this;
  }

}
