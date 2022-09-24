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

export default interface SliderModel extends WidgetModel {
  /**
   * Default is 0.
   */
  value?: number;
  /**
   * Default is 0.
   */
  minValue?: number;
  /**
   * Default is 100.
   */
  maxValue?: number;
  /**
   * Default is 1.
   */
  step?: number;
}
