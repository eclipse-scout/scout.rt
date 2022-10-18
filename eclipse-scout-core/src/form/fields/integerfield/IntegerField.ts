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
import {DecimalFormat, Locale, NumberField, objects} from '../../../index';

export default class IntegerField extends NumberField {

  static MIN_VALUE = Number.MIN_SAFE_INTEGER;
  static MAX_VALUE = Number.MAX_SAFE_INTEGER;

  constructor() {
    super();
    this.minValue = IntegerField.MIN_VALUE;
    this.maxValue = IntegerField.MAX_VALUE;
  }

  protected override _getDefaultFormat(locale: Locale): string | DecimalFormat {
    return '#,##0';
  }

  protected override _parseValue(displayText: string): number {
    let result = super._parseValue(displayText);
    if (objects.isNullOrUndefined(result)) {
      return null;
    }
    return this.decimalFormat.round(result, false);
  }
}
