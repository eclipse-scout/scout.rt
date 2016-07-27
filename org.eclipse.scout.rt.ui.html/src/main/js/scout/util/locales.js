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
scout.locales = {

  localesMap: {},

  bootstrap: function() {
    var that = this;

    return $.ajax({
        url: 'res/locales.json',
        dataType: 'json',
        contentType: 'application/json; charset=UTF-8'
      }).done(that._onLoadDone.bind(that))
      .fail(that._onLoadFail.bind(that));
  },

  _onLoadDone: function(data) {
    data.forEach(function(locale) {
      this.localesMap[locale.languageTag] = locale;
    }, this);
  },

  _onLoadFail: function(jqXHR, textStatus, errorThrown) {
    throw new Error('Error while loading locales: ' + errorThrown);
  },

  get: function(languageTag) {
    return this.localesMap[languageTag];
  }

};
