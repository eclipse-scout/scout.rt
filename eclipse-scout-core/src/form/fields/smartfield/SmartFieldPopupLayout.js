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
import {Dimension, graphics, HtmlEnvironment, PopupLayout, SmartFieldPopup} from '../../../index';

/**
 * The popup layout is different from other layouts, since it can determine its own size
 * when the autoSize flag is set to true. Otherwise it uses the given size, like a regular
 * layout. The autoSize feature is used, when a child of the SmartFieldPopupLayout invalidates the
 * tree up to the popup. Since the popup is a validate root it must re-layout itself.
 * However: the size of the popup depends on the field it belongs to.
 *
 * The proposal-chooser DIV is not always present.
 */
export default class SmartFieldPopupLayout extends PopupLayout {

  constructor(popup) {
    super(popup);

    this.animating = false;
    this.doubleCalcPrefSize = false;
  }

  layout($container) {
    let size, popupSize,
      htmlProposalChooser = this._htmlProposalChooser();

    // skip layout while CSS animation is running (prefSize would not work while animation is running)
    if (this.animating) {
      this.popup.htmlComp.$comp.oneAnimationEnd(() => {
        this.popup.revalidateLayout();
      });
      return;
    }

    super.layout($container);

    popupSize = this.popup.htmlComp.size();
    size = popupSize.subtract(this.popup.htmlComp.insets());
    htmlProposalChooser.setSize(size);

    if (this.popup.htmlComp.layouted) {
      // Reposition because opening direction may have to be switched if popup gets bigger
      // Don't do it the first time (will be done by popup.open), only if the popup is already
      // open and gets layouted again
      this.popup.position();
    } else if (SmartFieldPopup.hasPopupAnimation()) {
      // This code here is a bit complicated because:
      // 1. we must position the scrollTo position before we start the animation
      //    because it looks ugly, when we jump to the scroll position after the
      //    animation has ended
      // 2. we must first layout the popup with the table/tree correctly because
      //    revealSelection doesn't work when popup has not the right size or is
      //    not visible. That's why we must set the visibility to hidden.
      // 3. we wait for the layout validator until the popup layout is validated
      //    which means the scroll position is set correctly. Then we make the
      //    popup visible again and start the animation (which shrinks the popup
      //    to 1px height initially.
      this.animating = true;
      this.popup.htmlComp.$comp.css('visibility', 'hidden');

      this.popup.session.layoutValidator.schedulePostValidateFunction(() => {
        this.popup.htmlComp.$comp.css('visibility', '');
        this.popup.htmlComp.$comp.addClassForAnimation('animate-open');
        this.popup.htmlComp.$comp.oneAnimationEnd(() => {
          this.animating = false;
          this.popup._onAnimationEnd();
        });
      });
    }
  }

  /**
   * @override AbstractLayout.js
   */
  preferredLayoutSize($container, options) {
    let prefSize,
      htmlProposalChooser = this._htmlProposalChooser(),
      fieldBounds = graphics.offsetBounds(this.popup.smartField.$field);

    if (htmlProposalChooser) {
      prefSize = htmlProposalChooser.prefSize(options);
      prefSize = prefSize.add(this.popup.htmlComp.insets());
    } else {
      prefSize = new Dimension(
        HtmlEnvironment.get().formColumnWidth,
        HtmlEnvironment.get().formRowHeight * 2);
    }

    prefSize.width = Math.max(fieldBounds.width, prefSize.width);
    prefSize.height = Math.max(15, Math.min(350, prefSize.height)); // at least some pixels height in case there is no data, no status, no active filter

    if (prefSize.width > this._maxWindowSize()) {
      prefSize.width = this._maxWindowSize();
    }

    return prefSize;
  }

  _htmlProposalChooser() {
    let proposalChooser = this.popup.proposalChooser;
    if (!proposalChooser) {
      return null;
    }
    return proposalChooser.htmlComp;
  }

  _maxWindowSize() {
    return this.popup.$container.window().width() - (2 * this.popup.windowPaddingX);
  }
}
