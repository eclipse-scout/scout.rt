/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, HtmlCompPrefSizeOptions, TreeLayout} from '../../index';

export class LookupEditorTreeLayout extends TreeLayout {

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    // reset the height to auto as a fixed height limits the view range which would only render the first few nodes
    // this limitation needs to be removed in order to get the preferred width of all tree nodes
    const height = this.tree.$data.css('height');
    this.tree.$data.css('height', 'auto');
    this.tree.setViewRangeSize(this.tree.calculateViewRangeSize());
    const prefSize = super.preferredLayoutSize($container, options);
    this.tree.$data.css('height', height);
    return prefSize;
  }
}
