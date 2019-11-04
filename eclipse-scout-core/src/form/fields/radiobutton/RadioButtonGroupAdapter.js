/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ValueFieldAdapter} from '../../../index';

export default class RadioButtonGroupAdapter extends ValueFieldAdapter {

  constructor() {
    super();
  }

  /**
   * @override
   */
  _initModel(model, parent) {
    model = super._initModel(model, parent);
    // Set logical grid to null -> Calculation happens on server side
    model.logicalGrid = null;
    return model;
  }
}
