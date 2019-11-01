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
import {EventSupport} from '../index';
import {styles} from '../index';
import {scout} from '../index';
import {App} from '../index';

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
  this.formRowHeight = styles.getSize('html-env-logical-grid-row', 'height', 'height', this.formRowHeight, additionalClass);
  this.formRowGap = styles.getSize('html-env-logical-grid-row', 'margin-bottom', 'marginBottom', this.formRowGap, additionalClass);
  this.formColumnWidth = styles.getSize('html-env-logical-grid-column', 'width', 'width', this.formColumnWidth, additionalClass);
  this.formColumnGap = styles.getSize('html-env-logical-grid-column', 'margin-right', 'marginRight', this.formColumnGap, additionalClass);
  this.smallColumnGap = styles.getSize('html-env-logical-grid-column', 'margin-left', 'marginLeft', this.smallColumnGap, additionalClass);
  this.fieldLabelWidth = styles.getSize('html-env-field-label', 'width', 'width', this.fieldLabelWidth, additionalClass);
  this.fieldMandatoryIndicatorWidth = styles.getSize('html-env-field-mandatory-indicator', 'width', 'width', this.fieldMandatoryIndicatorWidth, additionalClass);
  this.fieldStatusWidth = styles.getSize('html-env-field-status', 'width', 'width', this.fieldStatusWidth, additionalClass);

  var event = {
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

static get() {
  return instance;
}
}

App.addListener('prepare', function() {
  if (instance) {
    // if the environment was created before the app itself, use it instead of creating a new one
    return;
  }
  instance = scout.create('HtmlEnvironment');
  instance.init();
});
