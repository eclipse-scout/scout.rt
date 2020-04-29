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
import {FormFieldLayout, graphics} from '../../../index';

export default class ButtonLayout extends FormFieldLayout {

  constructor(button) {
    super(button);
    this.button = button;
  }

  layout($container) {
    super.layout($container);

    let $icon = this.button.get$Icon(),
      $submenuIcon = this.button.$submenuIcon,
      $label = this.button.$buttonLabel,
      $fieldContainer = this.button.$fieldContainer;

    // Set max width to make it possible to set text-overflow: ellipsis using CSS
    $label.css('max-width', ''); // reset required because .size() operations below might return wrong results when label contains complex HTML
    let submenuIconWidth = $submenuIcon ? graphics.size($submenuIcon, {
      includeMargin: true,
      exact: true
    }).width : 0;
    let iconWidth = $icon.length ? graphics.size($icon, {
      includeMargin: true,
      exact: true
    }).width : 0;
    // Round up to make sure ellipsis are not shown unnecessarily when having rounding issues (e.g. in IE 11)
    let labelMaxWidth = Math.ceil($fieldContainer.width() - (submenuIconWidth + iconWidth));
    $label.css('max-width', labelMaxWidth);
  }

  preferredLayoutSize($container, options) {
    let $label = this.button.$buttonLabel;

    // Reset max width before calculating pref size
    let maxWidth = $label.css('max-width');
    $label.css('max-width', '');

    let prefSize = super.preferredLayoutSize($container, options);
    $label.css('max-width', maxWidth);

    return prefSize;
  }
}
