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
scout.Locale = function(model) {
  this.languageTag = model.languageTag;
  this.decimalFormatPatternDefault = model.decimalFormatPatternDefault;
  this.decimalFormatSymbols = model.decimalFormatSymbols;
  this.timeFormatPatternDefault = model.timeFormatPatternDefault;

  if (this.decimalFormatPatternDefault && this.decimalFormatSymbols) {
    this.decimalFormat = new scout.DecimalFormat(model);
  }

  this.dateFormatPatternDefault = model.dateFormatPatternDefault;
  this.dateFormatSymbols = model.dateFormatSymbols;

  if (this.dateFormatPatternDefault && this.dateFormatSymbols) {
    this.dateFormat = new scout.DateFormat(model);
  }
};
