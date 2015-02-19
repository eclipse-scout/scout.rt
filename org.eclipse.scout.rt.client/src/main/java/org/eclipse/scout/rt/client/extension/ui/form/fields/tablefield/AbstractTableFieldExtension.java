package org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldReloadTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveDeletedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveInsertedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveUpdatedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldUpdateTableStatusChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

public abstract class AbstractTableFieldExtension<T extends ITable, OWNER extends AbstractTableField<T>> extends AbstractFormFieldExtension<OWNER> implements ITableFieldExtension<T, OWNER> {

  public AbstractTableFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execReloadTableData(TableFieldReloadTableDataChain<? extends ITable> chain) throws ProcessingException {
    chain.execReloadTableData();
  }

  @Override
  public void execUpdateTableStatus(TableFieldUpdateTableStatusChain<? extends ITable> chain) {
    chain.execUpdateTableStatus();
  }

  @Override
  public void execSaveInsertedRow(TableFieldSaveInsertedRowChain<? extends ITable> chain, ITableRow row) throws ProcessingException {
    chain.execSaveInsertedRow(row);
  }

  @Override
  public void execSaveUpdatedRow(TableFieldSaveUpdatedRowChain<? extends ITable> chain, ITableRow row) throws ProcessingException {
    chain.execSaveUpdatedRow(row);
  }

  @Override
  public void execSaveDeletedRow(TableFieldSaveDeletedRowChain<? extends ITable> chain, ITableRow row) throws ProcessingException {
    chain.execSaveDeletedRow(row);
  }

  @Override
  public void execSave(TableFieldSaveChain<? extends ITable> chain, List<? extends ITableRow> insertedRows, List<? extends ITableRow> updatedRows, List<? extends ITableRow> deletedRows) {
    chain.execSave(insertedRows, updatedRows, deletedRows);
  }
}
