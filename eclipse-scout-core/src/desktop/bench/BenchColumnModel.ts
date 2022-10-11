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
import {SimpleTabBox, SimpleTabBoxModel, WidgetModel} from '../../index';
import {RefModel} from '../../types';
import BenchRowLayoutData from './layout/BenchRowLayoutData';

export default interface BenchColumnModel extends WidgetModel {
  layoutData?: BenchRowLayoutData;
  tabBoxes?: SimpleTabBox[] | RefModel<SimpleTabBoxModel>[];
  cacheKey?: string[];
}
