/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Alignment} from '../../index';

/**
 * The ColumnDescriptor is used to define texts, widths and order of columns.
 * It is typically used for smart fields with a proposal chooser of type table.
 */
export interface ColumnDescriptor {
  /**
   * Name of the corresponding property in the "additional table row data" or <code>null</code> if this descriptor describes the first (default) column.
   */
  propertyName?: string;
  text?: string;
  cssClass?: string;
  width?: number;
  fixedWidth?: boolean;
  fixedPosition?: boolean;
  autoOptimizeWidth?: boolean;
  horizontalAlignment?: Alignment;
  visible?: boolean;
  htmlEnabled?: boolean;
}
