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
import {WidgetModel} from '../index';

export default interface LabelModel extends WidgetModel {
  /**
   * Default is null.
   */
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
