/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

/**
 * This class is a convenient builder for creating message boxes. Use the static functions to 
 * create and open simple and often used message boxes.
 */
scout.MessageBoxes = function(parent) {
  this.yesText = null;
  this.noText = null;
  this.cancelText = null;
  this.bodyText = null;
  this.severity = scout.MessageBox.SEVERITY.INFO;
  this.headerText = null;
  this.closeOnClick = true;
  this.html = false;
  this.parent = parent;
};

scout.MessageBoxes.prototype.init = function(options) {
  scout.assertParameter('parent', options.parent);
  $.extend(this, options);
};

scout.MessageBoxes.prototype.withHeader = function(headerText) {
  this.headerText = headerText;
  return this;
};

/**
 * @param bodyText
 * @param {boolean} [html] Set to true if body must contain HTML, default is false
 * @returns {scout.MessageBoxes}
 */
scout.MessageBoxes.prototype.withBody = function(bodyText, html) {
  this.bodyText = bodyText;
  this.html = scout.nvl(html, false);
  return this;
};

scout.MessageBoxes.prototype.withSeverity = function(severity) {
  this.severity = scout.nvl(severity, scout.MessageBox.SEVERITY.INFO);
  return this;
};
scout.MessageBoxes.prototype.withYes = function(yesText) {
  this.yesText = scout.nvl(yesText, this.parent.session.text('Yes'));
  return this;
};

scout.MessageBoxes.prototype.withNo = function(noText) {
  this.noText = scout.nvl(noText, this.parent.session.text('No'));
  return this;
};

scout.MessageBoxes.prototype.withCancel = function(cancelText) {
  this.cancelText = scout.nvl(cancelText, this.parent.session.text('Cancel'));
  return this;
};

scout.MessageBoxes.prototype.build = function() {
  var headerText = scout.nvl(this.headerText, this._headerTextForSeverity());
  var options = {
    parent: this.parent,
    header: headerText,
    body: this.bodyText,
    severity: this.severity
  };
  if (scout.strings.hasText(this.yesText)) {
    options.yesButtonText = this.yesText;
  }
  if (scout.strings.hasText(this.noText)) {
    options.noButtonText = this.noText;
  }
  if (scout.strings.hasText(this.cancelText)) {
    options.cancelButtonText = this.cancelText;
  }
  if (this.html) { // TODO [awe] 6.2 - discuss with S.ME - why do we need TWO properties for body?
    options.html = options.body;
    delete options.body;
  }
  return scout.create('MessageBox', options);
};

/**
 * @returns {Promise} resolved to selected button / option
 * @see scout.MessageBox.Buttons
 */
scout.MessageBoxes.prototype.buildAndOpen = function() {
  var def = $.Deferred();
  var messageBox = this.build();
  messageBox.on('action', function(event) {
    if (this.closeOnClick) {
      messageBox.close();
    }
    def.resolve(event.option);
  }.bind(this));
  messageBox.open();
  return def.promise();
};

scout.MessageBoxes.prototype._headerTextForSeverity = function() {
  var session = this.parent.session;
  switch (this.severity) {
    case scout.MessageBox.SEVERITY.WARNING:
      return session.text('Warning');
    case scout.MessageBox.SEVERITY.ERROR:
      return session.text('Error');
    default: // ok and info
      return session.text('Info');
  }
};

/**
 * Opens a message box with an Ok button.
 *
 * @returns {Promise} resolved to clicked button
 * @param {Object} parent
 * @param {string} bodyText
 * @param {number} [severity] default is <code>scout.MessageBox.SEVERITY.INFO</code>
 * @static
 */
scout.MessageBoxes.openOk = function(parent, bodyText, severity) {
  return new scout.MessageBoxes(parent)
    .withBody(bodyText)
    .withYes(parent.session.text('Ok'))
    .withSeverity(severity)
    .buildAndOpen();
};

/**
 * Opens a message box with a yes and a no button.
 *
 * @returns {Promise} resolved to clicked button
 * @param {Object} parent
 * @param {string} bodyText
 * @param {number} [severity] default is <code>scout.MessageBox.SEVERITY.INFO</code>
 * @static
 */
scout.MessageBoxes.openYesNo = function(parent, bodyText, severity) {
  return new scout.MessageBoxes(parent)
    .withBody(bodyText)
    .withYes()
    .withNo()
    .withSeverity(severity)
    .buildAndOpen();
};
