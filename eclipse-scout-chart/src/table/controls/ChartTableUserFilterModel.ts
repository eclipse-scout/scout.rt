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
import {TableMatrixKeyAxis, TableUserFilterModel} from '@eclipse-scout/core';
import {TableControlDeterministicKey} from './ChartTableControl';

export interface ChartTableUserFilterModel extends TableUserFilterModel {
  text?: string;
  xAxis?: TableMatrixKeyAxis;
  yAxis?: TableMatrixKeyAxis;
  filters?: { deterministicKey: TableControlDeterministicKey }[];
}
