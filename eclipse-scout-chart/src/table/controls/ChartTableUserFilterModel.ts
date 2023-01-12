/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableMatrixKeyAxis, TableUserFilterModel} from '@eclipse-scout/core';
import {TableControlDeterministicKey} from './ChartTableControl';

export interface ChartTableUserFilterModel extends TableUserFilterModel {
  text?: string;
  xAxis?: TableMatrixKeyAxis;
  yAxis?: TableMatrixKeyAxis;
  filters?: { deterministicKey: TableControlDeterministicKey }[];
}
