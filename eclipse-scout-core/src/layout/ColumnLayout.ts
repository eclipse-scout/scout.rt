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

export interface ColumnLayoutData {
  widthHint?: number;
}

export interface ColumnLayoutOptions {
  /** If true, all elements will be as height as the container. Default is true. */
  stretch?: boolean;

  /** If true, the layout won't change the width of the elements because they depend on the width set by the stylesheet. Default is false. */
  useCssWidth?: number;
}

/**
 * ColumnLayout = each child element represents a column
 * +------+---+------+
 * |      |   |      |
 * +------+---+------+
 */
export class ColumnLayout extends AbstractLayout implements ColumnLayoutOptions {
  stretch: boolean;
  useCssWidth: number;

  constructor(options?: ColumnLayoutOptions) {
    super();
    options = options || {} as ColumnLayoutOptions;
    this.stretch = scout.nvl(options.stretch, true);
    this.useCssWidth = scout.nvl(options.useCssWidth, false);
  }

  protected _getChildren($container: JQuery): JQuery {
    return $container.children();
  }

  override layout($container: JQuery) {
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
      if (htmlChild.layoutData && (<ColumnLayoutData>htmlChild.layoutData).widthHint) {
        childPrefSize.width = (<ColumnLayoutData>htmlChild.layoutData).widthHint;
      }
      if (this.useCssWidth) {
        htmlChild.$comp.cssHeight(childPrefSize.height);
        htmlChild.revalidateLayout();
      } else {
        htmlChild.setSize(childPrefSize);
      }
    });
  }

  override preferredLayoutSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
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
      if (htmlChild.layoutData && (<ColumnLayoutData>htmlChild.layoutData).widthHint) {
        childPrefSize.width = (<ColumnLayoutData>htmlChild.layoutData).widthHint;
      }
      childPrefSize = childPrefSize.add(htmlChild.margins());
      prefSize.width = prefSize.width + childPrefSize.width;
      prefSize.height = Math.max(prefSize.height, childPrefSize.height);
    });

    prefSize = prefSize.add(htmlContainer.insets());
    return prefSize;
  }
}
