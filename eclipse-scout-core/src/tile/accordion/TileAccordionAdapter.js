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
import {AccordionAdapter} from '../../index';

export default class TileAccordionAdapter extends AccordionAdapter {

  constructor() {
    super();
  }

  /**
   * @override
   */
  _initProperties(model) {
    super._initProperties(model);
    // TileGridAdapter creates a RemoteTileFilter for each grid.
    // Such filters must not be added to the tile accordion, otherwise no tiles would be visible at all.
    // Because taking the filters from the group is only necessary for Scout JS usage, it is ok to disable this feature completely.
    model.takeTileFiltersFromGroup = false;
  }
}
