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
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public class PlannerEvent extends java.util.EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;
  /**
   * Some activities have been added valid properties: activities,
   * firstActivity, lastActivity
   */
  public static final int TYPE_ACTIVITIES_INSERTED = 100;
  /**
   * Some activities have been updated valid properties: activities,
   * firstActivity, lastActivity
   */
  public static final int TYPE_ACTIVITIES_UPDATED = 101;
  /**
   * Some activities have been deleted valid properties: activities,
   * firstActivity, lastActivity
   */
  public static final int TYPE_ACTIVITIES_DELETED = 102;
  /**
   * A row has been activated valid properties: activities, firstActivity,
   * lastActivity, resource, column
   */
  public static final int TYPE_CELL_ACTION = 104;
  /**
   * All activities have been deleted valid properties: activities,
   * firstActivity, lastActivity
   */
  public static final int TYPE_ALL_ACTIVITIES_DELETED = 105;

  private int m_type;
  private List<? extends Activity> m_activities = CollectionUtility.emptyArrayList();
  private List<IMenu> m_popupMenus;
  private Object m_resource;

  public PlannerEvent(IPlanner source, int type) {
    super(source);
    m_type = type;
  }

  public PlannerEvent(IPlanner source, int type, Activity activity) {
    super(source);
    m_type = type;
    if (activity != null) {
      ArrayList<Activity> list = new ArrayList<Activity>();
      list.add(activity);
      m_activities = list;
    }
  }

  public PlannerEvent(IPlanner source, int type, List<? extends Activity> activities) {
    super(source);
    m_type = type;
    if (CollectionUtility.hasElements(activities)) {
      m_activities = activities;
    }
  }

  public PlannerEvent(IPlanner source, int type, Object resource, Activity activity) {
    super(source);
    m_type = type;
    if (activity != null) {
      ArrayList<Activity> list = new ArrayList<Activity>();
      list.add(activity);
      m_activities = list;
    }
    m_resource = resource;
  }

  public IPlanner getPlanner() {
    return (IPlanner) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public Object getResourceId() {
    return m_resource;
  }

  public List<Activity> getActivities() {
    return CollectionUtility.arrayList(m_activities);
  }

  protected void setActivities(List<? extends Activity> activities) {
    m_activities = CollectionUtility.arrayList(activities);
  }

  public int getActivityCount() {
    return m_activities != null ? m_activities.size() : 0;
  }

  public Activity getFirstActivity() {
    return CollectionUtility.firstElement(m_activities);
  }

  public Activity getLastActivity() {
    return CollectionUtility.lastElement(m_activities);
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
    // activities
    if (CollectionUtility.hasElements(m_activities) && getPlanner() != null) {
      if (m_activities.size() == 1) {
        buf.append("row " + CollectionUtility.firstElement(m_activities));
      }
      else {
        Iterator<? extends Activity> actIt = m_activities.iterator();
        buf.append("" + actIt.next());
        while (actIt.hasNext()) {
          buf.append(",").append("" + actIt.next());
        }
      }
    }
    else {
      buf.append("{}");
    }
    buf.append("]");
    return buf.toString();
  }
}
