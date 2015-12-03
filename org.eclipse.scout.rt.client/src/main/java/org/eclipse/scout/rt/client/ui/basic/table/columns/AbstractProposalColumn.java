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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IProposalColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public abstract class AbstractProposalColumn<LOOKUP_TYPE> extends AbstractContentAssistColumn<String, LOOKUP_TYPE> implements IProposalColumn<LOOKUP_TYPE> {

  public AbstractProposalColumn() {
    super();
  }

  @Override
  protected String parseValueInternal(ITableRow row, Object rawValue) {
    return (String) rawValue;
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    updateDisplayText(row, cell);
  }

  @Override
  protected String formatValueInternal(ITableRow row, String value) {
    return value;
  }

  @Override
  protected IFormField prepareEditInternal(final ITableRow row) {
    ProposalEditorField f = (ProposalEditorField) getDefaultEditor();
    f.setRow(row);
    mapEditorFieldProperties(f);
    return f;
  }

  @Override
  protected IValueField<String> createDefaultEditor() {
    return new ProposalEditorField();
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    ICodeType<?, LOOKUP_TYPE> codeType = getCodeTypeClass() != null ? BEANS.opt(getCodeTypeClass()) : null;
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

  protected static class LocalProposalColumnExtension<LOOKUP_TYPE, OWNER extends AbstractProposalColumn<LOOKUP_TYPE>> extends LocalContentAssistColumnExtension<String, LOOKUP_TYPE, OWNER>
      implements IProposalColumnExtension<LOOKUP_TYPE, OWNER> {

    public LocalProposalColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IProposalColumnExtension<LOOKUP_TYPE, ? extends AbstractProposalColumn<LOOKUP_TYPE>> createLocalExtension() {
    return new LocalProposalColumnExtension<LOOKUP_TYPE, AbstractProposalColumn<LOOKUP_TYPE>>(this);
  }

  /**
   * Internal editor field
   */
  protected class ProposalEditorField extends AbstractProposalField<LOOKUP_TYPE> {
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
      propertySupport.putPropertiesMap(AbstractProposalColumn.this.propertySupport.getPropertiesMap());
    }

    @Override
    public Class<String> getHolderType() {
      return String.class;
    }

    @Override
    protected void execPrepareLookup(ILookupCall<LOOKUP_TYPE> call) {
      AbstractProposalColumn.this.interceptPrepareLookup(call, getRow());
    }
  }

}
