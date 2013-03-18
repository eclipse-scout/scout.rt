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
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

@SuppressWarnings("serial")
public class ActivityMapEvent<RI> extends java.util.EventObject {
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
  private ActivityCell[] m_activities = new ActivityCell[0];
  private List<IMenu> m_popupMenus;
  private RI m_resourceId;
  private MinorTimeColumn m_column;

  public ActivityMapEvent(IActivityMap source, int type) {
    super(source);
    m_type = type;
  }

  public ActivityMapEvent(IActivityMap source, int type, ActivityCell activity) {
    super(source);
    m_type = type;
    if (activity != null) {
      m_activities = new ActivityCell[]{activity};
    }
  }

  public ActivityMapEvent(IActivityMap source, int type, ActivityCell[] activities) {
    super(source);
    m_type = type;
    if (activities != null && activities.length > 0) {
      m_activities = activities;
    }
  }

  public ActivityMapEvent(IActivityMap source, int type, RI resourceId, MinorTimeColumn column, ActivityCell activity) {
    super(source);
    m_type = type;
    if (activity != null) {
      m_activities = new ActivityCell[]{activity};
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

  public RI getResourceId() {
    return m_resourceId;
  }

  public MinorTimeColumn getColumn() {
    return m_column;
  }

  public ActivityCell[] getActivities() {
    return m_activities;
  }

  protected void setActivities(ActivityCell[] activities) {
    m_activities = activities;
  }

  public int getActivityCount() {
    return m_activities != null ? m_activities.length : 0;
  }

  public ActivityCell getFirstActivity() {
    return m_activities.length > 0 ? m_activities[0] : null;
  }

  public ActivityCell getLastActivity() {
    return m_activities.length > 0 ? m_activities[m_activities.length - 1] : null;
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
  public void addPopupMenus(IMenu[] menus) {
    if (menus != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.addAll(Arrays.asList(menus));
    }
  }

  /**
   * used by TYPE_NEW_ACTIVITY_POPUP and TYPE_EDIT_ACTIVITY_POPUP to add actions
   */
  public IMenu[] getPopupMenus() {
    if (m_popupMenus != null) {
      return m_popupMenus.toArray(new IMenu[0]);
    }
    else {
      return new IMenu[0];
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
    if (m_activities != null && m_activities.length > 0 && getActivityMap() != null) {
      if (m_activities.length == 1) {
        buf.append("row " + m_activities[0]);
      }
      else {
        buf.append("activities {");
        for (int i = 0; i < m_activities.length; i++) {
          if (i >= 0) {
            buf.append(",");
          }
          buf.append("" + m_activities[i]);
        }
        buf.append("}");
      }
    }
    else {
      buf.append("{}");
    }
    buf.append("]");
    return buf.toString();
  }
}
