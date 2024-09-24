/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Action, DropType, FormField, FormFieldLabelPosition, FormFieldStatusPosition, FormFieldStyle, FormFieldSuppressStatus, FormFieldTooltipAnchor, FormFieldValidationResultProvider, GridData, Menu, ObjectOrChildModel, ObjectOrModel,
  ObjectType, StatusMenuMapping, StatusOrModel, TooltipSupportOptions, WidgetModel
} from '../../index';

export interface FormFieldModel extends WidgetModel {
  /**
   * Specifies whether it should be possible to drop elements onto the field.
   *
   * Currently, only {@link DropType.FILE_TRANSFER} is supported.
   *
   * By default, dropping is disabled.
   */
  dropType?: DropType;
  /**
   * Specifies the maximum size in bytes a file can have if it is being dropped.
   *
   * It only has an effect if {@link dropType} is set to {@link DropType.FILE_TRANSFER}.
   *
   * Default is {@link dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE}
   */
  dropMaximumSize?: number;
  /**
   * If set, the value will be considered invalid and the status displayed in the status area of the field.
   *
   * {@link FormField.getValidationResult} also considers this property when the field is validated, e.g. by a {@link Form}.
   */
  errorStatus?: StatusOrModel;
  /**
   * Defines the style of the field.
   *
   * - {@link FormFieldStyle.CLASSIC}: The field typically has a border surrounding the whole field.
   * - {@link FormFieldStyle.ALTERNATIVE}: The field typically only has a bottom border.
   *
   * Default is {@link FormFieldStyle.ALTERNATIVE}, set by {@link FormField.DEFAULT_FIELD_STYLE}.
   */
  fieldStyle?: FormFieldStyle;
  /**
   * Defines the dimensions of the grid cell containing the field.
   *
   * It has only an effect if the parent container uses a {@link LogicalGridLayout}, which is for example the case if the field is placed inside a {@link GroupBox}.
   *
   * The {@link logicalGrid} uses these hints to calculate the effective {@link FormField.gridData}, most importantly {@link GridData.x} and {@link GridData.y}.
   *
   * @see {@link LogicalGridLayout} on how the layout works
   * @see {@link GridDataModel} for the configuration possibilities.
   */
  gridDataHints?: ObjectOrModel<GridData>;
  /**
   * Configures the keystrokes that should be registered in the current {@link keyStrokeContext}.
   *
   * Use the {@link ActionModel.keyStroke} to assign the keys that need to be pressed.
   *
   * @see KeyStrokeContext
   */
  keyStrokes?: ObjectOrChildModel<Action>[];
  /**
   * The label of the field.
   */
  label?: string;
  /**
   * Defines whether the label should be visible.
   *
   * If set to false, the space, where the label would be, will be used by the actual field.
   */
  labelVisible?: boolean;
  /**
   * Defines the position of the label.
   *
   * By default, the label is positioned on the left side.
   */
  labelPosition?: FormFieldLabelPosition;
  /**
   * Defines the width of the label in pixel. If the value is <= 0 the property has no effect.
   *
   * Default is 0
   */
  labelWidthInPixel?: number;
  /**
   * Defines whether the label should be as width as preferred by the UI.
   */
  labelUseUiWidth?: boolean;
  /**
   * Defines whether HTML code in the {@link label} property should be interpreted. If set to false, the HTML will be encoded.
   *
   * Default is false.
   */
  labelHtmlEnabled?: boolean;
  /**
   * Defines whether the user has to fill out this field.
   *
   * If set to true, an indicator is shown so the user knows he cannot leave it empty.
   * {@link FormField.getValidationResult} also considers this property when the field is validated, e.g. by a {@link Form}.
   *
   * Default is false.
   */
  mandatory?: boolean;
  /**
   * Defines which menus should be visible inside the tooltip if an {@link errorStatus} is active.
   */
  statusMenuMappings?: ObjectOrChildModel<StatusMenuMapping>[];
  /**
   * Defines the menus for the form field.
   *
   * Depending on the concrete field, the menus will be available through a context menu in the status area on the right of the field
   * (e.g. for {@link StringField}) or in a {@link MenuBar} (e.g. for {@link GroupBox}.
   */
  menus?: ObjectOrChildModel<Menu>[];
  /**
   * Defines whether the {@link FocusManager} is allowed to consider this field when computing the first focusable field in a container, e.g. in a {@link Form}.
   *
   * Default is false.
   */
  preventInitialFocus?: boolean;
  /**
   * Defines the position of the status area, that displays {@link errorStatus}, {@link menus} and {@link tooltipText}.
   *
   * By default the status area is on the right side of the field.
   */
  statusPosition?: FormFieldStatusPosition;
  /**
   * Specifies whether the status area should always be visible.
   *
   * If set to true, a small space on the right of the field will be reserved for the status.
   * The space is always visible, even if there is no {@link errorStatus}, {@link menus} and {@link tooltipText}.
   * This makes sure, that fields in the same grid column are nicely aligned.
   *
   * If the alignment on the right side is not required or wastes too much space on small containers, it can be removed by setting this property to false.
   * However, the status area appears as soon as a {@link errorStatus}, {@link menus} or {@link tooltipText} needs to be displayed. So it will only be invisible if it would be empty.
   *
   * Default is true.
   */
  statusVisible?: boolean;
  /**
   * Allows to suppress the display of the {@link errorStatus}.
   */
  suppressStatus?: FormFieldSuppressStatus;
  /**
   * The text to be used in the {@link Tooltip} that is either displayed in the status area or when hovering over the field, depending on {@link tooltipAnchor}.
   */
  tooltipText?: string;
  /**
   * Defines where and how the tooltip configured by {@link tooltipText} should appear.
   *
   * By default, the tooltip is shown when the user clicks on the info icon in the status area.
   */
  tooltipAnchor?: FormFieldTooltipAnchor;
  /**
   * Can be used to provide custom options when creating an on-field-tooltip ({@link tooltipAnchor is set to {@link FormField.TooltipAnchor.ON_FIELD}}.
   * If not specified, the default options created by {@link FormField._createOnFieldTooltipOptions} are used.
   */
  onFieldTooltipOptionsCreator?: (this: FormField) => TooltipSupportOptions;
  /**
   * Sets the font of the field using the HTML style attribute.
   *
   * *Note*: this property should be avoided in general in favor of {@link cssClass}, because the style attribute will always override the rules defined by the stylesheet.
   */
  font?: string;
  /**
   * Sets the foreground color of the field using the HTML style attribute.
   *
   * It can either be a hex based color value (without #), a color name or a color function, see https://developer.mozilla.org/en-US/docs/Web/CSS/color_value.
   *
   * *Note*: this property should be avoided in general in favor of {@link cssClass}, because the style attribute will always override the rules defined by the stylesheet.
   * This means, if a color is specified by this property, the field will always have that color even if it is disabled, hovered, selected etc.
   */
  foregroundColor?: string;
  /**
   * Sets the background color of the field using the HTML style attribute.
   *
   * It can either be a hex based color value (without #), a color name or a color function, see https://developer.mozilla.org/en-US/docs/Web/CSS/color_value.
   *
   * *Note*: this property should be avoided in general in favor of {@link cssClass}, because the style attribute will always override the rules defined by the stylesheet.
   * This means, if a color is specified by this property, the field will always have that color even if it is disabled, hovered, selected etc.
   */
  backgroundColor?: string;
  /**
   * Sets the font of the label using the HTML style attribute.
   *
   * *Note*: this property should be avoided in general in favor of {@link cssClass}, because the style attribute will always override the rules defined by the stylesheet.
   */
  labelFont?: string;
  /**
   * Sets the foreground color of the label using the HTML style attribute.
   *
   * It can either be a hex based color value (without #), a color name or a color function, see https://developer.mozilla.org/en-US/docs/Web/CSS/color_value.
   *
   * *Note*: this property should be avoided in general in favor of {@link cssClass}, because the style attribute will always override the rules defined by the stylesheet.
   * This means, if a color is specified by this property, the field will always have that color even if it is disabled, hovered, selected etc.
   */
  labelForegroundColor?: string;
  /**
   * Sets the background color of the label using the HTML style attribute.
   *
   * It can either be a hex based color value (without #), a color name or a color function, see https://developer.mozilla.org/en-US/docs/Web/CSS/color_value.
   *
   * *Note*: this property should be avoided in general in favor of {@link cssClass}, because the style attribute will always override the rules defined by the stylesheet.
   * This means, if a color is specified by this property, the field will always have that color even if it is disabled, hovered, selected etc.
   */
  labelBackgroundColor?: string;
  /**
   * Configures whether save needed should be computed.
   *
   * If set to false, {@link FormField.saveNeeded} will always return false for this field, even if a child field would return true.
   * However, if the field was {@link FormField.touched}, {@link FormField.saveNeeded} will be true.
   *
   * Default is true.
   */
  checkSaveNeeded?: boolean;
  /**
   * Specifies whether the form lifecycle ignores child fields when visiting this field.
   *
   * The default is false.
   */
  lifecycleBoundary?: boolean;
  /**
   * Provides the validation result containing information about the validity of a form field.
   *
   * @see FormField.getValidationResult
   */
  validationResultProvider?: FormFieldValidationResultProvider | ObjectType<FormFieldValidationResultProvider>;
}
