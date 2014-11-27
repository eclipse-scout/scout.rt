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
};
scout.inherits(scout.FormFieldLayout, scout.AbstractLayout);


scout.FormFieldLayout.prototype.layout = function($container) {
  var containerSize, fieldSize, fieldBounds, htmlField,
    htmlContainer = scout.HtmlComponent.get($container),
    f = this.formField,
    leftWidth = 0,
    rightWidth = 0;

  containerSize = htmlContainer.getSize().
    subtract(htmlContainer.getInsets());

  if (f.$label && f.labelVisible) {
    scout.graphics.setBounds(f.$label, 0, 0, this.labelWidth, containerSize.height);
    // with this property we achieve "vertical-align:middle" which doesn't work for non-table-cell elements
    f.$label.css('line-height', containerSize.height + 'px');
    leftWidth += this.labelWidth;
  }
  if (f.$mandatory) {
    f.$mandatory.cssLeft(leftWidth);
    leftWidth += f.$mandatory.outerWidth(true);
  }
  if (f.$status && f.statusVisible) {
    // can not check for $status.isVisible() since we want to reserve
    // space used for status even when $status is invisible.
    f.$status.css('line-height', containerSize.height + 'px');
    rightWidth += f.$status.outerWidth(true);
  }

  fieldSize = containerSize.subtract(scout.graphics.getMargins(f.$fieldContainer));
  fieldBounds = new scout.Rectangle(leftWidth, 0, fieldSize.width - leftWidth - rightWidth, fieldSize.height);
  htmlField = scout.HtmlComponent.optGet(f.$fieldContainer);
  if (htmlField) {
    htmlField.setBounds(fieldBounds);
  } else {
    scout.graphics.setBounds(f.$fieldContainer, fieldBounds);
  }

  // Icon is placed inside the field (as overlay)
  if (f.$icon) {
    f.$icon.cssRight(f.$field.cssBorderRightWidth() + rightWidth);
  }

  // Make sure tooltip is at correct position after layouting, if there is one
  if (f.tooltip && f.tooltip.rendered) {
    f.tooltip.position();
  }
};

scout.FormFieldLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize, htmlField,
    width = 0,
    htmlContainer = scout.HtmlComponent.get($container),
    height = scout.HtmlEnvironment.formRowHeight,
    f = this.formField;

  if (f.$label && f.labelVisible) {
    width += this.labelWidth;
  }
  if (f.$mandatory) {
    width += f.$mandatory.outerWidth(true);
  }
  if (f.$status && f.statusVisible) {
    width += f.$status.outerWidth(true);
  }

  htmlField = scout.HtmlComponent.optGet(f.$fieldContainer);
  if (htmlField) {
    prefSize = htmlField.getPreferredSize()
      .add(htmlContainer.getInsets())
      .add(htmlField.getMargins());
  } else {
    prefSize = new scout.Dimension(f.$fieldContainer.width(), f.$fieldContainer.height());
  }
  width += prefSize.width;
  height = Math.max(height, prefSize.height);

  return new scout.Dimension(width, height);
};
