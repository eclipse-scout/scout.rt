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
import {Button, Dimension, FormFieldLayout, graphics, HtmlCompPrefSizeOptions} from '../../../index';

export class ButtonLayout extends FormFieldLayout {
  declare formField: Button;

  constructor(button: Button) {
    super(button);
  }

  override layout($container: JQuery) {
    super.layout($container);

    let $icon = this.formField.get$Icon(),
      $submenuIcon = this.formField.$submenuIcon,
      $label = this.formField.$buttonLabel,
      $fieldContainer = this.formField.$fieldContainer;

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

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let $label = this.formField.$buttonLabel;

    // Reset max width before calculating pref size
    let maxWidth = $label.css('max-width');
    $label.css('max-width', '');

    let prefSize = super.preferredLayoutSize($container, options);
    $label.css('max-width', maxWidth);

    return prefSize;
  }
}
