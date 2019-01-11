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
scout.GroupBoxResponsiveHandler = function(groupBox, options) {
  scout.GroupBoxResponsiveHandler.parent.call(this, groupBox, options);
  options = options || {};

  // default values.
  this.compactThreshold = scout.nvl(options.compactThreshold, scout.HtmlEnvironment.formColumnWidth);
  this.allowedStates = [scout.ResponsiveManager.ResponsiveState.NORMAL, scout.ResponsiveManager.ResponsiveState.CONDENSED, scout.ResponsiveManager.ResponsiveState.COMPACT];

  // transform handlers
  this.transformHandlers = [
    this._transformLabelPosition.bind(this),
    this._transformLabelVisibility.bind(this),
    this._transformStatusPosition.bind(this),
    this._transformStatusVisibility.bind(this),
    this._transformVerticalAlignment.bind(this),
    this._transformGridColumnCount.bind(this),
    this._transformHidePlaceHolderField.bind(this),
    this._transformFieldScalable.bind(this)
  ];

  // Event handlers
  this._formFieldAddedHandler = this._onFormFieldAdded.bind(this);
  this._compositeFields = [];
};
scout.inherits(scout.GroupBoxResponsiveHandler, scout.ResponsiveHandler);

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype.init = function() {
  scout.GroupBoxResponsiveHandler.parent.prototype.init.call(this);

  this.widget.visitFields(function(field) {
    if (field instanceof scout.CompositeField) {
      field.on('propertyChange', this._formFieldAddedHandler);
      this._compositeFields.push(field);
    }
  }.bind(this));
};

/**
 * @Override
 */
scout.GroupBoxResponsiveHandler.prototype.destroy = function() {
  scout.GroupBoxResponsiveHandler.parent.prototype.destroy.call(this);

  this._compositeFields.forEach(function(compositeField) {
    compositeField.off('propertyChange', this._formFieldAddedHandler);
  }.bind(this));
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

  var htmlContainer = this.widget.htmlComp;
  var containerSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets());

  return this.widget.htmlComp.prefSize({
    widthHint: containerSize.width,
    widthOnly: true,
    skipResponsive: this.calculatingPrefSize
  }).width;
};

scout.GroupBoxResponsiveHandler.prototype._transform = function() {
  this.widget.visitFields(this._transformField.bind(this));
};

scout.GroupBoxResponsiveHandler.prototype._transformField = function(field) {
  // skip group boxes with responsiveness set.
  if (field !== this.widget && field instanceof scout.GroupBox && field.responsive !== null) {
    return scout.TreeVisitResult.SKIP_SUBTREE;
  }

  // skip everything that is not a form field.
  if (!(field instanceof scout.FormField)) {
    return;
  }

  // suppress a revalidate of the layout tree, since setLabelPosition would trigger it.
  if (field.htmlComp) {
    field.htmlComp.suppressInvalidate = true;
  }

  this.transformHandlers.forEach(function(transformHandler) {
    transformHandler(field);
  }.bind(this));

  if (field.htmlComp) {
    field.htmlComp.suppressInvalidate = false;
  }
};

/* --- TRANSFORMATIONS ------------------------------------------------------------- */

/**
 * Label Position
 */
scout.GroupBoxResponsiveHandler.prototype._transformLabelPosition = function(field) {
  if (field.parent instanceof scout.SequenceBox ||
    field instanceof scout.CheckBoxField ||
    field instanceof scout.LabelField ||
    field.labelPosition === scout.FormField.LabelPosition.ON_FIELD) {
    return;
  }

  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  if (this._fromNormalToOtherState()) {
    this._storeFieldProperty(field, 'labelPosition', field.labelPosition);
    field.setLabelPosition(scout.FormField.LabelPosition.TOP);
  } else if (this.state === ResponsiveState.NORMAL) {
    if (this._hasFieldProperty(field, 'labelPosition')) {
      field.setLabelPosition(this._getFieldProperty(field, 'labelPosition'));
    }
  }
};

/**
 * Label visibility
 */
scout.GroupBoxResponsiveHandler.prototype._transformLabelVisibility = function(field) {
  if (!(field instanceof scout.CheckBoxField)) {
    return;
  }

  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  if (this._fromNormalToOtherState()) {
    this._storeFieldProperty(field, 'labelVisible', field.labelVisible);
    field.setLabelVisible(false);
  } else if (this.state === ResponsiveState.NORMAL) {
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
scout.GroupBoxResponsiveHandler.prototype._transformStatusPosition = function(field) {
  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  if (this.state === ResponsiveState.COMPACT) {
    this._storeFieldProperty(field, 'statusPosition', field.statusPosition);
    field.setStatusPosition(scout.FormField.StatusPosition.TOP);
  } else if (this.state === ResponsiveState.CONDENSED || this.state === ResponsiveState.NORMAL) {
    if (this._hasFieldProperty(field, 'statusPosition')) {
      field.setStatusPosition(this._getFieldProperty(field, 'statusPosition'));
    }
  }
};

/**
 * Status visibility
 */
scout.GroupBoxResponsiveHandler.prototype._transformStatusVisibility = function(field) {
  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  if (this.state === ResponsiveState.COMPACT) {
    this._storeFieldProperty(field, 'statusVisible', field.statusVisible);
    field.setStatusVisible(false);
  } else if (this.state === ResponsiveState.CONDENSED || this.state === ResponsiveState.NORMAL) {
    if (this._hasFieldProperty(field, 'statusVisible')) {
      field.setStatusVisible(this._getFieldProperty(field, 'statusVisible'));
    }
  }
};

/**
 * Vertical alignment
 */
scout.GroupBoxResponsiveHandler.prototype._transformVerticalAlignment = function(field) {
  if (!(field instanceof scout.Button && field.displayStyle === scout.Button.DisplayStyle.DEFAULT ||
      field instanceof scout.CheckBoxField) ||
      !field.gridData) {
    return;
  }

  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  var gridData = this.getGridData(field);
  if (this._fromNormalToOtherState()) {
    this._storeFieldProperty(field, 'fillVertical', gridData.fillVertical);
    this._storeFieldProperty(field, 'verticalAlignment', gridData.verticalAlignment);
    gridData.fillVertical = false;
    gridData.verticalAlignment = 1;
  } else if (this.state === ResponsiveState.NORMAL) {
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
scout.GroupBoxResponsiveHandler.prototype._transformGridColumnCount = function(field) {
  if (!(field instanceof scout.GroupBox)) {
    return;
  }

  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  if (this.state === ResponsiveState.COMPACT) {
    this._storeFieldProperty(field, 'gridColumnCount', field.gridColumnCount);
    field.setGridColumnCount(1);
  } else if (this.state === ResponsiveState.CONDENSED || this.state === ResponsiveState.NORMAL) {
    if (this._hasFieldProperty(field, 'gridColumnCount')) {
      field.setGridColumnCount(this._getFieldProperty(field, 'gridColumnCount'));
    }
  }
};

/**
 * Hide placeholder field
 */
scout.GroupBoxResponsiveHandler.prototype._transformHidePlaceHolderField = function(field) {
  if (!(field instanceof scout.PlaceholderField)) {
    return;
  }

  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;

  if (this.state === ResponsiveState.COMPACT) {
    this._storeFieldProperty(field, 'visible', field.visible);
    field.setVisible(false);
  } else if (this.state === ResponsiveState.CONDENSED || this.state === ResponsiveState.NORMAL) {
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
scout.GroupBoxResponsiveHandler.prototype._transformFieldScalable = function(field) {
  if (field.parent instanceof scout.SequenceBox) {
    return;
  }

  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;
  var gridData = this.getGridData(field);
  if (this.state === ResponsiveState.COMPACT && gridData.weightX === 0) {
    this._storeFieldProperty(field, 'weightX', gridData.weightX);
    gridData.weightX = 1;
  } else if (this.state === ResponsiveState.CONDENSED || this.state === ResponsiveState.NORMAL) {
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
      field.visitFields(this._transformField.bind(this));
    }.bind(this));
  }
};
