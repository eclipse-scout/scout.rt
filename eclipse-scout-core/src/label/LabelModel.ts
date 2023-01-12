/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {WidgetModel} from '../index';

export interface LabelModel extends WidgetModel {
  value?: string;
  /**
   * Configures, if HTML rendering is enabled.
   *
   * Subclasses can override this method. Default is false. Make sure that any user input (or other insecure input) is encoded (security), if this property is enabled.
   *
   * true, if HTML rendering is enabled, false otherwise.
   */
  htmlEnabled?: boolean;
  /**
   * Default is false.
   */
  scrollable?: boolean;
}
