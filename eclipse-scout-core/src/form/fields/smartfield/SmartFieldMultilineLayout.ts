/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, HtmlComponent, HtmlEnvironment, SmartField} from '../../../index';

/**
 * This layout only layouts the INPUT and DIV part of the multi-line smart-field, not the entire form-field.
 */
export class SmartFieldMultilineLayout extends AbstractLayout {
  smartField: SmartField<any>;
  htmlPropertyChangeHandler: () => void;
  rowHeight: number;

  constructor(smartField: SmartField<any>) {
    super();
    this.smartField = smartField;

    this._initDefaults();

    this.htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this.htmlPropertyChangeHandler);
    this.smartField.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this.htmlPropertyChangeHandler);
    });
  }

  protected _initDefaults() {
    this.rowHeight = HtmlEnvironment.get().formRowHeight;
  }

  protected _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this.smartField.invalidateLayoutTree();
  }

  override layout($container: JQuery) {
    let htmlContainer = HtmlComponent.get($container),
      $input = $container.children('.multiline-input'),
      $lines = $container.children('.multiline-lines'),
      innerSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());

    $input.cssHeight(this.rowHeight);
    $lines.cssHeight(innerSize.height - this.rowHeight);
  }
}
