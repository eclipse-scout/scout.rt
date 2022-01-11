/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, DefaultStatus, ObjectFactory, objects, ParsingFailedStatus, strings, ValidationFailedStatus} from '../index';
import $ from 'jquery';

export default class Status {

  constructor(model) {
    this.message = null;
    this.severity = Status.Severity.ERROR;
    this.iconId = null;
    this.code = 0;
    this.children = null;
    this.deletable = true;
    $.extend(this, model);

    // severity may be a string (e.g. if set in a model json file) -> convert to real severity
    if (typeof this.severity === 'string') {
      this.severity = Status.Severity[this.severity.toUpperCase()];
    }
    // children
    if (model && model.children && Array.isArray(model.children)) {
      this.children = model.children.map(child => {
        return Status.ensure(child);
      });
    }
  }

  static Severity = {
    OK: 0x01,
    INFO: 0x100,
    WARNING: 0x10000,
    ERROR: 0x1000000
  };

  static SEVERITY_CSS_CLASSES = 'error warning info ok';

  cssClass() {
    return Status.cssClassForSeverity(this.severity);
  }

  /**
   * @returns {boolean} true if severity is OK or INFO, false if severity is WARNING or ERROR.
   */
  isValid() {
    return this.severity === Status.Severity.OK ||
      this.severity === Status.Severity.INFO;
  }

  isError() {
    return this.severity === Status.Severity.ERROR;
  }

  isWarning() {
    return this.severity === Status.Severity.WARNING;
  }

  isInfo() {
    return this.severity === Status.Severity.INFO;
  }

  isOk() {
    return this.severity === Status.Severity.OK;
  }

  /**
   * @returns {Status[]} status including children as flat list.
   */
  asFlatList() {
    return Status.asFlatList(this);
  }

  /**
   * @return {Status} a clone of this Status instance.
   */
  clone() {
    let modelClone = $.extend({}, this);
    return new Status(modelClone);
  }

  equals(o) {
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
   * thus we cannot distinct them by type or instanceof.
   *
   * @param {object} statusType
   * @return {boolean} whether or not this status contains a child with the give type
   */
  containsStatus(statusType) {
    return this.containsStatusByPredicate(status => {
      return status instanceof statusType;
    });
  }

  containsStatusByPredicate(predicate) {
    return this.asFlatList().some(predicate);
  }

  addStatus(status) {
    if (this.hasChildren()) {
      this.children.push(status);
    } else {
      this.children = [status];
    }
    this._updateProperties();
  }

  /**
   * Removes all children of the given type from this status. The type is checked by inheritance.
   *
   * @param {object} statusType
   */
  removeAllStatus(statusType) {
    this.removeAllStatusByPredicate(status => {
      return status instanceof statusType;
    });
  }

  removeAllStatusByPredicate(predicate) {
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

  _updateProperties() {
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
   * @return {boolean} whether this status has children (= multi status)
   */
  hasChildren() {
    return !!(this.children && this.children.length > 0);
  }

  /**
   * In some cases we need to transform an error status without children to a multi-status with children.
   * If the instance already has children, this function returns a clone of the instance.
   * If the instance is not yet a multi-status, we return a new instance with the current instance as first child.
   *
   * @returns {Status}
   */
  ensureChildren() {
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
  static clone(original) {
    return original ? original.clone() : null;
  }

  /**
   * @param {number} severity
   * @returns {string}
   * @static
   */
  static cssClassForSeverity(severity) {
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

  static animateStatusMessage($status, message) {
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

  static ensure(status) {
    if (!status) {
      return status;
    }
    if (status instanceof Status) {
      return status;
    }
    // May return a specialized sub-class of Status
    if (!status.objectType) {
      status.objectType = 'Status';
    }
    return ObjectFactory.get().create(status);
  }

  /**
   * @returns {Status} a Status object with severity OK.
   */
  static ok(model) {
    return new Status(Status.ensureModel(model, Status.Severity.OK));
  }

  /**
   * @returns {Status} a Status object with severity INFO.
   */
  static info(model) {
    return new Status(Status.ensureModel(model, Status.Severity.INFO));
  }

  /**
   * @returns {Status} a Status object with severity WARNING.
   */
  static warning(model) {
    return new Status(Status.ensureModel(model, Status.Severity.WARNING));
  }

  /**
   * @returns {Status} a Status object with severity ERROR.
   */
  static error(model) {
    return new Status(Status.ensureModel(model, Status.Severity.ERROR));
  }

  /**
   * @returns {object}
   */
  static ensureModel(model, severity) {
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
   * @returns {Status[]} all Status objects as flat list (goes through the status hierarchy)
   */
  static asFlatList(status) {
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
   * <p>
   * The key of this map is a string which is equals to the objectType string, the value is a reference to the constructor function.
   * This map is required because in JavaScript we don't have the class-name at runtime.
   * <p>
   * Note: we cannot initialize this map as static variable, because webpack dependencies are not resolved in the moment the variable
   * is initialized.
   *
   * @param {string} className
   * @returns {function} Status constructor
   */
  static classForName(className) {
    return {
      Status: Status,
      DefaultStatus: DefaultStatus,
      ParsingFailedStatus: ParsingFailedStatus,
      ValidationFailedStatus: ValidationFailedStatus
    }[className];
  }
}
