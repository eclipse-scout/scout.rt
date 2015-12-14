/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.StringColumn = function() {
  scout.StringColumn.parent.call(this);
  this.filterType = 'TextColumnUserFilter'; // FIXME AWE: (filter) rename to StringColumnUserFilter
};
scout.inherits(scout.StringColumn, scout.Column);

scout.StringColumn.collator;

/**
 * Static function creates a window.Intl.Collator instance only once for all instances of StringColumn.
 * The compare function of the Collator object is used to compare texts.
 */
scout.StringColumn.textComparator = function(session) {
  if (!scout.StringColumn.collator) {
    scout.StringColumn.collator = new window.Intl.Collator(session.locale.languageTag);
  }
  return scout.StringColumn.collator.compare;
};

/**
 * Locale comparison only possible with Collator. Otherwise sorting must be done on server.
 *
 * @override Column.js
 */
scout.StringColumn.prototype.prepareForSorting = function() {
  if (scout.device.supportsInternationalization()) {
    this.compare = scout.StringColumn.textComparator(this.session);
    return true;
  } else {
    return false;
  }
};
