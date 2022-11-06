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
import {GridData, Widget, WidgetModel} from '../index';
import {ObjectOrChildModel} from '../scout';

export default interface CarouselModel extends WidgetModel {
  /**
   * Default is true
   */
  statusEnabled?: boolean;
  statusItemHtml?: string;
  /**
   * Default is 0.25
   */
  moveThreshold?: number;
  widgets?: ObjectOrChildModel<Widget> | ObjectOrChildModel<Widget>[];
  currentItem?: number;
  gridData?: GridData;
}
