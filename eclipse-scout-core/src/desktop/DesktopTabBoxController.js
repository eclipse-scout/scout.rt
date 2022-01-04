/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, SimpleTabBoxController} from '../index';

export default class DesktopTabBoxController extends SimpleTabBoxController {

  constructor() {
    super();
  }

  createTabArea() {
    return scout.create('DesktopTabArea', {
      parent: this.tabBox
    });
  }

  _createTab(view) {
    return scout.create('DesktopTab', {
      parent: this.tabArea,
      view: view
    });
  }

  _shouldCreateTabForView(view) {
    // Don't create a tab if the view itself already has a header.
    return !view.headerVisible;
  }
}
