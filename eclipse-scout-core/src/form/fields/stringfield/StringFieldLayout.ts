/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, Dimension, FormField, FormFieldLayout, graphics, HtmlCompPrefSizeOptions, Insets, Rectangle, StringField} from '../../../index';

export class StringFieldLayout extends FormFieldLayout {
  declare formField: StringField;

  constructor(stringField: StringField) {
    super(stringField);
  }

  protected override _layoutClearIcon(formField: StringField, fieldBounds: Rectangle, right: number, top: number) {
    if (formField.$icon && formField.$icon.isVisible()) {
      right += graphics.prefSize(formField.$icon, true).width;
    }
    super._layoutClearIcon(formField, fieldBounds, right, top);
  }

  protected override _getPrefFieldSize(formField: FormField, options?: HtmlCompPrefSizeOptions, fieldMargins?: Insets): Dimension {
    if (formField.gridData.useUiHeight || formField.gridData.useUiWidth) {
      let insets = graphics.insets(formField.$field);

      formField.$field.css('width', '');
      formField.$field.css('height', '');

      let minSize = graphics.cssMinSize(formField.$field);
      let maxSize = graphics.cssMaxSize(formField.$field);

      let oldStyle = formField.$field.attr('style');
      let oldScrollLeft = formField.$field.scrollLeft();
      let oldScrollTop = formField.$field.scrollTop();

      formField.$field.css('overflow', 'hidden');
      formField.$field.css('border', 'none');
      formField.$field.css('padding', '0');
      formField.$field.css('backgroundColor', 'red');

      if (options.widthHint) {
        graphics.setSize(formField.$field, new Dimension(options.widthHint - insets.horizontal(), 0));
      } else {
        formField.$field.css('textWrap', 'nowrap');
        graphics.setSize(formField.$field, new Dimension(0, 0));
      }

      let scrollSize = new Dimension(formField.$field[0].scrollWidth, formField.$field[0].scrollHeight);
      // add 1px to ensure cursor is visible and compensate for rounding errors in browsers
      scrollSize = scrollSize.add(new Insets(1, 1));

      // Add space for scrollbar if necessary
      if ((formField as StringField).multilineText) {
        let scrollbarWidth = Device.get().scrollbarWidth;
        let hasMaxSize = maxSize.width < Number.MAX_VALUE || maxSize.height < Number.MAX_VALUE;
        if ((formField as StringField).wrapText) {
          // horizontal scrollbar can never occur. vertical scrollbar can occur if height is limited.
          if (!formField.gridData.useUiHeight || !hasMaxSize) {
            scrollSize = scrollSize.add(new Insets(0, scrollbarWidth));
          }
        } else {
          // scrollbars in both directions are possible
          scrollSize = scrollSize.add(new Insets(scrollbarWidth, scrollbarWidth));
        }
      }

      // re-add insets because the measurement was taken without them
      let prefSize = new Dimension(scrollSize).add(insets);

      // limit size according to css
      prefSize.width = Math.min(Math.max(prefSize.width, minSize.width), maxSize.width);
      prefSize.height = Math.min(Math.max(prefSize.height, minSize.height), maxSize.height);

      // reset the modified style attribute but at the same time set the size to the computed prefSize.
      // this prevents scrollbars from being shown when they were not really necessary.
      formField.$field.attr('style', oldStyle + ' width: ' + prefSize.width + 'px; height: ' + prefSize.height + 'px;');
      formField.$field.scrollLeft(oldScrollLeft);
      formField.$field.scrollTop(oldScrollTop);

      return prefSize;
    }

    return super._getPrefFieldSize(formField, options, fieldMargins);
  }
}
