/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, FormFieldValidationResultProvider, scout, Status, TableField, TableRow, ValidationResult} from '../../..';

export class TableFieldValidationResultProvider extends FormFieldValidationResultProvider {
  declare field: TableField;

  override provide(errorStatus: Status): ValidationResult {
    let desc = super.provide(errorStatus);
    if (desc && !desc.valid) {
      return desc;
    }
    let cellValidationResult = this._getCellResult(desc);

    let label = this.field.label || '';
    if (label && cellValidationResult.label) {
      label += ': ';
    }
    label += cellValidationResult.label;

    if (cellValidationResult.errorStatus) {
      errorStatus = cellValidationResult.errorStatus;
    }

    let reveal = cellValidationResult.reveal || (() => {
      // nop
    });
    let validByMandatory = !this.field.mandatory || !this.field.empty && cellValidationResult.validByMandatory;
    let validByErrorStatus = !errorStatus || errorStatus.isValid();
    return {
      valid: validByErrorStatus && validByMandatory,
      validByMandatory,
      errorStatus,
      field: this.field,
      label: label,
      reveal: reveal
    };
  }

  protected _getCellResult(desc: ValidationResult): Omit<ValidationResult, 'field' | 'valid'> {
    let rows = arrays.ensure(this.field.table?.rows);
    let columns = arrays.ensure(this.field.table?.columns);
    let invalidCellLabels = new Set<string>();
    let invalidCellMessages = new Set<string>();
    let errorStatus;
    let validByMandatory = true;
    let reveal;
    for (let row of rows) {
      for (let column of columns) {
        let cellResult = column.isContentValid(row);
        if (cellResult.valid) {
          continue;
        }
        if (!reveal) {
          // Focus first invalid cell
          reveal = () => {
            desc.reveal();
            this.field.table.focusCell(column, row);
          };
        }
        let label = this._getCellLabel(column, row);
        if (label) {
          invalidCellLabels.add(label);
        }
        if (cellResult.errorStatus?.message) {
          invalidCellMessages.add(cellResult.errorStatus.message);
        }

        // Use error status with the highest severity
        if (!errorStatus) {
          errorStatus = cellResult.errorStatus;
        } else if (cellResult.errorStatus?.severity > errorStatus.severity) {
          errorStatus = cellResult.errorStatus;
        }
        validByMandatory = validByMandatory && cellResult.validByMandatory;
      }
    }
    let label = arrays.format(Array.from(invalidCellLabels.values()), ', ');
    if (invalidCellMessages.size > 1 || !validByMandatory) {
      // If there are indistinct error message, clear the message to only show the column names
      errorStatus = scout.create(Status, $.extend({}, errorStatus, {message: ''}));
    }
    return {label, errorStatus, validByMandatory, reveal};
  }

  protected _getCellLabel(column: Column, row: TableRow) {
    return column.text;
  }
}
