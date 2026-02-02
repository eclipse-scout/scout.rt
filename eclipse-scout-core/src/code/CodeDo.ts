/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, CodeModel, typeName} from '../index';

@typeName('scout.Code')
export class CodeDo extends BaseDoEntity implements CodeModel<any> {
  id: string;
  objectType: string;
  modelClass: string;
  active: boolean;
  enabled: boolean;
  iconId: string;
  tooltipText: string;
  backgroundColor: string;
  foregroundColor: string;
  font: string;
  cssClass: string;
  extKey: string;
  value: number;
  partitionId: number;
  sortCode: number;
  fieldName: string;
  text: string;
  texts: Record<string, string>;
  children: CodeDo[];
}
