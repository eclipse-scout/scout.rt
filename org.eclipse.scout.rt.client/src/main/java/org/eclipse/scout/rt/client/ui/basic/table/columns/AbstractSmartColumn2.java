/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IColumnExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ISmartColumn2Extension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.SmartColumn2Chains.SmartColumn2PrepareLookupChain;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.services.lookup.TableProvisioningContext;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IMixedSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.AbstractSmartField2;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.ISmartField2;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupResultCache;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;

@ClassId("65eaf372-09ff-425a-ad82-5be17fa9f1a7")
public abstract class AbstractSmartColumn2<VALUE> extends AbstractColumn<VALUE> implements ISmartColumn2<VALUE>, IContributionOwner {

  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private Class<? extends ICodeType<?, VALUE>> m_codeTypeClass;
  private ILookupCall<VALUE> m_lookupCall;

  private boolean m_sortCodesByDisplayText;
  private IContributionOwner m_contributionHolder;

  public AbstractSmartColumn2() {
    this(true);
  }

  public AbstractSmartColumn2(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected ISmartColumn2Extension<VALUE, ? extends AbstractSmartColumn2<VALUE>> createLocalExtension() {
    return new LocalSmartColumn2Extension<VALUE, AbstractSmartColumn2<VALUE>>(this);
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  /**
   * Configures whether the values are sorted by display text or by sort code in case of a code type class. This
   * configuration only is useful if a code type class is set (see {@link #getConfiguredCodeType()}). In case of a
   * lookup call, the values are sorted by display text.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if values are sorted by display text, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  protected boolean getConfiguredSortCodesByDisplayText() {
    return false;
  }

  /**
   * When the smart proposal finds no matching records and this property is not null, then it displays a link or menu
   * with this label.<br>
   * When clicked the method {@link AbstractSmartField#execBrowseNew(String)} is invoked, which in most cases is
   * implemented as opening a "New XY..." dialog
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(315)
  protected String getConfiguredBrowseNewText() {
    return null;
  }

  /**
   * Configures the lookup call used to determine the display text of the smart column value.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Lookup call class for this column.
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(140)
  protected Class<? extends ILookupCall<VALUE>> getConfiguredLookupCall() {
    return null;
  }

  /**
   * Configures the code type used to determine the display text of the smart column value. If a lookup call is set (
   * {@link #getConfiguredLookupCall()}), this configuration has no effect (lookup call is used instead of code type).
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Code type class for this column.
   */
  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(150)
  protected Class<? extends ICodeType<?, VALUE>> getConfiguredCodeType() {
    return null;
  }

  @ConfigOperation
  @Order(160)
  protected void execPrepareLookup(ILookupCall<VALUE> call, ITableRow row) {
  }

  /*
   * Configuration
   */

  /**
   * Configures whether the smartfield used for editable cells should represent the data as a list or as a tree (flat
   * vs. hierarchical data).
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if data behind the smart column is hierarchical (and the smartfield should represent it that
   *         way), {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  protected boolean getConfiguredBrowseHierarchy() {
    return false;
  }

  /**
   * valid when configuredBrowseHierarchy=true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  protected boolean getConfiguredBrowseAutoExpandAll() {
    return true;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(265)
  protected int getConfiguredBrowseMaxRowCount() {
    return 100;
  }

  /**
   * @return true: inactive rows are display together with active rows<br>
   *         false: inactive rows ae only displayed when selected by the model
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredActiveFilterEnabled() {
    return false;
  }

  /**
   * valid when configuredBrowseHierarchy=true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  protected boolean getConfiguredBrowseLoadIncremental() {
    return false;
  }

  protected ILookupCall<VALUE> prepareLookupCall(ITableRow row, VALUE value) {
    if (getLookupCall() != null) {
      ILookupCall<VALUE> call = BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new TableProvisioningContext(getTable(), row, this));
      call.setKey(value);
      call.setText(null);
      call.setAll(null);
      call.setRec(null);
      interceptPrepareLookup(call, row);
      return call;
    }
    else {
      return null;
    }
  }

  @Override
  public ILookupCall<VALUE> prepareLookupCall(ITableRow row) {
    return prepareLookupCall(row, getValueInternal(row));
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_contributionHolder = new ContributionComposite(this);
    setSortCodesByDisplayText(getConfiguredSortCodesByDisplayText());

    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lazy lookup decorator
    Class<? extends ILookupCall<VALUE>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      ILookupCall<VALUE> call;
      try {
        call = BEANS.get(lookupCallClass);
        setLookupCall(call);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + lookupCallClass.getName() + "'.", e));
      }
    }
  }

  @Override
  public Class<? extends ICodeType<?, VALUE>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, VALUE>> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
    }
    refreshValues();
  }

  @Override
  public ILookupCall<VALUE> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<VALUE> call) {
    m_lookupCall = call;
  }

  @SuppressWarnings("unchecked")
  public Class<VALUE> getLookupType() {
    return TypeCastUtility.getGenericsParameterClass(getClass(), AbstractContentAssistColumn.class, 1);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected VALUE parseValueInternal(ITableRow row, Object rawValue) {
    VALUE validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (getDataType().isAssignableFrom(rawValue.getClass())) {
      validValue = (VALUE) rawValue;
    }
    else {
      try {
        validValue = TypeCastUtility.castValue(rawValue, getDataType());
      }
      catch (RuntimeException e) {
        throw new ProcessingException("invalid {} value in column '{}': {} class={}", getDataType().getSimpleName(), getClass().getName(), rawValue, rawValue.getClass(), e);
      }
    }
    return validValue;
  }

  @Override
  public void updateDisplayTexts(List<ITableRow> rows) {
    try {
      if (rows.size() > 0) {
        BatchLookupCall batchCall = new BatchLookupCall();
        ArrayList<ITableRow> batchRowList = new ArrayList<ITableRow>();

        BatchLookupResultCache lookupResultCache = new BatchLookupResultCache();
        for (ITableRow row : rows) {
          ILookupCall<?> call = prepareLookupCall(row);
          if (call != null && call.getKey() != null) {
            //split: local vs remote
            if (call instanceof LocalLookupCall) {
              applyLookupResult(row, lookupResultCache.getDataByKey(call));
            }
            else {
              batchRowList.add(row);
              batchCall.addLookupCall(call);
            }
          }
          else {
            applyLookupResult(row, new ArrayList<ILookupRow<?>>(0));
          }
        }

        //
        if (!batchCall.isEmpty()) {
          ITableRow[] tableRows = batchRowList.toArray(new ITableRow[batchRowList.size()]);
          IBatchLookupService service = BEANS.get(IBatchLookupService.class);
          List<List<ILookupRow<?>>> resultArray = service.getBatchDataByKey(batchCall);
          for (int i = 0; i < tableRows.length; i++) {
            applyLookupResult(tableRows[i], resultArray.get(i));
          }
        }
      }

    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public void updateDisplayText(ITableRow row, VALUE value) {
    ILookupCall<?> call = prepareLookupCall(row, value);
    if (call != null && call.getKey() != null) {
      try {
        List<? extends ILookupRow<?>> result = call.getDataByKey();
        applyLookupResult(row, result);
      }
      catch (RuntimeException pe) {
        BEANS.get(ExceptionHandler.class).handle(pe);
      }
    }
    else {
      super.updateDisplayText(row, value);
    }
  }

  @Override
  public void updateDisplayText(ITableRow row, Cell cell) {
    ILookupCall<?> call = prepareLookupCall(row);
    if (call != null && call.getKey() != null) {
      try {
        List<? extends ILookupRow<?>> result = call.getDataByKey();
        applyLookupResult(row, result);
      }
      catch (RuntimeException pe) {
        BEANS.get(ExceptionHandler.class).handle(pe);
      }
    }
    else {
      super.updateDisplayText(row, cell);
    }
  }

  protected void applyLookupResult(ITableRow tableRow, List<? extends ILookupRow<?>> result) {
    // disable row changed trigger on row
    try {
      tableRow.setRowChanging(true);
      //
      Cell cell = tableRow.getCellForUpdate(this);
      String separator = getResultRowSeparator();

      List<String> texts = CollectionUtility.emptyArrayList();
      List<String> tooltipTexts = CollectionUtility.emptyArrayList();

      for (ILookupRow<?> row : result) {
        texts.add(row.getText());
        tooltipTexts.add(row.getTooltipText());
      }

      cell.setText(StringUtility.join(separator, texts));
      cell.setTooltipText(StringUtility.join(separator, tooltipTexts));
    }
    finally {
      tableRow.setRowPropertiesChanged(false);
      tableRow.setRowChanging(false);
    }
  }

  private String getResultRowSeparator() {
    if (getTable().isMultilineText()) {
      return "\n";
    }
    else {
      return ", ";
    }
  }

  protected void mapEditorFieldProperties(ISmartField2<VALUE> f) {
    super.mapEditorFieldProperties(f);
    f.setCodeTypeClass(getCodeTypeClass());
    f.setLookupCall(getLookupCall());
    f.setBrowseHierarchy(getConfiguredBrowseHierarchy());
    f.setBrowseMaxRowCount(getConfiguredBrowseMaxRowCount());
    f.setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    f.setBrowseAutoExpandAll(getConfiguredBrowseAutoExpandAll());
    f.setBrowseLoadIncremental(getConfiguredBrowseLoadIncremental());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSortCodesByDisplayText() {
    return m_sortCodesByDisplayText;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSortCodesByDisplayText(boolean b) {
    m_sortCodesByDisplayText = b;
  }

  @Override
  protected IFormField prepareEditInternal(final ITableRow row) {
    SmartField2Editor f = (SmartField2Editor) getDefaultEditor();
    f.setRow(row);
    mapEditorFieldProperties(f);
    return f;
  }

  @SuppressWarnings("deprecation")
  protected void mapEditorFieldProperties(IMixedSmartField<VALUE, VALUE> f) {
    super.mapEditorFieldProperties(f);
    f.setBrowseNewText(getConfiguredBrowseNewText());
  }

  @Override
  protected ISmartField2<VALUE> createDefaultEditor() {
    return new SmartField2Editor();
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    ICodeType<?, VALUE> codeType = getCodeTypeClass() != null ? BEANS.opt(getCodeTypeClass()) : null;
    ILookupCall<VALUE> call = getLookupCall() != null ? getLookupCall() : null;
    if (codeType != null) {
      if (isSortCodesByDisplayText()) {
        String s1 = getDisplayText(r1);
        String s2 = getDisplayText(r2);
        return StringUtility.compareIgnoreCase(s1, s2);
      }
      else {
        VALUE t1 = getValue(r1);
        VALUE t2 = getValue(r2);
        Integer sort1 = (t1 != null ? codeType.getCodeIndex(t1) : -1);
        Integer sort2 = (t2 != null ? codeType.getCodeIndex(t2) : -1);
        int c = sort1.compareTo(sort2);
        return c;
      }
    }
    else if (call != null) {
      String s1 = getDisplayText(r1);
      String s2 = getDisplayText(r2);
      return StringUtility.compareIgnoreCase(s1, s2);
    }
    else {
      return super.compareTableRows(r1, r2);
    }
  }

  protected final void interceptPrepareLookup(ILookupCall<VALUE> call, ITableRow row) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    SmartColumn2PrepareLookupChain<VALUE> chain = new SmartColumn2PrepareLookupChain<VALUE>(extensions);
    chain.execPrepareLookup(call, row);
  }

  protected static class LocalSmartColumn2Extension<VALUE, OWNER extends AbstractSmartColumn2<VALUE>> extends LocalColumnExtension<VALUE, OWNER>
      implements ISmartColumn2Extension<VALUE, OWNER> {

    public LocalSmartColumn2Extension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPrepareLookup(SmartColumn2PrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call, ITableRow row) {
      getOwner().execPrepareLookup(call, row);
    }

  }

  /**
   * Internal editor field
   */
  @ClassId("18e7b5e0-b4e4-424f-869b-7dab2f526560")
  @SuppressWarnings("bsiRulesDefinition:orderMissing")
  protected class SmartField2Editor extends AbstractSmartField2<VALUE> {
    private ITableRow m_row;

    protected ITableRow getRow() {
      return m_row;
    }

    protected void setRow(ITableRow row) {
      m_row = row;
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      propertySupport.putPropertiesMap(AbstractSmartColumn2.this.propertySupport.getPropertiesMap());
    }

    @Override
    public Class<VALUE> getHolderType() {
      return AbstractSmartColumn2.this.getDataType();
    }

    @Override
    protected void execPrepareLookup(ILookupCall<VALUE> call) {
      AbstractSmartColumn2.this.interceptPrepareLookup(call, getRow());
    }

    @Override
    protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
      Class[] menuCandidates = ConfigurationUtility.getDeclaredPublicClasses(AbstractSmartColumn2.this.getClass());
      List<Class<IMenu>> menuClazzes = ConfigurationUtility.filterClasses(menuCandidates, IMenu.class);
      for (Class<? extends IMenu> menuClazz : menuClazzes) {
        menus.addOrdered(ConfigurationUtility.newInnerInstance(AbstractSmartColumn2.this, menuClazz));
      }

      List<IMenu> contributedMenus = AbstractSmartColumn2.this.m_contributionHolder.getContributionsByClass(IMenu.class);
      menus.addAllOrdered(contributedMenus);
    }
  }

}
