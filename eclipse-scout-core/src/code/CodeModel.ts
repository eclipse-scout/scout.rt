/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, CodeType, InitModelOf, ObjectModel} from '../index';

export interface CodeModel<TCodeId> extends ObjectModel<Code<TCodeId>, TCodeId> {
  /**
   * If the Code is loaded from the Scout Java server and the application is running in dev mode, this property stores the Java Code class this code comes from.
   */
  modelClass?: string;
  /**
   * Specifies if the Code is active. Default is true.
   */
  active?: boolean;
  /**
   * Specifies if the Code is enabled. Default is true.
   */
  enabled?: boolean;
  /**
   * Icon to use for this code.
   */
  iconId?: string;
  /**
   * The tooltip to show for this Code.
   */
  tooltipText?: string;
  /**
   * The background color of the Code.
   */
  backgroundColor?: string;
  /**
   * The foreground color of the Code.
   */
  foregroundColor?: string;
  /**
   * The font to use for this Code.
   */
  font?: string;
  /**
   * Space separated list of additional css classes to add for this Code.
   */
  cssClass?: string;
  /**
   * An external key for this Code.
   */
  extKey?: string;
  /**
   * The value of this Code.
   */
  value?: number;
  /**
   * The partition of this Code. Default is 0.
   */
  partitionId?: number;
  /**
   * The sort code of this Code within the CodeType. Default is -1.
   */
  sortCode?: number;
  /**
   * If the Code is part of a CodeType and the CodeType declares a field with this name, this Code is written into that field.
   */
  fieldName?: string;
  /**
   * Text key for this Code. Must be of the form '${textKey:key}'. If this property is specified, the texts property must be null.
   */
  text?: string;
  /**
   * Map of languageTag to the corresponding text in that language. If this map is specified, the text property must be null.
   */
  texts?: Record<string, string>;
  /**
   * The child Codes of this code.
   */
  children?: InitModelOf<Code<TCodeId>>[];
  /**
   * The CodeType instance this Code belongs to.
   */
  codeType?: CodeType<TCodeId, Code<TCodeId>, any>;
}
