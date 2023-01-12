/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BooleanColumn, Column, comparators, DateColumn, DateFormat, dates, EnumObject, IconColumn, Locale, NumberColumn, objects, scout, Session, Table, TableRow} from '../index';

export class TableMatrix {
  session: Session;
  locale: Locale;

  protected _allData: TableMatrixDataAxis[];
  protected _allAxis: TableMatrixKeyAxis[];
  protected _rows: TableRow[];
  protected _table: Table;

  constructor(table: Table, session: Session) {
    this.session = session;
    this.locale = session.locale;
    this._allData = [];
    this._allAxis = [];
    this._rows = table.rows;
    this._table = table;
  }

  static DateGroup = {
    NONE: 0,
    YEAR: 256,
    MONTH: 257,
    WEEKDAY: 258,
    DATE: 259
  } as const;

  static NumberGroup = {
    COUNT: -1,
    SUM: 1,
    AVG: 2
  } as const;

  /**
   * add data axis
   */
  addData(data: Column<any>, dataGroup: TableMatrixNumberGroup): TableMatrixDataAxis {
    // @ts-expect-error
    let dataAxis: TableMatrixDataAxis = {},
      locale = this.locale;

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

  // add x or y Axis
  addAxis(axis: Column<any>, axisGroup: TableMatrixNumberGroup | TableMatrixDateGroup): TableMatrixKeyAxis {
    // @ts-expect-error
    let keyAxis: TableMatrixKeyAxis = [],
      locale = this.locale,
      session = this.session,
      getText = this.session.text.bind(this.session),
      emptyCell = getText('ui.EmptyCell');

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

    // default functions
    keyAxis.reorder = () => {
      keyAxis.sort((a, b) => {
        // make sure -empty- is at the bottom
        if (a === null) {
          return 1;
        }
        if (b === null) {
          return -1;
        }
        let sortCodeA = keyAxis.sortCodeMap[a],
          sortCodeB = keyAxis.sortCodeMap[b];
        if (!objects.isNullOrUndefined(sortCodeA) || !objects.isNullOrUndefined(sortCodeB)) {
          return comparators.NUMERIC.compare(sortCodeA, sortCodeB);
        }
        // sort others
        return (a - b);
      });
    };
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
    keyAxis.deterministicKeyToKey = deterministicKey => keyAxis.norm(deterministicKey);
    keyAxis.keyToDeterministicKey = key => {
      if (key === null) {
        return null;
      }
      return keyAxis.format(key);
    };
    keyAxis.normDeterministic = f => keyAxis.keyToDeterministicKey(keyAxis.norm(f));

    // norm and format depends of datatype and group functionality
    if (axis instanceof DateColumn) {
      if (axisGroup === TableMatrix.DateGroup.NONE) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          return f.getTime();
        };
        keyAxis.format = n => {
          if (n === null) {
            return null;
          }
          let format = axis.format;
          if (format) {
            format = DateFormat.ensure(locale, format);
          } else {
            format = locale.dateFormat;
          }
          return format.format(new Date(n));
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
      } else if (axisGroup === TableMatrix.DateGroup.DATE) {
        keyAxis.norm = f => {
          if (f === null || f === '') {
            return null;
          }
          return dates.trunc(f).getTime();
        };
        keyAxis.format = n => {
          if (n === null) {
            return emptyCell;
          }
          return dates.format(new Date(n), locale, locale.dateFormatPatternDefault);
        };
      }
      keyAxis.deterministicKeyToKey = (deterministicKey: number) => deterministicKey;
      keyAxis.keyToDeterministicKey = key => key;
      keyAxis.normDeterministic = f => keyAxis.norm(f);
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
      keyAxis.deterministicKeyToKey = (deterministicKey: number) => deterministicKey;
      keyAxis.keyToDeterministicKey = key => key;
      keyAxis.normDeterministic = f => keyAxis.norm(f);
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
          return getText('ui.BooleanColumnGroupingMixed');
        }
        if (n === 0) {
          return getText('ui.BooleanColumnGroupingFalse');
        }
        if (n === 1) {
          return getText('ui.BooleanColumnGroupingTrue');
        }
      };
      keyAxis.deterministicKeyToKey = (deterministicKey: number) => deterministicKey;
      keyAxis.keyToDeterministicKey = key => key;
      keyAxis.normDeterministic = f => keyAxis.norm(f);
    } else if (axis instanceof IconColumn) {
      keyAxis.textIsIcon = true;
      keyAxis.deterministicKeyToKey = (deterministicKey: number) => deterministicKey;
      keyAxis.keyToDeterministicKey = key => key;
      keyAxis.normDeterministic = f => keyAxis.norm(f);
    } else {
      keyAxis.reorder = () => {
        let comparator = comparators.TEXT;
        comparator.install(session);

        keyAxis.sort((a, b) => {
          // make sure -empty- is at the bottom
          if (a === null) {
            return 1;
          }
          if (b === null) {
            return -1;
          }
          let sortCodeA = keyAxis.sortCodeMap[a],
            sortCodeB = keyAxis.sortCodeMap[b];
          if (!objects.isNullOrUndefined(sortCodeA) || !objects.isNullOrUndefined(sortCodeB)) {
            return comparators.NUMERIC.compare(sortCodeA, sortCodeB);
          }
          // sort others
          return comparator.compare(keyAxis.format(a), keyAxis.format(b));
        });
      };
    }
    return keyAxis;
  }

  /**
   * @returns a cube containing the results
   */
  calculate(): TableMatrixResult {
    let cube: Record<string, Array<number[] | number>> & { length?: number; getValue?(keys: number[]): number[] } = {}, length = 0;

    // collect data from table
    for (let r = 0; r < this._rows.length; r++) {
      let row = this._rows[r];
      // collect keys of x, y axis from row
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

      // To calculate correct y axis scale data.max must not be 0. If data.max===0-> log(data.max)=-infinity
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
   *
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
    return this._table.visibleColumns().filter(column => {
      if (column.guiOnly) {
        return false;
      }
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
    return this._table.rows.length === 0 || this.columns(false).length === this._table.rows[0].cells.length;
  }
}

export type TableMatrixNumberGroup = EnumObject<typeof TableMatrix.NumberGroup>;
export type TableMatrixDateGroup = EnumObject<typeof TableMatrix.DateGroup>;

export type TableMatrixKeyAxis = number[] & {
  column: Column<any>;
  normTable: string[];
  sortCodeMap: Record<number, number>;
  textIsIcon?: boolean;
  min: number;
  max: number;
  format(n: number): string;
  keyToDeterministicKey(n: number): number | string;
  deterministicKeyToKey(f: string | number): number;
  normDeterministic(f: any): string | number;
  norm(f: any): number;
  add(k: number);
  reorder(): void;
};

export type TableMatrixDataAxis = {
  column: Column<any>;
  total: number;
  min: number;
  max: number;
  format(n: number): string;
  norm(f: any): number;
  group(array: number[]): number;
};

export type TableMatrixResult = Record<string, number[]> & { length: number; getValue(keys: number[]): number[] };
