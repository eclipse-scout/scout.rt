/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Alignment} from '../../cell/Cell';

/**
 * The ColumnDescriptor is used to define texts, widths and order of columns.
 * It is typically used for smart fields with a proposal chooser of type table.
 */
export default interface ColumnDescriptor {
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
