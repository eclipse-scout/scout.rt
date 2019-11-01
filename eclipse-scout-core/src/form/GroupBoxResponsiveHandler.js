/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ResponsiveManager} from '../index';
import {GridData} from '../index';
import {HtmlEnvironment} from '../index';
import {CompositeField} from '../index';
import {TreeVisitResult} from '../index';
import {Button} from '../index';
import {ResponsiveHandler} from '../index';
import {LabelField} from '../index';
import {FormField} from '../index';
import {PlaceholderField} from '../index';
import {SequenceBox} from '../index';
import {GroupBox} from '../index';
import {CheckBoxField} from '../index';
import {arrays} from '../index';

export default class GroupBoxResponsiveHandler extends ResponsiveHandler {

constructor() {
  super();

  this._initDefaults();
  this.allowedStates = [ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.CONDENSED, ResponsiveManager.ResponsiveState.COMPACT];

  // Event handlers
  this._formFieldAddedHandler = this._onFormFieldAdded.bind(this);
  this._compositeFields = [];
  this._htmlPropertyChangeHandler = this._onHtmlEnvironmenPropertyChange.bind(this);
}


static TransformationType = {
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

_initDefaults() {
  this.compactThreshold = HtmlEnvironment.get().formColumnWidth;
}

_onHtmlEnvironmenPropertyChange() {
  this._initDefaults();
}

/**
 * @Override
 */
init(model) {
  super.init( model);

  var transformationType = GroupBoxResponsiveHandler.TransformationType;
  var responsiveState = ResponsiveManager.ResponsiveState;

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
  HtmlEnvironment.get().on('propertyChange', this.htmlPropertyChangeHandler);
  this.widget.one('remove', function() {
    HtmlEnvironment.get().off('propertyChange', this.htmlPropertyChangeHandler);
  }.bind(this));

  this.widget.visitFields(function(field) {
    if (field instanceof CompositeField) {
      field.on('propertyChange', this._formFieldAddedHandler);
      this._compositeFields.push(field);
    }
  }.bind(this));

  HtmlEnvironment.get().on('propertyChange', this._htmlPropertyChangeHandler);
}

/**
 * @Override
 */
destroy() {
  super.destroy();

  this._compositeFields.forEach(function(compositeField) {
    compositeField.off('propertyChange', this._formFieldAddedHandler);
  }.bind(this));

  HtmlEnvironment.get().off('propertyChange', this._htmlPropertyChangeHandler);
}

/**
 * @Override
 */
active() {
  return this.widget.responsive;
}

/**
 * @Override
 */
getCondensedThreshold() {
  if (this.condensedThreshold > 0) {
    return this.condensedThreshold;
  }

  return this.widget.htmlComp.prefSize({
    widthOnly: true
  }).width;
}

/**
 * @Override
 */
_transform() {
  this.widget.visitFields(this._transformWidget.bind(this));
}

/**
 * @Override
 */
_transformWidget(widget) {
  // skip group boxes with responsiveness set.
  if (widget !== this.widget && widget instanceof GroupBox && widget.responsive !== null) {
    return TreeVisitResult.SKIP_SUBTREE;
  }

  // skip everything that is not a form field.
  if (!(widget instanceof FormField)) {
    return;
  }

  // suppress a revalidate of the layout tree, since setLabelPosition would trigger it.
  var htmlParent;
  if (widget.htmlComp) {
    widget.htmlComp.suppressInvalidate = true;
    htmlParent = widget.htmlComp.getParent();
    if (htmlParent) {
      htmlParent.suppressInvalidate = true;
    }
  }

  super._transformWidget( widget);

  if (widget.htmlComp) {
    widget.htmlComp.suppressInvalidate = false;
  }
  if (htmlParent) {
    htmlParent.suppressInvalidate = false;
  }
}

/* --- TRANSFORMATIONS ------------------------------------------------------------- */

/**
 * Label Position -> ON_FIELD
 */
_transformLabelPositionOnField(field, apply) {
  if (field.parent instanceof SequenceBox ||
    field instanceof CheckBoxField ||
    field instanceof LabelField) {
    return;
  }

  if (apply) {
    this._storeFieldProperty(field, 'labelPosition', field.labelPosition);
    field.setLabelPosition(FormField.LabelPosition.ON_FIELD);
  } else {
    if (this._hasFieldProperty(field, 'labelPosition')) {
      field.setLabelPosition(this._getFieldProperty(field, 'labelPosition'));
    }
  }
}

/**
 * Label Position -> ON_TOP
 */
_transformLabelPositionOnTop(field, apply) {
  if (field.parent instanceof SequenceBox ||
    field instanceof CheckBoxField ||
    field instanceof LabelField ||
    field.labelPosition === FormField.LabelPosition.ON_FIELD) {
    return;
  }

  if (apply) {
    this._storeFieldProperty(field, 'labelPosition', field.labelPosition);
    field.setLabelPosition(FormField.LabelPosition.TOP);
  } else {
    if (this._hasFieldProperty(field, 'labelPosition')) {
      field.setLabelPosition(this._getFieldProperty(field, 'labelPosition'));
    }
  }
}

/**
 * Label visibility
 */
_transformLabelVisibility(field, apply) {
  if (!(field instanceof CheckBoxField)) {
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
}

// Scoutjs specific method. This methods will be overridden by GroupBoxAdapter for scout classic case.
getGridData(field) {
  return new GridData(field.gridDataHints);
}

//Scoutjs specific method. This methods will be overridden by GroupBoxAdapter for scout classic case.
setGridData(field, gridData) {
  field.setGridDataHints(gridData);
}

/**
 * Status position
 */
_transformStatusPosition(field, apply) {
  if (apply) {
    this._storeFieldProperty(field, 'statusPosition', field.statusPosition);
    field.setStatusPosition(FormField.StatusPosition.TOP);
  } else {
    if (this._hasFieldProperty(field, 'statusPosition')) {
      field.setStatusPosition(this._getFieldProperty(field, 'statusPosition'));
    }
  }
}

/**
 * Status visibility
 */
_transformStatusVisibility(field, apply) {
  var ResponsiveState = ResponsiveManager.ResponsiveState;

  if (apply) {
    this._storeFieldProperty(field, 'statusVisible', field.statusVisible);
    field.setStatusVisible(false);
  } else {
    if (this._hasFieldProperty(field, 'statusVisible')) {
      field.setStatusVisible(this._getFieldProperty(field, 'statusVisible'));
    }
  }
}

/**
 * Vertical alignment
 */
_transformVerticalAlignment(field, apply) {
  if (!(field instanceof Button && field.displayStyle === Button.DisplayStyle.DEFAULT ||
      field instanceof CheckBoxField) ||
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
}

/**
 * Column count
 */
_transformGridColumnCount(field, apply) {
  if (!(field instanceof GroupBox)) {
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
}

/**
 * Hide placeholder field
 */
_transformHidePlaceHolderField(field, apply) {
  if (!(field instanceof PlaceholderField)) {
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
}

/**
 * GroupBox: Makes sure weightX is set to 1 which makes the field scalable.
 *
 * Reason:<br>
 * The width of the field should be adjusted according to the display width, otherwise it may be too big to be
 * displayed. <br>
 * Additionally, since we use a one column layout, setting weightX to 0 might destroy the layout because it affects
 * all the fields in the groupBox.
 */
_transformFieldScalable(field, apply) {
  if (field.parent instanceof SequenceBox) {
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
}

/* --- HANDLERS ------------------------------------------------------------- */

_onFormFieldAdded(event) {
  if (this.state !== ResponsiveManager.ResponsiveState.NORMAL && (event.propertyName === 'fields' || event.propertyName === 'tabItems')) {
    var newFields = arrays.diff(event.newValue, event.oldValue);
    newFields.forEach(function(field) {
      field.visitFields(this._transformWidget.bind(this));
    }.bind(this));
  }
}
}
