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
package org.eclipse.scout.rt.ui.swing.basic.activitymap;

import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityMapEvent;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityMapListener;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.ActivityMapSelectionEvent;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.ActivityMapSelectionListener;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.JActivityMap;

public class SwingScoutActivityMap extends SwingScoutComposite<IActivityMap<?, ?>> {
  private JScrollPane m_swingScrollPane;
  /**
   * The metrics table is used to determine header and row heights
   */
  private JTable m_metricsTable;
  private ActivityMapListener m_scoutListener;

  public SwingScoutActivityMap(JTable metricsTable) {
    // the master table is defining our header height and row heights
    m_metricsTable = metricsTable;
    //keep empty
  }

  @Override
  protected void initializeSwing() {
    final JActivityMap activityMap = new JActivityMap();
    m_swingScrollPane = new JScrollPaneEx(activityMap, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    m_swingScrollPane.setOpaque(false);
    // swing properties
    // listeners
    activityMap.addActivityMapSelectionListener(
        new ActivityMapSelectionListener() {
          @Override
          public void selectionChanged(ActivityMapSelectionEvent e) {
            setSelectionFromSwing(e.getRows(), e.getRange());
          }
        }
        );
    activityMap.addActivityProxyMouseListener(
        new MouseAdapter() {
          MouseClickedBugFix fix;

          @Override
          public void mousePressed(MouseEvent e) {
            fix = new MouseClickedBugFix(e);
            if (e.isPopupTrigger()) {
              if (!activityMap.isInsideSelection(e)) {
                handleSwingEditActivityPopup(e);
              }
            }
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
              if (!activityMap.isInsideSelection(e)) {
                handleSwingEditActivityPopup(e);
              }
            }
            if (fix != null) {
              fix.mouseReleased(this, e);
            }
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            if (fix.mouseClicked()) {
              return;
            }
            if (e.getClickCount() == 2) {
              if (!e.isMetaDown()) {
                handleCellActionFromSwing(e);
              }
            }
          }
        }
        );
    activityMap.addActivityProxyFocusListener(
        new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent e) {
            if (e.isTemporary()) {
              return;
            }
            setSelectedActivityCellFromSwing((SwingActivityComponent) e.getComponent());
          }

          @Override
          public void focusLost(FocusEvent e) {
            if (e.isTemporary()) {
              return;
            }
            setSelectedActivityCellFromSwing(null);
          }
        }
        );
    activityMap.getSelector().addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
              if (activityMap.isInsideSelection(e)) {
                handleSwingNewActivityPopup(e);
              }
            }
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
              if (activityMap.isInsideSelection(e)) {
                handleSwingNewActivityPopup(e);
              }
            }
          }
        }
        );
    activityMap.addMouseListener(
        new MouseAdapter() {
          MouseClickedBugFix fix;

          @Override
          public void mousePressed(MouseEvent e) {
            fix = new MouseClickedBugFix(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            if (fix != null) {
              fix.mouseReleased(this, e);
            }
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            if (fix.mouseClicked()) {
              return;
            }
            if (e.getClickCount() == 2) {
              if (!e.isMetaDown()) {
                handleCellActionFromSwing(e);
              }
            }
          }
        }
        );
    //
    setSwingField(activityMap);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutListener == null) {
      m_scoutListener = new ActivityMapListener() {
        @Override
        public void activityMapChanged(final ActivityMapEvent e) {
          if (isIgnoredScoutEvent(ActivityMapEvent.class, "" + e.getType())) {
            return;
          }
          //
          switch (e.getType()) {
            case ActivityMapEvent.TYPE_ACTIVITIES_DELETED:
            case ActivityMapEvent.TYPE_ACTIVITIES_INSERTED:
            case ActivityMapEvent.TYPE_ACTIVITIES_UPDATED:
            case ActivityMapEvent.TYPE_ALL_ACTIVITIES_DELETED: {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  switch (e.getType()) {
                    case ActivityMapEvent.TYPE_ACTIVITIES_DELETED:
                    case ActivityMapEvent.TYPE_ACTIVITIES_INSERTED:
                    case ActivityMapEvent.TYPE_ACTIVITIES_UPDATED:
                    case ActivityMapEvent.TYPE_ALL_ACTIVITIES_DELETED: {
                      try {
                        getUpdateSwingFromScoutLock().acquire();
                        //
                        handleActivitiesChangedFromScout();
                      }
                      finally {
                        getUpdateSwingFromScoutLock().release();
                      }
                      break;
                    }
                  }
                }
              };
              getSwingEnvironment().invokeSwingLater(t);
              break;
            }
          }
        }
      };
      getScoutActivityMap().addActivityMapListener(m_scoutListener);
    }
    getSwingActivityMap().setModel(new SwingActivityMapModel(getScoutActivityMap(), m_metricsTable));
    getSwingActivityMap().setColumnModel(new SwingActivityMapColumnModel(getScoutActivityMap().getTimeScale()));
    getSwingActivityMap().getSelector().setDrawSections(getScoutActivityMap().isDrawSections());
    setSelectionFromScout();
  }

  public IActivityMap<?, ?> getScoutActivityMap() {
    return getScoutObject();
  }

  public JActivityMap getSwingActivityMap() {
    return (JActivityMap) getSwingField();
  }

  public JScrollPane getSwingScrollPane() {
    return m_swingScrollPane;
  }

  private void setTimeScaleFromScout(TimeScale scale) {
    getSwingActivityMap().setColumnModel(new SwingActivityMapColumnModel(scale));
    // re-set selection
    setSelectionFromScout();
  }

  private void setResourceIdsFromScout(Object[] resourceIds) {
    getSwingActivityMap().setModel(new SwingActivityMapModel(getScoutActivityMap(), m_metricsTable));
  }

  private void setSelectedActivityCellFromScout(ActivityCell cell) {
    // do nothing, the selector display is enough
  }

  private void setSelectedActivityCellFromSwing(SwingActivityComponent comp) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    final ActivityCell<?, ?> cell = (comp != null ? comp.getScoutActivityCell() : null);
    // notify Scout
    Runnable t = new Runnable() {
      @SuppressWarnings("unchecked")
      @Override
      public void run() {
        getScoutActivityMap().getUIFacade().setSelectedActivityCellFromUI(cell);
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  private void handleCellActionFromSwing(MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    final ActivityCell cell = (e.getComponent() instanceof SwingActivityComponent) ? ((SwingActivityComponent) e.getComponent()).getScoutActivityCell() : null;
    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), getSwingActivityMap());
    final double[] range = getSwingActivityMap().pixToRange(p.x);
    final int row = getSwingActivityMap().pixToRow(p.y);
    // notify Scout
    Runnable t = new Runnable() {
      @SuppressWarnings("unchecked")
      @Override
      public void run() {
        Object resourceId = getScoutActivityMap().getResourceIds()[row];
        getScoutActivityMap().getUIFacade().fireCellActionFromUI(resourceId, range, cell);
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 2345);
    // end notify
  }

  private void setSelectionFromScout() {
    HashSet<Object> selectedIds = new HashSet<Object>(Arrays.asList(getScoutActivityMap().getSelectedResourceIds()));
    Object[] ids = getScoutActivityMap().getResourceIds();
    TreeSet<Integer> indexes = new TreeSet<Integer>();
    for (int i = 0; i < ids.length; i++) {
      if (selectedIds.contains(ids[i])) {
        indexes.add(i);
      }
    }
    int[] rowIndexes = new int[indexes.size()];
    int i = 0;
    for (Integer n : indexes) {
      rowIndexes[i++] = n;
    }
    double[] range = getScoutActivityMap().getTimeScale().getRangeOf(getScoutActivityMap().getSelectedBeginTime(), getScoutActivityMap().getSelectedEndTime());
    getSwingActivityMap().setSelection(rowIndexes, range);
  }

  private void setSelectionFromSwing(int[] rows, double[] range) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    final double[] normalizedRange = range;
    Object[] ids = getScoutActivityMap().getResourceIds();
    final Set<Object> selectedIds = new HashSet<Object>();
    for (int i : rows) {
      selectedIds.add(ids[i]);
    }
    // notify Scout
    Runnable t = new Runnable() {
      @SuppressWarnings("unchecked")
      @Override
      public void run() {
        getScoutActivityMap().getUIFacade().setSelectionFromUI(selectedIds.toArray(new Object[selectedIds.size()]), normalizedRange);
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutListener != null) {
      getScoutActivityMap().removeActivityMapListener(m_scoutListener);
      m_scoutListener = null;
    }
  }

  @Override
  protected boolean isHandleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IActivityMap.PROP_TIME_SCALE)) {
      return true;
    }
    else if (name.equals(IActivityMap.PROP_SELECTED_ACTIVITY_CELL)) {
      return true;
    }
    else if (name.equals(IActivityMap.PROP_SELECTED_RESOURCE_IDS)) {
      return true;
    }
    else if (name.equals(IActivityMap.PROP_SELECTED_BEGIN_TIME)) {
      return true;
    }
    else if (name.equals(IActivityMap.PROP_SELECTED_END_TIME)) {
      return true;
    }
    else if (name.equals(IActivityMap.PROP_DRAW_SECTIONS)) {
      return true;
    }
    return super.isHandleScoutPropertyChange(name, newValue);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    // NOTE: See isHandleScoutPropertyChange() for filtered properties
    if (name.equals(IActivityMap.PROP_TIME_SCALE)) {
      setTimeScaleFromScout((TimeScale) newValue);
    }
    else if (name.equals(IActivityMap.PROP_SELECTED_ACTIVITY_CELL)) {
      setSelectedActivityCellFromScout((ActivityCell) newValue);
    }
    else if (name.equals(IActivityMap.PROP_SELECTED_RESOURCE_IDS) ||
        name.equals(IActivityMap.PROP_SELECTED_BEGIN_TIME) ||
        name.equals(IActivityMap.PROP_SELECTED_END_TIME)

    ) {
      setSelectionFromScout();
    }
    else if (name.equals(IActivityMap.PROP_RESOURCE_IDS)) {
      setResourceIdsFromScout((Object[]) newValue);
    }
    else if (name.equals(IActivityMap.PROP_DRAW_SECTIONS)) {
      getSwingActivityMap().getSelector().setDrawSections((Boolean) newValue);
    }
  }

  private void handleActivitiesChangedFromScout() {
    getSwingActivityMap().setModel(new SwingActivityMapModel(getScoutActivityMap(), m_metricsTable));
  }

  private void handleSwingNewActivityPopup(final MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        IMenu[] scoutMenus = getScoutActivityMap().getUIFacade().fireNewActivityPopupFromUI();
        // call swing menu
        new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }

  private void handleSwingEditActivityPopup(final MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        IMenu[] scoutMenus = getScoutActivityMap().getUIFacade().fireEditActivityPopupFromUI();
        // call swing menu
        new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }
}
