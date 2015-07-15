/**
 * DateFieldLayout
 */
scout.DateFieldLayout = function(dateField) {
  scout.DateFieldLayout.parent.call(this, dateField);
  this.dateField = dateField;
};
scout.inherits(scout.DateFieldLayout, scout.FormFieldLayout);

scout.DateFieldLayout.prototype.layout = function($container) {
  var containerSize, fieldSize, fieldBounds, fieldContainerBounds, htmlField, labelPositionLeft, labelHasFieldWidth,
    htmlContainer = scout.HtmlComponent.get($container),
    formField = this.formField,
    labelWidth = this.labelWidth,
    statusWidth = this.statusWidth,
    left = 0,
    right = 0,
    top = 0;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (this._isLabelVisible()) {
    // currently a gui only flag, necessary for sequencebox
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
  if (this._isStatusVisible()) {
    formField.$status
      .cssTop(top)
      .cssWidth(statusWidth)
      .cssHeight(this.rowHeight)
      .cssLineHeight(this.rowHeight);
    right += statusWidth + formField.$status.cssMarginX();
  }

  // Make sure tooltip is at correct position after layouting, if there is one
  if (formField.tooltip && formField.tooltip.rendered) {
    formField.tooltip.position();
  }

  if (formField.$fieldContainer) {
    fieldSize = containerSize.subtract(scout.graphics.getMargins(formField.$fieldContainer));
    fieldContainerBounds = new scout.Rectangle(left, top, fieldSize.width - left - right, fieldSize.height - top);
    fieldBounds = new scout.Rectangle(0, top, fieldSize.width - left - right, fieldSize.height - top);
    htmlField = scout.HtmlComponent.optGet(formField.$fieldContainer);
    var dateFieldBounds = fieldBounds;
    var timeFieldBounds = fieldBounds;
    if (this.dateField.$dateField && this.dateField.$timeField) {
      dateFieldBounds = new scout.Rectangle(0, top, Math.floor((fieldSize.width - left - right) / 3 * 2) - 1, fieldSize.height - top);
      timeFieldBounds = new scout.Rectangle(dateFieldBounds.width + 1, top, Math.floor((fieldSize.width - left - right) / 3) - 1, fieldSize.height - top);
      scout.graphics.setBounds(this.dateField.$dateField, dateFieldBounds);
      scout.graphics.setBounds(this.dateField.$timeField, timeFieldBounds);

    } else if (this.dateField.$dateField) {
      scout.graphics.setBounds(this.dateField.$dateField, dateFieldBounds);
    } else if (this.dateField.$timeField) {
      scout.graphics.setBounds(this.dateField.$timeField, timeFieldBounds);
    }

    //Icon is placed inside the datefield (as overlay)
    if (formField.$icon && formField.$dateField) {
      formField.$icon
        .cssRight(fieldBounds.width - dateFieldBounds.width)
        .cssTop(top);
    }
    if (formField.$timeFieldIcon && formField.$timeField) {
      formField.$timeFieldIcon
        .cssRight(0)
        .cssTop(top);
    }

    if (htmlField) {
      htmlField.setBounds(fieldContainerBounds);
    } else {
      scout.graphics.setBounds(formField.$fieldContainer, fieldContainerBounds);
    }

    if (labelHasFieldWidth) {
      formField.$label.cssWidth(fieldContainerBounds.width);
    }
  }

};
