/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Form-Field Layout, for a form-field with label, status, mandatory-indicator and a field.
 * This layout class works with a FormField instance, since we must access properties of the model.
 * Note: we use optGet() here, since some form-fields have only a bare HTML element as field, other
 * (composite) form-fields work with a HtmlComponent which has its own LayoutManager.
 */
scout.FormFieldLayout = function(formField) {
  scout.FormFieldLayout.parent.call(this);
  this.formField = formField;
  this.mandatoryIndicatorWidth = scout.HtmlEnvironment.fieldMandatoryIndicatorWidth;
  this.statusWidth = scout.HtmlEnvironment.fieldStatusWidth;
  this.rowHeight = scout.HtmlEnvironment.formRowHeight;
};
scout.inherits(scout.FormFieldLayout, scout.AbstractLayout);

// Minimum field with to normal state, for smaller widths the "compact" style is applied.
scout.FormFieldLayout.MIN_FIELD_WIDTH = 61;

scout.FormFieldLayout.prototype.layout = function($container) {
  var containerPadding, fieldOffset, fieldSize, fieldBounds, htmlField, labelHasFieldWidth, top, bottom, left, right,
    htmlContainer = scout.HtmlComponent.get($container),
    formField = this.formField,
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
    if (formField.labelWidthInPixel === scout.FormField.LabelWidth.UI || formField.labelUseUiWidth) {
      if (formField.$label.hasClass('empty')) {
        labelWidth = 0;
      } else {
        labelWidth = scout.graphics.prefSize(formField.$label, true).width;
      }
    }
    if (scout.isOneOf(formField.labelPosition, scout.FormField.LabelPosition.DEFAULT, scout.FormField.LabelPosition.LEFT)) {
      scout.graphics.setBounds(formField.$label, left, top, labelWidth, this.rowHeight);
      left += labelWidth + formField.$label.cssMarginX();
    } else if (formField.labelPosition === scout.FormField.LabelPosition.TOP) {
      formField.$label.cssHeight(this.rowHeight);
      top += formField.$label.outerHeight(true);
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
    if (formField.statusPosition === scout.FormField.StatusPosition.TOP && labelHasFieldWidth) {
      var statusHeight = scout.graphics.prefSize(formField.$status, {
        useCssSize: true
      }).height;
      // Vertically center status with label
      var statusTop = containerPadding.top + formField.$label.cssPaddingTop() + (formField.$label.height() / 2) - (statusHeight / 2);
      formField.$status
        .cssTop(statusTop)
        .cssRight(right + formField.$label.cssMarginRight())
        .cssHeight(statusHeight)
        .cssLineHeight(null);
      // Add padding to label to prevent overlay of text and status icon
      var w = scout.graphics.size(formField.$status, true).width;
      formField.$label.cssPaddingRight(w);
    } else {
      // Default status position
      formField.$status
        .cssTop(top)
        .cssRight(right)
        .cssHeight(this.rowHeight)
        .cssLineHeight(this.rowHeight);
      right += statusWidth + formField.$status.cssMarginX();
    }
  }

  if (formField.$fieldContainer) {
    // Calculate the additional field offset (because of label, mandatory indicator etc.) without the containerInset.
    fieldOffset = new scout.Insets(
      top - containerPadding.top,
      right - containerPadding.right,
      bottom - containerPadding.bottom,
      left - containerPadding.left);
    // Calculate field size: "available size" - "insets (border and padding)" - "additional offset" - "field's margin"
    var fieldMargins = scout.graphics.margins(formField.$fieldContainer);
    fieldSize = htmlContainer.availableSize({
        exact: true
      })
      .subtract(htmlContainer.insets())
      .subtract(fieldOffset)
      .subtract(fieldMargins);
    fieldBounds = new scout.Rectangle(left, top, fieldSize.width, fieldSize.height);
    if (formField.$fieldContainer.css('position') !== 'absolute') {
      fieldBounds.x = 0;
      fieldBounds.y = 0;
    }
    htmlField = scout.HtmlComponent.optGet(formField.$fieldContainer);
    if (htmlField) {
      htmlField.setBounds(fieldBounds);
    } else {
      scout.graphics.setBounds(formField.$fieldContainer, fieldBounds);
    }
    formField.$field.toggleClass('compact', fieldBounds.width <= scout.FormFieldLayout.MIN_FIELD_WIDTH);
    formField.$container.toggleClass('compact', fieldBounds.width <= scout.FormFieldLayout.MIN_FIELD_WIDTH);

    if (labelHasFieldWidth) {
      var fieldWidth = fieldSize.add(fieldMargins).width - formField.$label.cssMarginX();
      if (formField.$mandatory && formField.$mandatory.isVisible()) {
        fieldWidth += formField.$mandatory.outerWidth(true);
      }
      formField.$label.cssWidth(fieldWidth);
    }
  }

  if (formField.$fieldContainer) {
    // Icons are placed inside the field (as overlay)
    var $iconInput = this._$elementForIconLayout(formField);
    var fieldBorder = scout.graphics.borders($iconInput);
    var inputBounds = scout.graphics.offsetBounds($iconInput);
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
  if (formField.tooltip && formField.tooltip.rendered) {
    formField.tooltip.position();
  }

  // Check for scrollbars, update them if neccessary
  if (formField.$field) {
    scout.scrollbars.update(formField.$field);
  }

  this._layoutDisabledCopyOverlay();
};

scout.FormFieldLayout.prototype._layoutDisabledCopyOverlay = function() {
  if (this.formField.$field && this.formField.$disabledCopyOverlay) {
    var $overlay = this.formField.$disabledCopyOverlay;
    var $field = this.formField.$field;

    var pos = $field.position();
    var padding = scout.graphics.insets($field, {
      includePadding: true
    });

    // subtract scrollbars sizes from width and height so overlay does not block scrollbars
    // we read the size from the scrollbar from our device, because we already determined
    // it on startup
    var elem = $field[0];
    var scrollHorizontal = (elem.scrollWidth - elem.clientWidth) > 0;
    var scrollVertical = (elem.scrollHeight - elem.clientHeight) > 0;
    var scrollbarSize = scout.device.scrollbarWidth;

    $overlay
      .css('top', pos.top)
      .css('left', pos.left)
      .width($field.width() + padding.horizontal() - (scrollVertical ? scrollbarSize : 0))
      .height($field.height() + padding.vertical() - (scrollHorizontal ? scrollbarSize : 0));
  }
};

scout.FormFieldLayout.prototype._isLabelVisible = function() {
  return !!this.formField.$label && this.formField.labelVisible;
};

scout.FormFieldLayout.prototype._isStatusVisible = function() {
  return !!this.formField.$status && (this.formField.statusVisible || this.formField.$status.isVisible());
};

scout.FormFieldLayout.prototype.preferredLayoutSize = function($container, options) {
  var htmlContainer = scout.HtmlComponent.get(this.formField.$container);
  var formField = this.formField;
  var prefSizeLabel = new scout.Dimension();
  var prefSizeMandatory = new scout.Dimension();
  var prefSizeStatus = new scout.Dimension();
  var prefSizeField = new scout.Dimension();
  var widthHint = scout.nvl(options.widthHint, 0);
  var heightHint = scout.nvl(options.heightHint, 0);
  // Status is only pulled up if status AND label are on top
  var statusOnTop = formField.statusPosition === scout.FormField.StatusPosition.TOP && this._isLabelVisible() && formField.labelPosition === scout.FormField.LabelPosition.TOP;

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
    if (formField.labelPosition === scout.FormField.LabelPosition.TOP) {
      // Label is always as width as the field if it is on top
      prefSizeLabel.width = 0;
    } else if (formField.labelWidthInPixel === scout.FormField.LabelWidth.UI || formField.labelUseUiWidth) {
      if (formField.$label.hasClass('empty')) {
        prefSizeLabel.width = 0;
      } else {
        prefSizeLabel = scout.graphics.prefSize(formField.$label, true);
      }
    }

    if (scout.isOneOf(formField.labelPosition, scout.FormField.LabelPosition.DEFAULT, scout.FormField.LabelPosition.LEFT)) {
      widthHint -= prefSizeLabel.width;
    } else if (formField.labelPosition === scout.FormField.LABEL_POSITION_TOP) {
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
    var fieldMargins = scout.graphics.margins(formField.$fieldContainer);
    if (options.widthHint) {
      widthHint -= fieldMargins.horizontal();
      options.widthHint = widthHint;
    }
    if (options.heightHint) {
      heightHint -= fieldMargins.vertical();
      options.heightHint = heightHint;
    }
    var htmlField = scout.HtmlComponent.optGet(formField.$fieldContainer);
    if (htmlField) {
      prefSizeField = htmlField.prefSize(options)
        .add(fieldMargins);
    } else {
      prefSizeField = scout.graphics.prefSize(formField.$fieldContainer, options)
        .add(fieldMargins);
    }
  }

  // Now sum up to calculate the preferred size of the container
  var prefSize = new scout.Dimension();

  // Field is the base, and it should be at least as height as a form row height.
  prefSize.width = prefSizeField.width;
  prefSize.height = prefSizeField.height;

  // Mandatory
  prefSize.width += prefSizeMandatory.width;
  prefSize.height = Math.max(prefSize.height, prefSizeMandatory.height);

  // Label
  if (scout.isOneOf(formField.labelPosition, scout.FormField.LabelPosition.DEFAULT, scout.FormField.LabelPosition.LEFT)) {
    prefSize.width += prefSizeLabel.width;
    prefSize.height = Math.max(prefSize.height, prefSizeLabel.height);
  } else if (formField.labelPosition === scout.FormField.LabelPosition.TOP) {
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
};

/**
 * @returns the input element used to position the icon. May be overridden if another element than $field should be used.
 */
scout.FormFieldLayout.prototype._$elementForIconLayout = function() {
  return this.formField.$field;
};

scout.FormFieldLayout.prototype._layoutIcon = function(formField, fieldBounds, right, top) {
  var height = this.rowHeight;
  if (fieldBounds) {
    // If field is bigger than rowHeight (e.g. if used in desktop cell editor), make sure icon is as height as field
    height = fieldBounds.height;
  }
  formField.$icon
    .cssRight(right)
    .cssTop(fieldBounds.y)
    .cssHeight(height)
    .cssLineHeight(height);
};

scout.FormFieldLayout.prototype._layoutClearIcon = function(formField, fieldBounds, right, top) {
  var height = this.rowHeight;
  if (fieldBounds) {
    // If field is bigger than rowHeight (e.g. if used in desktop cell editor), make sure icon is as height as field
    height = fieldBounds.height;
  }
  if (formField instanceof scout.BasicField && formField.gridData.horizontalAlignment > 0) {
    formField.$clearIcon
      .cssLeft(fieldBounds.x)
      .cssTop(fieldBounds.y)
      .cssHeight(height)
      .cssLineHeight(height);
  } else {
    formField.$clearIcon
      .cssRight(right)
      .cssTop(fieldBounds.y)
      .cssHeight(height)
      .cssLineHeight(height);
  }
};

scout.FormFieldLayout.prototype.labelWidth = function() {
  // use configured label width in pixel or default label width
  if (scout.FormField.LabelWidth.DEFAULT === this.formField.labelWidthInPixel) {
    return scout.HtmlEnvironment.fieldLabelWidth;
  }
  return this.formField.labelWidthInPixel;
};
