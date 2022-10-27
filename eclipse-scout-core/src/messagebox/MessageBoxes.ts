/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {MessageBox, MessageBoxesOptions, scout, Status, strings, Widget} from '../index';
import $ from 'jquery';
import {StatusSeverity} from '../status/Status';
import MessageBoxModel from './MessageBoxModel';
import {MessageBoxOption} from './MessageBox';

/**
 * This class is a convenient builder for creating message boxes. Use the static functions to
 * create and open simple and often used message boxes.
 */
export default class MessageBoxes implements MessageBoxesOptions {
  parent: Widget;
  yesText: string;
  noText: string;
  cancelText: string;
  bodyText: string;
  severity: StatusSeverity;
  headerText: string;
  iconId: string;
  closeOnClick: boolean;
  html: boolean;

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

  init(options: MessageBoxesOptions) {
    scout.assertParameter('parent', options.parent);
    $.extend(this, options);
  }

  withHeader(headerText: string): this {
    this.headerText = headerText;
    return this;
  }

  /**
   * @param html Set to true if body must contain HTML, default is false
   */
  withBody(bodyText: string, html?: boolean): this {
    this.bodyText = bodyText;
    this.html = scout.nvl(html, false);
    return this;
  }

  withIconId(iconId: string): this {
    this.iconId = iconId;
    return this;
  }

  withSeverity(severity?: StatusSeverity): this {
    this.severity = scout.nvl(severity, Status.Severity.INFO);
    return this;
  }

  withYes(yesText?: string): this {
    this.yesText = scout.nvl(yesText, this.parent.session.text('Yes'));
    return this;
  }

  withNo(noText?: string): this {
    this.noText = scout.nvl(noText, this.parent.session.text('No'));
    return this;
  }

  withCancel(cancelText?: string): this {
    this.cancelText = scout.nvl(cancelText, this.parent.session.text('Cancel'));
    return this;
  }

  build(): MessageBox {
    let options: MessageBoxModel = {
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
    return scout.create(MessageBox, options);
  }

  /**
   * @returns promise resolved to selected {@link MessageBoxOption}.
   * @see MessageBox.Buttons
   */
  buildAndOpen(): JQuery.Promise<MessageBoxOption> {
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

  static create(parent: Widget): MessageBoxes {
    return scout.create(MessageBoxes, {
      parent: parent
    });
  }

  static createOk(parent: Widget): MessageBoxes {
    return this.create(parent).withYes(parent.session.text('Ok'));
  }

  static createYesNo(parent: Widget): MessageBoxes {
    return this.create(parent).withYes().withNo();
  }

  static createYesNoCancel(parent: Widget): MessageBoxes {
    return this.create(parent).withYes().withNo().withCancel();
  }

  /**
   * Opens a message box with an Ok button.
   *
   * @param severity default is {@link Status.Severity.INFO}
   * @returns promise resolved to clicked button
   */
  static openOk(parent: Widget, bodyText: string, severity?: StatusSeverity): JQuery.Promise<MessageBoxOption> {
    return this.createOk(parent)
      .withBody(bodyText)
      .withSeverity(severity)
      .buildAndOpen();
  }

  /**
   * Opens a message box with a yes and a no button.
   *
   * @param severity default is {@link Status.Severity.INFO}
   * @returns promise resolved to clicked button
   */
  static openYesNo(parent: Widget, bodyText: string, severity?: StatusSeverity): JQuery.Promise<MessageBoxOption> {
    return this.createYesNo(parent)
      .withBody(bodyText)
      .withSeverity(severity)
      .buildAndOpen();
  }
}
