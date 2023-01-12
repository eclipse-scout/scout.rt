/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlComponent, HtmlCompPrefSizeOptions, scout} from '../index';
import $ from 'jquery';

export interface RowLayoutOptions {
  /** If true, all elements will be as width as the container. Default is true. */
  stretch?: boolean;

  /** If false, the layout won't change the size of the elements and just calls {@link HtmlComponent.validateLayout}. Default is true. */
  pixelBasedSizing?: boolean;
}

/**
 * RowLayout = each child element represents a row
 * +-----------------+
 * |                 |
 * +-----------------+
 * |                 |
 * |                 |
 * +-----------------+
 * |                 |
 * +-----------------+
 */
export class RowLayout extends AbstractLayout implements RowLayoutOptions {
  stretch: boolean;
  pixelBasedSizing: boolean;

  constructor(options?: RowLayoutOptions) {
    super();
    options = options || {} as RowLayoutOptions;
    this.pixelBasedSizing = scout.nvl(options.pixelBasedSizing, true);
    this.stretch = scout.nvl(options.stretch, true);
  }

  protected _getChildren($container: JQuery): JQuery {
    return $container.children();
  }

  override layout($container: JQuery) {
    let htmlComp = HtmlComponent.get($container);
    let containerSize = htmlComp.availableSize()
      .subtract(htmlComp.insets());

    this._getChildren($container).each((index, elem) => {
      let $elem = $(elem);
      let htmlChild = HtmlComponent.optGet($elem);
      if (!htmlChild || !$elem.isVisible()) {
        return;
      }
      if (!this.pixelBasedSizing) {
        htmlChild.validateLayout();
        return;
      }
      let prefSize = htmlChild.prefSize({
        widthHint: containerSize.width
      });

      if (this.stretch) {
        // All elements in a row layout have the same width which is the width of the container
        prefSize.width = containerSize.width - htmlChild.margins().horizontal();
      }

      htmlChild.setSize(prefSize);
    });
  }

  override preferredLayoutSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    let prefSize = new Dimension(),
      htmlContainer = HtmlComponent.get($container),
      maxWidth = 0;

    this._getChildren($container).each((index, elem) => {
      let $elem = $(elem);
      let htmlChild = HtmlComponent.optGet($elem);
      if (!htmlChild || !$elem.isVisible()) {
        return;
      }
      let htmlChildPrefSize = htmlChild.prefSize(options)
        .add(htmlChild.margins());
      maxWidth = Math.max(htmlChildPrefSize.width, maxWidth);
      prefSize.height += htmlChildPrefSize.height;
      prefSize.width = maxWidth;
    });

    prefSize = prefSize.add(htmlContainer.insets());
    return prefSize;
  }
}
