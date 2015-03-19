/**
 * Form-Field Layout, for a form-field with label, status, mandatory-indicator and a field.
 * This layout class works with a FormField instance, since we must access properties of the model.
 * Note: we use optGet() here, since some form-fields have only a bare HTML element as field, other
 * (composite) form-fields work with a HtmlComponent which has its own LayoutManager.
 */
scout.FormFieldLayout = function(formField) {
  scout.FormFieldLayout.parent.call(this);
  this.formField = formField;
  this.labelWidth = scout.HtmlEnvironment.fieldLabelWidth;
  this.mandatoryIndicatorWidth = scout.HtmlEnvironment.fieldMandatoryIndicatorWidth;
  this.statusWidth = scout.HtmlEnvironment.fieldStatusWidth;
  this.rowHeight = scout.HtmlEnvironment.formRowHeight;
};
scout.inherits(scout.FormFieldLayout, scout.AbstractLayout);

scout.FormFieldLayout.prototype.layout = function($container) {
  var containerSize, fieldSize, fieldBounds, htmlField, labelPositionLeft, labelHasFieldWidth,
    htmlContainer = scout.HtmlComponent.get($container),
    formField = this.formField,
    labelWidth = this.labelWidth,
    left = 0,
    right = 0,
    top = 0;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (formField.$label && formField.labelVisible) {
    // currently a gui only flag, necessary for sequencebox
    if (formField.labelUseUiWidth) {
      labelWidth = scout.graphics.prefSize(formField.$label, true).width;
    }
    labelPositionLeft = formField.labelPosition === scout.FormField.LABEL_POSITION_DEFAULT ||
      formField.labelPosition === scout.FormField.LABEL_POSITION_LEFT;
    if (labelPositionLeft) {
      scout.graphics.setBounds(formField.$label, 0, 0, labelWidth, this.rowHeight);
      left += labelWidth;
    } else if (formField.labelPosition === scout.FormField.LABEL_POSITION_TOP) {
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
  if (formField.$status && formField.statusVisible) {
    // can not check for $status.isVisible() since we want to reserve
    // space used for status even when $status is invisible.
    formField.$status
      .cssTop(top)
      .cssWidth(this.statusWidth)
      .cssHeight(this.rowHeight)
      .cssLineHeight(this.rowHeight);
    right += formField.$status.outerWidth(true);
  }

  if (formField.$fieldContainer) {
    fieldSize = containerSize.subtract(scout.graphics.getMargins(formField.$fieldContainer));
    fieldBounds = new scout.Rectangle(left, top, fieldSize.width - left - right, fieldSize.height - top);
    htmlField = scout.HtmlComponent.optGet(formField.$fieldContainer);
    if (htmlField) {
      htmlField.setBounds(fieldBounds);
    } else {
      scout.graphics.setBounds(formField.$fieldContainer, fieldBounds);
    }

    if (labelHasFieldWidth) {
      formField.$label.cssWidth(fieldBounds.width);
    }
  }

  // Icon is placed inside the field (as overlay)
  if (formField.$icon && formField.$field) {
    formField.$icon
      .cssRight(formField.$field.cssBorderRightWidth() + right)
      .cssTop(top);
  }

  // Make sure tooltip is at correct position after layouting, if there is one
  if (formField.tooltip && formField.tooltip.rendered) {
    formField.tooltip.position();
  }
};

scout.FormFieldLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize, htmlField, labelPositionLeft,
    width = 0,
    htmlContainer = scout.HtmlComponent.get($container),
    height = scout.HtmlEnvironment.formRowHeight,
    labelWidth = this.labelWidth,
    formField = this.formField;

  if (formField.$label && formField.labelVisible) {
    if (formField.labelUseUiWidth) {
      labelWidth = scout.graphics.prefSize(formField.$label, true).width;
    }
    labelPositionLeft = formField.labelPosition === scout.FormField.LABEL_POSITION_DEFAULT ||
      formField.labelPosition === scout.FormField.LABEL_POSITION_LEFT;
    if (labelPositionLeft) {
      width += labelWidth;
    } else if (formField.labelPosition === scout.FormField.LABEL_POSITION_TOP) {
      height += formField.$label.outerHeight(true);
    }
  }
  if (formField.$mandatory) {
    width += formField.$mandatory.outerWidth(true);
  }
  if (formField.$status && formField.statusVisible) {
    width += formField.$status.outerWidth(true);
  }

  if (formField.$fieldContainer) {
    htmlField = scout.HtmlComponent.optGet(formField.$fieldContainer);
    if (htmlField) {
      prefSize = htmlField.getPreferredSize()
        .add(htmlContainer.getInsets())
        .add(htmlField.getMargins());
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

/**
 * Returns the 'natural' size of the field - which means the current size of the field in the browser.
 * By default we return the size of the $fieldContainer. Override this method when you must return
 * another size (which is required when the field-content is scrollable).
 */
scout.FormFieldLayout.prototype.naturalSize = function(formField) {
  return new scout.Dimension(formField.$fieldContainer.outerWidth(true), formField.$fieldContainer.outerHeight(true));
};
