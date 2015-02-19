package org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldReloadTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveDeletedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveInsertedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveUpdatedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldUpdateTableStatusChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

public interface ITableFieldExtension<T extends ITable, OWNER extends AbstractTableField<T>> extends IFormFieldExtension<OWNER> {

  void execReloadTableData(TableFieldReloadTableDataChain<? extends ITable> chain) throws ProcessingException;

  void execUpdateTableStatus(TableFieldUpdateTableStatusChain<? extends ITable> chain);

  void execSaveInsertedRow(TableFieldSaveInsertedRowChain<? extends ITable> chain, ITableRow row) throws ProcessingException;

  void execSaveUpdatedRow(TableFieldSaveUpdatedRowChain<? extends ITable> chain, ITableRow row) throws ProcessingException;

  void execSaveDeletedRow(TableFieldSaveDeletedRowChain<? extends ITable> chain, ITableRow row) throws ProcessingException;

  void execSave(TableFieldSaveChain<? extends ITable> chain, List<? extends ITableRow> insertedRows, List<? extends ITableRow> updatedRows, List<? extends ITableRow> deletedRows);
}
