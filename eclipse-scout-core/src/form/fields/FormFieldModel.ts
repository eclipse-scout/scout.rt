/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, ActionModel, FormField, GridData, Menu, MenuModel, StatusMenuMapping, StatusMenuMappingModel, WidgetModel} from '../../index';
import {DropType} from '../../util/dragAndDrop';
import {TooltipSupportOptions} from '../../tooltip/TooltipSupport';
import {FormFieldLabelPosition, FormFieldMode, FormFieldStatusPosition, FormFieldStyle, FormFieldSuppressStatus, FormFieldTooltipAnchor} from './FormField';
import {StatusOrModel} from '../../status/Status';
import {RefModel} from '../../types';

export default interface FormFieldModel extends WidgetModel {
  dropType?: DropType;
  dropMaximumSize?: number;
  errorStatus?: StatusOrModel;
  fieldStyle?: FormFieldStyle;
  gridDataHints?: GridData;
  mode?: FormFieldMode;
  keyStrokes?: (Action | RefModel<ActionModel>)[];
  displayText?: string;
  label?: string;
  labelVisible?: boolean;
  labelPosition?: FormFieldLabelPosition;
  labelWidthInPixel?: number;
  labelUseUiWidth?: boolean;
  labelHtmlEnabled?: boolean;
  mandatory?: boolean;
  statusMenuMappings?: StatusMenuMapping[] | RefModel<StatusMenuMappingModel>[];
  menus?: Menu[] | RefModel<MenuModel>[];
  menusVisible?: boolean;
  defaultMenuTypes?: string[];
  preventInitialFocus?: boolean;
  statusPosition?: FormFieldStatusPosition;
  statusVisible?: boolean;
  suppressStatus?: FormFieldSuppressStatus;
  touched?: boolean;
  tooltipText?: string;
  font?: string;
  foregroundColor?: string;
  backgroundColor?: string;
  labelFont?: string;
  labelForegroundColor?: string;
  labelBackgroundColor?: string;
  tooltipAnchor?: FormFieldTooltipAnchor;
  onFieldTooltipOptionsCreator?: (this: FormField) => TooltipSupportOptions;
}
