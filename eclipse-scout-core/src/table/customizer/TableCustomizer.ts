/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, InitModelOf, ITableCustomizerDo, ObjectWithType, scout, Session, Table, TableCustomizerModel} from '../../index';

export abstract class TableCustomizer implements TableCustomizerModel, ObjectWithType {
  declare model: TableCustomizerModel;

  objectType: string;
  parent: Table;

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

  /**
   * Adjusts the table according to the given customizer data. Existing custom columns are removed and replaced
   * by new custom columns.
   */
  abstract setCustomizerData(customizerData: ITableCustomizerDo);

  /**
   * Returns a data object describing the custom columns. Can be persisted and re-applied later using {@link setCustomizerData}.
   */
  abstract getCustomizerData(): ITableCustomizerDo;

  // --------------------

  abstract addColumn(insertAfterColumn?: Column<any>): JQuery.Promise<void>;

  abstract modifyColumn(column: Column<any>): JQuery.Promise<void>;

  abstract removeColumns(columns: Column<any>[]);

  abstract removeAllColumns();

  abstract isCustomizable(column: Column<any>): boolean;
}
