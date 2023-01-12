/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, DefaultStatus, EnumObject, FullModelOf, InitModelOf, ObjectOrModel, objects, ObjectWithType, ParsingFailedStatus, Predicate, scout, StatusModel, strings, ValidationFailedStatus} from '../index';
import $ from 'jquery';

export class Status implements StatusModel, ObjectWithType {
  declare model: StatusModel;

  objectType: string;
  message: string;
  severity: StatusSeverity;
  iconId: string;
  code: number;
  children: Status[];
  deletable: boolean;
  uiState: string;

  constructor(model?: InitModelOf<Status>) {
    this.message = null;
    this.severity = Status.Severity.ERROR;
    this.iconId = null;
    this.code = 0;
    this.children = null;
    this.deletable = true;
    $.extend(this, model);

    // severity may be a string (e.g. if set in a model json file) -> convert to real severity
    if (typeof this.severity === 'string') {
      let currentSeverity = this.severity as string;
      this.severity = Status.Severity[currentSeverity.toUpperCase()];
    }
    // children
    if (model && model.children && Array.isArray(model.children)) {
      this.children = model.children.map(child => Status.ensure(child));
    }
  }

  static Severity = {
    OK: 0x01,
    INFO: 0x100,
    WARNING: 0x10000,
    ERROR: 0x1000000
  } as const;

  static SEVERITY_CSS_CLASSES = 'error warning info ok';

  cssClass(): string {
    return Status.cssClassForSeverity(this.severity);
  }

  /**
   * @returns true if severity is OK or INFO, false if severity is WARNING or ERROR.
   */
  isValid(): boolean {
    return this.severity === Status.Severity.OK ||
      this.severity === Status.Severity.INFO;
  }

  isError(): boolean {
    return this.severity === Status.Severity.ERROR;
  }

  isWarning(): boolean {
    return this.severity === Status.Severity.WARNING;
  }

  isInfo(): boolean {
    return this.severity === Status.Severity.INFO;
  }

  isOk(): boolean {
    return this.severity === Status.Severity.OK;
  }

  /**
   * @returns status including all children recursively as flat array.
   */
  asFlatList(): Status[] {
    return Status.asFlatList(this);
  }

  /**
   * @returns a clone of this Status instance.
   */
  clone(): Status {
    let modelClone = $.extend({}, this);
    return new Status(modelClone);
  }

  equals(o: any): boolean {
    if (!(o instanceof Status)) {
      return false;
    }
    if (!objects.equalsRecursive(this.children, o.children)) {
      return false;
    }
    return objects.propertiesEquals(this, o, ['severity', 'message', 'invalidDate', 'invalidTime']);
  }

  /**
   * Note: we cannot 'overload' this function, because predicates and status-types are both functions,
   * thus we cannot distinguish them by type or instanceof.
   *
   * @returns whether or not this status contains a child with the give type
   */
  containsStatus(statusType: new() => Status): boolean {
    return this.containsStatusByPredicate(status => status instanceof statusType);
  }

  containsStatusByPredicate(predicate: Predicate<Status>): boolean {
    return this.asFlatList().some(predicate);
  }

  addStatus(status: Status) {
    if (this.hasChildren()) {
      this.children.push(status);
    } else {
      this.children = [status];
    }
    this._updateProperties();
  }

  /**
   * Removes all children of the given type from this status. The type is checked by inheritance.
   */
  removeAllStatus(statusType: new() => Status) {
    this.removeAllStatusByPredicate(status => status instanceof statusType);
  }

  removeAllStatusByPredicate(predicate: Predicate<Status>) {
    if (this.hasChildren()) {
      this.children.forEach(status => {
        status.removeAllStatusByPredicate(predicate);
      });
      this.children = this.children.filter(status => {
        // when status is not deletable we must add it as child again, thus --> true
        if (!status.deletable) {
          return true;
        }
        return !predicate(status); // negate predicate
      });
      this._updateProperties();
    }
  }

  protected _updateProperties() {
    if (!this.hasChildren()) {
      this.message = null;
      this.severity = Status.Severity.OK;
      this.code = 0;
      return;
    }

    let firstStatus = this.asFlatList().sort((a, b) => {
      return calcPriority(b) - calcPriority(a);

      function calcPriority(status) {
        let multiplier = 1;
        if (status instanceof ParsingFailedStatus) {
          multiplier = 4;
        } else if (status instanceof ValidationFailedStatus) {
          multiplier = 2;
        }
        return multiplier * status.severity;
      }
    })[0];
    this.message = firstStatus.message;
    this.severity = firstStatus.severity;
    this.code = firstStatus.code;
  }

  /**
   * @returns whether this status has children (= multi status)
   */
  hasChildren(): boolean {
    return !!(this.children && this.children.length > 0);
  }

  /**
   * In some cases we need to transform an error status without children to a multi-status with children.
   * @returns If this instance already has children, a clone of the instance. Otherwise, a new instance with the current instance as first child.
   */
  ensureChildren(): Status {
    if (objects.isArray(this.children)) {
      return this.clone();
    }
    let childStatus = this;
    let newStatus = this.clone();
    newStatus.children = [childStatus];
    newStatus._updateProperties();
    return newStatus;
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * Null-safe static clone method.
   */
  static clone(original: Status): Status {
    return original ? original.clone() : null;
  }

  static cssClassForSeverity(severity: StatusSeverity): string {
    let cssSeverity,
      Severity = Status.Severity;

    switch (severity) {
      case Severity.OK:
        cssSeverity = 'ok';
        break;
      case Severity.INFO:
        cssSeverity = 'info';
        break;
      case Severity.WARNING:
        cssSeverity = 'warning';
        break;
      case Severity.ERROR:
        cssSeverity = 'error';
        break;
    }
    return cssSeverity;
  }

  static animateStatusMessage($status: JQuery, message: string) {
    if (strings.endsWith(message, '...')) {
      let $ellipsis = $status.makeSpan('ellipsis');
      for (let i = 0; i < 3; i++) {
        $ellipsis.append($status.makeSpan('animate-dot delay-' + i, '.'));
      }
      message = message.substring(0, message.length - 3);
      $status.empty().text(message).append($ellipsis);
    } else {
      $status.text(message);
    }
  }

  /**
   * @returns a {@link Status} object with severity OK.
   */
  static ok(model?: StatusModel | string): Status {
    return new Status(Status.ensureModel(model, Status.Severity.OK));
  }

  /**
   * @returns a {@link Status} object with severity INFO.
   */
  static info(model?: StatusModel | string): Status {
    return new Status(Status.ensureModel(model, Status.Severity.INFO));
  }

  /**
   * @returns a {@link Status} object with severity WARNING.
   */
  static warning(model?: StatusModel | string): Status {
    return new Status(Status.ensureModel(model, Status.Severity.WARNING));
  }

  /**
   * @returns a {@link Status} object with severity ERROR.
   */
  static error(model?: StatusModel | string): Status {
    return new Status(Status.ensureModel(model, Status.Severity.ERROR));
  }

  static ensureModel(model: StatusModel | string, severity: StatusSeverity | StatusSeverityNames): StatusModel {
    if (typeof model === 'string') {
      model = {
        message: model
      };
    } else {
      model = model || {};
    }
    return $.extend({}, model, {
      severity: severity
    });
  }

  /**
   * @returns all {@link  Status} objects as flat array (recursively goes through the status hierarchy)
   */
  static asFlatList(status: Status): Status[] {
    if (!status) {
      return [];
    }
    let list = [];
    if (status.hasChildren()) {
      status.children.forEach(childStatus => {
        arrays.pushAll(list, Status.asFlatList(childStatus));
      });
    } else {
      list.push(status);
    }
    return list;
  }

  /**
   * Returns a constructor function for the given class-name.
   *
   * @returns Status constructor
   */
  static classForName(className: StatusType): new(model?: StatusModel) => Status {
    return {
      Status: Status,
      DefaultStatus: DefaultStatus,
      ParsingFailedStatus: ParsingFailedStatus,
      ValidationFailedStatus: ValidationFailedStatus
    }[className];
  }

  static ensure<T extends Status = Status>(status: StatusOrModel<T>): T {
    if (!status) {
      return status as T;
    }
    if (status instanceof Status) {
      return status;
    }
    // May return a specialized subclass of Status
    if (!status.objectType) {
      status.objectType = Status;
    }
    return scout.create(status as FullModelOf<T>);
  }
}

export type StatusSeverity = EnumObject<typeof Status.Severity>;
export type StatusSeverityNames = keyof typeof Status.Severity;
export type StatusOrModel<T extends Status = Status> = ObjectOrModel<T>;
export type StatusType = 'Status' | 'DefaultStatus' | 'ParsingFailedStatus' | 'ValidationFailedStatus';
