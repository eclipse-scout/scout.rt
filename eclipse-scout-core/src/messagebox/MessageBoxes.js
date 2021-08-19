/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {MessageBox, scout, Status, strings} from '../index';
import $ from 'jquery';

/**
 * This class is a convenient builder for creating message boxes. Use the static functions to
 * create and open simple and often used message boxes.
 */
export default class MessageBoxes {

  constructor() {
    this.parent = null;

    this.yesText = null;
    this.noText = null;
    this.cancelText = null;
    this.bodyText = null;
    this.severity = Status.Severity.INFO;
    this.headerText = null;
    this.iconId = null;
    this.closeOnClick = true;
    this.html = false;
  }

  init(options) {
    scout.assertParameter('parent', options.parent);
    $.extend(this, options);
  }

  withHeader(headerText) {
    this.headerText = headerText;
    return this;
  }

  /**
   * @param bodyText
   * @param {boolean} [html] Set to true if body must contain HTML, default is false
   * @returns {MessageBoxes}
   */
  withBody(bodyText, html) {
    this.bodyText = bodyText;
    this.html = scout.nvl(html, false);
    return this;
  }

  withIconId(iconId) {
    this.iconId = iconId;
    return this;
  }

  withSeverity(severity) {
    this.severity = scout.nvl(severity, Status.Severity.INFO);
    return this;
  }

  withYes(yesText) {
    this.yesText = scout.nvl(yesText, this.parent.session.text('Yes'));
    return this;
  }

  withNo(noText) {
    this.noText = scout.nvl(noText, this.parent.session.text('No'));
    return this;
  }

  withCancel(cancelText) {
    this.cancelText = scout.nvl(cancelText, this.parent.session.text('Cancel'));
    return this;
  }

  build() {
    let options = {
      parent: this.parent,
      header: this.headerText,
      body: this.bodyText,
      severity: this.severity
    };
    if (strings.hasText(this.iconId)) {
      options.iconId = this.iconId;
    }
    if (strings.hasText(this.yesText)) {
      options.yesButtonText = this.yesText;
    }
    if (strings.hasText(this.noText)) {
      options.noButtonText = this.noText;
    }
    if (strings.hasText(this.cancelText)) {
      options.cancelButtonText = this.cancelText;
    }
    // When this class is refactored we should check with the author, why it needs two properties html and body.
    if (this.html) {
      options.html = options.body;
      delete options.body;
    }
    return scout.create('MessageBox', options);
  }

  /**
   * @returns {Promise} resolved to selected button / option
   * @see MessageBox.Buttons
   */
  buildAndOpen() {
    let def = $.Deferred();
    let messageBox = this.build();
    messageBox.on('action', event => {
      if (this.closeOnClick) {
        messageBox.close();
      }
      def.resolve(event.option);
    });
    messageBox.open();
    return def.promise();
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static create(parent) {
    return scout.create('MessageBoxes', {
      parent: parent
    });
  }

  static createOk(parent) {
    return this.create(parent).withYes(parent.session.text('Ok'));
  }

  static createYesNo(parent) {
    return this.create(parent).withYes().withNo();
  }

  static createYesNoCancel(parent) {
    return this.create(parent).withYes().withNo().withCancel();
  }

  /**
   * Opens a message box with an Ok button.
   *
   * @returns {Promise} resolved to clicked button
   * @param {Object} parent
   * @param {string} bodyText
   * @param {number} [severity] default is <code>Status.Severity.INFO</code>
   * @static
   */
  static openOk(parent, bodyText, severity) {
    return this.createOk(parent)
      .withBody(bodyText)
      .withSeverity(severity)
      .buildAndOpen();
  }

  /**
   * Opens a message box with a yes and a no button.
   *
   * @returns {Promise} resolved to clicked button
   * @param {Object} parent
   * @param {string} bodyText
   * @param {number} [severity] default is <code>Status.Severity.INFO</code>
   * @static
   */
  static openYesNo(parent, bodyText, severity) {
    return this.createYesNo(parent)
      .withBody(bodyText)
      .withSeverity(severity)
      .buildAndOpen();
  }
}
