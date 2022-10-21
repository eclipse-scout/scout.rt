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
import {CompositeFieldAdapter, Widget} from '../../../index';
import {RefModel, SomeRequired} from '../../../types';
import WidgetModel from '../../../widget/WidgetModel';

export default class SequenceBoxAdapter extends CompositeFieldAdapter {


  protected override _initModel(m: RefModel<WidgetModel>, parent: Widget): SomeRequired<WidgetModel, 'objectType'> {
    let model = super._initModel(m, parent);
    // Set logical grid to null -> Calculation happens on server side
    model.logicalGrid = null;
    return model;
  }
}
