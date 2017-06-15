/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

  // use configured label width in pixel or default label width
  if (scout.FormFieldLayout.LABEL_WIDTH_DEFAULT === formField.labelWidthInPixel) {
    this.labelWidth = scout.HtmlEnvironment.fieldLabelWidth;
  } else {
    this.labelWidth = formField.labelWidthInPixel;
  }
};
scout.inherits(scout.FormFieldLayout, scout.AbstractLayout);

//Minimum field with to normal state, for smaller widths the "compact" style is applied.
scout.FormFieldLayout.MIN_FIELD_WIDTH = 50;

scout.FormFieldLayout.LABEL_WIDTH_UI = -1;
scout.FormFieldLayout.LABEL_WIDTH_DEFAULT = 0;

scout.FormFieldLayout.prototype.layout = function($container) {
  var containerPadding, fieldOffset, fieldSize, fieldBounds, htmlField, labelHasFieldWidth, top, bottom, left, right,
    htmlContainer = scout.HtmlComponent.get($container),
    formField = this.formField,
    labelWidth = this.labelWidth,
    statusWidth = this.statusWidth;

  // Note: Position coordinates start _inside_ the border, therefore we only use the padding
  containerPadding = htmlContainer.getInsets({
    includeBorder: false
  });
  top = containerPadding.top;
  right = containerPadding.right;
  bottom = containerPadding.bottom;
  left = containerPadding.left;

  if (this._isLabelVisible()) {
    // currently a gui only flag, necessary for sequencebox
    if (labelWidth === scout.FormFieldLayout.LABEL_WIDTH_UI || formField.labelUseUiWidth) {
      if (formField.$label.hasClass('empty')) {
        labelWidth = 0;
      } else {
        labelWidth = scout.graphics.prefSize(formField.$label, true).width;
      }
    }
    if (scout.isOneOf(formField.labelPosition, scout.FormField.LABEL_POSITION_DEFAULT, scout.FormField.LABEL_POSITION_LEFT)) {
      scout.graphics.setBounds(formField.$label, top, left, labelWidth, this.rowHeight);
      left += labelWidth;
    } else if (formField.labelPosition === scout.FormField.LABEL_POSITION_TOP) {
      formField.$label.cssHeight(this.rowHeight);
      top += formField.$label.outerHeight(true);
      labelHasFieldWidth = true;
    }
  }
  if (formField.$mandatory) {
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
    if (formField.statusPosition === scout.FormField.STATUS_POSITION_TOP && labelHasFieldWidth) {
      var statusHeight = scout.graphics.prefSize(formField.$status).height;
      // Vertically center status with label
      var statusTop = containerPadding.top + formField.$label.cssPaddingTop() + (formField.$label.height() / 2) - (statusHeight / 2);
      formField.$status
        .cssTop(statusTop)
        .cssRight(right + formField.$label.cssMarginRight())
        .cssHeight(statusHeight);
      // Add padding to label to prevent overlay of text and status icon
      var w = scout.graphics.getSize(formField.$status, true).width;
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
    var fieldMargins = scout.graphics.getMargins(formField.$fieldContainer);
    fieldSize = htmlContainer.getAvailableSize()
      .subtract(htmlContainer.getInsets())
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
    formField.$field.toggleClass('compact', fieldBounds.width < scout.FormFieldLayout.MIN_FIELD_WIDTH);

    if (labelHasFieldWidth) {
      var fieldWidth = fieldSize.add(fieldMargins).width - formField.$label.cssMarginX();
      if (formField.$mandatory) {
        fieldWidth += formField.$mandatory.outerWidth(true);
      }
      formField.$label.cssWidth(fieldWidth);
    }
  }

  // Icon is placed inside the field (as overlay)
  if (formField.$icon && formField.$field) {
    this._layoutIcon(formField, fieldBounds, right, top);
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
    var padding = scout.graphics.getInsets($field, {
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
  return this.formField.$label && this.formField.labelVisible;
};

scout.FormFieldLayout.prototype._isStatusVisible = function() {
  return this.formField.$status && (this.formField.statusVisible || this.formField.$status.isVisible());
};

scout.FormFieldLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize, htmlField, labelPositionLeft,
    width = 0,
    htmlContainer = scout.HtmlComponent.get($container),
    height = scout.HtmlEnvironment.formRowHeight,
    labelWidth = this.labelWidth,
    topLabelHeight = 0, // only set when label is on top
    formField = this.formField;

  if (this._isLabelVisible()) {
    if (formField.labelUseUiWidth) {
      if (formField.$label.hasClass('empty')) {
        labelWidth = 0;
      } else {
        labelWidth = scout.graphics.prefSize(formField.$label, true).width;
      }
    }
    labelPositionLeft = formField.labelPosition === scout.FormField.LABEL_POSITION_DEFAULT ||
      formField.labelPosition === scout.FormField.LABEL_POSITION_LEFT;
    if (labelPositionLeft) {
      width += labelWidth;
    } else if (formField.labelPosition === scout.FormField.LABEL_POSITION_TOP) {
      topLabelHeight = formField.$label.outerHeight(true);
      height += topLabelHeight;
    }
  }
  if (formField.$mandatory) {
    width += this.mandatoryIndicatorWidth + formField.$mandatory.cssMarginX();
  }
  if (this._isStatusVisible()) {
    width += this.statusWidth + formField.$status.cssMarginX();
  }

  if (formField.$fieldContainer) {
    htmlField = scout.HtmlComponent.optGet(formField.$fieldContainer);
    if (htmlField) {
      prefSize = htmlField.getPreferredSize()
        .add(htmlContainer.getInsets())
        .add(htmlField.getMargins());
      prefSize.height += topLabelHeight;
    } else {
      prefSize = this.naturalSize(formField);
    }
  } else {
    prefSize = new scout.Dimension(0, 0);
  }
  width += prefSize.width;
  height = Math.max(height, prefSize.height);

  return new scout.Dimension(width, height);
};

scout.FormFieldLayout.prototype._layoutIcon = function(formField, fieldBounds, right, top) {
  var height = this.rowHeight;
  if (fieldBounds) {
    // If field is bigger than rowHeight (e.g. if used in desktop cell editor), make sure icon is as height as field
    height = fieldBounds.height;
  }
  formField.$icon
    .cssRight(formField.$field.cssBorderRightWidth() + right)
    .cssTop(top)
    .cssHeight(height)
    .cssLineHeight(height);
};

/**
 * Returns the 'natural' size of the field - which means the current size of the field in the browser.
 * By default we return the size of the $fieldContainer. Override this method when you must return
 * another size (which is required when the field-content is scrollable).
 */
scout.FormFieldLayout.prototype.naturalSize = function(formField) {
  return scout.graphics.prefSize(formField.$fieldContainer, true);
};
