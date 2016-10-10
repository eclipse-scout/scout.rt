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
scout.Code = function() {
  this.id;
};

scout.Code.prototype.init = function(model) {
  scout.assertParameter('id', model.id);
  this.id = model.id;
  this._text = model.text;
  this.modelClass = model.modelClass;

  // If model contains a text map, generate a text key and add the texts to the text maps of the session
  if (model.texts) {
    if (this._text) {
      throw new Error('Either set texts or text property, not both.');
    }
    var key = scout.codes.registerTexts(this, model.texts);
    // Convert to ${textKey:key} so that text() may resolve it
    this._text = scout.texts.buildKey(key);
  }
};

/**
 * @param vararg the language tag or the locale (object with a property languageTag) to load the text for.
 */
scout.Code.prototype.text = function(vararg) {
  var languageTag = vararg;
  if (typeof vararg === 'object') {
    languageTag = vararg.languageTag;
  }
  return scout.texts.resolveText(this._text, languageTag);
};

