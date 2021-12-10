/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, FormLayout, graphics, HtmlComponent, Point} from '../index';

export default class DialogLayout extends FormLayout {

  constructor(form) {
    super(form);
    this.autoSize = true;
    this.shrinkEnabled = false;
  }

  layout($container) {
    if (!this.autoSize) {
      super.layout($container);
      return;
    }

    let currentBounds,
      htmlComp = this.form.htmlComp,
      prefBounds = this.form.prefBounds(),
      dialogMargins = htmlComp.margins(),
      windowSize = $container.windowSize();

    if (prefBounds) {
      currentBounds = prefBounds;
    } else {
      currentBounds = htmlComp.bounds();
    }
    let dialogSize = this._calcSize($container, currentBounds, prefBounds);

    // Add markers to be able to style the dialog in a different way when it uses the full width or height
    $container
      .toggleClass('full-width', this.form.maximized || (currentBounds.x === 0 && dialogMargins.horizontal() === 0 && windowSize.width === dialogSize.width))
      .toggleClass('full-height', this.form.maximized || (currentBounds.y === 0 && dialogMargins.vertical() === 0 && windowSize.height === dialogSize.height));

    // Ensure the dialog can only get larger, not smaller.
    // This prevents 'snapping' the dialog back to the calculated size when a field changes its visibility, but the user previously enlarged the dialog.
    // This must not happen when the dialog is laid out the first time (-> when it is opened, because it has not the right size yet and may get too big)
    if (htmlComp.layouted && !this.shrinkEnabled) {
      dialogSize.width = Math.max(dialogSize.width, currentBounds.width);
      dialogSize.height = Math.max(dialogSize.height, currentBounds.height);
    }

    graphics.setSize($container, dialogSize);
    super.layout($container);
  }

  /**
   * @param currentBounds
   *          bounds as returned by the graphics.bounds() function, i.e. position is the CSS
   *          position (top-left of "margin box"), dimension excludes margins
   * @param prefBounds
   *          optional preferred bounds (same expectations as with "currentBounds")
   * @return {Dimension}
   *          adjusted size excluding margins (suitable to pass to graphics.setSize())
   */
  _calcSize($container, currentBounds, prefBounds) {
    let dialogSize,
      htmlComp = this.form.htmlComp,
      dialogMargins = htmlComp.margins(),
      windowSize = $container.windowSize();

    if (this.form.maximized) {
      return windowSize;
    }

    if (prefBounds) {
      dialogSize = prefBounds.dimension();
      currentBounds = prefBounds;
      dialogSize = DialogLayout.fitContainerInWindow(windowSize, currentBounds.point(), dialogSize, dialogMargins);
      if (prefBounds.dimension().width === dialogSize.width) {
        // If width is still the same (=fitContainerInWindow did not reduce the width), then just return it. Otherwise read pref size again
        return dialogSize;
      }
    }

    // Calculate preferred width first...
    dialogSize = this.preferredLayoutSize($container, {
      widthOnly: true
    });
    dialogSize = DialogLayout.fitContainerInWindow(windowSize, currentBounds.point(), dialogSize, dialogMargins);

    // ...then calculate the actual preferred size based on the width. This is necessary because the dialog may contain fields with wrapping content. Without a width hint the height would not be correct.
    dialogSize = this.preferredLayoutSize($container, {
      widthHint: dialogSize.width
    }).ceil(); // always round up. If we'd round a height of 380.00005 pixel down
    // there is not enough space to display the group-box, thus the browser would show scrollbars.

    dialogSize = DialogLayout.fitContainerInWindow(windowSize, currentBounds.point(), dialogSize, dialogMargins);
    return dialogSize;
  }

  /**
   * Calculates the new container size and position. If the given containerSize is larger then the windowSize, the size will be adjusted.
   *
   * @param windowSize total size of the window
   * @param containerPosition {Point} current CSS position of the container (top-left of the "margin box")
   * @param containerSize {Dimension} preferred size of container (excluding margins)
   * @param containerMargins {Insets} margins of the container
   * @returns {Dimension} the new, adjusted container size (excluding margins)
   * @static
   */
  static fitContainerInWindow(windowSize, containerPosition, containerSize, containerMargins) {
    // class .dialog may specify a margin
    // currentBounds.y and x are 0 initially, but if size changes while dialog is open they are greater than 0
    // This guarantees the dialog size may not exceed the document size
    let maxWidth = (windowSize.width - containerMargins.horizontal() - containerPosition.x);
    let maxHeight = (windowSize.height - containerMargins.vertical() - containerPosition.y);

    // Calculate new dialog size, ensuring that the dialog is not larger than container
    let size = new Dimension();
    size.width = Math.min(maxWidth, containerSize.width);
    size.height = Math.min(maxHeight, containerSize.height);

    return size;
  }

  /**
   * Returns the coordinates to place the given container in the optical middle of the window.
   *
   * @param $container
   * @returns {Point} new X,Y position of the container
   * @static
   */
  static positionContainerInWindow($container) {
    let
      windowSize = $container.windowSize(),
      containerSize = HtmlComponent.get($container).size(true),
      left = (windowSize.width - containerSize.width) / 2,
      top = (windowSize.height - containerSize.height) / 2;

    // optical middle (move up 20% of distance between window and dialog)
    let opticalMiddleOffset = (top / 5);
    top -= opticalMiddleOffset;

    // Ensure integer numbers
    left = Math.floor(left);
    top = Math.floor(top);

    return new Point(left, top);
  }
}
