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
import {graphics, OutlineLayout, SearchOutline} from '../../index';

export default class SearchOutlineLayout extends OutlineLayout {
  declare outline: SearchOutline;

  constructor(outline: SearchOutline) {
    super(outline);
  }

  protected override _setDataHeight(heightOffset: number) {
    // Add search panel height to heightOffset
    let searchPanelSize = graphics.size(this.outline.$searchPanel, true);
    heightOffset += searchPanelSize.height;

    super._setDataHeight(heightOffset);
  }
}
