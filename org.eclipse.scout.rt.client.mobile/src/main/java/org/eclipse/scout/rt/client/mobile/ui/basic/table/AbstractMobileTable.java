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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.columns.IRowSummaryColumn;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.DefaultTableRowFormProvider;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.ITableRowForm;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.form.ITableRowFormProvider;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableUIFacade;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * @since 3.9.0
 */
public abstract class AbstractMobileTable extends AbstractTable implements IMobileTable {
  private DrillDownStyleMap m_drillDownStyleMap;
  private int m_tableRowFormDisplayHint;
  private String m_tableRowFormDisplayViewId;

  public AbstractMobileTable() {
    this(true);
  }

  public AbstractMobileTable(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_drillDownStyleMap = new DrillDownStyleMap();
    setPagingEnabled(getConfiguredPagingEnabled());
    setPageSize(getConfiguredPageSize());
    setAutoCreateTableRowForm(execIsAutoCreateTableRowForm());
    setDefaultDrillDownStyle(execComputeDefaultDrillDownStyle());
    setTableRowFormProvider(createTableRowFormProvider());
  }

  @Override
  public boolean isPagingEnabled() {
    return propertySupport.getPropertyBool(PROP_PAGING_ENABLED);
  }

  @Override
  public void setPagingEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_PAGING_ENABLED, enabled);
  }

  @Override
  public int getPageSize() {
    return propertySupport.getPropertyInt(PROP_PAGE_SIZE);
  }

  @Override
  public void setPageSize(int pageSize) {
    propertySupport.setPropertyInt(PROP_PAGE_SIZE, pageSize);
  }

  @Override
  public int getPageIndex() {
    return propertySupport.getPropertyInt(PROP_PAGE_INDEX);
  }

  @Override
  public void setPageIndex(int index) {
    propertySupport.setPropertyInt(PROP_PAGE_INDEX, index);
  }

  @Override
  public int getPageCount() {
    if (getRowCount() == 0) {
      return 1;
    }
    return new Double(Math.ceil((double) getRowCount() / (double) getPageSize())).intValue();
  }

  @Override
  public ITableRowFormProvider getTableRowFormProvider() {
    return (ITableRowFormProvider) propertySupport.getProperty(PROP_TABLE_ROW_FORM_PROVIDER);
  }

  @Override
  public void setTableRowFormProvider(ITableRowFormProvider provider) {
    propertySupport.setProperty(PROP_TABLE_ROW_FORM_PROVIDER, provider);
  }

  @Override
  public boolean isAutoCreateTableRowForm() {
    return propertySupport.getPropertyBool(PROP_AUTO_CREATE_TABLE_ROW_FORM);
  }

  @Override
  public void setAutoCreateTableRowForm(boolean autoCreateTableRowForm) {
    propertySupport.setPropertyBool(PROP_AUTO_CREATE_TABLE_ROW_FORM, autoCreateTableRowForm);
  }

  @Override
  public String getDefaultDrillDownStyle() {
    return propertySupport.getPropertyString(PROP_DEFAULT_DRILL_DOWN_STYLE);
  }

  @Override
  public void setDefaultDrillDownStyle(String defaultDrillDownStyle) {
    propertySupport.setPropertyString(PROP_DEFAULT_DRILL_DOWN_STYLE, defaultDrillDownStyle);
  }

  public void putDrillDownStyle(ITableRow row, String drillDownStyle) {
    m_drillDownStyleMap.put(row, drillDownStyle);
  }

  public String getDrillDownStyle(ITableRow row) {
    return m_drillDownStyleMap.get(row);
  }

  public int getTableRowFormDisplayHint() {
    return m_tableRowFormDisplayHint;
  }

  public void setTableRowFormDisplayHint(int tableRowFormDisplayHint) {
    m_tableRowFormDisplayHint = tableRowFormDisplayHint;
  }

  public String getTableRowFormDisplayViewId() {
    return m_tableRowFormDisplayViewId;
  }

  public void setTableRowFormDisplayViewId(String tableRowFormDisplayViewId) {
    m_tableRowFormDisplayViewId = tableRowFormDisplayViewId;
  }

  protected ITableRowFormProvider createTableRowFormProvider() {
    return new DefaultTableRowFormProvider();
  }

  @Override
  public void setDrillDownStyleMap(DrillDownStyleMap drillDownStyleMap) {
    m_drillDownStyleMap = drillDownStyleMap;
    if (m_drillDownStyleMap == null) {
      m_drillDownStyleMap = new DrillDownStyleMap();
    }
  }

  @Override
  public DrillDownStyleMap getDrillDownStyleMap() {
    return m_drillDownStyleMap;
  }

  public static void setAutoCreateRowForm(ITable table, boolean autoCreateRowForm) {
    table.setProperty(IMobileTable.PROP_AUTO_CREATE_TABLE_ROW_FORM, autoCreateRowForm);
  }

  public static boolean isAutoCreateRowForm(ITable table) {
    Boolean b = (Boolean) table.getProperty(PROP_AUTO_CREATE_TABLE_ROW_FORM);
    return b != null ? b.booleanValue() : false;
  }

  public static void setDrillDownStyleMap(ITable table, DrillDownStyleMap drillDownStyles) {
    table.setProperty(IMobileTable.PROP_DRILL_DOWN_STYLE_MAP, drillDownStyles);
  }

  public static DrillDownStyleMap getDrillDownStyleMap(ITable table) {
    return (DrillDownStyleMap) table.getProperty(IMobileTable.PROP_DRILL_DOWN_STYLE_MAP);
  }

  public static void setDefaultDrillDownStyle(ITable table, String defaultDrillDownStyle) {
    table.setProperty(IMobileTable.PROP_DEFAULT_DRILL_DOWN_STYLE, defaultDrillDownStyle);
  }

  public static String getDefaultDrillDownStyle(ITable table) {
    return (String) table.getProperty(IMobileTable.PROP_DEFAULT_DRILL_DOWN_STYLE);
  }

  public static boolean isPagingEnabled(ITable table) {
    Boolean b = (Boolean) table.getProperty(PROP_PAGING_ENABLED);
    return b != null ? b.booleanValue() : false;
  }

  public static void setPagingEnabled(ITable table, boolean enabled) {
    table.setProperty(IMobileTable.PROP_PAGING_ENABLED, enabled);
  }

  public static int getPageSize(ITable table) {
    Number n = (Number) table.getProperty(PROP_PAGE_SIZE);
    return n != null ? n.intValue() : 0;
  }

  public static void setPageSize(ITable table, int pageSize) {
    table.setProperty(IMobileTable.PROP_PAGE_SIZE, pageSize);
  }

  public static int getPageIndex(ITable table) {
    Number n = (Number) table.getProperty(PROP_PAGE_INDEX);
    return n != null ? n.intValue() : 0;
  }

  public static void setPageIndex(ITable table, int index) {
    table.setProperty(IMobileTable.PROP_PAGE_INDEX, index);
  }

  public static ITableRowFormProvider getTableRowFormProvider(ITable table) {
    return (ITableRowFormProvider) table.getProperty(PROP_TABLE_ROW_FORM_PROVIDER);
  }

  public static void setTableRowFormProvider(ITable table, ITableRowFormProvider provider) {
    table.setProperty(PROP_TABLE_ROW_FORM_PROVIDER, provider);
  }

  protected boolean getConfiguredPagingEnabled() {
    return true;
  }

  protected int getConfiguredPageSize() {
    return 50;
  }

  protected boolean execIsAutoCreateTableRowForm() {
    if (isCheckable()) {
      return false;
    }

    return true;
  }

  protected String execComputeDefaultDrillDownStyle() {
    if (isCheckable()) {
      return IRowSummaryColumn.DRILL_DOWN_STYLE_NONE;
    }

    return IRowSummaryColumn.DRILL_DOWN_STYLE_ICON;
  }

  protected void startTableRowForm(ITableRow row) {
    ITableRowForm form = getTableRowFormProvider().createTableRowForm(row);
    form.setDisplayHint(getTableRowFormDisplayHint());
    form.setDisplayViewId(getTableRowFormDisplayViewId());
    form.setModal(IForm.DISPLAY_HINT_DIALOG == form.getDisplayHint());
    form.start();
    if (IRowSummaryColumn.DRILL_DOWN_STYLE_ICON.equals(getDrillDownStyle(row))) {
      form.addFormListener(new ClearTableSelectionFormCloseListener(this));
    }
  }

  protected void clearSelectionDelayed() {
    ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        clearSelection();
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  protected void clearSelection() {
    selectRow(null);
  }

  @Override
  protected ITableUIFacade createUIFacade() {
    return new P_MobileTableUIFacade();
  }

  @Override
  public IMobileTableUiFacade getUIFacade() {
    return (IMobileTableUiFacade) super.getUIFacade();
  }

  protected class P_MobileTableUIFacade extends P_TableUIFacade implements IMobileTableUiFacade {

    @Override
    public void setPageIndexFromUi(int pageIndex) {
      setPageIndex(pageIndex);
    }

  }

}
