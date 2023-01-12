/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, Group, HtmlComponent, HtmlCompPrefSizeOptions} from '../index';

export class GroupLayout extends AbstractLayout {
  group: Group;

  constructor(group: Group) {
    super();
    this.group = group;
  }

  override layout($container: JQuery) {
    let htmlComp = this.group.htmlComp;
    let containerSize = htmlComp.availableSize()
      .subtract(htmlComp.insets());

    let htmlHeader = this.group.htmlHeader;
    let headerSize = htmlHeader.prefSize({
      widthHint: containerSize.width
    });
    headerSize.width = containerSize.width;
    headerSize = headerSize.subtract(htmlHeader.margins());
    htmlHeader.setSize(headerSize);

    let htmlFooter = this.group.htmlFooter;
    if (htmlFooter.isVisible()) {
      let footerSize = htmlFooter.prefSize({
        includeMargin: false,
        useCssSize: true
      });
      footerSize.width = containerSize.width;
      htmlFooter.setSize(footerSize.subtract(htmlFooter.margins()));
    }

    // 1st condition: Set size only if group is expanded
    // 2nd condition: There is no need to update it during the expand animation (the body will be layouted correctly before the animation starts)
    // 3rd condition: When Group.setCollapsed(false) has been called an event is triggered that might causes invalidating layout on other all groups (inclusive currently expanding group).
    //                The body of the currently expanding group is not rendered at this time.
    // 4th condition: When body is invisible by property (bodyVisible)
    if (this.group.collapsed || this.group.bodyAnimating || !this.group.body.rendered || !this.group.body.isVisible()) {
      return;
    }

    let htmlBody = this.group.body.htmlComp;
    let bodySize = containerSize.subtract(htmlBody.margins());
    bodySize.height -= headerSize.height;
    if (htmlFooter.isVisible()) {
      bodySize.height -= htmlFooter.prefSize(true).height;
    }
    htmlBody.setSize(bodySize);
  }

  override invalidate(htmlSource: HtmlComponent) {
    let htmlBody = this.group.body.htmlComp;
    // If a child triggers a layout invalidation, the animation should be stopped and restarted because the body will likely have another height.
    // This will happen for sure if a child is an image which will be loaded during the animation.
    if (htmlBody && this.group.bodyAnimating && htmlSource && htmlSource.isDescendantOf(this.group.htmlComp)) {
      // Stop running animation
      this.group.body.$container.stop();

      // Resize to new height
      this.group.resizeBody();
    }
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    options = options || {};
    let prefSize;
    let htmlComp = this.group.htmlComp;
    let htmlHeader = this.group.htmlHeader;
    let htmlBody = this.group.body.htmlComp;
    let htmlFooter = this.group.htmlFooter;

    // HeightHint not supported
    options.heightHint = null;

    if (this.group.bodyAnimating) {
      // Return the current size when the body is collapsing or expanding
      // so that the widgets on the bottom and on top move smoothly with the animation
      prefSize = htmlBody.size(true);
    } else if (this.group.collapsed || !this.group.body.rendered || !this.group.body.isVisible()) {
      // Body may not be rendered even if collapsed is false if property has changed but _renderCollapse not called yet
      // (if revalidateLayoutTree is called during collapsed property event)
      prefSize = new Dimension(0, 0);
    } else {
      prefSize = htmlBody.prefSize(options)
        .add(htmlBody.margins());
    }
    prefSize = prefSize.add(htmlComp.insets({
      includeMargin: true
    }));
    prefSize.height += htmlHeader.prefSize(options)
      .add(htmlHeader.margins()).height;
    if (htmlFooter.isVisible()) {
      prefSize.height += htmlFooter.prefSize({
        includeMargin: true,
        useCssSize: true
      }).height;
    }
    return prefSize;
  }
}
