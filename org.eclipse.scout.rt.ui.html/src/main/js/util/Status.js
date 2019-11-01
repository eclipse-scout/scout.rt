/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects} from '../index';
import {arrays} from '../index';
import {strings} from '../index';
import * as $ from 'jquery';

export default class Status {

constructor(model) {
  this.message = null;
  this.severity = Status.Severity.ERROR;
  this.code = 0;
  $.extend(this, model);

  // severity may be a string (e.g. if set in a model json file) -> convert to real severity
  if (typeof this.severity === 'string') {
    this.severity = Status.Severity[this.severity.toUpperCase()];
  }
  // children
  if (model && model.children && Array.isArray(model.children)) {
    this.children = model.children.map(function(child) {
      return Status.ensure(child);
    }.bind(this));
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
 * @returns true if severity is OK or INFO, false if severity is WARNING or ERROR.
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
  var modelClone = $.extend({}, this);
  return new Status(modelClone);
}

equals(o) {
  if (!(o instanceof Status)) {
    return false;
  }
  return objects.propertiesEquals(this, o, ['severity', 'message', 'invalidDate', 'invalidTime']);
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
  var cssSeverity,
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
    var $ellipsis = $status.makeSpan('ellipsis');
    for (var i = 0; i < 3; i++) {
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
  return new Status(status);
}

/**
 * @returns {Status} a Status object with severity OK.
 */
static ok(model) {
  return Status._create(model, Status.Severity.OK);
}

/**
 * @returns {Status} a Status object with severity INFO.
 */
static info(model) {
  return Status._create(model, Status.Severity.INFO);
}

/**
 * @returns {Status} a Status object with severity WARNING.
 * @deprecated do not use this legacy function, use Status.warning() instead!
 */
static _warnDeprecationLogged = false;
static warn(model) {
  if (!Status._warnDeprecationLogged && window.console && (window.console.warn || window.console.log)) {
    (window.console.warn || window.console.log)('scout.Status.warn() is deprecated and will be removed in a future release. Please use Status.warning() instead.');
    Status._warnDeprecationLogged = true; // only warn once
  }
  return Status.warning(model);
}

/**
 * @returns {Status} a Status object with severity WARNING.
 */
static warning(model) {
  return Status._create(model, Status.Severity.WARNING);
}

/**
 * @returns {Status} a Status object with severity ERROR.
 */
static error(model) {
  return Status._create(model, Status.Severity.ERROR);
}

static _create(model, severity) {
  if (typeof model === 'string') {
    model = {
      message: model
    };
  } else {
    model = model || {};
  }
  model = $.extend({}, model, {
    severity: severity
  });
  return new Status(model);
}

/**
 * @returns {Status[]} all Status objects as flat list (goes through the status hierarchy)
 */
static asFlatList(status) {
  if (!status) {
    return [];
  }
  var list = [];
  if (status.children) {
    status.children.forEach(function(childStatus) {
      arrays.pushAll(list, Status.asFlatList(childStatus));
    });
  }
  list.push(status);
  return list;
}
}
