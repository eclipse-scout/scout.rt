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
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class PlannerEvent extends java.util.EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_RESOURCES_INSERTED = 100;
  public static final int TYPE_RESOURCES_UPDATED = 101;
  public static final int TYPE_RESOURCES_DELETED = 102;
  public static final int TYPE_RESOURCES_SELECTED = 103;
  public static final int TYPE_ACTIVITY_ACTION = 104;
  public static final int TYPE_ALL_RESOURCES_DELETED = 105;

  private int m_type;
  private List<? extends Resource> m_resources = CollectionUtility.emptyArrayList();
  private List<IMenu> m_popupMenus;

  public PlannerEvent(IPlanner source, int type) {
    super(source);
    m_type = type;
  }

  public PlannerEvent(IPlanner source, int type, List<? extends Resource> resources) {
    super(source);
    m_type = type;
    if (CollectionUtility.hasElements(resources)) {
      m_resources = resources;
    }
  }

  public IPlanner getPlanner() {
    return (IPlanner) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public List<? extends Resource> getResources() {
    return CollectionUtility.arrayList(m_resources);
  }

  protected void setResources(List<? extends Resource> resources) {
    m_resources = CollectionUtility.arrayList(resources);
  }

  public int getResourceCount() {
    return m_resources != null ? m_resources.size() : 0;
  }

  /**
   * used by TYPE_NEW_ACTIVITY_POPUP and TYPE_EDIT_ACTIVITY_POPUP to add actions
   */
  public void addPopupMenu(IMenu menu) {
    if (menu != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.add(menu);
    }
  }

  /**
   * used by TYPE_NEW_ACTIVITY_POPUP and TYPE_EDIT_ACTIVITY_POPUP to add actions
   */
  public void addPopupMenus(List<IMenu> menus) {
    if (menus != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.addAll(menus);
    }
  }

  /**
   * used by TYPE_NEW_ACTIVITY_POPUP and TYPE_EDIT_ACTIVITY_POPUP to add actions
   */
  public List<IMenu> getPopupMenus() {
    return CollectionUtility.arrayList(m_popupMenus);
  }

  /**
   * used by TYPE_NEW_ACTIVITY_POPUP and TYPE_EDIT_ACTIVITY_POPUP to add actions
   */
  public int getPopupMenuCount() {
    if (m_popupMenus != null) {
      return m_popupMenus.size();
    }
    else {
      return 0;
    }
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getClass().getSimpleName() + "[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (int i = 0; i < f.length; i++) {
        if (Modifier.isPublic(f[i].getModifiers()) && Modifier.isStatic(f[i].getModifiers()) && f[i].getName().startsWith("TYPE_")) {
          if (((Number) f[i].get(null)).intValue() == m_type) {
            buf.append(f[i].getName());
            break;
          }
        }
      }
    }
    catch (Exception t) {
      buf.append("#" + m_type);
    }
    buf.append(" ");
    if (CollectionUtility.hasElements(m_resources) && getPlanner() != null) {
      buf.append("Resources: " + m_resources);
    }
    else {
      buf.append("{}");
    }
    buf.append("]");
    return buf.toString();
  }
}
