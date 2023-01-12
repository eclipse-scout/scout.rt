/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, EventEmitter, HtmlEnvironmentEventMap, scout, styles} from '../index';

let instance;
/**
 * @singleton
 */
export class HtmlEnvironment extends EventEmitter {
  declare eventMap: HtmlEnvironmentEventMap;
  declare self: HtmlEnvironment;

  formRowHeight: number;

  formRowGap: number;
  formColumnWidth: number;

  /** 40 pixel actual form gap - fieldMandatoryIndicatorWidth */
  formColumnGap: number;

  smallColumnGap: number;
  fieldLabelWidth: number;
  fieldMandatoryIndicatorWidth: number;
  fieldStatusWidth: number;

  constructor() {
    super();
    // -------------------------------
    // The values for these properties are defined using CSS (sizes.less).
    // The following values are default values in case the CSS values are not available.
    // -------------------------------
    this.formRowHeight = 30;
    this.formRowGap = 10;
    this.formColumnWidth = 420;
    this.formColumnGap = 32;
    this.smallColumnGap = 4;
    this.fieldLabelWidth = 140;
    this.fieldMandatoryIndicatorWidth = 8;
    this.fieldStatusWidth = 20;
  }

  init(additionalClass?: string) {
    additionalClass = additionalClass ? ' ' + additionalClass : '';
    this.formRowHeight = styles.getSize('html-env-logical-grid-row' + additionalClass, 'height', 'height', this.formRowHeight);
    this.formRowGap = styles.getSize('html-env-logical-grid-row' + additionalClass, 'margin-bottom', 'marginBottom', this.formRowGap);
    this.formColumnWidth = styles.getSize('html-env-logical-grid-column' + additionalClass, 'width', 'width', this.formColumnWidth);
    this.formColumnGap = styles.getSize('html-env-logical-grid-column' + additionalClass, 'margin-right', 'marginRight', this.formColumnGap);
    this.smallColumnGap = styles.getSize('html-env-logical-grid-column' + additionalClass, 'margin-left', 'marginLeft', this.smallColumnGap);
    this.fieldLabelWidth = styles.getSize('html-env-field-label' + additionalClass, 'width', 'width', this.fieldLabelWidth);
    this.fieldMandatoryIndicatorWidth = styles.getSize('html-env-field-mandatory-indicator' + additionalClass, 'width', 'width', this.fieldMandatoryIndicatorWidth);
    this.fieldStatusWidth = styles.getSize('html-env-field-status' + additionalClass, 'width', 'width', this.fieldStatusWidth);

    this.trigger('propertyChange');
  }

  static get(): HtmlEnvironment {
    return instance;
  }
}

App.addListener('prepare', () => {
  if (instance) {
    // if the environment was created before the app itself, use it instead of creating a new one
    return;
  }
  instance = scout.create(HtmlEnvironment);
  instance.init();
});
