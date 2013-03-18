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
package org.eclipse.scout.rt.client.ui.form.fields.plannerfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.activitymap.AbstractActivityMap;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractPlannerField<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractFormField implements IPlannerField<T, P, RI, AI> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractPlannerField.class);

  private IPlannerFieldUIFacade m_uiFacade;
  private T m_resourceTable;
  private IColumn<RI> m_resourceIdColumn;
  private P m_activityMap;
  private boolean m_selectionMediatorRunning;// true when mediation is running

  public AbstractPlannerField() {
    this(true);
  }

  public AbstractPlannerField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  private Class<? extends ITable> getConfiguredResourceTable() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, ITable.class);
  }

  private Class<? extends IActivityMap> getConfiguredActivityMap() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, IActivityMap.class);
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(10)
  @ConfigPropertyValue("2")
  protected int getConfiguredMiniCalendarCount() {
    return 2;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  @ConfigPropertyValue("168")
  protected int getConfiguredSplitterPosition() {
    return 168;
  }

  @Override
  protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException {
    loadResourceTableData();
  }

  @ConfigOperation
  @Order(10)
  protected Object[][] execLoadResourceTableData() throws ProcessingException {
    return null;
  }

  /**
   * interceptor is called after data was fetched from LookupCall and is adding
   * a table row for every LookupRow using IListBoxTable.createTableRow(row) and
   * ITable.addRows()
   * <p>
   * For most cases the override of just {@link #execLoadTableData()} is sufficient
   * 
   * <pre>
   * Object[][] data = execLoadResourceTableData();
   * getResourceTable().replaceRowsByMatrix(data);
   * </pre>
   */
  @ConfigOperation
  @Order(20)
  protected void execPopulateResourceTable() throws ProcessingException {
    Object[][] data = execLoadResourceTableData();
    getResourceTable().replaceRowsByMatrix(data);
  }

  /**
   * Load data matrix with the maximum of the following columns:
   * <ul>
   * <li>resourceId of type RI
   * <li>activityId of type AI
   * <li>startTime of type {@link Date}
   * <li>endTime of type {@link Date}
   * <li>text of type {@link String}
   * <li>tooltipText of type {@link String}
   * <li>iconId of type {@link String}
   * <li>majorValue of type {@link Number}
   * <li>minorValue of type {@link Number}
   * </ul>
   */
  @ConfigOperation
  @Order(10)
  protected Object[][] execLoadActivityMapData(RI[] resourceIds, ITableRow[] resourceRows) throws ProcessingException {
    return null;
  }

  /**
   * Load activity data<br>
   * By default loads data using {@link #execLoadActivityMapData(ITableRow[], long[])}, transforms to
   * {@link ActivityCell}, maps to resources using the resourceId, and sets the {@link ActivityCell}s on the
   * coresponding activtyRow.
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(10)
  protected void execPopulateActivities(RI[] resourceIds, ITableRow[] resourceRows) throws ProcessingException {
    Object[][] data = execLoadActivityMapData(resourceIds, resourceRows);
    ArrayList<ActivityCell<RI, AI>> list = new ArrayList<ActivityCell<RI, AI>>();
    for (Object[] row : data) {
      ActivityCell<RI, AI> cell = new ActivityCell<RI, AI>(row);
      list.add(cell);
    }
    getActivityMap().removeActivityCells(resourceIds);
    getActivityMap().addActivityCells(list.toArray(new ActivityCell[list.size()]));
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    m_uiFacade = new P_PlannerFieldUIFacade();
    super.initConfig();
    setMiniCalendarCount(getConfiguredMiniCalendarCount());
    setSplitterPosition(getConfiguredSplitterPosition());
    if (getConfiguredResourceTable() != null) {
      try {
        m_resourceTable = (T) ConfigurationUtility.newInnerInstance(this, getConfiguredResourceTable());
        if (m_resourceTable instanceof AbstractTable) {
          ((AbstractTable) m_resourceTable).setContainerInternal(this);
        }
        m_resourceTable.setEnabled(isEnabled());
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
      for (IColumn c : getResourceTable().getColumnSet().getKeyColumns()) {
        m_resourceIdColumn = c;
        break;
      }
    }
    else {
      LOG.warn("there is no inner class of type ITable in " + getClass());
    }
    if (getConfiguredActivityMap() != null) {
      try {
        m_activityMap = (P) ConfigurationUtility.newInnerInstance(this, getConfiguredActivityMap());
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
      if (m_activityMap instanceof AbstractActivityMap) {
        ((AbstractActivityMap) m_activityMap).setContainerInternal(this);
      }
    }
    else {
      LOG.warn("there is no inner class of type IActivityMap in " + getClass());
    }
    // add mediator between resource table and activitymap table
    m_resourceTable.addTableListener(new P_ResourceTableListener());
    m_activityMap.addPropertyChangeListener(
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IActivityMap.PROP_SELECTED_RESOURCE_IDS)) {
              syncSelectionFromActivityToResource();
            }
          }
        }
        );
  }

  /*
   * Runtime
   */

  private IColumn<RI> getResourceIdColumnInternal() {
    return m_resourceIdColumn;
  }

  @Override
  protected void initFieldInternal() throws ProcessingException {
    getResourceTable().initTable();
    getActivityMap().initActivityMap();
    super.initFieldInternal();
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    getResourceTable().disposeTable();
    getActivityMap().disposeActivityMap();
  }

  @Override
  public int getMiniCalendarCount() {
    return propertySupport.getPropertyInt(PROP_MINI_CALENDAR_COUNT);
  }

  @Override
  public void setMiniCalendarCount(int n) {
    if (n < 0 || n > 6) {
      return;// ignore it
    }
    propertySupport.setPropertyInt(PROP_MINI_CALENDAR_COUNT, n);
  }

  @Override
  public int getSplitterPosition() {
    return propertySupport.getPropertyInt(PROP_SPLITTER_POSITION);
  }

  @Override
  public void setSplitterPosition(int splitterPosition) {
    propertySupport.setPropertyInt(PROP_SPLITTER_POSITION, splitterPosition);
  }

  @Override
  public final T getResourceTable() {
    return m_resourceTable;
  }

  @Override
  public final P getActivityMap() {
    return m_activityMap;
  }

  @Override
  public void loadResourceTableData() throws ProcessingException {
    execPopulateResourceTable();
  }

  @Override
  public void loadActivityMapData() throws ProcessingException {
    loadActivityMapDataInternal(getResourceTable().getRows());
  }

  @Override
  public void loadActivityMapDataOfSelectedRecources() throws ProcessingException {
    loadActivityMapDataInternal(getResourceTable().getSelectedRows());
  }

  private void loadActivityMapDataInternal(ITableRow[] resourceRows) throws ProcessingException {
    RI[] resourceIds = getResourceIdColumnInternal().getValues(resourceRows);
    try {
      getActivityMap().setActivityMapChanging(true);
      //
      execPopulateActivities(resourceIds, resourceRows);
    }
    finally {
      getActivityMap().setActivityMapChanging(false);
    }
  }

  @Override
  public ITableRow activityCellToResourceRow(ActivityCell<RI, AI> activityCell) {
    if (activityCell != null) {
      return getResourceIdColumnInternal().findRow(activityCell.getResourceId());
    }
    else {
      return null;
    }
  }

  @Override
  public ITableRow[] activityCellsToResourceRows(ActivityCell<RI, AI>[] activityCells) {
    HashSet<ITableRow> resourceRowSet = new HashSet<ITableRow>();
    for (ActivityCell<RI, AI> cell : activityCells) {
      ITableRow resourceRow = getResourceIdColumnInternal().findRow(cell.getResourceId());
      if (resourceRow != null) {
        resourceRowSet.add(resourceRow);
      }
    }
    return resourceRowSet.toArray(new ITableRow[resourceRowSet.size()]);
  }

  @Override
  public ActivityCell<RI, AI>[] resourceRowToActivityCells(ITableRow resourceRow) {
    return resourceRowsToActivityCells(new ITableRow[]{resourceRow});
  }

  @Override
  public ActivityCell<RI, AI>[] resourceRowsToActivityCells(ITableRow[] resourceRows) {
    return getActivityMap().getActivityCells((RI[]) getResourceIdColumnInternal().getValues(resourceRows));
  }

  private void syncSelectionFromResourceToActivity() {
    if (m_selectionMediatorRunning) {
      return;
    }
    try {
      m_selectionMediatorRunning = true;
      //
      getActivityMap().setSelectedResourceIds((RI[]) getResourceIdColumnInternal().getSelectedValues());
    }
    finally {
      m_selectionMediatorRunning = false;
    }
  }

  private void syncSelectionFromActivityToResource() {
    if (m_selectionMediatorRunning) {
      return;
    }
    try {
      m_selectionMediatorRunning = true;
      //
      ITableRow[] resourceRows = getResourceIdColumnInternal().findRows(getActivityMap().getSelectedResourceIds());
      getResourceTable().selectRows(resourceRows, false);
    }
    finally {
      m_selectionMediatorRunning = false;
    }
  }

  @Override
  public IPlannerFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private class P_PlannerFieldUIFacade implements IPlannerFieldUIFacade {

    @Override
    public void refreshFromUI() {
      try {
        loadResourceTableData();
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

  }

  private class P_ResourceTableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      //reset resourceId mappings
      switch (e.getType()) {
        case TableEvent.TYPE_ALL_ROWS_DELETED:
        case TableEvent.TYPE_ROWS_DELETED:
        case TableEvent.TYPE_ROWS_INSERTED:
        case TableEvent.TYPE_ROW_FILTER_CHANGED:
        case TableEvent.TYPE_ROW_ORDER_CHANGED: {
          m_activityMap.setResourceIds(getResourceIdColumnInternal().getValues(getResourceTable().getFilteredRows()));
          break;
        }
      }
      //load additional data
      switch (e.getType()) {
        case TableEvent.TYPE_ROW_FILTER_CHANGED: {
          try {
            loadActivityMapDataInternal(getResourceTable().getFilteredRows());
          }
          catch (ProcessingException ex) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
          }
        }
        case TableEvent.TYPE_ROWS_INSERTED: {
          try {
            loadActivityMapDataInternal(e.getRows());
          }
          catch (ProcessingException ex) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
          }
          break;
        }
        case TableEvent.TYPE_ROWS_UPDATED: {
          try {
            loadActivityMapDataInternal(e.getRows());
          }
          catch (ProcessingException ex) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
          }
          break;
        }
        case TableEvent.TYPE_ROWS_SELECTED: {
          syncSelectionFromResourceToActivity();
          break;
        }
      }
    }
  }

}
