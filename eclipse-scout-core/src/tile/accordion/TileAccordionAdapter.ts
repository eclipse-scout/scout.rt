/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AccordionAdapter, TileAccordionModel} from '../../index';

export class TileAccordionAdapter extends AccordionAdapter {
  protected override _initProperties(model: TileAccordionModel & { takeTileFiltersFromGroup: boolean }) {
    super._initProperties(model);
    // TileGridAdapter creates a RemoteTileFilter for each grid.
    // Such filters must not be added to the tile accordion, otherwise no tiles would be visible at all.
    // Because taking the filters from the group is only necessary for Scout JS usage, it is ok to disable this feature completely.
    model.takeTileFiltersFromGroup = false;
  }
}
