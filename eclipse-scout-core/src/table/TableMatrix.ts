/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BooleanColumn, Column, comparators, DateColumn, DateFormat, DateGroupType, dates, EnumObject, IconColumn, Locale, NumberColumn, scout, Session, Table, TableRow} from '../index';

export class TableMatrix {

  protected _table: Table;
  protected _rows: TableRow[];
  protected _allData: TableMatrixDataAxis[];
  protected _allAxis: TableMatrixKeyAxis[];

  constructor(table: Table) {
    this._table = scout.assertParameter('table', table);
    this._rows = table.rows;
    this._allData = [];
    this._allAxis = [];
  }

  static DateGroup = {
    NONE: 0,
    YEAR: 256,
    MONTH: 257,
    MONTH_AND_YEAR: 260,
    CALENDAR_WEEK: 261,
    WEEKDAY: 258,
    DATE: 259
  } as const;

  static NumberGroup = {
    COUNT: -1,
    SUM: 1,
    AVG: 2
  } as const;

  get session(): Session {
    return this._table.session;
  }

  get locale(): Locale {
    return this.session.locale;
  }

  /**
   * Adds a new data axis (value) that operates on the given table column.
   */
  addData(data: Column<any>, dataGroup: TableMatrixNumberGroup): TableMatrixDataAxis {
    // @ts-expect-error
    const dataAxis: TableMatrixDataAxis = {};
    const locale = this.locale;

    // collect all axis
    this._allData.push(dataAxis);

    // copy column for later access
    dataAxis.column = data;

    // data always is number
    dataAxis.format = n => locale.decimalFormat.format(n);

    // count, sum, avg
    if (dataGroup === TableMatrix.NumberGroup.COUNT) {
      dataAxis.norm = f => 1;
      dataAxis.group = array => array.length;
    } else if (dataGroup === TableMatrix.NumberGroup.SUM) {
      dataAxis.norm = f => {
        if (isNaN(f) || f === null || f === '') {
          return null;
        }
        return parseFloat(f);
      };
      dataAxis.group = array => array.reduce((a, b) => a + b);
    } else if (dataGroup === TableMatrix.NumberGroup.AVG) {
      dataAxis.norm = f => {
        if (isNaN(f) || f === null || f === '') {
          return null;
        }
        return parseFloat(f);
      };
      dataAxis.group = array => {
        let sum = array.reduce((a, b) => a + b);
        let count = array.reduce((a, b) => b === null ? a : a + 1, 0);
        if (count === 0) {
          return null;
        }
        return sum / count;
      };
    }
    return dataAxis;
  }

  /**
   * Adds a new key axis (x or y) that operates on the data in the given table column.
   */
  addAxis(axis: Column<any>, axisGroup: TableMatrixNumberGroup | TableMatrixDateGroup): TableMatrixKeyAxis {
    // @ts-expect-error
    const keyAxis: TableMatrixKeyAxis = [];
    const locale = this.locale;
    const session = this.session;
    const emptyCell = session.text('ui.EmptyCell');

    // collect all axis
    this._allAxis.push(keyAxis);
    keyAxis.column = axis;

    // normalized string data
    keyAxis.normTable = [];

    keyAxis.sortCodeMap = {};

    // add a key to the axis
    keyAxis.add = k => {
      if (keyAxis.indexOf(k) === -1) {
        keyAxis.push(k);
      }
    };

    // Helper function to create a sort function that always sorts 'null' at the end.
    // We use this to make sure '-empty-' is at the bottom.
    const nullsLast = (compareFn: (a: any, b: any) => number) => {
      return (a, b) => {
        if (a === null && b === null) {
          return 0;
        }
        if (a === null) {
          return 1;
        }
        if (b === null) {
          return -1;
        }
        return compareFn(a, b);
      };
    };

    // default functions
    keyAxis.reorder = () => {
      keyAxis.sort(nullsLast((a: number, b: number): number => {
        let sortCodeA = keyAxis.sortCodeMap[a];
        let sortCodeB = keyAxis.sortCodeMap[b];
        let c = comparators.NUMERIC.compare(sortCodeA, sortCodeB);
        if (c) {
          return c;
        }
        return keyAxis.compareKeys(a, b);
      }));
    };
    keyAxis.compareKeys = (a: number, b: number): number => a - b;
    keyAxis.norm = f => {
      if (f === null || f === '') {
        return null;
      }
      let index = keyAxis.normTable.indexOf(f);
      if (index === -1) {
        return keyAxis.normTable.push(f) - 1;
      }
      return index;
    };
    keyAxis.format = n => {
      if (n === null) {
        return emptyCell;
      }
      return keyAxis.normTable[n];
    };
    keyAxis.keyToDeterministicKey = n => {
      if (n === null) {
        return null;
      }
      return keyAxis.format(n);
    };
    keyAxis.deterministicKeyToKey = d => keyAxis.norm(d);
    keyAxis.normDeterministic = f => keyAxis.keyToDeterministicKey(keyAxis.norm(f));

    // norm and format depends on datatype and group functionality
    if (axis instanceof DateColumn) {
      // deterministic key is always a number
      keyAxis.keyToDeterministicKey = (n: number): number => n;
      keyAxis.deterministicKeyToKey = (d: number): number => d;

      // Milliseconds in a normal day
      const DAY_MILLIS = 24 * 60 * 60 * 1000;
      // Offset from "local midnight" to "UTC midnight"
      const LOCAL_EPOCH_OFFSET_MILLIS = new Date(1970, 0, 1).getTime();

      if (axisGroup === TableMatrix.DateGroup.NONE) {
        // Dates from the server are sent without timezone, i.e. they look the same for all users but don't represent the same
        // point in time. To make sure all users get the same numeric value, we shift it according to the local timezone.
        //
        // Example:
        //
        // dates.parseJsonDate('2025-10-20')                                      getTime()       LOCAL_EPOCH_OFFSET_MILLIS            norm()
        // -------------------------------------------------------------------------------------------------------------------------------------
        // Mon Oct 20 2025 00:00:00 GMT+0000 (Coordinated Universal Time)         1760918400000                           0      1760918400000
        // Mon Oct 20 2025 00:00:00 GMT+0100 (Central European Standard Time)     1760914800000                    -3600000      1760918400000
        // Mon Oct 20 2025 00:00:00 GMT+0200 (Central European Summer Time)       1760911200000                    -7200000      1760918400000
        // Mon Oct 20 2025 00:00:00 GMT-0800 (Pacific Standard Time)              1760947200000                    28800000      1760918400000
        // Mon Oct 20 2025 00:00:00 GMT-0700 (Pacific Daylight Time)              1760943600000                    25200000      1760918400000
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          return f.getTime() - LOCAL_EPOCH_OFFSET_MILLIS;
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          let format = DateFormat.ensure(locale, axis.format || locale.dateFormat);
          return format.format(new Date(n + LOCAL_EPOCH_OFFSET_MILLIS));
        };
      } else if (axisGroup === TableMatrix.DateGroup.YEAR) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          return f.getFullYear();
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          return String(n);
        };
      } else if (axisGroup === TableMatrix.DateGroup.MONTH) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          return f.getMonth();
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          return locale.dateFormatSymbols.months[n];
        };
      } else if (axisGroup === TableMatrix.DateGroup.MONTH_AND_YEAR) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          // months since 1970-01-01
          return ((f.getFullYear() - 1970) * 12) + f.getMonth();
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          let date = dates.shift(new Date(1970, 0, 1), 0, n);
          return dates.format(date, locale, 'MMMM yyyy');
        };
      } else if (axisGroup === TableMatrix.DateGroup.CALENDAR_WEEK) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          return dates.weekInYear(f);
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          return session.text('ui.CW', n);
        };
      } else if (axisGroup === TableMatrix.DateGroup.WEEKDAY) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          return (f.getDay() + 7 - locale.dateFormatSymbols.firstDayOfWeek) % 7;
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          return locale.dateFormatSymbols.weekdaysOrdered[n];
        };
        // Convert locale-dependent weekday (0 = firstDayOfWeek) to locale-independent weekday (0 = Sun)
        keyAxis.keyToDeterministicKey = (n: number): number => {
          if (n === null) {
            return null;
          }
          return (n + locale.dateFormatSymbols.firstDayOfWeek) % 7;
        };
        keyAxis.deterministicKeyToKey = (d: number): number => {
          if (d === null) {
            return null;
          }
          return (d + 7 - locale.dateFormatSymbols.firstDayOfWeek) % 7;
        };
      } else if (axisGroup === TableMatrix.DateGroup.DATE) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          // Truncate to midnight in UTC, so that dividing by DAY_MILLIS will result in a whole number
          let utcMillis = Date.UTC(f.getFullYear(), f.getMonth(), f.getDate());
          return utcMillis / DAY_MILLIS;
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          let utcMillis = n * DAY_MILLIS;
          // shift "UTC midnight" to "local midnight"
          let date = new Date(utcMillis + LOCAL_EPOCH_OFFSET_MILLIS);
          return dates.format(date, locale, locale.dateFormatPatternDefault);
        };
      }
    } else if (axis instanceof NumberColumn) {
      keyAxis.norm = f => {
        if (isNaN(f) || f === null || f === '') {
          return null;
        }
        return parseFloat(f);
      };
      keyAxis.format = n => {
        if (isNaN(n) || n === null) {
          return emptyCell;
        }
        return axis.decimalFormat.format(n);
      };
      // deterministic key is always a number
      keyAxis.keyToDeterministicKey = (n: number): number => n;
      keyAxis.deterministicKeyToKey = (d: number): number => d;
    } else if (axis instanceof BooleanColumn) {
      keyAxis.norm = f => {
        if (axis.triStateEnabled && f === null) {
          return -1;
        }
        if (f === true) {
          return 1;
        }
        return 0;
      };
      keyAxis.format = n => {
        if (n === -1) {
          return session.text('ui.BooleanColumnGroupingMixed');
        }
        if (n === 0) {
          return session.text('ui.BooleanColumnGroupingFalse');
        }
        if (n === 1) {
          return session.text('ui.BooleanColumnGroupingTrue');
        }
        return '';
      };
      // use inverse order -> true, false, mixed
      keyAxis.compareKeys = (a: number, b: number): number => b - a;
      // deterministic key is always a number
      keyAxis.keyToDeterministicKey = (n: number): number => n;
      keyAxis.deterministicKeyToKey = (d: number): number => d;
    } else if (axis instanceof IconColumn) {
      keyAxis.isIcon = true;
    } else {
      comparators.TEXT.install(session);
      keyAxis.compareKeys = (a: number, b: number): number => {
        return comparators.TEXT.compare(keyAxis.format(a), keyAxis.format(b));
      };
    }

    return keyAxis;
  }

  /**
   * @returns a cube containing the results
   */
  calculate(): TableMatrixResult {
    let cube: Record<string, Array<number[] | number>> & { length?: number; getValue?(keys: number[]): number[] } = {};
    let length = 0;

    // collect data from table
    for (let r = 0; r < this._rows.length; r++) {
      let row = this._rows[r];
      // collect keys of x- and y-axis from row
      let keys: number[] = [];
      for (let k = 0; k < this._allAxis.length; k++) {
        let column = this._allAxis[k].column;
        let key = column.cellValueOrTextForCalculation(row);
        let normKey = this._allAxis[k].norm(key);

        if (normKey !== undefined) {
          this._allAxis[k].add(normKey);
          let cell = column.cell(row);
          if (cell.sortCode !== null) {
            this._allAxis[k].sortCodeMap[normKey] = cell.sortCode;
          }
          keys.push(normKey);
        }
      }
      let keysString = JSON.stringify(keys);

      // collect values of data axis from row
      let values: number[] = [];
      for (let v = 0; v < this._allData.length; v++) {
        let data = this._table.cellValue(this._allData[v].column, row);
        let normData = this._allData[v].norm(data);
        if (normData !== undefined) {
          values.push(normData);
        }
      }

      // build cube
      if (cube[keysString]) {
        cube[keysString].push(values);
      } else {
        cube[keysString] = [values];
        length++;
      }
    }

    // group values and find sum, min and max of data axis
    for (let v = 0; v < this._allData.length; v++) {
      let data = this._allData[v];

      data.total = 0;
      data.min = null;
      data.max = null;

      for (let k in cube) {
        if (cube.hasOwnProperty(k)) {
          let allCell = cube[k],
            subCell: number[] = [];

          for (let i = 0; i < allCell.length; i++) {
            subCell.push(allCell[i][v]);
          }

          let newValue = this._allData[v].group(subCell);
          cube[k][v] = newValue;
          data.total += newValue;

          if (newValue === null) {
            continue;
          }

          if (newValue < data.min || data.min === null) {
            data.min = newValue;
          }
          if (newValue > data.max || data.min === null) {
            data.max = newValue;
          }
        }
      }

      // To calculate correct y-axis scale data.max must not be 0. If data.max===0-> log(data.max)=-infinity
      if (scout.nvl(data.max, 0) === 0) {
        data.max = 0.1;
      }

      let f = Math.ceil(Math.log(data.max) / Math.LN10) - 1;

      data.max = Math.ceil(data.max / Math.pow(10, f)) * Math.pow(10, f);
      data.max = Math.ceil(data.max / 4) * 4;
    }

    // find dimensions and sort for x, y axis
    for (let k = 0; k < this._allAxis.length; k++) {
      let key = this._allAxis[k];

      key.min = arrays.min(key);
      key.max = arrays.max(key);

      // null value should be handled as first value (in charts)
      if (key.indexOf(null) !== -1) {
        key.max = key.max + 1;
      }

      key.reorder();
    }

    // access function used by chart
    cube.getValue = keys => {
      let keysString = JSON.stringify(keys);
      if (cube.hasOwnProperty(keysString)) {
        return cube[keysString] as number[];
      }
      return null;
    };

    cube.length = length;
    return cube as TableMatrixResult; // cast necessary because in this method cube temporary contains an Array<number | number[]>. But in the end it is reduced to only number[].
  }

  /**
   * @returns Array holding an entry for each column. Each entry consists of an array with the column at index 0 and the count at index 1.
   */
  columnCount(filterNumberColumns?: boolean): Array<Array<Column<any> | number>> {
    let columns = this.columns(filterNumberColumns),
      colCount: Array<Array<Column<any> | any[] | number>> = [],
      count = 0;

    for (let c = 0; c < columns.length; c++) {
      let column = columns[c];
      colCount.push([column, []]);

      let values = colCount[count][1] as any[];
      for (let r = 0; r < this._rows.length; r++) {
        let row = this._rows[r];
        let cellValue = column.cellValueOrTextForCalculation(row);
        if (values.indexOf(cellValue) === -1) {
          values.push(cellValue);
        }
      }

      colCount[count][1] = values.length;
      count++;
    }
    return colCount as Array<Array<Column<any> | number>>;
  }

  isEmpty(): boolean {
    return this._rows.length === 0 || this.columns().length === 0;
  }

  /**
   * @returns valid columns for table-matrix (not instance of NumberColumn and not guiOnly)
   * @param filterNumberColumns whether or not to filter NumberColumn, default is true
   */
  columns(filterNumberColumns?: boolean): Column<any>[] {
    filterNumberColumns = scout.nvl(filterNumberColumns, true);
    return this._table.visibleColumns(false, true).filter(column => {
      if (filterNumberColumns && column instanceof NumberColumn) {
        return false;
      }
      return true;
    });
  }

  /**
   * Table rows and columns are not always in a consistent state.
   * @returns true, if table is in a valid, consistent state
   */
  isMatrixValid(): boolean {
    return this._table.rows.length === 0 || this._table.filterColumns(() => true, false).length === this._table.rows[0].cells.length;
  }

  /**
   * Converts the given {@link DateGroupType} enum to the corresponding {@link TableMatrixDateGroup}.
   */
  static resolveDateGroup(groupType: DateGroupType): TableMatrixDateGroup {
    switch (groupType) {
      case DateGroupType.YEAR:
        return TableMatrix.DateGroup.YEAR;
      case DateGroupType.MONTH:
        return TableMatrix.DateGroup.MONTH;
      case DateGroupType.MONTH_AND_YEAR:
        return TableMatrix.DateGroup.MONTH_AND_YEAR;
      case DateGroupType.CALENDAR_WEEK:
        return TableMatrix.DateGroup.CALENDAR_WEEK;
      case DateGroupType.WEEKDAY:
        return TableMatrix.DateGroup.WEEKDAY;
      case DateGroupType.DATE:
        return TableMatrix.DateGroup.DATE;
    }
    return null;
  }
}

export type TableMatrixNumberGroup = EnumObject<typeof TableMatrix.NumberGroup>;
export type TableMatrixDateGroup = EnumObject<typeof TableMatrix.DateGroup>;

export type TableMatrixKeyAxis = number[] & {
  column: Column<any>;
  normTable: string[];
  sortCodeMap: Record<number, number>;
  isIcon?: boolean;
  iconId?: string;
  /** The smallest numeric key in this axis */
  min: number;
  /** The biggest numeric key in this axis */
  max: number;
  /** Converts any value to a numeric key */
  norm(f: any): number;
  /** Formats the given numeric key ({@link norm}) for display */
  format(n: number): string;
  /** Adds the numeric key ({@link norm}) to this axis if it does not already exist */
  add(k: number);
  /** Converts the given numeric key ({@link norm}) to a persistable representation (aka "deterministic key") */
  keyToDeterministicKey(n: number): number | string;
  /** Converts the given persistable key (aka "deterministic key") back to a numeric key ({@link norm}) */
  deterministicKeyToKey(d: string | number): number;
  /** Same as {@link norm} + {@link keyToDeterministicKey} */
  normDeterministic(f: any): string | number;
  /** Sorts the keys in this axis. the default implementation first considers {@link sortCodeMap}, then calls {@link compareKeys}. */
  reorder(): void;
  /** Compares the given numeric keys ({@link norm}). used by {@link reorder}, both keys are not null. */
  compareKeys(n1: number, n2: number): number;
};

export type TableMatrixDataAxis = {
  column: Column<any>;
  total: number;
  min: number;
  max: number;
  /** Converts any value to a numeric key */
  norm(f: any): number;
  /** Formats the given numeric key ({@link norm}) for display */
  format(n: number): string;
  /** Aggregates the given values into a single value */
  group(array: number[]): number;
};

export type TableMatrixResult = Record<string, number[]> & { length: number; getValue(keys: number[]): number[] };
