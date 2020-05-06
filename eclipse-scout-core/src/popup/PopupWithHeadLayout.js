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
import {graphics, HtmlComponent, Insets, PopupLayout} from '../index';

export default class PopupWithHeadLayout extends PopupLayout {

  constructor(popup) {
    super(popup);
  }

  _setSize(prefSize) {
    super._setSize(prefSize);

    let htmlComp = this.popup.htmlComp,
      popupSize = prefSize,
      htmlBody = HtmlComponent.optGet(this.popup.$body);

    // While animating the body animation sets the size
    if (!this.popup.bodyAnimating) {
      // Set size of body
      popupSize = popupSize.subtract(htmlComp.insets());
      if (this._headVisible) {
        let headSize = graphics.size(this.popup.$head, true);
        // Adjust popup size if head changed size
        if (popupSize.width < headSize.width) {
          popupSize.width = headSize.width;
        }
      }
      htmlBody.setSize(popupSize);
    }
  }

  /**
   * @override
   */
  _adjustSize(prefSize) {
    return this._adjustSizeWithAnchor(prefSize);
  }

  /**
   * @override
   */
  _calcMaxSizeAroundAnchor() {
    if (!this.popup._headVisible) {
      return super._calcMaxSizeAroundAnchor();
    }

    // Position the popup at the desired location before doing any calculations,
    // but do no try to switch the position if there is not enough space
    this._position(false);

    let maxWidthLeft, maxWidthRight, maxHeightDown, maxHeightUp,
      htmlComp = this.popup.htmlComp,
      windowPaddingLeft = this.popup.windowPaddingX,
      windowPaddingRight = this.popup.windowPaddingX,
      windowPaddingY = this.popup.windowPaddingY,
      popupBounds = graphics.offsetBounds(htmlComp.$comp),
      popupHeadBounds = graphics.offsetBounds(this.popup.$head),
      popupMargins = htmlComp.margins(),
      windowSize = this.popup.getWindowSize();

    maxWidthRight = windowSize.width - popupHeadBounds.x - windowPaddingRight;
    maxWidthLeft = popupHeadBounds.x + popupHeadBounds.width - windowPaddingLeft;
    maxHeightDown = (windowSize.height - popupBounds.y - windowPaddingY);
    // head height is irrelevant because popup has a margin as height as the header
    maxHeightUp = (popupBounds.y - popupMargins.vertical() - windowPaddingY);

    return new Insets(maxHeightUp, maxWidthRight, maxHeightDown, maxWidthLeft);
  }

  preferredLayoutSize($container, options) {
    let htmlComp = this.popup.htmlComp,
      htmlBody,
      prefSize;

    if (!this.popup.bodyAnimating) {
      let popupStyleBackup = this.popup.$container.attr('style');
      let $siblingBodies = this.popup.$body.siblings('.popup-body');
      $siblingBodies.addClass('hidden');
      this.popup.$container.css({
        width: 'auto',
        height: 'auto'
      });

      htmlBody = HtmlComponent.optGet(this.popup.$body);
      if (htmlBody) {
        prefSize = htmlBody.prefSize(options)
          .add(htmlBody.margins());
      } else {
        prefSize = graphics.prefSize(this.popup.$body, options)
          .add(graphics.margins(this.popup.$body));
      }

      $siblingBodies.removeClass('hidden');
      this.popup.$container.attr('style', popupStyleBackup);
    } else {
      prefSize = graphics.size(this.popup.$body, options)
        .add(graphics.margins(this.popup.$body));
    }

    if (this.popup._headVisible) {
      let headSize = graphics.size(this.popup.$head, options)
        .add(graphics.margins(this.popup.$head));
      prefSize.width = prefSize.width < headSize.width ? headSize.width : prefSize.width;
    }
    prefSize = prefSize.add(htmlComp.insets());
    return prefSize;
  }
}
