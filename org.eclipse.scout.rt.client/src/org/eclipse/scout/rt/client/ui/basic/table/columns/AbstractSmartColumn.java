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
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public abstract class AbstractSmartColumn<T> extends AbstractColumn<T> implements ISmartColumn<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSmartColumn.class);

  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private Class<? extends ICodeType> m_codeTypeClass;
  private LookupCall m_lookupCall;
  private boolean m_sortCodesByDisplayText;

  public AbstractSmartColumn() {
    super();
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(140)
  @ConfigPropertyValue("null")
  protected Class<? extends LookupCall> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(150)
  @ConfigPropertyValue("null")
  protected Class<? extends ICodeType> getConfiguredCodeType() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredSortCodesByDisplayText() {
    return false;
  }

  @ConfigOperation
  @Order(140)
  protected void execPrepareLookup(LookupCall call, ITableRow row) {
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSortCodesByDisplayText(getConfiguredSortCodesByDisplayText());
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lazy lookup decorator
    Class<? extends LookupCall> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      LookupCall call;
      try {
        call = lookupCallClass.newInstance();
        setLookupCall(call);
      }
      catch (InstantiationException e) {
        LOG.warn(null, e);
      }
      catch (IllegalAccessException e) {
        LOG.warn(null, e);
      }
    }
  }

  /*
   * Runtime
   */

  public Class<? extends ICodeType> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  public void setCodeTypeClass(Class<? extends ICodeType> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = new CodeLookupCall(m_codeTypeClass);
    }
  }

  public LookupCall getLookupCall() {
    return m_lookupCall;
  }

  public void setLookupCall(LookupCall call) {
    m_lookupCall = call;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isSortCodesByDisplayText() {
    return m_sortCodesByDisplayText;
  }

  /**
   * {@inheritDoc}
   */
  public void setSortCodesByDisplayText(boolean b) {
    m_sortCodesByDisplayText = b;
  }

  public LookupCall prepareLookupCall(ITableRow row) {
    if (getLookupCall() != null) {
      LookupCall call = (LookupCall) getLookupCall().clone();
      call.setKey(getValue(row));
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
  protected T parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    T validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (getDataType().isAssignableFrom(rawValue.getClass())) {
      validValue = (T) rawValue;
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
    AbstractSmartField<T> f = new AbstractSmartField<T>() {
      @Override
      public Class<T> getHolderType() {
        return AbstractSmartColumn.this.getDataType();
      }

      @Override
      protected void execPrepareLookup(LookupCall call) throws ProcessingException {
        AbstractSmartColumn.this.execPrepareLookup(call, row);
      }
    };

    f.setLookupCall(getLookupCall());
    f.setLabelVisible(false);
    f.setValue(getValue(row));
    f.markSaved();
    return f;
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    ICodeType codeType = getCodeTypeClass() != null ? CODES.getCodeType(getCodeTypeClass()) : null;
    LookupCall call = getLookupCall() != null ? getLookupCall() : null;
    if (codeType != null) {
      if (isSortCodesByDisplayText()) {
        String s1 = getDisplayText(r1);
        String s2 = getDisplayText(r2);
        return StringUtility.compareIgnoreCase(s1, s2);
      }
      else {
        T t1 = getValue(r1);
        T t2 = getValue(r2);
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
