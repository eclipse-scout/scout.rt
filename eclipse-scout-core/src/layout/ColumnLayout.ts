/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension, HtmlComponent, scout} from '../index';
import $ from 'jquery';

export default class ColumnLayout extends AbstractLayout {

  constructor(options) {
    super();
    options = options || {};
    this.stretch = scout.nvl(options.stretch, true);
    this.useCssWidth = scout.nvl(options.useCssWidth, false);

    // ColumnLayout = each child element represents a column
    // +------+---+------+
    // |      |   |      |
    // +------+---+------+
  }

  _getChildren($container) {
    return $container.children();
  }

  layout($container) {
    let htmlComp = HtmlComponent.get($container);
    let containerSize = htmlComp.availableSize()
      .subtract(htmlComp.insets());

    this._getChildren($container).each((i, elem) => {
      let $elem = $(elem);
      let htmlChild = HtmlComponent.optGet($elem);
      if (!htmlChild || !$elem.isVisible()) {
        return;
      }

      let childPrefSize = htmlChild.prefSize({
        useCssSize: true
      });

      if (this.stretch) {
        // All elements in a column layout have the same height which is the height of the container
        childPrefSize.height = containerSize.height - htmlChild.margins().vertical();
      }

      // Use layout data width if set
      if (htmlChild.layoutData && htmlChild.layoutData.widthHint) {
        childPrefSize.width = htmlChild.layoutData.widthHint;
      }
      if (this.useCssWidth) {
        htmlChild.$comp.cssHeight(childPrefSize.height);
        htmlChild.revalidateLayout();
      } else {
        htmlChild.setSize(childPrefSize);
      }
    });
  }

  preferredLayoutSize($container, options) {
    let prefSize = new Dimension(),
      htmlContainer = HtmlComponent.get($container),
      childOptions = {
        useCssSize: true
      };

    this._getChildren($container).each((i, elem) => {
      let $elem = $(elem);
      let htmlChild = HtmlComponent.optGet($elem);
      if (!htmlChild || !$elem.isVisible()) {
        return;
      }

      let childPrefSize = htmlChild.prefSize(childOptions);
      // Use layout data width if set
      if (htmlChild.layoutData && htmlChild.layoutData.widthHint) {
        childPrefSize.width = htmlChild.layoutData.widthHint;
      }
      childPrefSize = childPrefSize.add(htmlChild.margins());
      prefSize.width = prefSize.width + childPrefSize.width;
      prefSize.height = Math.max(prefSize.height, childPrefSize.height);
    });

    prefSize = prefSize.add(htmlContainer.insets());
    return prefSize;
  }
}
