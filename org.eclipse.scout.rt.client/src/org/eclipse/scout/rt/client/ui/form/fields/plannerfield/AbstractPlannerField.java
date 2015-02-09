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
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.IPlannerFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldLoadActivityMapDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldLoadResourceTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldPopulateActivitiesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldPopulateResourceTableChain;
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

@ClassId("8fbfbf19-ff9d-4e89-8a78-8e6a4a8dc36c")
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
  protected int getConfiguredMiniCalendarCount() {
    return 2;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
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
   * Interceptor is called after data was fetched from LookupCall and is adding
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
    Object[][] data = interceptLoadResourceTableData();
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
  protected Object[][] execLoadActivityMapData(List<? extends RI> resourceIds, List<? extends ITableRow> resourceRows) throws ProcessingException {
    return null;
  }

  /**
   * Load activity data<br>
   * By default loads data using {@link #interceptLoadActivityMapData(List, List)}, transforms to {@link ActivityCell},
   * maps
   * to resources using the resourceId, and sets the {@link ActivityCell}s on the
   * corresponding activtyRow.
   */
  @ConfigOperation
  @Order(10)
  protected void execPopulateActivities(List<RI> resourceIds, List<ITableRow> resourceRows) throws ProcessingException {
    Object[][] data = interceptLoadActivityMapData(resourceIds, resourceRows);
    ArrayList<ActivityCell<RI, AI>> list = new ArrayList<ActivityCell<RI, AI>>();
    for (Object[] row : data) {
      ActivityCell<RI, AI> cell = new ActivityCell<RI, AI>(row);
      list.add(cell);
    }
    getActivityMap().removeActivityCellsById(resourceIds);
    getActivityMap().addActivityCells(list);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    m_uiFacade = new P_PlannerFieldUIFacade();
    super.initConfig();
    setMiniCalendarCount(getConfiguredMiniCalendarCount());
    setSplitterPosition(getConfiguredSplitterPosition());

    List<ITable> contributedTables = m_contributionHolder.getContributionsByClass(ITable.class);
    m_resourceTable = (T) CollectionUtility.firstElement(contributedTables);
    if (m_resourceTable == null) {
      Class<? extends ITable> configuredResourceTable = getConfiguredResourceTable();
      if (configuredResourceTable != null) {
        try {
          m_resourceTable = (T) ConfigurationUtility.newInnerInstance(this, configuredResourceTable);
        }
        catch (Exception e) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + getConfiguredResourceTable().getName() + "'.", e));
        }
      }
    }

    if (m_resourceTable != null) {
      if (m_resourceTable instanceof AbstractTable) {
        ((AbstractTable) m_resourceTable).setContainerInternal(this);
      }
      m_resourceTable.setEnabled(isEnabled());

      for (IColumn c : getResourceTable().getColumnSet().getKeyColumns()) {
        m_resourceIdColumn = c;
        break;
      }

      // add mediator between resource table and activity map table
      m_resourceTable.addTableListener(new P_ResourceTableListener());
    }
    else {
      LOG.warn("there is no inner class of type ITable in " + getClass());
    }

    List<IActivityMap> contributedActivityMaps = m_contributionHolder.getContributionsByClass(IActivityMap.class);
    m_activityMap = (P) CollectionUtility.firstElement(contributedActivityMaps);
    if (m_activityMap == null) {
      Class<? extends IActivityMap> configuredActivityMap = getConfiguredActivityMap();
      if (configuredActivityMap != null) {
        try {
          m_activityMap = (P) ConfigurationUtility.newInnerInstance(this, configuredActivityMap);
        }
        catch (Exception e) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + configuredActivityMap.getName() + "'.", e));
        }
      }
    }

    if (m_activityMap != null) {
      if (m_activityMap instanceof AbstractActivityMap) {
        ((AbstractActivityMap) m_activityMap).setContainerInternal(this);
      }
      m_activityMap.addPropertyChangeListener(
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
              if (e.getPropertyName().equals(IActivityMap.PROP_SELECTED_RESOURCE_IDS)) {
                syncSelectionFromActivityToResource();
                m_activityMap.getContextMenu().callOwnerValueChanged();
              }
            }
          }
          );
    }
    else {
      LOG.warn("there is no inner class of type IActivityMap in " + getClass());
    }
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
    interceptPopulateResourceTable();
  }

  @Override
  public void loadActivityMapData() throws ProcessingException {
    loadActivityMapDataInternal(getResourceTable().getRows());
  }

  @Override
  public void loadActivityMapDataOfSelectedRecources() throws ProcessingException {
    loadActivityMapDataInternal(getResourceTable().getSelectedRows());
  }

  private void loadActivityMapDataInternal(List<? extends ITableRow> resourceRows) throws ProcessingException {
    List<RI> resourceIds = getResourceIdColumnInternal().getValues(resourceRows);
    try {
      getActivityMap().setActivityMapChanging(true);
      interceptPopulateActivities(CollectionUtility.arrayList(resourceIds), CollectionUtility.arrayList(resourceRows));
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
  public List<ITableRow> activityCellsToResourceRows(List<? extends ActivityCell<RI, AI>> activityCells) {
    List<ITableRow> resourceRowSet = new ArrayList<ITableRow>(activityCells.size());
    for (ActivityCell<RI, AI> cell : activityCells) {
      ITableRow resourceRow = getResourceIdColumnInternal().findRow(cell.getResourceId());
      if (resourceRow != null) {
        resourceRowSet.add(resourceRow);
      }
    }
    return resourceRowSet;
  }

  @Override
  public List<ActivityCell<RI, AI>> resourceRowToActivityCells(ITableRow resourceRow) {
    return resourceRowsToActivityCells(CollectionUtility.arrayList(resourceRow));
  }

  @Override
  public List<ActivityCell<RI, AI>> resourceRowsToActivityCells(List<? extends ITableRow> resourceRows) {
    return getActivityMap().getActivityCells(getResourceIdColumnInternal().getValues(resourceRows));
  }

  private void syncSelectionFromResourceToActivity() {
    if (m_selectionMediatorRunning) {
      return;
    }
    try {
      m_selectionMediatorRunning = true;
      //
      getActivityMap().setSelectedResourceIds(getResourceIdColumnInternal().getSelectedValues());
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
      List<ITableRow> resourceRows = getResourceIdColumnInternal().findRows(getActivityMap().getSelectedResourceIds());
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

    @Override
    public void setSplitterPositionFromUI(Integer value) {
      propertySupport.setPropertyNoFire(PROP_SPLITTER_POSITION, value);
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

  protected final void interceptPopulateActivities(List<RI> resourceIds, List<ITableRow> resourceRows) throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    PlannerFieldPopulateActivitiesChain<T, P, RI, AI> chain = new PlannerFieldPopulateActivitiesChain<T, P, RI, AI>(extensions);
    chain.execPopulateActivities(resourceIds, resourceRows);
  }

  protected final Object[][] interceptLoadResourceTableData() throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    PlannerFieldLoadResourceTableDataChain<T, P, RI, AI> chain = new PlannerFieldLoadResourceTableDataChain<T, P, RI, AI>(extensions);
    return chain.execLoadResourceTableData();
  }

  protected final void interceptPopulateResourceTable() throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    PlannerFieldPopulateResourceTableChain<T, P, RI, AI> chain = new PlannerFieldPopulateResourceTableChain<T, P, RI, AI>(extensions);
    chain.execPopulateResourceTable();
  }

  protected final Object[][] interceptLoadActivityMapData(List<? extends RI> resourceIds, List<? extends ITableRow> resourceRows) throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    PlannerFieldLoadActivityMapDataChain<T, P, RI, AI> chain = new PlannerFieldLoadActivityMapDataChain<T, P, RI, AI>(extensions);
    return chain.execLoadActivityMapData(resourceIds, resourceRows);
  }

  protected static class LocalPlannerFieldExtension<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI, OWNER extends AbstractPlannerField<T, P, RI, AI>> extends LocalFormFieldExtension<OWNER> implements IPlannerFieldExtension<T, P, RI, AI, OWNER> {

    public LocalPlannerFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPopulateActivities(PlannerFieldPopulateActivitiesChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<RI> resourceIds, List<ITableRow> resourceRows) throws ProcessingException {
      getOwner().execPopulateActivities(resourceIds, resourceRows);
    }

    @Override
    public Object[][] execLoadResourceTableData(PlannerFieldLoadResourceTableDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException {
      return getOwner().execLoadResourceTableData();
    }

    @Override
    public void execPopulateResourceTable(PlannerFieldPopulateResourceTableChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException {
      getOwner().execPopulateResourceTable();
    }

    @Override
    public Object[][] execLoadActivityMapData(PlannerFieldLoadActivityMapDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<? extends RI> resourceIds, List<? extends ITableRow> resourceRows) throws ProcessingException {
      return getOwner().execLoadActivityMapData(resourceIds, resourceRows);
    }
  }

  @Override
  protected IPlannerFieldExtension<T, P, RI, AI, ? extends AbstractPlannerField<T, P, RI, AI>> createLocalExtension() {
    return new LocalPlannerFieldExtension<T, P, RI, AI, AbstractPlannerField<T, P, RI, AI>>(this);
  }

}
