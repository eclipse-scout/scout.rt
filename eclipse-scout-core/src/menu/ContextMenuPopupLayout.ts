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
import {graphics, HtmlComponent, PopupLayout, scrollbars} from '../index';

export default class ContextMenuPopupLayout extends PopupLayout {

  constructor(popup) {
    super(popup);
  }

  _setSize(prefSize) {
    if (this.popup.bodyAnimating) {
      scrollbars.update(this.popup.get$Scrollable());
      return;
    }
    super._setSize(prefSize);
    scrollbars.update(this.popup.get$Scrollable());
    let htmlBody = HtmlComponent.get(this.popup.$body);
    htmlBody.pack();
  }

  invalidate(htmlSource) {
    // If a child triggers a layout invalidation, the popup needs to be resized.
    // This will happen for sure if a child is an image which will be loaded during the animation.
    // Ideally, the running animations would be stopped, the popup layouted, the animations adjusted to the new bounds and then continued.
    // This is currently too complicated to implement, so instead we let the animations finish and resize the popup at the end (but before other resize animations start).
    if (this.popup.bodyAnimating && htmlSource && htmlSource.isDescendantOf(this.popup.htmlComp)) {
      this.popup._toggleSubMenuQueue.unshift(() => {
        if (!this.popup.rendered) {
          return;
        }
        let oldOffset = this.popup.$body.data('text-offset');
        this.popup._adjustTextAlignment();
        this.popup.animateResize = true;
        this.resizeAnimationDuration = this.popup.animationDuration;
        this.popup.revalidateLayoutTree();
        this.popup.animateResize = false;
        this.popup._animateTextOffset(this.popup.$body, oldOffset);
        this.popup.$container.promise().done(() => this.popup._processSubMenuQueue());
      });
    }
  }

  preferredLayoutSize($container, options) {
    let htmlComp = this.popup.htmlComp;
    let htmlBody = HtmlComponent.get(this.popup.$body);
    let prefSize;
    if (this.popup.bodyAnimating) {
      prefSize = graphics.size(this.popup.$body, options);
    } else {
      let popupStyleBackup = this.popup.$container.attr('style');
      let $siblingBodies = this.popup.$body.siblings('.context-menu');
      $siblingBodies.addClass('hidden');
      this.popup.$body.css({
        width: 'auto',
        height: 'auto'
      });

      htmlBody = HtmlComponent.get(this.popup.$body);
      prefSize = htmlBody.prefSize(options);

      $siblingBodies.removeClass('hidden');
      this.popup.$container.attr('style', popupStyleBackup);
    }
    return prefSize
      .add(htmlComp.insets())
      .add(htmlBody.margins());
  }
}
