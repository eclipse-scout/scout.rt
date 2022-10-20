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
import {AbstractLayout, Dimension, graphics, HtmlComponent, scrollbars} from '../../../index';

export default class ProposalChooserLayout extends AbstractLayout {

  constructor(proposalChooser, layoutResetter) {
    super();
    this._proposalChooser = proposalChooser;
    this._layoutResetter = layoutResetter;
  }

  layout($container) {
    let filterPrefSize,
      htmlContainer = HtmlComponent.get($container),
      htmlComp = HtmlComponent.get($container.children(this._layoutResetter.cssSelector)),
      size = htmlContainer.size().subtract(htmlContainer.insets()),
      $status = this._proposalChooser.$status,
      hasStatus = $status && $status.isVisible(),
      filter = this._proposalChooser.activeFilterGroup;

    if (hasStatus) {
      size.height -= graphics.size($status).height;
    }
    if (filter) {
      filterPrefSize = filter.htmlComp.prefSize();
      size.height -= filterPrefSize.height;
    }
    htmlComp.setSize(size);

    if (filter) {
      filter.htmlComp.setSize(new Dimension(size.width, filterPrefSize.height));
    }
  }

  /**
   * This preferred size implementation modifies the DIV where the table/tree is rendered
   * in a way the DIV does not limit the size of the table/tree. Thus we can read the preferred
   * size of the table/tree. After that the original width and height is restored.
   */
  preferredLayoutSize($container, options) {
    let oldDisplay, prefSize, modelSize, statusSize, filterPrefSize,
      pcWidth, pcHeight,
      htmlComp = this._proposalChooser.htmlComp,
      $status = this._proposalChooser.$status,
      filter = this._proposalChooser.activeFilterGroup,
      $parent = $container.parent();

    modelSize = this._proposalChooser.model.htmlComp.prefSize(options);
    prefSize = modelSize;
    scrollbars.storeScrollPositions($container, this._proposalChooser.session);

    // pref size of table and tree don't return accurate values for width -> measure width
    pcWidth = $container.css('width');
    pcHeight = $container.css('height');

    $container.detach();
    this._layoutResetter.modifyDom();
    $container
      .css('display', 'inline-block')
      .css('width', 'auto')
      .css('height', 'auto');
    $parent.append($container);
    modelSize.width = graphics.prefSize($container, {
      restoreScrollPositions: false
    }).width;

    $container.detach();
    this._layoutResetter.restoreDom();
    $container
      .css('display', 'block')
      .css('width', pcWidth)
      .css('height', pcHeight);
    $parent.append($container);
    scrollbars.restoreScrollPositions($container, this._proposalChooser.session);

    if ($status && $status.isVisible()) {
      oldDisplay = $status.css('display');
      $status.css('display', 'inline-block');
      statusSize = graphics.prefSize($status, {
        includeMargin: true,
        useCssSize: true
      });
      $status.css('display', oldDisplay);
      prefSize = new Dimension(Math.max(prefSize.width, statusSize.width), prefSize.height + statusSize.height);
    }

    if (filter) {
      filterPrefSize = filter.htmlComp.prefSize();
      prefSize = new Dimension(Math.max(prefSize.width, filterPrefSize.width), prefSize.height + filterPrefSize.height);
    }

    $container.toggleClass('empty', modelSize.height === 0);
    return prefSize.add(htmlComp.insets());
  }
}
