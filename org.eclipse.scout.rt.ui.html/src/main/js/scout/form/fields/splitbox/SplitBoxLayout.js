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
  this.splitBox = splitBox;
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
    splitXAxis = this.splitBox.splitHorizontal;

  $splitter.removeClass('hidden');

  var firstFieldSize, secondFieldSize, firstFieldBounds, secondFieldBounds,
    splitterSize = scout.graphics.size($splitter, true),
    availableSize = htmlContainer.availableSize().subtract(htmlContainer.insets()),
    hasFirstField = (htmlFirstField && htmlFirstField.isVisible()),
    hasSecondField = (htmlSecondField && htmlSecondField.isVisible()),
    hasTwoFields = hasFirstField && hasSecondField,
    hasOneField = !hasTwoFields && (hasFirstField || hasSecondField),
    splitterPosition = this.splitBox.getEffectiveSplitterPosition();

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
      if (this.splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) {
        // Relative first
        firstFieldSize.width = Math.floor(firstFieldSize.width * splitterPosition);
        secondFieldSize.width -= firstFieldSize.width;
      } else if (this.splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) {
        // Relative second
        secondFieldSize.width = Math.floor(secondFieldSize.width * splitterPosition);
        firstFieldSize.width -= secondFieldSize.width;
      } else {
        // Absolute
        splitterPosition = Math.min(splitterPosition, availableSizeForFields.width);
        if (this.splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          firstFieldSize.width = availableSizeForFields.width - splitterPosition;
          secondFieldSize.width = splitterPosition;
        } else {
          firstFieldSize.width = splitterPosition;
          secondFieldSize.width = availableSizeForFields.width - splitterPosition;
        }
      }
    } else { // "--"
      if (this.splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) {
        // Relative first
        firstFieldSize.height = Math.floor(firstFieldSize.height * splitterPosition);
        secondFieldSize.height -= firstFieldSize.height;
      } else if (this.splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) {
        // Relative second
        secondFieldSize.height = Math.floor(secondFieldSize.height * splitterPosition);
        firstFieldSize.height -= secondFieldSize.height;
      } else {
        // Absolute
        splitterPosition = Math.min(splitterPosition, availableSizeForFields.height);
        if (this.splitBox.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          firstFieldSize.height = availableSizeForFields.height - splitterPosition;
          secondFieldSize.height = splitterPosition;
        } else {
          firstFieldSize.height = splitterPosition;
          secondFieldSize.height = availableSizeForFields.height - splitterPosition;
        }
      }
    }
    firstFieldSize = firstFieldSize.subtract(htmlFirstField.margins());
    secondFieldSize = secondFieldSize.subtract(htmlSecondField.margins());

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
        singleFieldSize = availableSize.subtract(singleField.margins());
      singleField.setBounds(new scout.Rectangle(0, 0, singleFieldSize.width, singleFieldSize.height));
    }
    $splitter.addClass('hidden');
  }

  // Calculate collapse button position
  if (this.splitBox._collapseHandle) {
    var $collapseHandle = this.splitBox._collapseHandle.$container;

    // Show collapse handle, if split box has two fields which are visible (one field may be collapsed)
    var collapseHandleVisible = this.splitBox.firstField && this.splitBox.firstField.visible && this.splitBox.secondField && this.splitBox.secondField.visible;
    $collapseHandle.toggleClass('hidden', !collapseHandleVisible);

    var x = null;
    if (hasTwoFields) {
      //- if 1st field is collapsible -> align button on the right side of the field (there is not enough space on the left side)
      //- if 2nd field is collapsible -> button is always aligned on the right side using CSS
      if (this.splitBox.collapsibleField === this.splitBox.firstField) {
        var collapseHandleSize = scout.graphics.size($collapseHandle);
        x = firstFieldBounds.width - collapseHandleSize.width;
      }
    }
    $collapseHandle.cssLeft(x);
  }
};

scout.SplitBoxLayout.prototype.preferredLayoutSize = function($container) {
  // Extract components
  var htmlContainer = scout.HtmlComponent.get($container), // = split-area
    $splitter = $container.children('.splitter'),
    $fields = $container.children('.form-field'),
    htmlFirstField = scout.HtmlComponent.optGet($fields.eq(0)),
    htmlSecondField = scout.HtmlComponent.optGet($fields.eq(1));

  var splitXAxis = this.splitBox.splitHorizontal;
  var splitterSize = scout.graphics.size($splitter, true);

  // Get preferred size of fields
  var firstFieldSize = new scout.Dimension(0, 0);
  if (htmlFirstField) {
    firstFieldSize = htmlFirstField.prefSize()
      .add(htmlFirstField.margins());
  }
  var secondFieldSize = new scout.Dimension(0, 0);
  if (htmlSecondField) {
    secondFieldSize = htmlSecondField.prefSize()
      .add(htmlSecondField.margins());
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
  prefSize = prefSize.add(htmlContainer.insets());

  return prefSize;
};
