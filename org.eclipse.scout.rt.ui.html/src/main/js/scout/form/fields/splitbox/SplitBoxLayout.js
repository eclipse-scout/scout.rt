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
scout.SplitBoxLayout = function(splitBox) {
  scout.SplitBoxLayout.parent.call(this);
  this._splitBox = splitBox;
};
scout.inherits(scout.SplitBoxLayout, scout.AbstractLayout);

scout.SplitBoxLayout.prototype.layout = function($container) {
  // Extract components
  var htmlContainer = scout.HtmlComponent.get($container), // = split-area
    $splitter = $container.children('.splitter'),
    $fields = $container.children('.form-field'),
    htmlFirstField = scout.HtmlComponent.optGet($fields.eq(0)),
    htmlSecondField = scout.HtmlComponent.optGet($fields.eq(1)),
    // Calculate available size for split area
    splitXAxis = this._splitBox.splitHorizontal;

  $splitter.removeClass('hidden');

  var firstFieldSize, secondFieldSize, firstFieldBounds, secondFieldBounds,
    splitterSize = scout.graphics.getVisibleSize($splitter, true),
    availableSize = htmlContainer
      .getAvailableSize()
      .subtract(htmlContainer.getInsets()),

    hasFirstField = (htmlFirstField && htmlFirstField.isVisible()),
    hasSecondField = (htmlSecondField && htmlSecondField.isVisible()),
    hasTwoFields = hasFirstField && hasSecondField,
    hasOneField = !hasTwoFields && (hasFirstField || hasSecondField),
    splitterPosition = this._splitBox.splitterPosition;

  // remove splitter size from available with, only when both fields are visible
  // otherwise the splitter is invisible and requires no space.
  var availableSizeForFields = new scout.Dimension(availableSize);
  if (hasTwoFields) {
    if (splitXAxis) { // "|"
      availableSizeForFields.width -= splitterSize.width;
    } else { // "--"
      availableSizeForFields.height -= splitterSize.height;
    }
  }

  // Default case: two fields
  if (hasTwoFields) {
    // Distribute available size to the two fields according to the splitter position ratio
    firstFieldSize = new scout.Dimension(availableSizeForFields);
    secondFieldSize = new scout.Dimension(availableSizeForFields);
    if (splitXAxis) { // "|"
      if (this._splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE) {
        // Relative
        firstFieldSize.width *= splitterPosition;
        secondFieldSize.width *= (1 - splitterPosition);
      } else {
        // Absolute
        splitterPosition = Math.min(splitterPosition, availableSizeForFields.width);
        if (this._splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          firstFieldSize.width = availableSizeForFields.width - splitterPosition;
          secondFieldSize.width = splitterPosition;
        } else {
          firstFieldSize.width = splitterPosition;
          secondFieldSize.width = availableSizeForFields.width - splitterPosition;
        }
      }
    } else { // "--"
      if (this._splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE) {
        // Relative
        firstFieldSize.height *= splitterPosition;
        secondFieldSize.height *= (1 - splitterPosition);
      } else {
        // Absolute
        splitterPosition = Math.min(splitterPosition, availableSizeForFields.height);
        if (this._splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          firstFieldSize.height = availableSizeForFields.height - splitterPosition;
          secondFieldSize.height = splitterPosition;
        } else {
          firstFieldSize.height = splitterPosition;
          secondFieldSize.height = availableSizeForFields.height - splitterPosition;
        }
      }
    }
    firstFieldSize = firstFieldSize.subtract(htmlFirstField.getMargins());
    secondFieldSize = secondFieldSize.subtract(htmlSecondField.getMargins());

    // Calculate and set bounds (splitter and second field have to be moved)
    firstFieldBounds = new scout.Rectangle(0, 0, firstFieldSize.width, firstFieldSize.height);
    secondFieldBounds = new scout.Rectangle(0, 0, secondFieldSize.width, secondFieldSize.height);
    if (splitXAxis) { // "|"
      $splitter.cssLeft(firstFieldBounds.width);
      secondFieldBounds.x = firstFieldBounds.width + splitterSize.width;
    } else { // "--"
      $splitter.cssTop(firstFieldBounds.height);
      secondFieldBounds.y = firstFieldBounds.height + splitterSize.height;
    }
    htmlFirstField.setBounds(firstFieldBounds);
    htmlSecondField.setBounds(secondFieldBounds);
  }
  // Special case: only one field (or none at all)
  else {
    if (hasOneField) {
      var singleField = hasFirstField ? htmlFirstField : htmlSecondField,
        singleFieldSize = availableSize.subtract(singleField.getMargins());
      singleField.setBounds(new scout.Rectangle(0, 0, singleFieldSize.width, singleFieldSize.height));
    }
    $splitter.addClass('hidden');
  }

  // Calculate collapse button position
  if (this._splitBox._collapseHandle) {

    // Horizontal layout:
    // - if 1st field is collapsible -> align button on the right side of the field
    // - if 2nd field is collapsible -> align button on the left side of the field
    var x,
      $collapseHandle = this._splitBox._collapseHandle.$container,
      collapseHandleSize = scout.graphics.getSize($collapseHandle);

    if (this._splitBox.collapsibleField === this._splitBox.firstField) {
      x = hasFirstField ? firstFieldBounds.width - collapseHandleSize.width : 0;
    } else { // secondField
      x = hasSecondField ? availableSize.width - secondFieldBounds.width : availableSize.width - collapseHandleSize.width;
    }

    var collapseHandleLocation = new scout.Point(x, availableSize.height - collapseHandleSize.height);
    scout.graphics.setLocation($collapseHandle, collapseHandleLocation);
  }
};

scout.SplitBoxLayout.prototype.preferredLayoutSize = function($container) {
  // Extract components
  var htmlContainer = scout.HtmlComponent.get($container), // = split-area
    $splitter = $container.children('.splitter'),
    $fields = $container.children('.form-field'),
    htmlFirstField = scout.HtmlComponent.optGet($fields.eq(0)),
    htmlSecondField = scout.HtmlComponent.optGet($fields.eq(1));

  var splitXAxis = this._splitBox.splitHorizontal;
  var splitterSize = scout.graphics.getVisibleSize($splitter, true);

  // Get preferred size of fields
  var firstFieldSize = new scout.Dimension(0, 0);
  if (htmlFirstField) {
    firstFieldSize = htmlFirstField.getPreferredSize()
      .add(htmlFirstField.getMargins());
  }
  var secondFieldSize = new scout.Dimension(0, 0);
  if (htmlSecondField) {
    secondFieldSize = htmlSecondField.getPreferredSize()
      .add(htmlSecondField.getMargins());
  }

  // Calculate prefSize
  var prefSize;
  if (splitXAxis) { // "|"
    prefSize = new scout.Dimension(
      firstFieldSize.width + secondFieldSize.width + splitterSize.width,
      Math.max(firstFieldSize.height, secondFieldSize.height)
    );
  } else { // "--"
    prefSize = new scout.Dimension(
      Math.max(firstFieldSize.width, secondFieldSize.width),
      firstFieldSize.height + secondFieldSize.height + splitterSize.height
    );
  }
  prefSize = prefSize.add(htmlContainer.getInsets());

  return prefSize;
};
