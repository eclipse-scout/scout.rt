/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Status = function(model) {
  this.message = null;
  this.severity = scout.Status.Severity.ERROR;
  this.code = 0;
  $.extend(this, model);

  // severity may be a string (e.g. if set in a model json file) -> convert to real severity
  if (typeof this.severity === 'string') {
    this.severity = scout.Status.Severity[this.severity.toUpperCase()];
  }
  // children
  if (model && model.children && Array.isArray(model.children)) {
    this.children = model.children.map(function(child) {
      return scout.Status.ensure(child);
    }.bind(this));
  }
};

scout.Status.Severity = {
  OK: 0x01,
  INFO: 0x100,
  WARNING: 0x10000,
  ERROR: 0x1000000
};

scout.Status.SEVERITY_CSS_CLASSES = 'error warning info ok';

scout.Status.prototype.cssClass = function() {
  return scout.Status.cssClassForSeverity(this.severity);
};

/**
 * @returns true if severity is OK or INFO, false if severity is WARNING or ERROR.
 */
scout.Status.prototype.isValid = function() {
  return this.severity === scout.Status.Severity.OK ||
    this.severity === scout.Status.Severity.INFO;
};

scout.Status.prototype.isError = function() {
  return this.severity === scout.Status.Severity.ERROR;
};

/**
 * @returns {scout.Status[]} status including children as flat list.
 */
scout.Status.prototype.asFlatList = function() {
  return scout.Status.asFlatList(this);
};

/**
 * @return {scout.Status} a clone of this Status instance.
 */
scout.Status.prototype.clone = function() {
  var modelClone = $.extend({}, this);
  return new scout.Status(modelClone);
};

scout.Status.prototype.equals = function(o) {
  if (!(o instanceof scout.Status)) {
    return false;
  }
  return scout.objects.propertiesEquals(this, o, ['severity', 'message', 'invalidDate', 'invalidTime']);
};

/**
 * Null-safe static clone method.
 */
scout.Status.clone = function(original) {
  return original ? original.clone() : null;
};

/**
 * @param {number} severity
 * @returns {string}
 * @static
 */
scout.Status.cssClassForSeverity = function(severity) {
  var cssSeverity,
    Severity = scout.Status.Severity;

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
};

scout.Status.animateStatusMessage = function($status, message) {
  if (scout.strings.endsWith(message, '...')) {
    var $ellipsis = $status.makeSpan('ellipsis');
    for (var i = 0; i < 3; i++) {
      $ellipsis.append($status.makeSpan('animate-dot delay-' + i, '.'));
    }
    message = message.substring(0, message.length - 3);
    $status.empty().text(message).append($ellipsis);
  } else {
    $status.text(message);
  }
};

scout.Status.ensure = function(status) {
  if (!status) {
    return status;
  }
  if (status instanceof scout.Status) {
    return status;
  }
  return new scout.Status(status);
};

/**
 * @returns {scout.Status} a Status object with severity OK.
 */
scout.Status.ok = function(model) {
  model = model || {};
  model = $.extend({}, model, {
    severity: scout.Status.Severity.OK
  });
  return new scout.Status(model);
};

/**
 * @returns {scout.Status} a Status object with severity INFO.
 */
scout.Status.info = function(model) {
  model = model || {};
  model = $.extend({}, model, {
    severity: scout.Status.Severity.INFO
  });
  return new scout.Status(model);
};

/**
 * @returns {scout.Status} a Status object with severity WARN.
 */
scout.Status.warn = function(model) {
  model = model || {};
  model = $.extend({}, model, {
    severity: scout.Status.Severity.WARNING
  });
  return new scout.Status(model);
};

/**
 * @returns {scout.Status} a Status object with severity ERROR.
 */
scout.Status.error = function(model) {
  model = model || {};
  model = $.extend({}, model, {
    severity: scout.Status.Severity.ERROR
  });
  return new scout.Status(model);
};

/**
 * @returns {scout.Status[]} all Status objects as flat list (goes through the status hierarchy)
 */
scout.Status.asFlatList = function(status) {
  if (!status) {
    return [];
  }
  var list = [];
  if (status.children) {
    status.children.forEach(function(childStatus) {
      scout.arrays.pushAll(list, scout.Status.asFlatList(childStatus));
    });
  }
  list.push(status);
  return list;
};
