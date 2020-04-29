/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';

/**
 * This class is used to reset and restore styles in the DOM, so we can measure the preferred size of the table.
 */
export default class TableLayoutResetter {

  constructor(table) {
    this._table = table;
    this._fillerWidth = null;
    this.cssSelector = '.table';
  }

  /**
   * Modifies the table in a way that the preferred width may be read.
   * Removes explicit widths on rows, cells, fillers and sets display to inline-block.
   */
  modifyDom() {
    this._table.$container
      .css('display', 'inline-block')
      .css('width', 'auto')
      .css('height', 'auto');
    this._table.$data
      .css('display', 'inline-block');

    this._modifyFiller(this._table.$fillBefore);
    this._modifyFiller(this._table.$fillAfter);
    this._modifyTableData(this._cssBackup);
  }

  restoreDom() {
    this._table.$container
      .css('display', 'block')
      .css('width', '100%')
      .css('height', '100%');
    this._table.$data
      .css('display', 'block');

    this._restoreFiller(this._table.$fillBefore);
    this._restoreFiller(this._table.$fillAfter);
    this._modifyTableData(this._cssRestore);
  }

  /**
   * Clears the given CSS property and stores the old value as data with prefix 'backup'
   * which is used to restore the CSS property later.
   */
  _cssBackup($element, property) {
    let oldValue = $element.css(property);
    $element
      .css(property, '')
      .data('backup' + property, oldValue);
  }

  _cssRestore($element, property) {
    let dataProperty = 'backup' + property,
      oldValue = $element.data(dataProperty);
    $element
      .css(property, oldValue)
      .removeData(dataProperty);
  }

  /**
   * Go through all rows and cells and call the given modifyFunc (backup/restore) on each element.
   */
  _modifyTableData(modifyFunc) {
    let that = this;
    this._table.$rows().each(function() {
      let $row = $(this);
      modifyFunc($row, 'width');
      that._table.$cellsForRow($row).each(function() {
        let $cell = $(this);
        modifyFunc($cell, 'min-width');
        modifyFunc($cell, 'max-width');
      });
    });
  }

  _modifyFiller($filler) {
    if ($filler) {
      this._fillerWidth = $filler.css('width');
      $filler.css('width', '');
    }
  }

  _restoreFiller($filler) {
    if ($filler) {
      $filler.css('width', this._fillerWidth);
    }
  }
}
