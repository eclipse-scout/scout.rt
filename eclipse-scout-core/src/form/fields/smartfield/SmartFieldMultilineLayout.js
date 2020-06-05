/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, HtmlComponent, HtmlEnvironment} from '../../../index';

/**
 * This layout only layouts the INPUT and DIV part of the multi-line smart-field, not the entire form-field.
 */
export default class SmartFieldMultilineLayout extends AbstractLayout {

  constructor(smartField) {
    super();
    this.smartField = smartField;

    this._initDefaults();

    this.htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this.htmlPropertyChangeHandler);
    this.smartField.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this.htmlPropertyChangeHandler);
    });
  }

  _initDefaults() {
    this.rowHeight = HtmlEnvironment.get().formRowHeight;
  }

  _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this.smartField.invalidateLayoutTree();
  }

  layout($container) {
    let htmlContainer = HtmlComponent.get($container),
      $input = $container.children('.multiline-input'),
      $lines = $container.children('.multiline-lines'),
      innerSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());

    $input.cssHeight(this.rowHeight);
    $lines.cssHeight(innerSize.height - this.rowHeight);
  }
}
