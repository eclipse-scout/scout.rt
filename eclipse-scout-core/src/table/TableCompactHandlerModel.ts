/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, CompactLine, ObjectModel, Predicate, Table, TableCompactHandler} from '../index';

export interface TableCompactHandlerModel extends ObjectModel<TableCompactHandler> {
  /**
   * Defines the table for which the compact handler should be used.
   */
  table?: Table;
  /**
   * Defines whether invisible columns should be ignored for compact value generation.
   *
   * Default is true.
   */
  useOnlyVisibleColumns?: boolean;
  /**
   * Defines the maximum number of lines in the content block.
   *
   * If {@link moreLinkAvailable} is true and the more link would only reveal one line, the number is automatically increased by 1.
   *
   * Default is 3.
   */
  maxContentLines?: number;
  /**
   * Defines whether a more link should be displayed if needed.
   *
   * The more link is shown if there are more content lines than {@link maxContentLines}.
   * Clicking on the more link will reveal the other lines.
   *
   * Set this property to `false` to never show a more link.
   *
   * Default is true.
   */
  moreLinkAvailable?: boolean;
  /**
   * The customizer makes it possible to adjust every created compact line.
   *
   * There is no customizer set by default.
   */
  lineCustomizer?: (line: CompactLine) => void;
  /**
   * Defines filters that control which columns are used for compact value generation.
   *
   * To exclude a column, the predicate needs to return false.
   * If no filter is set, all columns are accepted, unless the column is {@link Column.guiOnly} or the columns is invisible and {@link useOnlyVisibleColumns} is true.
   *
   * By default, there are no column filters.
   */
  columnFilters?: Set<Predicate<Column>>;
}
