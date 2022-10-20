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
import {AbstractLayout, BasicField, Device, Dimension, FormField, graphics, HtmlComponent, HtmlEnvironment, Insets, Rectangle, scout, scrollbars} from '../../index';

/**
 * Form-Field Layout, for a form-field with label, status, mandatory-indicator and a field.
 * This layout class works with a FormField instance, since we must access properties of the model.
 * Note: we use optGet() here, since some form-fields have only a bare HTML element as field, other
 * (composite) form-fields work with a HtmlComponent which has its own LayoutManager.
 */
export default class FormFieldLayout extends AbstractLayout {

  constructor(formField) {
    super();
    this.formField = formField;
    this._initDefaults();

    this.htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this.htmlPropertyChangeHandler);
    this.formField.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this.htmlPropertyChangeHandler);
    });
  }

  // Minimum field with to normal state, for smaller widths the "compact" style is applied.
  static COMPACT_FIELD_WIDTH = 61;

  _initDefaults() {
    this.mandatoryIndicatorWidth = HtmlEnvironment.get().fieldMandatoryIndicatorWidth;
    this.statusWidth = HtmlEnvironment.get().fieldStatusWidth;
    this.rowHeight = HtmlEnvironment.get().formRowHeight;
    this.compactFieldWidth = FormFieldLayout.COMPACT_FIELD_WIDTH;
  }

  _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this.formField.invalidateLayoutTree();
  }

  layout($container) {
    let containerPadding, fieldOffset, fieldSize, fieldBounds, htmlField, labelHasFieldWidth, top, bottom, left, right,
      htmlContainer = HtmlComponent.get($container),
      formField = this.formField,
      tooltip = formField._tooltip(),
      labelWidth = this.labelWidth(),
      statusWidth = this.statusWidth;

    // Note: Position coordinates start _inside_ the border, therefore we only use the padding
    containerPadding = htmlContainer.insets({
      includeBorder: false
    });
    top = containerPadding.top;
    right = containerPadding.right;
    bottom = containerPadding.bottom;
    left = containerPadding.left;

    if (this._isLabelVisible()) {
      // currently a gui only flag, necessary for sequencebox
      if (formField.labelWidthInPixel === FormField.LabelWidth.UI || formField.labelUseUiWidth) {
        if (formField.$label.hasClass('empty')) {
          labelWidth = 0;
        } else {
          labelWidth = graphics.prefSize(formField.$label).width;
        }
      }
      if (scout.isOneOf(formField.labelPosition, FormField.LabelPosition.DEFAULT, FormField.LabelPosition.LEFT)) {
        graphics.setBounds(formField.$label, left, top, labelWidth, this.rowHeight);
        left += labelWidth + formField.$label.cssMarginX();
      } else if (formField.labelPosition === FormField.LabelPosition.TOP) {
        let labelHeight = graphics.prefSize(formField.$label).height;
        // prefSize rounds the value -> ensure label height is set to that value to prevent gaps between container and label.
        // In addition, this also ensures that the correct height is set when changing the label position from left to top
        formField.$label.cssHeight(labelHeight);
        top += labelHeight + formField.$label.cssMarginY();
        labelHasFieldWidth = true;
      }
    }
    if (formField.$mandatory && formField.$mandatory.isVisible()) {
      formField.$mandatory
        .cssTop(top)
        .cssLeft(left)
        .cssWidth(this.mandatoryIndicatorWidth);
      left += formField.$mandatory.outerWidth(true);
    }
    if (this._isStatusVisible()) {
      formField.$status
        .cssWidth(statusWidth);
      // If both status and label position is "top", pull status up (without margin on the right side)
      if (formField.statusPosition === FormField.StatusPosition.TOP && labelHasFieldWidth) {
        let statusHeight = graphics.prefSize(formField.$status, {
          useCssSize: true
        }).height;
        // Vertically center status with label
        let statusTop = containerPadding.top + formField.$label.cssPaddingTop() + (formField.$label.height() / 2) - (statusHeight / 2);
        formField.$status
          .cssTop(statusTop)
          .cssRight(right + formField.$label.cssMarginRight())
          .cssHeight(statusHeight);
        // Add padding to label to prevent overlay of text and status icon
        let w = graphics.size(formField.$status, true).width;
        formField.$label.cssPaddingRight(w);
      } else {
        // Default status position
        formField.$status
          .cssTop(top)
          .cssRight(right)
          .cssHeight(this.rowHeight);
        right += statusWidth + formField.$status.cssMarginX();
      }
    }

    if (formField.$fieldContainer) {
      // Calculate the additional field offset (because of label, mandatory indicator etc.) without the containerInset.
      fieldOffset = new Insets(
        top - containerPadding.top,
        right - containerPadding.right,
        bottom - containerPadding.bottom,
        left - containerPadding.left);
      // Calculate field size: "available size" - "insets (border and padding)" - "additional offset" - "field's margin"
      let fieldMargins = graphics.margins(formField.$fieldContainer);
      fieldSize = htmlContainer.availableSize({
        exact: true
      })
        .subtract(htmlContainer.insets())
        .subtract(fieldOffset)
        .subtract(fieldMargins);
      fieldBounds = new Rectangle(left, top, fieldSize.width, fieldSize.height);
      if (formField.$fieldContainer.css('position') !== 'absolute') {
        fieldBounds.x = 0;
        fieldBounds.y = 0;
      }
      htmlField = HtmlComponent.optGet(formField.$fieldContainer);
      if (htmlField) {
        htmlField.setBounds(fieldBounds);
      } else {
        graphics.setBounds(formField.$fieldContainer, fieldBounds);
      }
      if (this.compactFieldWidth > -1) {
        formField.$field.toggleClass('compact', fieldBounds.width <= this.compactFieldWidth);
        formField.$container.toggleClass('compact', fieldBounds.width <= this.compactFieldWidth);
      }

      if (labelHasFieldWidth) {
        let fieldWidth = fieldSize.add(fieldMargins).width - formField.$label.cssMarginX();
        if (formField.$mandatory && formField.$mandatory.isVisible()) {
          fieldWidth += formField.$mandatory.outerWidth(true);
        }
        formField.$label.cssWidth(fieldWidth);
      }
    }

    if (formField.$fieldContainer) {
      // Icons are placed inside the field (as overlay)
      let $iconInput = this._$elementForIconLayout();
      let fieldBorder = graphics.borders($iconInput);
      let inputBounds = graphics.offsetBounds($iconInput);
      top += fieldBorder.top;
      right += fieldBorder.right;
      fieldBounds.x += fieldBorder.left;
      fieldBounds.y += fieldBorder.top;
      fieldBounds.height = inputBounds.height - fieldBorder.top - fieldBorder.bottom;
      fieldBounds.width = inputBounds.width - fieldBorder.left - fieldBorder.right;

      if (formField.$icon) {
        this._layoutIcon(formField, fieldBounds, right, top);
      }

      // Clear icon if present
      if (formField.$clearIcon) {
        this._layoutClearIcon(formField, fieldBounds, right, top);
      }
    }

    // Make sure tooltip is at correct position after layouting, if there is one
    if (tooltip && tooltip.rendered) {
      tooltip.position();
    }

    // Check for scrollbars, update them if necessary
    if (formField.$field) {
      scrollbars.update(formField.$field, true);
    }

    this._layoutDisabledCopyOverlay();
  }

  _layoutDisabledCopyOverlay() {
    if (this.formField.$field && this.formField.$disabledCopyOverlay) {
      let $overlay = this.formField.$disabledCopyOverlay;
      let $field = this.formField.$field;

      let pos = $field.position();
      let padding = graphics.insets($field, {
        includePadding: true
      });

      // subtract scrollbars sizes from width and height so overlay does not block scrollbars
      // we read the size from the scrollbar from our device, because we already determined
      // it on startup. Only do this when element is scrollable.
      let elem = $field[0];
      let overflowX = $field.css('overflow-x');
      let overflowY = $field.css('overflow-y');
      let scrollHorizontal = overflowX === 'scroll' || overflowX === 'auto' && (elem.scrollWidth - elem.clientWidth) > 0;
      let scrollVertical = overflowY === 'scroll' || overflowY === 'auto' && (elem.scrollHeight - elem.clientHeight) > 0;
      let scrollbarSize = Device.get().scrollbarWidth;

      $overlay
        .css('top', pos.top)
        .css('left', pos.left)
        .width($field.width() + padding.horizontal() - (scrollVertical ? scrollbarSize : 0))
        .height($field.height() + padding.vertical() - (scrollHorizontal ? scrollbarSize : 0));

    }
  }

  _isLabelVisible() {
    return !!this.formField.$label && this.formField.labelVisible;
  }

  _isStatusVisible() {
    return !!this.formField.$status && (this.formField.statusVisible || this.formField.$status.isVisible());
  }

  preferredLayoutSize($container, options) {
    let htmlContainer = HtmlComponent.get(this.formField.$container);
    let formField = this.formField;
    let prefSizeLabel = new Dimension();
    let prefSizeMandatory = new Dimension();
    let prefSizeStatus = new Dimension();
    let prefSizeField = new Dimension();
    let widthHint = scout.nvl(options.widthHint, 0);
    let heightHint = scout.nvl(options.heightHint, 0);
    // Status is only pulled up if status AND label are on top
    let statusOnTop = formField.statusPosition === FormField.StatusPosition.TOP && this._isLabelVisible() && formField.labelPosition === FormField.LabelPosition.TOP;

    // Calculate the preferred sizes of the individual parts
    // Mandatory indicator
    if (formField.$mandatory && formField.$mandatory.isVisible()) {
      prefSizeMandatory.width = this.mandatoryIndicatorWidth + formField.$mandatory.cssMarginX();
      widthHint -= prefSizeMandatory.width;
    }

    // Label
    if (this._isLabelVisible()) {
      prefSizeLabel.width = this.labelWidth() + formField.$label.cssMarginX();
      prefSizeLabel.height = this.rowHeight;
      if (formField.labelPosition === FormField.LabelPosition.TOP) {
        // Label is always as width as the field if it is on top
        prefSizeLabel.width = 0;
        prefSizeLabel.height = graphics.prefSize(formField.$label, true).height;
      } else if (formField.labelWidthInPixel === FormField.LabelWidth.UI || formField.labelUseUiWidth) {
        if (formField.$label.hasClass('empty')) {
          prefSizeLabel.width = 0;
        } else {
          prefSizeLabel = graphics.prefSize(formField.$label, true);
        }
      }

      if (scout.isOneOf(formField.labelPosition, FormField.LabelPosition.DEFAULT, FormField.LabelPosition.LEFT)) {
        widthHint -= prefSizeLabel.width;
      } else if (formField.labelPosition === FormField.LabelPosition.TOP) {
        heightHint -= prefSizeLabel.height;
      }
    }

    // Status
    if (this._isStatusVisible()) {
      prefSizeStatus.width = this.statusWidth + formField.$status.cssMarginX();
      if (!statusOnTop) {
        prefSizeStatus.height = this.rowHeight;
        widthHint -= prefSizeStatus.width;
      }
    }

    // Field
    if (formField.$fieldContainer) {
      let fieldMargins = graphics.margins(formField.$fieldContainer);
      let htmlField = HtmlComponent.optGet(formField.$fieldContainer);
      if (!htmlField) {
        widthHint -= fieldMargins.horizontal();
        heightHint -= fieldMargins.vertical();
      }
      if (options.widthHint) {
        options.widthHint = widthHint;
      }
      if (options.heightHint) {
        options.heightHint = heightHint;
      }
      if (htmlField) {
        prefSizeField = htmlField.prefSize(options)
          .add(fieldMargins);
      } else {
        prefSizeField = graphics.prefSize(formField.$fieldContainer, options)
          .add(fieldMargins);
      }
    }

    // Now sum up to calculate the preferred size of the container
    let prefSize = new Dimension();

    // Field is the base, and it should be at least as height as a form row height.
    prefSize.width = prefSizeField.width;
    prefSize.height = prefSizeField.height;

    // Mandatory
    prefSize.width += prefSizeMandatory.width;
    prefSize.height = Math.max(prefSize.height, prefSizeMandatory.height);

    // Label
    if (scout.isOneOf(formField.labelPosition, FormField.LabelPosition.DEFAULT, FormField.LabelPosition.LEFT)) {
      prefSize.width += prefSizeLabel.width;
      prefSize.height = Math.max(prefSize.height, prefSizeLabel.height);
    } else if (formField.labelPosition === FormField.LabelPosition.TOP) {
      prefSize.width = Math.max(prefSize.width, prefSizeLabel.width);
      prefSize.height += prefSizeLabel.height;
    }

    // Status
    if (!statusOnTop) {
      prefSize.width += prefSizeStatus.width;
      prefSize.height = Math.max(prefSize.height, prefSizeStatus.height);
    }

    // Add padding and border
    prefSize = prefSize.add(htmlContainer.insets());

    return prefSize;
  }

  /**
   * @returns {$} the input element used to position the icon. May be overridden if another element than $field should be used.
   */
  _$elementForIconLayout() {
    return this.formField.$field;
  }

  _layoutIcon(formField, fieldBounds, right, top) {
    let height = this.rowHeight;
    if (fieldBounds) {
      // If field is bigger than rowHeight (e.g. if used in desktop cell editor), make sure icon is as height as field
      height = fieldBounds.height;
    }
    formField.$icon
      .cssRight(right)
      .cssTop(fieldBounds.y)
      .cssHeight(height);
  }

  _layoutClearIcon(formField, fieldBounds, right, top) {
    let height = this.rowHeight;
    if (fieldBounds) {
      // If field is bigger than rowHeight (e.g. if used in desktop cell editor), make sure icon is as height as field
      height = fieldBounds.height;
    }
    if (formField instanceof BasicField && formField.gridData.horizontalAlignment > 0) {
      formField.$clearIcon
        .cssLeft(fieldBounds.x)
        .cssRight('')
        .cssTop(fieldBounds.y)
        .cssHeight(height);
    } else {
      formField.$clearIcon
        .cssLeft('')
        .cssRight(right)
        .cssTop(fieldBounds.y)
        .cssHeight(height);
    }
  }

  labelWidth() {
    // use configured label width in pixel or default label width
    if (FormField.LabelWidth.DEFAULT === this.formField.labelWidthInPixel) {
      return HtmlEnvironment.get().fieldLabelWidth;
    }
    return this.formField.labelWidthInPixel;
  }
}
