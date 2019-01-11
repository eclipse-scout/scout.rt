/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * HtmlEnvironment is used in place of org.eclipse.scout.rt.ui.swing.DefaultSwingEnvironment.
 */
scout.HtmlEnvironment = {
  // -------------------------------
  // The values for these properties are defined using CSS (sizes.less).
  // The following values are default values in case the CSS values are not available.
  // -------------------------------
  formRowHeight: 30,
  formRowGap: 10,
  formColumnWidth: 420,
  formColumnGap: 32, // 40 pixel actual form gap - fieldMandatoryIndicatorWidth
  smallColumnGap: 4,
  fieldLabelWidth: 140,
  fieldMandatoryIndicatorWidth: 8,
  fieldStatusWidth: 20,
  events: new scout.EventSupport(),

  init: function(additionalClass) {
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
  },

  on: function(type, func) {
    return this.events.on(type, func);
  }
};

scout.addAppListener('prepare', function() {
  scout.HtmlEnvironment.init();
});
