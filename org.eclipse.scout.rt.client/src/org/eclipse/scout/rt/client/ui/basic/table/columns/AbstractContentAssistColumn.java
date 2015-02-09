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

import java.util.List;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ContentAssistColumnChains.ContentAssistColumnConvertValueToKeyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ContentAssistColumnChains.ContentAssistColumnPrepareLookupChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IColumnExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IContentAssistColumnExtension;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.services.lookup.TableProvisioningContext;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractContentAssistColumn<VALUE, LOOKUP_TYPE> extends AbstractColumn<VALUE> implements IContentAssistColumn<VALUE, LOOKUP_TYPE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractContentAssistColumn.class);

  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private Class<? extends ICodeType<?, LOOKUP_TYPE>> m_codeTypeClass;
  private ILookupCall<LOOKUP_TYPE> m_lookupCall;

  public AbstractContentAssistColumn() {
    super();
  }

  /*
   * Configuration
   */

  /**
   * Configures the lookup call used to determine the display text of the smart column value.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Lookup call class for this column.
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(140)
  protected Class<? extends ILookupCall<LOOKUP_TYPE>> getConfiguredLookupCall() {
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
  protected Class<? extends ICodeType<?, LOOKUP_TYPE>> getConfiguredCodeType() {
    return null;
  }

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

  @ConfigOperation
  @Order(140)
  protected void execPrepareLookup(ILookupCall<LOOKUP_TYPE> call, ITableRow row) {
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
  protected LOOKUP_TYPE execConvertValueToKey(VALUE value) {
    return (LOOKUP_TYPE) value;
  }

  @Override
  public ILookupCall<LOOKUP_TYPE> prepareLookupCall(ITableRow row) {
    if (getLookupCall() != null) {
      ILookupCall<LOOKUP_TYPE> call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new TableProvisioningContext(getTable(), row, this));
      call.setKey(interceptConvertValueToKey(getValueInternal(row)));
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
  protected void initConfig() {
    super.initConfig();
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lazy lookup decorator
    Class<? extends ILookupCall<LOOKUP_TYPE>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      ILookupCall<LOOKUP_TYPE> call;
      try {
        call = lookupCallClass.newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + lookupCallClass.getName() + "'.", e));
      }
    }
  }

  /*
   * Runtime
   */

  @Override
  public Class<? extends ICodeType<?, LOOKUP_TYPE>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, LOOKUP_TYPE>> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
    }
    validateColumnValues();
  }

  @Override
  public ILookupCall<LOOKUP_TYPE> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<LOOKUP_TYPE> call) {
    m_lookupCall = call;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected VALUE parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
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
      catch (Exception e) {
        throw new ProcessingException("invalid " + getDataType().getSimpleName() + " value in column '" + getClass().getName() + "': " + rawValue + " class=" + rawValue.getClass());
      }
    }
    return validValue;
  }

  protected final LOOKUP_TYPE interceptConvertValueToKey(VALUE value) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ContentAssistColumnConvertValueToKeyChain<VALUE, LOOKUP_TYPE> chain = new ContentAssistColumnConvertValueToKeyChain<VALUE, LOOKUP_TYPE>(extensions);
    return chain.execConvertValueToKey(value);
  }

  protected final void interceptPrepareLookup(ILookupCall<LOOKUP_TYPE> call, ITableRow row) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ContentAssistColumnPrepareLookupChain<VALUE, LOOKUP_TYPE> chain = new ContentAssistColumnPrepareLookupChain<VALUE, LOOKUP_TYPE>(extensions);
    chain.execPrepareLookup(call, row);
  }

  protected static class LocalContentAssistColumnExtension<VALUE, LOOKUP_TYPE, OWNER extends AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>> extends LocalColumnExtension<VALUE, OWNER> implements IContentAssistColumnExtension<VALUE, LOOKUP_TYPE, OWNER> {

    public LocalContentAssistColumnExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public LOOKUP_TYPE execConvertValueToKey(ContentAssistColumnConvertValueToKeyChain<VALUE, LOOKUP_TYPE> chain, VALUE value) {
      return getOwner().execConvertValueToKey(value);
    }

    @Override
    public void execPrepareLookup(ContentAssistColumnPrepareLookupChain<VALUE, LOOKUP_TYPE> chain, ILookupCall<LOOKUP_TYPE> call, ITableRow row) {
      getOwner().execPrepareLookup(call, row);
    }
  }

  @Override
  protected IContentAssistColumnExtension<VALUE, LOOKUP_TYPE, ? extends AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>> createLocalExtension() {
    return new LocalContentAssistColumnExtension<VALUE, LOOKUP_TYPE, AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>>(this);
  }

}
