/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, Button, CheckBoxField, CompositeField, EventHandler, FormField, GridData, GroupBox, HtmlComponent, HtmlEnvironment, InitModelOf, LabelField, PlaceholderField, PropertyChangeEvent, RadioButtonGroup, ResponsiveHandler,
  ResponsiveManager, SequenceBox, TreeVisitResult
} from '../index';

export class GroupBoxResponsiveHandler extends ResponsiveHandler {
  declare widget: GroupBox;

  protected _compositeFields: CompositeField[];
  protected _formFieldAddedHandler: EventHandler<PropertyChangeEvent<any, CompositeField>>;
  protected _htmlPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, HtmlEnvironment>>;

  constructor() {
    super();

    this._initDefaults();
    this.allowedStates = [ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.CONDENSED, ResponsiveManager.ResponsiveState.COMPACT];
    this._formFieldAddedHandler = this._onFormFieldAdded.bind(this);
    this._compositeFields = [];
    this._htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
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
    FIELD_SCALABLE: 'fieldScalable',
    RADIO_BUTTON_GROUP_USE_UI_HEIGHT: 'radioButtonGroupUseUiHeight'
  } as const;

  protected _initDefaults() {
    this.compactThreshold = HtmlEnvironment.get().formColumnWidth;
  }

  protected _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
  }

  override init(model: InitModelOf<this>) {
    super.init(model);

    let transformationType = GroupBoxResponsiveHandler.TransformationType;
    let responsiveState = ResponsiveManager.ResponsiveState;

    this._registerTransformation(transformationType.LABEL_POSITION_ON_FIELD, this._transformLabelPositionOnField);
    this._registerTransformation(transformationType.LABEL_POSITION_ON_TOP, this._transformLabelPositionOnTop);
    this._registerTransformation(transformationType.LABEL_VISIBILITY, this._transformLabelVisibility);
    this._registerTransformation(transformationType.STATUS_POSITION_ON_TOP, this._transformStatusPosition);
    this._registerTransformation(transformationType.STATUS_VISIBILITY, this._transformStatusVisibility);
    this._registerTransformation(transformationType.VERTICAL_ALIGNMENT, this._transformVerticalAlignment);
    this._registerTransformation(transformationType.GRID_COLUMN_COUNT, this._transformGridColumnCount);
    this._registerTransformation(transformationType.HIDE_PLACE_HOLDER_FIELD, this._transformHidePlaceHolderField);
    this._registerTransformation(transformationType.FIELD_SCALABLE, this._transformFieldScalable);
    this._registerTransformation(transformationType.RADIO_BUTTON_GROUP_USE_UI_HEIGHT, this._transformRadioButtonGroupUseUiHeight);

    this._enableTransformation(responsiveState.CONDENSED, transformationType.LABEL_POSITION_ON_TOP);
    this._enableTransformation(responsiveState.CONDENSED, transformationType.LABEL_VISIBILITY);
    this._enableTransformation(responsiveState.CONDENSED, transformationType.VERTICAL_ALIGNMENT);
    this._enableTransformation(responsiveState.CONDENSED, transformationType.RADIO_BUTTON_GROUP_USE_UI_HEIGHT);

    this._enableTransformation(responsiveState.COMPACT, transformationType.LABEL_POSITION_ON_TOP);
    this._enableTransformation(responsiveState.COMPACT, transformationType.LABEL_VISIBILITY);
    this._enableTransformation(responsiveState.COMPACT, transformationType.STATUS_POSITION_ON_TOP);
    this._enableTransformation(responsiveState.COMPACT, transformationType.STATUS_VISIBILITY);
    this._enableTransformation(responsiveState.COMPACT, transformationType.VERTICAL_ALIGNMENT);
    this._enableTransformation(responsiveState.COMPACT, transformationType.GRID_COLUMN_COUNT);
    this._enableTransformation(responsiveState.COMPACT, transformationType.HIDE_PLACE_HOLDER_FIELD);
    this._enableTransformation(responsiveState.COMPACT, transformationType.FIELD_SCALABLE);
    this._enableTransformation(responsiveState.COMPACT, transformationType.RADIO_BUTTON_GROUP_USE_UI_HEIGHT);

    HtmlEnvironment.get().on('propertyChange', this._htmlPropertyChangeHandler);
    this.widget.visitFields(field => {
      if (field instanceof CompositeField) {
        field.on('propertyChange', this._formFieldAddedHandler);
        this._compositeFields.push(field);
      }
    });
  }

  override destroy() {
    super.destroy();

    this._compositeFields.forEach(compositeField => compositeField.off('propertyChange', this._formFieldAddedHandler));
    HtmlEnvironment.get().off('propertyChange', this._htmlPropertyChangeHandler);
  }

  override active(): boolean {
    return this.widget.responsive;
  }

  override getCondensedThreshold(): number {
    if (this.condensedThreshold > 0) {
      return this.condensedThreshold;
    }
    return this.widget.htmlComp.prefSize({widthOnly: true}).width;
  }

  protected override _transform() {
    this.widget.visitFields(this._transformWidget.bind(this));
  }

  protected override _transformWidget(widget: FormField): TreeVisitResult {
    // skip group boxes with responsiveness set.
    if (widget !== this.widget && widget instanceof GroupBox && widget.responsive !== null) {
      return TreeVisitResult.SKIP_SUBTREE;
    }

    // skip everything that is not a form field.
    if (!(widget instanceof FormField)) {
      return;
    }

    // suppress a validate of the layout tree, because these widgets will be validated later anyway.
    // the component still might be marked as invalid (which is necessary) caused by the responsive modifications applied.
    let htmlParent: HtmlComponent;
    if (widget.htmlComp) {
      widget.htmlComp.suppressValidate = true;
      htmlParent = widget.htmlComp.getParent();
      if (htmlParent) {
        htmlParent.suppressValidate = true;
      }
    }

    super._transformWidget(widget);

    if (widget.htmlComp) {
      widget.htmlComp.suppressValidate = false;
    }
    if (htmlParent) {
      htmlParent.suppressValidate = false;
    }
  }

  /* --- TRANSFORMATIONS ------------------------------------------------------------- */

  /**
   * Label Position -> ON_FIELD
   */
  protected _transformLabelPositionOnField(field: FormField, apply: boolean) {
    if (field.parent instanceof SequenceBox || field instanceof CheckBoxField || field instanceof LabelField) {
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
  protected _transformLabelPositionOnTop(field: FormField, apply: boolean) {
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
  protected _transformLabelVisibility(field: FormField, apply: boolean) {
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

  // ScoutJs specific method. This methods will be overridden by GroupBoxAdapter for scout classic case.
  getGridData(field: FormField): GridData {
    return new GridData(field.gridDataHints);
  }

  // ScoutJs specific method. This methods will be overridden by GroupBoxAdapter for scout classic case.
  setGridData(field: FormField, gridData: GridData) {
    field.setGridDataHints(gridData);
  }

  /**
   * Status position
   */
  protected _transformStatusPosition(field: FormField, apply: boolean) {
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
  protected _transformStatusVisibility(field: FormField, apply: boolean) {
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
  protected _transformVerticalAlignment(field: FormField, apply: boolean) {
    let isDefaultButton = field instanceof Button && field.displayStyle === Button.DisplayStyle.DEFAULT;
    let isCheckbox = field instanceof CheckBoxField;
    let isSingleHeightOnFieldLabelField = field.labelPosition === FormField.LabelPosition.ON_FIELD && field.gridData && field.gridData.h === 1;

    if (!(isDefaultButton || isCheckbox || isSingleHeightOnFieldLabelField) ||
      !field.gridData) {
      return;
    }

    let gridData = this.getGridData(field);
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
  protected _transformGridColumnCount(field: FormField, apply: boolean) {
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
  protected _transformHidePlaceHolderField(field: FormField, apply: boolean) {
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
  protected _transformFieldScalable(field: FormField, apply: boolean) {
    if (field.parent instanceof SequenceBox) {
      return;
    }

    let gridData = this.getGridData(field);
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

  protected _transformRadioButtonGroupUseUiHeight(field: FormField, apply: boolean) {
    if (!(field instanceof RadioButtonGroup)) {
      return;
    }

    let gridData = this.getGridData(field);
    if (apply && !gridData.useUiHeight) {
      this._storeFieldProperty(field, 'useUiHeight', gridData.useUiHeight);
      gridData.useUiHeight = true;
    } else if (!apply) {
      if (this._hasFieldProperty(field, 'useUiHeight')) {
        gridData.useUiHeight = this._getFieldProperty(field, 'useUiHeight');
      }
    }

    this.setGridData(field, gridData);
  }

  /* --- HANDLERS ------------------------------------------------------------- */

  protected _onFormFieldAdded(event: PropertyChangeEvent<any, CompositeField>) {
    if (this.state !== ResponsiveManager.ResponsiveState.NORMAL && (event.propertyName === 'fields' || event.propertyName === 'tabItems')) {
      let newFields = arrays.diff(event.newValue, event.oldValue) as CompositeField[];
      newFields.forEach(field => field.visitFields(this._transformWidget.bind(this)));
    }
  }
}
