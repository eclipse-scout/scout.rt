/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldModel, ObjectOrChildModel, Widget} from '../../index';

export interface WidgetFieldModel extends FormFieldModel {
  /**
   * Configures whether vertical scrollbars should be shown if the content is bigger than the available size.
   *
   * Default is false.
   */
  scrollable?: boolean;
  /**
   * Configures the widget to be wrapped.
   */
  fieldWidget?: ObjectOrChildModel<Widget>;
}
