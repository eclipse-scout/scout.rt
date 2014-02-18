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
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

@SuppressWarnings("serial")
public class ActivityMapEvent extends java.util.EventObject {
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
   * lastActivity, resourceId, column
   */
  public static final int TYPE_CELL_ACTION = 104;
  /**
   * All activities have been deleted valid properties: activities,
   * firstActivity, lastActivity
   */
  public static final int TYPE_ALL_ACTIVITIES_DELETED = 105;
  /**
   * Broadcast request to add actions for activities popup valid properties:
   * activities, firstActivity, lastActivity add actions to: popupActions
   */
  public static final int TYPE_EDIT_ACTIVITY_POPUP = 700;
  /**
   * Broadcast request to add actions for "new" popup valid properties: add
   * actions to: popupActions
   */
  public static final int TYPE_NEW_ACTIVITY_POPUP = 701;

  private int m_type;
  private List<? extends ActivityCell> m_activities = Collections.emptyList();
  private List<IMenu> m_popupMenus;
  private Object m_resourceId;
  private MinorTimeColumn m_column;

  public ActivityMapEvent(IActivityMap source, int type) {
    super(source);
    m_type = type;
  }

  public ActivityMapEvent(IActivityMap source, int type, ActivityCell activity) {
    super(source);
    m_type = type;
    if (activity != null) {
      ArrayList<ActivityCell> list = new ArrayList<ActivityCell>();
      list.add(activity);
      m_activities = list;
    }
  }

  public ActivityMapEvent(IActivityMap source, int type, List<? extends ActivityCell> activities) {
    super(source);
    m_type = type;
    if (CollectionUtility.hasElements(activities)) {
      m_activities = activities;
    }
  }

  public ActivityMapEvent(IActivityMap source, int type, Object resourceId, MinorTimeColumn column, ActivityCell activity) {
    super(source);
    m_type = type;
    if (activity != null) {
      ArrayList<ActivityCell> list = new ArrayList<ActivityCell>();
      list.add(activity);
      m_activities = list;
    }
    m_resourceId = resourceId;
    m_column = column;
  }

  public IActivityMap getActivityMap() {
    return (IActivityMap) getSource();
  }

  public int getType() {
    return m_type;
  }

  public Object getResourceId() {
    return m_resourceId;
  }

  public MinorTimeColumn getColumn() {
    return m_column;
  }

  public List<ActivityCell> getActivities() {
    return Collections.unmodifiableList(m_activities);
  }

  protected void setActivities(List<? extends ActivityCell> activities) {
    m_activities = activities;
  }

  public int getActivityCount() {
    return m_activities != null ? m_activities.size() : 0;
  }

  public ActivityCell getFirstActivity() {
    return CollectionUtility.firstElement(m_activities);
  }

  public ActivityCell getLastActivity() {
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
    if (m_popupMenus != null) {
      return Collections.unmodifiableList(m_popupMenus);
    }
    else {
      return Collections.emptyList();
    }
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
    catch (Throwable t) {
      buf.append("#" + m_type);
    }
    buf.append(" ");
    // activities
    if (CollectionUtility.hasElements(m_activities) && getActivityMap() != null) {
      if (m_activities.size() == 1) {
        buf.append("row " + CollectionUtility.firstElement(m_activities));
      }
      else {
        Iterator<? extends ActivityCell> actIt = m_activities.iterator();
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
