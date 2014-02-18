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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public abstract class AbstractProposalColumn<LOOKUP_TYPE> extends AbstractContentAssistColumn<String, LOOKUP_TYPE> implements IProposalColumn<LOOKUP_TYPE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractProposalColumn.class);

  public AbstractProposalColumn() {
    super();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
  }

  @Override
  protected String parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    return (String) rawValue;
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    cell.setText((String) cell.getValue());
  }

  @Override
  protected IFormField prepareEditInternal(final ITableRow row) throws ProcessingException {
    AbstractProposalField<LOOKUP_TYPE> f = new AbstractProposalField<LOOKUP_TYPE>() {
      @Override
      protected void initConfig() {
        super.initConfig();
        propertySupport.putPropertiesMap(AbstractProposalColumn.this.propertySupport.getPropertiesMap());
      }

      @Override
      public Class<String> getHolderType() {
        return String.class;
      }

      @Override
      protected void execPrepareLookup(ILookupCall<LOOKUP_TYPE> call) throws ProcessingException {
        AbstractProposalColumn.this.execPrepareLookup(call, row);
      }
    };

    f.setCodeTypeClass(getCodeTypeClass());
    f.setLookupCall(getLookupCall());
    f.setBrowseHierarchy(getConfiguredBrowseHierarchy());
    f.setBrowseMaxRowCount(getConfiguredBrowseMaxRowCount());
    f.setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    f.setBrowseAutoExpandAll(getConfiguredBrowseAutoExpandAll());
    f.setBrowseLoadIncremental(getConfiguredBrowseLoadIncremental());
    return f;
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    ICodeType<?, LOOKUP_TYPE> codeType = getCodeTypeClass() != null ? CODES.getCodeType(getCodeTypeClass()) : null;
    ILookupCall<LOOKUP_TYPE> call = getLookupCall() != null ? getLookupCall() : null;
    if (codeType != null) {
      String s1 = getDisplayText(r1);
      String s2 = getDisplayText(r2);
      return StringUtility.compareIgnoreCase(s1, s2);
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
