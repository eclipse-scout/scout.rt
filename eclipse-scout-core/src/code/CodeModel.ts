/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, CodeType, FullModelOf, ObjectModel} from '../index';

export interface CodeModel<TCodeId> extends ObjectModel<Code<TCodeId>, TCodeId> {
  modelClass?: string;
  active?: boolean;
  enabled?: boolean;
  iconId?: string;
  tooltipText?: string;
  backgroundColor?: string;
  foregroundColor?: string;
  font?: string;
  cssClass?: string;
  extKey?: string;
  value?: number;
  partitionId?: number;
  sortCode?: number;
  fieldName?: string;
  /**
   * Text Key
   */
  text?: string;
  /**
   * Map of languageTag to the corresponding text in that language
   */
  texts?: Record<string, string>;
  children?: FullModelOf<Code<TCodeId>>[];
  codeType?: CodeType<TCodeId>;
}
