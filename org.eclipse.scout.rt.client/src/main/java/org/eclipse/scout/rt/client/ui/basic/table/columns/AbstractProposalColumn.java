/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IProposalColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

@ClassId("7e21b24b-ddd8-4114-8b82-91dd396bf11b")
public abstract class AbstractProposalColumn<LOOKUP_TYPE> extends AbstractSmartColumn<LOOKUP_TYPE> implements IProposalColumn<LOOKUP_TYPE> {

  public AbstractProposalColumn() {
    this(true);
  }

  public AbstractProposalColumn(boolean callInitializer) {
    super(callInitializer);
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
  public void updateDisplayText(ITableRow row, LOOKUP_TYPE value) {
    Cell cell = row.getCellForUpdate(this);
    updateDisplayText(row, cell, (String) value);
  }

  @SuppressWarnings("unchecked")
  private void updateDisplayText(ITableRow row, Cell cell, String value) {
    cell.setText(formatValueInternal(row, (LOOKUP_TYPE) value));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected LOOKUP_TYPE parseValueInternal(ITableRow row, Object rawValue) {
    return (LOOKUP_TYPE) rawValue;
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    updateDisplayText(row, cell);
  }

  @Override
  protected String formatValueInternal(ITableRow row, LOOKUP_TYPE value) {
    return (String) value;
  }

  @Override
  protected IFormField prepareEditInternal(final ITableRow row) {
    ProposalField2Editor f = (ProposalField2Editor) getDefaultEditor();
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
  protected ProposalField2Editor createDefaultEditor() {
    return new ProposalField2Editor();
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

  protected static class LocalProposalColumnExtension<LOOKUP_TYPE, OWNER extends AbstractProposalColumn<LOOKUP_TYPE>> extends LocalSmartColumnExtension<LOOKUP_TYPE, OWNER>
      implements IProposalColumnExtension<LOOKUP_TYPE, OWNER> {

    public LocalProposalColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IProposalColumnExtension<LOOKUP_TYPE, ? extends AbstractProposalColumn<LOOKUP_TYPE>> createLocalExtension() {
    return new LocalProposalColumnExtension<>(this);
  }

  /**
   * Internal editor field
   */
  @ClassId("45103179-6dc2-47b1-9b77-790507533714")
  @SuppressWarnings("bsiRulesDefinition:orderMissing")
  protected class ProposalField2Editor extends AbstractProposalField<LOOKUP_TYPE> {
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
    public Class<LOOKUP_TYPE> getHolderType() {
      return AbstractProposalColumn.this.getDataType();
    }

    @Override
    protected void execPrepareLookup(ILookupCall<LOOKUP_TYPE> call) {
      AbstractProposalColumn.this.interceptPrepareLookup(call, getRow());
    }
  }
}
