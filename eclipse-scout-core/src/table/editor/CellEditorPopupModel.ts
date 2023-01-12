/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, Column, PopupModel, Table, TableRow} from '../../index';

export interface CellEditorPopupModel<TValue> extends PopupModel {
  column?: Column<TValue>;
  cell?: Cell<TValue>;
  table?: Table;
  row?: TableRow;
}
