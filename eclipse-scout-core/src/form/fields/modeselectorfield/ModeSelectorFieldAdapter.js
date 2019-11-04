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

export default class ModeSelectorFieldAdapter extends ValueFieldAdapter {

  constructor() {
    super();
  }

  _createWidget(model) {
    this._addModeSelector(model);
    return super._createWidget(model);
  }

  _addModeSelector(model) {
    model.modeSelector = {
      objectType: 'ModeSelector'
    };
  }

  _postCreateWidget() {
    this.widget.modeSelector.setModes(this.widget.modes);
    delete this.widget.modes;
  }
}
