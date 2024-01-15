/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, InitModelOf, LoginAppModel, LoginBox, ObjectFactory, scout, texts} from '../index';
import $ from 'jquery';

export class LoginApp extends App {

  declare model: LoginAppModel;

  override init(options?: InitModelOf<this>): JQuery.Promise<any> {
    return super.init(options);
  }

  protected override _prepareEssentials(options: LoginAppModel) {
    ObjectFactory.get().init();
  }

  /**
   * No bootstrapping required
   */
  protected override _defaultBootstrappers(): (() => JQuery.Promise<void>)[] {
    return [];
  }

  protected override _init(options: InitModelOf<this>): JQuery.Promise<any> {
    options = options || {} as InitModelOf<this>;
    options.texts = $.extend({}, texts.readFromDOM(), options.texts);
    this._prepareDOM();

    let loginBox = scout.create(LoginBox, options);
    loginBox.render($('body').addClass('login-body'));
    return $.resolvedPromise();
  }
}
