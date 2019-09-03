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
/**
 * @singleton
 */
scout.HtmlEnvironment = function() {
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
  this.events = new scout.EventSupport();
};

scout.HtmlEnvironment.prototype.init = function(additionalClass) {
  this.formRowHeight = scout.styles.getSize('html-env-logical-grid-row', 'height', 'height', this.formRowHeight, additionalClass);
  this.formRowGap = scout.styles.getSize('html-env-logical-grid-row', 'margin-bottom', 'marginBottom', this.formRowGap, additionalClass);
  this.formColumnWidth = scout.styles.getSize('html-env-logical-grid-column', 'width', 'width', this.formColumnWidth, additionalClass);
  this.formColumnGap = scout.styles.getSize('html-env-logical-grid-column', 'margin-right', 'marginRight', this.formColumnGap, additionalClass);
  this.smallColumnGap = scout.styles.getSize('html-env-logical-grid-column', 'margin-left', 'marginLeft', this.smallColumnGap, additionalClass);
  this.fieldLabelWidth = scout.styles.getSize('html-env-field-label', 'width', 'width', this.fieldLabelWidth, additionalClass);
  this.fieldMandatoryIndicatorWidth = scout.styles.getSize('html-env-field-mandatory-indicator', 'width', 'width', this.fieldMandatoryIndicatorWidth, additionalClass);
  this.fieldStatusWidth = scout.styles.getSize('html-env-field-status', 'width', 'width', this.fieldStatusWidth, additionalClass);

  var event = {
    source: this
  };
  this.events.trigger('propertyChange', event);
};

scout.HtmlEnvironment.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.HtmlEnvironment.prototype.off = function(type, func) {
  return this.events.off(type, func);
};

scout.addAppListener('prepare', function() {
  if (scout.htmlEnvironment) {
    // if the environment was created before the app itself, use it instead of creating a new one
    return;
  }
  scout.htmlEnvironment = scout.create('HtmlEnvironment');
  scout.htmlEnvironment.init();
});
