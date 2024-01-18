/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, CodeType, ModelOf, ObjectModel} from '../index';

export interface CodeTypeModel<TCodeId> extends ObjectModel<CodeType<TCodeId>> {
  /**
   * If the Code is loaded from the Scout Java server and the application is running in dev mode, this property stores the Java Code class this code comes from.
   */
  modelClass?: string;
  /**
   * Icon to use for this code.
   */
  iconId?: string;
  /**
   * Specifies if the CodeType is hierarchical. Default is false.
   */
  hierarchical?: boolean;
  /**
   * Specifies the max level of this CodeType. Default is 2147483647.
   */
  maxLevel?: number;
  /**
   * Map of languageTag to the corresponding text in that language.
   */
  texts?: Record<string, string>;
  /**
   * Map of languageTag to the corresponding plural text in that language.
   */
  textsPlural?: Record<string, string>;
  /**
   * The root Codes of the CodeType.
   */
  codes?: ModelOf<Code<TCodeId>>[];
}
