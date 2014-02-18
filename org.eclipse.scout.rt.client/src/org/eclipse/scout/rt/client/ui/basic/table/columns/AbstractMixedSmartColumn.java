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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.services.lookup.TableProvisioningContext;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractMixedSmartField;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> extends AbstractContentAssistColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> implements IMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMixedSmartColumn.class);

  private boolean m_sortCodesByDisplayText;

  public AbstractMixedSmartColumn() {
    super();
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
   * When the smart proposal finds no matching records and this property is not
   * null, then it displays a link or menu with this label.<br>
   * When clicked the method {@link #execBrowseNew(String)} is invoked, which in
   * most cases is implemented as opening a "New XY..." dialog
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(315)
  protected String getConfiguredBrowseNewText() {
    return null;
  }

  /**
   * the default implementation simply casts one to the other type
   * 
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(400)
  protected VALUE_TYPE execConvertKeyToValue(LOOKUP_CALL_KEY_TYPE key) {
    return (VALUE_TYPE) key;
  }

  /**
   * the default implementation simply casts one to the other type
   * 
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(410)
  protected LOOKUP_CALL_KEY_TYPE execConvertValueToKey(VALUE_TYPE value) {
    return (LOOKUP_CALL_KEY_TYPE) value;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSortCodesByDisplayText(getConfiguredSortCodesByDisplayText());
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
  public ILookupCall<LOOKUP_CALL_KEY_TYPE> prepareLookupCall(ITableRow row) {
    if (getLookupCall() != null) {
      ILookupCall<LOOKUP_CALL_KEY_TYPE> call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new TableProvisioningContext(getTable(), row, AbstractMixedSmartColumn.this));
      call.setKey(execConvertValueToKey(getValueInternal(row)));
      call.setText(null);
      call.setAll(null);
      call.setRec(null);
      execPrepareLookup(call, row);
      return call;
    }
    else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected VALUE_TYPE parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    VALUE_TYPE validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (getDataType().isAssignableFrom(rawValue.getClass())) {
      validValue = (VALUE_TYPE) rawValue;
    }
    else {
      try {
        validValue = TypeCastUtility.castValue(rawValue, getDataType());
      }
      catch (Exception e) {
        throw new ProcessingException("invalid " + getDataType().getSimpleName() + " value in column '" + getClass().getName() + "': " + rawValue + " class=" + rawValue.getClass());
      }
    }
    return validValue;
  }

  @Override
  protected IFormField prepareEditInternal(final ITableRow row) throws ProcessingException {
    AbstractMixedSmartField<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> f = new AbstractMixedSmartField<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE>() {
      @Override
      protected void initConfig() {
        super.initConfig();
        propertySupport.putPropertiesMap(AbstractMixedSmartColumn.this.propertySupport.getPropertiesMap());
      }

      @Override
      public Class<VALUE_TYPE> getHolderType() {
        return AbstractMixedSmartColumn.this.getDataType();
      }

      @Override
      protected void execPrepareLookup(ILookupCall<LOOKUP_CALL_KEY_TYPE> call) throws ProcessingException {
        AbstractMixedSmartColumn.this.execPrepareLookup(call, row);
      }
    };

    f.setCodeTypeClass(getCodeTypeClass());
    f.setLookupCall(getLookupCall());
    f.setBrowseHierarchy(getConfiguredBrowseHierarchy());
    f.setBrowseMaxRowCount(getConfiguredBrowseMaxRowCount());
    f.setBrowseNewText(getConfiguredBrowseNewText());
    f.setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    f.setBrowseAutoExpandAll(getConfiguredBrowseAutoExpandAll());
    f.setBrowseLoadIncremental(getConfiguredBrowseLoadIncremental());
    return f;
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    ICodeType<?, LOOKUP_CALL_KEY_TYPE> codeType = getCodeTypeClass() != null ? CODES.getCodeType(getCodeTypeClass()) : null;
    ILookupCall<LOOKUP_CALL_KEY_TYPE> call = getLookupCall() != null ? getLookupCall() : null;
    if (codeType != null) {
      if (isSortCodesByDisplayText()) {
        String s1 = getDisplayText(r1);
        String s2 = getDisplayText(r2);
        return StringUtility.compareIgnoreCase(s1, s2);
      }
      else {
        LOOKUP_CALL_KEY_TYPE t1 = execConvertValueToKey(getValue(r1));
        LOOKUP_CALL_KEY_TYPE t2 = execConvertValueToKey(getValue(r2));
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
}
