/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, graphics, HtmlComponent, HtmlCompPrefSizeOptions, Rectangle, SplitBox} from '../../../index';
import $ from 'jquery';

export class SplitBoxLayout extends AbstractLayout {
  splitBox: SplitBox;

  constructor(splitBox: SplitBox) {
    super();
    this.splitBox = splitBox;
  }

  override layout($container: JQuery) {
    // Extract components
    let htmlContainer = HtmlComponent.get($container), // = split-area
      $splitter = $container.children('.splitter'),
      $fields = $container.children('.form-field'),
      htmlFirstField = HtmlComponent.optGet($fields.eq(0)),
      htmlSecondField = HtmlComponent.optGet($fields.eq(1)),
      // Calculate available size for split area
      splitXAxis = this.splitBox.splitHorizontal;

    $splitter.removeClass('hidden');

    let firstFieldBounds: Rectangle,
      availableSize = htmlContainer.availableSize().subtract(htmlContainer.insets()),
      hasFirstField = (htmlFirstField && htmlFirstField.isVisible()),
      hasSecondField = (htmlSecondField && htmlSecondField.isVisible()),
      hasTwoFields = hasFirstField && hasSecondField,
      hasOneField = !hasTwoFields && (hasFirstField || hasSecondField),
      splitterPosition = this.splitBox.getEffectiveSplitterPosition();

    // remove splitter size from available with, only when both fields are visible
    // otherwise the splitter is invisible and requires no space.
    let availableSizeForFields = new Dimension(availableSize);
    if (hasTwoFields) {
      if (splitXAxis) { // "|"
        availableSizeForFields.width -= htmlFirstField.margins().right;
      } else { // "--"
        availableSizeForFields.height -= htmlFirstField.margins().bottom;
      }
    }

    // Default case: two fields
    if (hasTwoFields) {
      // Distribute available size to the two fields according to the splitter position ratio
      let firstFieldSize = new Dimension(availableSizeForFields);
      let secondFieldSize = new Dimension(availableSizeForFields);
      this.computeInnerFieldsDimensions(splitXAxis, firstFieldSize, secondFieldSize, splitterPosition);

      // Calculate and set bounds (splitter and second field have to be moved)
      firstFieldBounds = new Rectangle(0, 0, firstFieldSize.width, firstFieldSize.height);
      let secondFieldBounds = new Rectangle(0, 0, secondFieldSize.width, secondFieldSize.height);
      if (splitXAxis) { // "|"
        $splitter.cssLeft(firstFieldBounds.width);
        secondFieldBounds.x = firstFieldBounds.width + htmlFirstField.margins().right;
      } else { // "--"
        $splitter.cssTop(firstFieldBounds.height);
        secondFieldBounds.y = firstFieldBounds.height + htmlFirstField.margins().bottom;
      }
      htmlFirstField.setBounds(firstFieldBounds);
      htmlSecondField.setBounds(secondFieldBounds);
    } else {
      // Special case: only one field (or none at all)
      if (hasOneField) {
        let singleField = hasFirstField ? htmlFirstField : htmlSecondField,
          singleFieldSize = availableSize.subtract(singleField.margins());
        singleField.setBounds(new Rectangle(0, 0, singleFieldSize.width, singleFieldSize.height));
      }
      $splitter.addClass('hidden');
    }

    // Calculate collapse button position
    let collapseHandle = this.splitBox.collapseHandle;
    if (collapseHandle) {
      let $collapseHandle = collapseHandle.$container;

      // Show collapse handle, if split box has two fields which are visible (one field may be collapsed)
      let collapseHandleVisible = this.splitBox.firstField && this.splitBox.firstField.visible && this.splitBox.secondField && this.splitBox.secondField.visible;
      $collapseHandle.setVisible(collapseHandleVisible);

      let x = null;
      if (hasTwoFields) {
        // - if 1st field is collapsible -> align button on the right side of the field (there is not enough space on the left side)
        // - if 2nd field is collapsible -> button is always aligned on the right side using CSS
        if (this.splitBox.collapsibleField === this.splitBox.firstField) {
          let collapseHandleSize = graphics.size($collapseHandle);
          x = firstFieldBounds.width - collapseHandleSize.width;
        }
      }
      $collapseHandle.cssLeft(x);
    }
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    // Extract components
    let htmlContainer = HtmlComponent.get($container), // = split-area
      $fields = $container.children('.form-field'),
      htmlFirstField = HtmlComponent.optGet($fields.eq(0)),
      htmlSecondField = HtmlComponent.optGet($fields.eq(1));

    let splitXAxis = this.splitBox.splitHorizontal;
    let splitterPosition = this.splitBox.getEffectiveSplitterPosition();

    // compute width hints
    let firstFieldOptions = $.extend({}, options);
    let secondFieldOptions = $.extend({}, options);

    if (options.widthHint) {
      let firstFieldSizeHint = new Dimension(options.widthHint, 0);
      let secondFieldSizeHint = new Dimension(options.widthHint, 0);
      this.computeInnerFieldsDimensions(splitXAxis, firstFieldSizeHint, secondFieldSizeHint, splitterPosition);

      firstFieldOptions.widthHint = firstFieldSizeHint.width;
      secondFieldOptions.widthHint = secondFieldSizeHint.width;
    }

    // Get preferred size of fields
    let firstFieldSize = new Dimension(0, 0);
    if (htmlFirstField) {
      firstFieldSize = htmlFirstField.prefSize(firstFieldOptions)
        .add(htmlFirstField.margins());
    }
    let secondFieldSize = new Dimension(0, 0);
    if (htmlSecondField) {
      secondFieldSize = htmlSecondField.prefSize(secondFieldOptions)
        .add(htmlSecondField.margins());
    }

    // Calculate prefSize
    let prefSize: Dimension;
    if (splitXAxis) { // "|"
      prefSize = new Dimension(
        firstFieldSize.width + secondFieldSize.width,
        Math.max(firstFieldSize.height, secondFieldSize.height)
      );
    } else { // "--"
      prefSize = new Dimension(
        Math.max(firstFieldSize.width, secondFieldSize.width),
        firstFieldSize.height + secondFieldSize.height
      );
    }
    prefSize = prefSize.add(htmlContainer.insets());

    return prefSize;
  }

  /**
   * Distributes the available size according to the split axis and the splitter position
   *
   * @param splitXAxis truthy if the splitter splits vertical |, falsy if the splitter splits horizontal --
   * @param firstFieldSize initialize with the total available space. Will be adjusted to the available size of the first field.
   * @param secondFieldSize initialize with the total available space. Will be adjusted to the available size of the second field.
   * @param splitterPosition effective splitter position
   */
  computeInnerFieldsDimensions(splitXAxis: boolean, firstFieldSize: Dimension, secondFieldSize: Dimension, splitterPosition: number) {
    if (splitXAxis) { // "|"
      if (this.splitBox.splitterPositionType === SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) {
        // Relative first
        firstFieldSize.width = Math.floor(firstFieldSize.width * splitterPosition);
        secondFieldSize.width -= firstFieldSize.width;
      } else if (this.splitBox.splitterPositionType === SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) {
        // Relative second
        secondFieldSize.width = Math.floor(secondFieldSize.width * splitterPosition);
        firstFieldSize.width -= secondFieldSize.width;
      } else {
        // Absolute
        splitterPosition = Math.min(splitterPosition, firstFieldSize.width);
        if (this.splitBox.splitterPositionType === SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          firstFieldSize.width = firstFieldSize.width - splitterPosition;
          secondFieldSize.width = splitterPosition;
        } else {
          firstFieldSize.width = splitterPosition;
          secondFieldSize.width = secondFieldSize.width - splitterPosition;
        }
      }
    } else { // "--"
      if (this.splitBox.splitterPositionType === SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) {
        // Relative first
        firstFieldSize.height = Math.floor(firstFieldSize.height * splitterPosition);
        secondFieldSize.height -= firstFieldSize.height;
      } else if (this.splitBox.splitterPositionType === SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) {
        // Relative second
        secondFieldSize.height = Math.floor(secondFieldSize.height * splitterPosition);
        firstFieldSize.height -= secondFieldSize.height;
      } else {
        // Absolute
        splitterPosition = Math.min(splitterPosition, firstFieldSize.height);
        if (this.splitBox.splitterPositionType === SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          firstFieldSize.height = firstFieldSize.height - splitterPosition;
          secondFieldSize.height = splitterPosition;
        } else {
          firstFieldSize.height = splitterPosition;
          secondFieldSize.height = secondFieldSize.height - splitterPosition;
        }
      }
    }
  }
}
