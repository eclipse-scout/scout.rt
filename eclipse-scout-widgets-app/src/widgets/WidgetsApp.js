/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
import { scout, App, Desktop, Models, OutlineViewButton } from 'eclipse-scout';

import desktopModel from './Desktop.json'; // FIXME [awe] ES6: check if JSON-extensibility still works with this approach, remove request for JSON models.

export default class WidgetsApp extends App {

  _createDesktop(parent) {
    let desktop = scout.create(Desktop, Models.getModel(desktopModel, parent));
    let dataButton = scout.create(OutlineViewButton, {
      parent: desktop,
      text: 'Data',
      displayStyle: 'TAB'
    });
    let searchButton = scout.create(OutlineViewButton, {
      parent: desktop,
      text: 'Search',
      displayStyle: 'TAB'
    });
    desktop._setViewButtons([dataButton, searchButton]);
    return desktop;
  }

}
