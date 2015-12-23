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
scout.NumberField = function() {
  scout.NumberField.parent.call(this);
};
scout.inherits(scout.NumberField, scout.BasicField);

scout.NumberField.prototype._init = function(model) {
  scout.NumberField.parent.prototype._init.call(this, model);
  if (!(this.decimalFormat instanceof scout.DecimalFormat)) {
    this.decimalFormat = new scout.DecimalFormat(this.session.locale, this.decimalFormat);
  }
};

/**
 * @override Widget.js
 */
scout.NumberField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.NumberField.prototype._render = function($parent) {
  this.addContainer($parent, 'number-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(
    scout.fields.makeTextField($parent)
    .blur(this._parse.bind(this))
    .blur(this._onFieldBlur.bind(this)));
  this.addStatus();
};

scout.NumberField.prototype._renderGridData = function() {
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};

scout.NumberField.prototype._syncDecimalFormat = function(decimalFormat) {
  if (decimalFormat instanceof scout.DecimalFormat) {
    this.decimalFormat = decimalFormat;
  } else {
    this.decimalFormat = new scout.DecimalFormat(this.session.locale, decimalFormat);
  }
};

scout.NumberField.prototype._parse = function() {
  var input = this.$field.val();
  if (input) {
    //check if valid thousends
    var thousends = input.match(new RegExp('\\d*[' + this.decimalFormat.groupingChar + ']\\d*', 'g'));
    if(thousends){
      for(var i = 0; i < thousends.length; i++){
        var parts = thousends[i].split(this.decimalFormat.groupingChar);
        for(var j = 0 ; j<parts.length;j++){
          if((j===0 && parts[j].length>3 && parts[j].length<0) || (j!==0 && parts[j].length!==3)){
            return;
          }
        }
      }
    }

    input = input
      .replace(new RegExp('[' + this.decimalFormat.groupingChar + ']', 'g'), '')
      .replace(new RegExp('[' + this.decimalFormat.decimalSeparatorChar + ']', 'g'), '.')
      .replace(/\s/g, '');

    // if only math symbols are in the input string...
    if (input.match(/^[\d\(\)\+\-\*\/\.]+$/)) {
      // Remove leading zeros from numbers to prevent interpretation as octal value
      input = input.replace(/(^|[^\d])0+(\d+)/g, '$1$2');
      // ...evaluate, reformat the result and set is to the field. If the display text
      // changed, ValueField.js will make sure, the new value is sent to the model.
      try {
        input = eval(input);
        input = this.decimalFormat.format(input);
        this.$field.val(input);
      } catch (err) {
        // ignore errors, let the input be handled by scout model
      }
    }
  }
};
