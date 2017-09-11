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
scout.DateTimeCompositeLayout = function(dateField) {
  scout.DateTimeCompositeLayout.parent.call(this);
  this._dateField = dateField;

  // Minimum field with to normal state, for smaller widths the "compact" style is applied.
  this.MIN_DATE_FIELD_WIDTH = 90;
  this.MIN_TIME_FIELD_WIDTH = 60;
};
scout.inherits(scout.DateTimeCompositeLayout, scout.AbstractLayout);

scout.DateTimeCompositeLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    $dateField = this._dateField.$dateField,
    $timeField = this._dateField.$timeField,
    $dateFieldIcon = this._dateField.$dateFieldIcon,
    $timeFieldIcon = this._dateField.$timeFieldIcon,
    $dateClearIcon = this._dateField.$dateClearIcon,
    $timeClearIcon = this._dateField.$timeClearIcon,
    $predictDateField = this._dateField._$predictDateField,
    $predictTimeField = this._dateField._$predictTimeField,
    htmlDateField = ($dateField ? scout.HtmlComponent.get($dateField) : null),
    htmlTimeField = ($timeField ? scout.HtmlComponent.get($timeField) : null),
    hasDate = ($dateField ? !$dateField.isDisplayNone() : false),
    hasTime = ($timeField ? !$timeField.isDisplayNone() : false);

  var availableSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets());

  var dateFieldSize, timeFieldSize;
  // --- Date and time ---
  if (hasDate && hasTime) {
    // Field size
    var dateFieldMargins = htmlDateField.margins();
    var timeFieldMargins = htmlTimeField.margins();
    var compositeMargins = new scout.Insets(
      Math.max(dateFieldMargins.top, timeFieldMargins.top),
      Math.max(dateFieldMargins.right, timeFieldMargins.right),
      Math.max(dateFieldMargins.bottom, timeFieldMargins.bottom),
      Math.max(dateFieldMargins.left, timeFieldMargins.left)
    );
    var compositeSize = availableSize.subtract(compositeMargins);
    var hgap = this._hgap();
    var totalWidth = compositeSize.width - hgap;
    // Date field 60%, time field 40%
    var dateFieldWidth = (totalWidth * 0.6);
    var timeFieldWidth = (totalWidth - dateFieldWidth);

    dateFieldSize = new scout.Dimension(dateFieldWidth, compositeSize.height);
    timeFieldSize = new scout.Dimension(timeFieldWidth, compositeSize.height);
    htmlDateField.setSize(dateFieldSize);
    htmlTimeField.setSize(timeFieldSize);
    $timeField.cssRight(0);

    // Icons
    $dateFieldIcon.cssTop($dateField.cssBorderTopWidth())
      .cssRight(timeFieldWidth + hgap)
      .cssHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth())
      .cssLineHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth());

    $dateClearIcon.cssTop($dateField.cssBorderTopWidth())
      .cssRight(timeFieldWidth + hgap)
      .cssHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth())
      .cssLineHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth());
    $timeFieldIcon.cssTop($timeField.cssBorderTopWidth())
      .cssRight(0)
      .cssHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth())
      .cssLineHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth());
    $timeClearIcon.cssTop($timeField.cssBorderTopWidth())
      .cssRight(0)
      .cssHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth())
      .cssLineHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth());

    // Compact style
    $dateField.toggleClass('compact', dateFieldSize.width < this.MIN_DATE_FIELD_WIDTH);
    $timeField.toggleClass('compact', timeFieldSize.width < this.MIN_TIME_FIELD_WIDTH);

    // Prediction
    if ($predictDateField) {
      scout.graphics.setSize($predictDateField, dateFieldSize);
    }
    if ($predictTimeField) {
      scout.graphics.setSize($predictTimeField, timeFieldSize);
      $predictTimeField.cssRight(0);
    }
  }
  // --- Date only ---
  else if (hasDate) {
    // Field size
    dateFieldSize = availableSize.subtract(htmlDateField.margins());
    htmlDateField.setSize(dateFieldSize);

    // Icons
    $dateFieldIcon.cssTop($dateField.cssBorderTopWidth())
      .cssRight(0)
      .cssHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth())
      .cssLineHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth());

    $dateClearIcon.cssTop($dateField.cssBorderTopWidth())
      .cssRight(0)
      .cssHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth())
      .cssLineHeight(dateFieldSize.height - $dateField.cssBorderTopWidth() - $dateField.cssBorderBottomWidth());

    // Compact style
    $dateField.toggleClass('compact', dateFieldSize.width < this.MIN_DATE_FIELD_WIDTH);

    // Prediction
    if ($predictDateField) {
      scout.graphics.setSize($predictDateField, dateFieldSize);
    }
  }
  // --- Time only ---
  else if (hasTime) {
    // Field size
    timeFieldSize = availableSize.subtract(htmlTimeField.margins());
    htmlTimeField.setSize(timeFieldSize);

    // Icons
    $timeFieldIcon.cssTop($timeField.cssBorderTopWidth())
      .cssRight(0)
      .cssHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth())
      .cssLineHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth());
    $timeClearIcon.cssTop($timeField.cssBorderTopWidth())
      .cssRight(0)
      .cssHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth())
      .cssLineHeight(timeFieldSize.height - $timeField.cssBorderTopWidth() - $timeField.cssBorderBottomWidth());

    // Compact style
    $timeField.toggleClass('compact', timeFieldSize.width < this.MIN_TIME_FIELD_WIDTH);

    // Prediction
    if ($predictTimeField) {
      scout.graphics.setSize($predictTimeField, timeFieldSize);
    }
  }
  var popup = this._dateField.popup;
  if (popup && popup.rendered) {
    // Make sure the popup is correctly positioned (especially necessary for cell editor)
    popup.position();
  }
};

scout.DateTimeCompositeLayout.prototype._hgap = function() {
  if (this._dateField.cellEditor) {
    return 0;
  }
  return scout.HtmlEnvironment.smallColumnGap;
};

scout.DateTimeCompositeLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize = scout.graphics.prefSize($container),
    $dateField = this._dateField.$dateField,
    $timeField = this._dateField.$timeField,
    hasDate = ($dateField ? !$dateField.isDisplayNone() : false),
    hasTime = ($timeField ? !$timeField.isDisplayNone() : false);

  // --- Date and time ---
  if (hasDate && hasTime) {
    prefSize.width = this.MIN_DATE_FIELD_WIDTH + this._hgap() + this.MIN_TIME_FIELD_WIDTH;
  }

  // --- Date only ---
  else if (hasDate) {
    prefSize.width = this.MIN_DATE_FIELD_WIDTH;
  }
  // --- Time only ---
  else if (hasTime) {
    prefSize.width = this.MIN_TIME_FIELD_WIDTH;
  }
  return prefSize;
};
