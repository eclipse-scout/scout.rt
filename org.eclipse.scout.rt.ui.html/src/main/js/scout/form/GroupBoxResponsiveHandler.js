/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.GroupBoxResponsiveHandler = function() {
  scout.GroupBoxResponsiveHandler.parent.call(this);

  this._initDefaults();
  this.allowedStates = [scout.ResponsiveManager.ResponsiveState.NORMAL, scout.ResponsiveManager.ResponsiveState.CONDENSED, scout.ResponsiveManager.ResponsiveState.COMPACT];

  // Event handlers
  this._formFieldAddedHandler = this._onFormFieldAdded.bind(this);
  this._compositeFields = [];
  this._htmlPropertyChangeHandler = this._onHtmlEnvironmenPropertyChange.bind(this);
};
scout.inherits(scout.GroupBoxResponsiveHandler, scout.ResponsiveHandler);

scout.GroupBoxResponsiveHandler.TransformationType = {
  LABEL_POSITION_ON_FIELD: 'labelPositionOnField',
  LABEL_POSITION_ON_TOP: 'labelPositionOnTop',
  LABEL_VISIBILITY: 'labelVisibility',
  STATUS_POSITION_ON_TOP: 'statusPositionOnTop',
  STATUS_VISIBILITY: 'statusVisibility',
  VERTICAL_ALIGNMENT: 'verticalAlignment',
  GRID_COLUMN_COUNT: 'gridColumnCount',
  HIDE_PLACE_HOLDER_FIELD: 'hidePlaceHolderField',
  FIELD_SCALABLE: 'fieldScalable'
};

scout.GroupBoxResponsiveHandler.prototype._initDefaults = function() {
  this.compactThreshold = scout.HtmlEnvironment.formColumnWidth;
};

scout.GroupBoxResponsiveHandler.prototype._onHtmlEnvironmenPropertyChange = function() {
  this._initDefaults();
};

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype.init = function(model) {
  scout.GroupBoxResponsiveHandler.parent.prototype.init.call(this, model);

  var transformationType = scout.GroupBoxResponsiveHandler.TransformationType;
  var responsiveState = scout.ResponsiveManager.ResponsiveState;

  this._registerTransformation(transformationType.LABEL_POSITION_ON_FIELD, this._transformLabelPositionOnField);
  this._registerTransformation(transformationType.LABEL_POSITION_ON_TOP, this._transformLabelPositionOnTop);
  this._registerTransformation(transformationType.LABEL_VISIBILITY, this._transformLabelVisibility);
  this._registerTransformation(transformationType.STATUS_POSITION_ON_TOP, this._transformStatusPosition);
  this._registerTransformation(transformationType.STATUS_VISIBILITY, this._transformStatusVisibility);
  this._registerTransformation(transformationType.VERTICAL_ALIGNMENT, this._transformVerticalAlignment);
  this._registerTransformation(transformationType.GRID_COLUMN_COUNT, this._transformGridColumnCount);
  this._registerTransformation(transformationType.HIDE_PLACE_HOLDER_FIELD, this._transformHidePlaceHolderField);
  this._registerTransformation(transformationType.FIELD_SCALABLE, this._transformFieldScalable);

  this._enableTransformation(responsiveState.CONDENSED, transformationType.LABEL_POSITION_ON_TOP);
  this._enableTransformation(responsiveState.CONDENSED, transformationType.LABEL_VISIBILITY);
  this._enableTransformation(responsiveState.CONDENSED, transformationType.VERTICAL_ALIGNMENT);

  this._enableTransformation(responsiveState.COMPACT, transformationType.LABEL_POSITION_ON_TOP);
  this._enableTransformation(responsiveState.COMPACT, transformationType.LABEL_VISIBILITY);
  this._enableTransformation(responsiveState.COMPACT, transformationType.STATUS_POSITION_ON_TOP);
  this._enableTransformation(responsiveState.COMPACT, transformationType.STATUS_VISIBILITY);
  this._enableTransformation(responsiveState.COMPACT, transformationType.VERTICAL_ALIGNMENT);
  this._enableTransformation(responsiveState.COMPACT, transformationType.GRID_COLUMN_COUNT);
  this._enableTransformation(responsiveState.COMPACT, transformationType.HIDE_PLACE_HOLDER_FIELD);
  this._enableTransformation(responsiveState.COMPACT, transformationType.FIELD_SCALABLE);

  this.htmlPropertyChangeHandler = this._onHtmlEnvironmenPropertyChange.bind(this);
  scout.HtmlEnvironment.on('propertyChange', this.htmlPropertyChangeHandler);
  this.widget.one('remove', function() {
    scout.HtmlEnvironment.off('propertyChange', this.htmlPropertyChangeHandler);
  }.bind(this));

  this.widget.visitFields(function(field) {
    if (field instanceof scout.CompositeField) {
      field.on('propertyChange', this._formFieldAddedHandler);
      this._compositeFields.push(field);
    }
  }.bind(this));

  scout.HtmlEnvironment.on('propertyChange', this._htmlPropertyChangeHandler);
};

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype.destroy = function() {
  scout.GroupBoxResponsiveHandler.parent.prototype.destroy.call(this);

  this._compositeFields.forEach(function(compositeField) {
    compositeField.off('propertyChange', this._formFieldAddedHandler);
  }.bind(this));

  scout.HtmlEnvironment.off('propertyChange', this._htmlPropertyChangeHandler);
};

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype.active = function() {
  return this.widget.responsive;
};

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype.getCondensedThreshold = function() {
  if (this.condensedThreshold > 0) {
    return this.condensedThreshold;
  }

  return this.widget.htmlComp.prefSize({
    widthOnly: true
  }).width;
};

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype._transform = function() {
  this.widget.visitFields(this._transformWidget.bind(this));
};

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype._transformWidget = function(widget) {
  // skip group boxes with responsiveness set.
  if (widget !== this.widget && widget instanceof scout.GroupBox && widget.responsive !== null) {
    return scout.TreeVisitResult.SKIP_SUBTREE;
  }

  // skip everything that is not a form field.
  if (!(widget instanceof scout.FormField)) {
    return;
  }

  // suppress a revalidate of the layout tree, since setLabelPosition would trigger it.
  var htmlParent;
  if (widget.htmlComp && widget.rendered) {
    widget.htmlComp.suppressInvalidate = true;
    htmlParent = widget.htmlComp.getParent();
    if (htmlParent) {
      htmlParent.suppressInvalidate = true;
    }
  }

  scout.GroupBoxResponsiveHandler.parent.prototype._transformWidget.call(this, widget);

  if (widget.htmlComp) {
    widget.htmlComp.suppressInvalidate = false;
  }
  if (htmlParent) {
    htmlParent.suppressInvalidate = false;
  }
};

/* --- TRANSFORMATIONS ------------------------------------------------------------- */

/**
 * Label Position -> ON_FIELD
 */
scout.GroupBoxResponsiveHandler.prototype._transformLabelPositionOnField = function(field, apply) {
  if (field.parent instanceof scout.SequenceBox ||
    field instanceof scout.CheckBoxField ||
    field instanceof scout.LabelField) {
    return;
  }

  if (apply) {
    this._storeFieldProperty(field, 'labelPosition', field.labelPosition);
    field.setLabelPosition(scout.FormField.LabelPosition.ON_FIELD);
  } else {
    if (this._hasFieldProperty(field, 'labelPosition')) {
      field.setLabelPosition(this._getFieldProperty(field, 'labelPosition'));
    }
  }
};

/**
 * Label Position -> ON_TOP
 */
scout.GroupBoxResponsiveHandler.prototype._transformLabelPositionOnTop = function(field, apply) {
  if (field.parent instanceof scout.SequenceBox ||
    field instanceof scout.CheckBoxField ||
    field instanceof scout.LabelField ||
    field.labelPosition === scout.FormField.LabelPosition.ON_FIELD) {
    return;
  }

  if (apply) {
    this._storeFieldProperty(field, 'labelPosition', field.labelPosition);
    field.setLabelPosition(scout.FormField.LabelPosition.TOP);
  } else {
    if (this._hasFieldProperty(field, 'labelPosition')) {
      field.setLabelPosition(this._getFieldProperty(field, 'labelPosition'));
    }
  }
};

/**
 * Label visibility
 */
scout.GroupBoxResponsiveHandler.prototype._transformLabelVisibility = function(field, apply) {
  if (!(field instanceof scout.CheckBoxField)) {
    return;
  }

  if (apply) {
    this._storeFieldProperty(field, 'labelVisible', field.labelVisible);
    field.setLabelVisible(false);
  } else {
    if (this._hasFieldProperty(field, 'labelVisible')) {
      field.setLabelVisible(this._getFieldProperty(field, 'labelVisible'));
    }
  }
};

// Scoutjs specific method. This methods will be overridden by GroupBoxAdapter for scout classic case.
scout.GroupBoxResponsiveHandler.prototype.getGridData = function(field) {
  return new scout.GridData(field.gridDataHints);
};

//Scoutjs specific method. This methods will be overridden by GroupBoxAdapter for scout classic case.
scout.GroupBoxResponsiveHandler.prototype.setGridData = function(field, gridData) {
  field.setGridDataHints(gridData);
};

/**
 * Status position
 */
scout.GroupBoxResponsiveHandler.prototype._transformStatusPosition = function(field, apply) {
  if (apply) {
    this._storeFieldProperty(field, 'statusPosition', field.statusPosition);
    field.setStatusPosition(scout.FormField.StatusPosition.TOP);
  } else {
    if (this._hasFieldProperty(field, 'statusPosition')) {
      field.setStatusPosition(this._getFieldProperty(field, 'statusPosition'));
    }
  }
};

/**
 * Status visibility
 */
scout.GroupBoxResponsiveHandler.prototype._transformStatusVisibility = function(field, apply) {
  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  if (apply) {
    this._storeFieldProperty(field, 'statusVisible', field.statusVisible);
    field.setStatusVisible(false);
  } else {
    if (this._hasFieldProperty(field, 'statusVisible')) {
      field.setStatusVisible(this._getFieldProperty(field, 'statusVisible'));
    }
  }
};

/**
 * Vertical alignment
 */
scout.GroupBoxResponsiveHandler.prototype._transformVerticalAlignment = function(field, apply) {
  if (!(field instanceof scout.Button && field.displayStyle === scout.Button.DisplayStyle.DEFAULT ||
      field instanceof scout.CheckBoxField) ||
    !field.gridData) {
    return;
  }

  var gridData = this.getGridData(field);
  if (apply) {
    this._storeFieldProperty(field, 'fillVertical', gridData.fillVertical);
    this._storeFieldProperty(field, 'verticalAlignment', gridData.verticalAlignment);
    gridData.fillVertical = false;
    gridData.verticalAlignment = 1;
  } else {
    if (this._hasFieldProperty(field, 'fillVertical')) {
      gridData.fillVertical = this._getFieldProperty(field, 'fillVertical');
    }
    if (this._hasFieldProperty(field, 'verticalAlignment')) {
      gridData.verticalAlignment = this._getFieldProperty(field, 'verticalAlignment');
    }
  }

  this.setGridData(field, gridData);
};

/**
 * Column count
 */
scout.GroupBoxResponsiveHandler.prototype._transformGridColumnCount = function(field, apply) {
  if (!(field instanceof scout.GroupBox)) {
    return;
  }

  if (apply) {
    this._storeFieldProperty(field, 'gridColumnCount', field.gridColumnCount);
    field.setGridColumnCount(1);
  } else {
    if (this._hasFieldProperty(field, 'gridColumnCount')) {
      field.setGridColumnCount(this._getFieldProperty(field, 'gridColumnCount'));
    }
  }
};

/**
 * Hide placeholder field
 */
scout.GroupBoxResponsiveHandler.prototype._transformHidePlaceHolderField = function(field, apply) {
  if (!(field instanceof scout.PlaceholderField)) {
    return;
  }

  if (apply) {
    this._storeFieldProperty(field, 'visible', field.visible);
    field.setVisible(false);
  } else {
    if (this._hasFieldProperty(field, 'visible')) {
      field.setVisible(this._getFieldProperty(field, 'visible'));
    }
  }
};

/**
 * GroupBox: Makes sure weightX is set to 1 which makes the field scalable.
 *
 * Reason:<br>
 * The width of the field should be adjusted according to the display width, otherwise it may be too big to be
 * displayed. <br>
 * Additionally, since we use a one column layout, setting weightX to 0 might destroy the layout because it affects
 * all the fields in the groupBox.
 */
scout.GroupBoxResponsiveHandler.prototype._transformFieldScalable = function(field, apply) {
  if (field.parent instanceof scout.SequenceBox) {
    return;
  }

  var gridData = this.getGridData(field);
  if (apply && gridData.weightX === 0) {
    this._storeFieldProperty(field, 'weightX', gridData.weightX);
    gridData.weightX = 1;
  } else if (!apply) {
    if (this._hasFieldProperty(field, 'weightX')) {
      gridData.weightX = this._getFieldProperty(field, 'weightX');
    }
  }

  this.setGridData(field, gridData);
};

/* --- HANDLERS ------------------------------------------------------------- */

scout.GroupBoxResponsiveHandler.prototype._onFormFieldAdded = function(event) {
  if (this.state !== scout.ResponsiveManager.ResponsiveState.NORMAL && (event.propertyName === 'fields' || event.propertyName === 'tabItems')) {
    var newFields = scout.arrays.diff(event.newValue, event.oldValue);
    newFields.forEach(function(field) {
      field.visitFields(this._transformWidget.bind(this));
    }.bind(this));
  }
};
