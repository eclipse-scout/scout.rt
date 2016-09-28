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

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IProposalColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

@ClassId("ab22d90c-fedf-4f24-9ab2-4fd5096e36fb")
public abstract class AbstractProposalColumn<LOOKUP_TYPE> extends AbstractContentAssistColumn<String, LOOKUP_TYPE> implements IProposalColumn<LOOKUP_TYPE> {

  public AbstractProposalColumn() {
    super();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMaxLength(getConfiguredMaxLength());
    setTrimText(getConfiguredTrimText());
  }

  /**
   * Configures the initial value of {@link AbstractProposalField#getMaxLength() <p> Subclasses can override this
   * method<p> Default is 4000
   *
   * @since 6.1
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  /**
   * @return true if leading and trailing whitespace should be stripped from the entered text while validating the
   *         value. default is true.
   * @since 6.1
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredTrimText() {
    return true;
  }

  @Override
  public void setMaxLength(int maxLength) {
    propertySupport.setPropertyInt(PROP_MAX_LENGTH, Math.max(0, maxLength));
  }

  @Override
  public int getMaxLength() {
    return propertySupport.getPropertyInt(PROP_MAX_LENGTH);
  }

  @Override
  public void setTrimText(boolean b) {
    propertySupport.setPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE, b);
  }

  @Override
  public boolean isTrimText() {
    return propertySupport.getPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE);
  }

  @Override
  protected void updateDisplayTexts() {
    if (getTable() != null) {
      updateDisplayTexts(getTable().getRows());
    }
  }

  @Override
  public void updateDisplayTexts(List<ITableRow> rows) {
    for (ITableRow row : Assertions.assertNotNull(rows)) {
      updateDisplayText(row, row.getCellForUpdate(this));
    }
  }

  @Override
  public void updateDisplayText(ITableRow row, Cell cell) {
    updateDisplayText(row, cell, (String) cell.getValue());
  }

  @Override
  public void updateDisplayText(ITableRow row, String value) {
    Cell cell = row.getCellForUpdate(this);
    updateDisplayText(row, cell, value);
  }

  private void updateDisplayText(ITableRow row, Cell cell, String value) {
    cell.setText(formatValueInternal(row, value));
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

  protected void mapEditorFieldProperties(IProposalField<LOOKUP_TYPE> f) {
    super.mapEditorFieldProperties(f);
    f.setMaxLength(getMaxLength());
    f.setTrimText(isTrimText());
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
  @ClassId("ceb1ae13-664c-43ce-a670-034e1f36c0b5")
  @SuppressWarnings("bsiRulesDefinition:orderMissing")
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
