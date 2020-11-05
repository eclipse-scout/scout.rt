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
import {App, EventSupport, scout, styles} from '../index';

let instance;
/**
 * @singleton
 */
export default class HtmlEnvironment {

  constructor() {
    // -------------------------------
    // The values for these properties are defined using CSS (sizes.less).
    // The following values are default values in case the CSS values are not available.
    // -------------------------------
    this.formRowHeight = 30;
    this.formRowGap = 10;
    this.formColumnWidth = 420;
    this.formColumnGap = 32; // 40 pixel actual form gap - fieldMandatoryIndicatorWidth
    this.smallColumnGap = 4;
    this.fieldLabelWidth = 140;
    this.fieldMandatoryIndicatorWidth = 8;
    this.fieldStatusWidth = 20;
    this.events = new EventSupport();
  }

  init(additionalClass) {
    additionalClass = additionalClass ? ' ' + additionalClass : '';
    this.formRowHeight = styles.getSize('html-env-logical-grid-row' + additionalClass, 'height', 'height', this.formRowHeight);
    this.formRowGap = styles.getSize('html-env-logical-grid-row' + additionalClass, 'margin-bottom', 'marginBottom', this.formRowGap);
    this.formColumnWidth = styles.getSize('html-env-logical-grid-column' + additionalClass, 'width', 'width', this.formColumnWidth);
    this.formColumnGap = styles.getSize('html-env-logical-grid-column' + additionalClass, 'margin-right', 'marginRight', this.formColumnGap);
    this.smallColumnGap = styles.getSize('html-env-logical-grid-column' + additionalClass, 'margin-left', 'marginLeft', this.smallColumnGap);
    this.fieldLabelWidth = styles.getSize('html-env-field-label' + additionalClass, 'width', 'width', this.fieldLabelWidth);
    this.fieldMandatoryIndicatorWidth = styles.getSize('html-env-field-mandatory-indicator' + additionalClass, 'width', 'width', this.fieldMandatoryIndicatorWidth);
    this.fieldStatusWidth = styles.getSize('html-env-field-status' + additionalClass, 'width', 'width', this.fieldStatusWidth);

    let event = {
      source: this
    };
    this.events.trigger('propertyChange', event);
  }

  on(type, func) {
    return this.events.on(type, func);
  }

  off(type, func) {
    return this.events.off(type, func);
  }

  /**
   * @returns {HtmlEnvironment}
   */
  static get() {
    return instance;
  }
}

App.addListener('prepare', () => {
  if (instance) {
    // if the environment was created before the app itself, use it instead of creating a new one
    return;
  }
  instance = scout.create('HtmlEnvironment');
  instance.init();
});
