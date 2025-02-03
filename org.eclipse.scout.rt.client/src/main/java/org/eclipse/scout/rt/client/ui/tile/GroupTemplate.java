/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Used by implementations of {@link ITileAccordionGroupManager}. This is a template to create instances of
 * {@link IGroup} within the {@link AbstractTileAccordion}.
 */
@ClassId("66075cfe-6ef2-4a0c-9932-097038d16838")
public class GroupTemplate {

  private String m_title;
  private Object m_groupId;
  private String m_iconId;
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

  public String getIconId() {
    return m_iconId;
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

  public GroupTemplate withIconId(String iconId) {
    m_iconId = iconId;
    return this;
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
