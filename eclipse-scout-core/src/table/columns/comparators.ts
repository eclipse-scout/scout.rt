/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Comparator, Device, objects, Session} from '../../index';
import $ from 'jquery';

export type ColumnComparator<T = any> = {
  compare(a: T, b: T): number;
  compareIgnoreCase?(a: T, b: T): number;
  /**
   * @returns whether it was possible to install a compare function.
   */
  install(session: Session): boolean;
  [key: string]: any;
};

export const comparators = {
  /**
   * Text comparator, used to compare strings with support for internationalization (i18n).
   * The collator object is only installed once.
   */
  TEXT: {
    collator: null,
    installed: false,
    install(session: Session) {
      if (this.installed) {
        return !!this.collator;
      }

      // set static collator variable once
      if (Device.get().supportsInternationalization()) {
        this.collator = new window.Intl.Collator(session.locale.languageTag);
        $.log.isInfoEnabled() && $.log.info('(comparators.TEXT#install) Browser supports i18n - installed Intl.Collator, can sort in Browser');
      } else {
        $.log.isInfoEnabled() && $.log.info('(comparators.TEXT#install) Browser doesn\'t support i18n. Must sort on server');
      }

      this.installed = true;
      return !!this.collator;
    },
    compare(valueA: string, valueB: string) {
      if (!valueA && !valueB) {
        return 0;
      }
      if (!valueA) {
        return -1;
      }
      if (!valueB) {
        return 1;
      }

      if (!this.collator) {
        // Fallback for browsers that don't support internationalization. This is only necessary
        // for callers that call this method without check for internationalization support
        // first (e.g. TableMatrix).
        return (valueA < valueB ? -1 : ((valueA > valueB) ? 1 : 0));
      }
      // We don't check the installed flag here. It's a program error when we come here
      // and the collator is not set. Either we forgot to call install() or we've called
      // install but the browser does not support i18n.
      return this.collator.compare(valueA, valueB);
    },
    compareIgnoreCase(valueA: string, valueB: string) {
      if (!valueA) {
        valueA = null;
      }
      if (!valueB) {
        valueB = null;
      }
      if (valueA === valueB) {
        return 0;
      }
      if (valueA === null) {
        return -1;
      }
      if (valueB === null) {
        return 1;
      }
      return this.compare(valueA.toLowerCase(), valueB.toLowerCase());
    }
  } as ColumnComparator<string>,

  /**
   * Numeric comparator, used to compare numeric values. Used for numbers, dates, etc.
   */
  NUMERIC: {
    install(session: Session) {
      return true;
    },
    compare(valueA: number, valueB: number) {
      if (objects.isNullOrUndefined(valueA) && objects.isNullOrUndefined(valueB)) {
        return 0;
      }
      if (objects.isNullOrUndefined(valueA)) {
        return -1;
      }
      if (objects.isNullOrUndefined(valueB)) {
        return 1;
      }

      if (valueA < valueB) {
        return -1;
      } else if (valueA > valueB) {
        return 1;
      }
      return 0;
    }
  } as ColumnComparator<number>,

  /**
   * Alphanumeric comparator.
   */
  ALPHANUMERIC: {
    collator: null,
    installed: false,
    install(session: Session) {
      comparators.TEXT.install(session);
      this.collator = comparators.TEXT.collator;
      return !!this.collator && comparators.NUMERIC.install(session);
    },
    compare(valueA: string | number, valueB: string | number) {
      return this._compare(valueA, valueB, false);
    },
    compareIgnoreCase(valueA: string | number, valueB: string | number) {
      return this._compare(valueA, valueB, true);
    },
    _compare(valueA: string | number, valueB: string | number, ignoreCase: boolean) {
      if (!valueA && !valueB) {
        return 0;
      }
      if (!valueA) {
        return -1;
      }
      if (!valueB) {
        return 1;
      }

      let pattern = '(([0-9]+)|([^0-9]+))';
      let regexp1 = new RegExp(pattern, 'g');
      let regexp2 = new RegExp(pattern, 'g');
      let found1 = regexp1.exec(valueA + '');
      let found2 = regexp2.exec(valueB + '');
      while (found1 && found2) {
        let n1 = parseInt(found1[1], 0);
        let n2 = parseInt(found2[1], 0);
        if (!isNaN(n1) && !isNaN(n2)) {
          let numericResult = comparators.NUMERIC.compare(n1, n2);
          if (numericResult !== 0) {
            return numericResult;
          }
        } else {
          let textResult = ignoreCase ? comparators.TEXT.compareIgnoreCase(found1[1], found2[1]) : comparators.TEXT.compare(found1[1], found2[1]);
          if (textResult !== 0) {
            return textResult;
          }
        }
        found1 = regexp1.exec(valueA + '');
        found2 = regexp2.exec(valueB + '');
      }

      if (!found1 && !found2) {
        return 0;
      }
      if (!found1) {
        return -1;
      }
      return 1;
    }
  } as ColumnComparator<string | number>,

  /**
   * Applies the comparator to each pair until one pair doesn't return 0 or all pairs are compared.
   * @param comparator a function that takes 2 parameters and returns -1, 0 or 1.
   * @param pairs array of pairs, where a pair is an array with 2 values.
   */
  compare<T>(comparator: Comparator<T>, ...pairs: T[][]): number {
    let result = 0;
    pairs.some(pair => {
      if (pair.length !== 2) {
        throw new Error('The pair must have exactly 2 elements');
      }
      result = comparator(pair[0], pair[1]);
      if (result !== 0) {
        return true;
      }
      return false;
    });
    return result;
  }
};
