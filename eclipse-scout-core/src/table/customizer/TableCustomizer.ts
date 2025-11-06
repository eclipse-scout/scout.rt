/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, ChildModelOf, Column, FullModelOf, IColumnConfigDo, InitModelOf, ITableCustomizerDo, ObjectType, ObjectWithType, scout, Session, SomeRequired, Table, TableCustomizerModel} from '../../index';

/**
 * Manages custom columns for a table.
 */
export abstract class TableCustomizer implements TableCustomizerModel, ObjectWithType {
  declare model: TableCustomizerModel;
  declare initModel: SomeRequired<this['model'], 'parent'>;

  objectType: string;
  parent: Table;

  protected _busyCounter = 0;
  protected _readyPromise = Promise.resolve();
  protected _resolveReadyPromise: () => void;

  init(model: InitModelOf<this>) {
    scout.assertParameter('parent', model.parent);
    $.extend(this, model);
  }

  get table(): Table {
    return this.parent;
  }

  get session(): Session {
    return this.parent.session;
  }

  // --------------------

  get ready(): boolean {
    return !this.busy;
  }

  get busy(): boolean {
    return this._busyCounter > 0;
  }

  /**
   * Returns a promise that is resolved when all asynchronous tasks are done (e.g. creating columns).
   */
  whenReady(): Promise<void> {
    return this._readyPromise;
  }

  protected _setBusy(busy: boolean) {
    if (busy) {
      this._busyCounter++;
      if (this._busyCounter === 1) {
        // set unready
        this._readyPromise = new Promise((resolve, reject) => {
          this._resolveReadyPromise = resolve;
        });
      }
    } else {
      this._busyCounter = Math.max(this._busyCounter - 1, 0);
      if (this._busyCounter === 0) {
        this._resolveReadyPromise();
      }
    }
  }

  // --------------------

  /**
   * Adjusts the table according to the given customizer data. Existing custom columns are removed and replaced
   * by new custom columns. Depending on the implementation, creating columns may involve loading additional data.
   * Therefore, a promise is returned that is resolved when all columns are ready.
   */
  abstract setCustomizerData(customizerData: ITableCustomizerDo): Promise<void>;

  /**
   * Returns a data object describing the custom columns. Can be persisted and re-applied later using {@link setCustomizerData}.
   * If there are no custom columns, `null` is returned.
   */
  abstract getCustomizerData(): ITableCustomizerDo;

  // --------------------

  /**
   * Creates a new column instance for the given column configuration. The configuration is *not* added to the list of
   * customized columns (use {@link addCustomColumnConfig} for that).
   */
  async createColumn(columnConfig: IColumnConfigDo, options?: TableCustomizerCreateColumnsOptions): Promise<Column<any>> {
    return arrays.first(await this.createColumns(arrays.ensure(columnConfig), options));
  }

  /**
   * Creates new column instances for the given column configurations. The configurations are *not* added to the list of
   * customized columns (use {@link addCustomColumnConfig} for that).
   *
   * The resulting list has the same length and order as the input. Columns that could not be created are represented by `null`.
   */
  abstract createColumns(columnConfigs: IColumnConfigDo[], options?: TableCustomizerCreateColumnsOptions): Promise<Column<any>[]>;

  // --------------------

  /**
   * Opens a form that lets the user configure a new column. The new configuration is added to the list of customized columns,
   * then a new column instance is created and inserted into the table at the given position.
   *
   * Depending on the implementation, the form may allow the user to create additional columns ("save and new"). This method
   * returns a list of all created columns when the form is finally closed.
   */
  abstract addCustomColumn(positionOrInsertAfterColumn?: number | Column<any>): Promise<Column<any>[]>;

  /**
   * Same as {@link addCustomColumn} but without user interaction.
   */
  abstract addCustomColumnConfig(columnConfig: IColumnConfigDo, positionOrInsertAfterColumn?: number | Column<any>): Promise<Column<any>>;

  // --------------------

  /**
   * Opens a form that lets the user change the configuration of the given column. If the customizer does not have a column
   * configuration for the given column, nothing happens. When the form is stored, the existing configuration is replaced.
   * A new column instance is created and replaced in the table. The column ID remains unchanged.
   *
   * Depending on the implementation, the form may allow the user to create additional columns ("save and new"). This method
   * returns a list of all created columns when the form is finally closed.
   */
  abstract modifyCustomColumn(column: Column<any>): Promise<Column<any>[]>;

  /**
   * Same as {@link modifyCustomColumn} but without user interaction.
   */
  abstract modifyCustomColumnConfig(columnConfig: IColumnConfigDo): Promise<Column<any>>;

  // --------------------

  /**
   * Removes the given column from the table. If the column represents a custom column, the corresponding column configuration
   * is removed from the customizer as well.
   */
  removeCustomColumn(column: Column<any>) {
    this.removeCustomColumns(arrays.ensure(column));
  }

  /**
   * Removes the given columns from the table. If a column represents a custom column, the corresponding column configuration
   * is removed from the customizer as well.
   */
  abstract removeCustomColumns(columns: Column<any>[]);

  /**
   * Removes all custom columns from the table. The corresponding column configurations are removed from the customizer as well.
   * All other columns remain untouched.
   */
  abstract removeAllCustomColumns();

  // --------------------

  /**
   * Returns true if the given column is a "custom column", i.e. the customizer has a corresponding column configuration.
   * Note that columns that are simply created by {@link createColumn} are *not* considered to be custom columns.
   */
  abstract isCustomizable(column: Column<any>): boolean;

  // --------------------

  static ensure<TTableCustomizer extends TableCustomizer>(tableCustomizer: TableCustomizerOrModel<TTableCustomizer>, table: Table): TTableCustomizer {
    if (!tableCustomizer) {
      return tableCustomizer as TTableCustomizer;
    }
    if (tableCustomizer instanceof TableCustomizer) {
      return tableCustomizer;
    }
    if (typeof tableCustomizer === 'string' || typeof tableCustomizer === 'function') {
      return scout.create(tableCustomizer, {
        parent: table
      } as InitModelOf<TTableCustomizer>);
    }
    tableCustomizer.parent = table;
    return scout.create(tableCustomizer as FullModelOf<TTableCustomizer>);
  }
}

/**
 * A {@link TableCustomizer}, a {@link TableCustomizerModel} or an object type.
 */
export type TableCustomizerOrModel<TTableCustomizer extends TableCustomizer = TableCustomizer> = TTableCustomizer | ChildModelOf<TTableCustomizer> | ObjectType<TTableCustomizer>;

export interface TableCustomizerCreateColumnsOptions {
  /**
   * default is false
   */
  insertIntoTable?: boolean;
  /**
   * default is undefined (= insert at the end)
   */
  positionOrInsertAfterColumn?: number | Column<any>;
  /**
   * default is false, only relevant if {@link insertIntoTable} is set
   */
  applyPreferences?: boolean;
}
