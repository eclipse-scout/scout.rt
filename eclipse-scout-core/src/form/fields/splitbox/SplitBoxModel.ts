/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, FormFieldModel, ObjectOrChildModel, SplitBoxSplitterPositionType} from '../../../index';

export interface SplitBoxModel extends FormFieldModel {
  firstField?: ObjectOrChildModel<FormField>;
  secondField?: ObjectOrChildModel<FormField>;
  collapsibleField?: FormField | string;
  fieldCollapsed?: boolean;
  toggleCollapseKeyStroke?: string;
  firstCollapseKeyStroke?: string;
  secondCollapseKeyStroke?: string;
  /** true = split x-axis, false = split y-axis */
  splitHorizontal?: boolean;
  splitterEnabled?: boolean;
  splitterPosition?: number;
  minSplitterPosition?: number;
  splitterPositionType?: SplitBoxSplitterPositionType;
  fieldMinimized?: boolean;
  minimizeEnabled?: boolean;
}
