var handleEvent = function(event) {
  // keys as defined in org.eclipse.scout.rt.ui.rap.form.fields.numberfield.RwtScoutNumberField
  var maxInt = event.widget.getData('RwtScoutNumberField.maxInt');
  var maxFrac = event.widget.getData('RwtScoutNumberField.maxFra');
  var zeroDigit = event.widget.getData('RwtScoutNumberField.zeroDig');
  var decimalSeparator = event.widget.getData('RwtScoutNumberField.decSep');
  event.doit = isWithinNumberFormatLimits(maxInt, maxFrac, zeroDigit, decimalSeparator, event.widget.getText(), event.start, event.end - event.start, event.text);
  if (!event.doit && textWasPasted(event)) {
    try {
        var newText = createNumberWithinFormatLimits(maxInt, maxFrac, zeroDigit, decimalSeparator, event.widget.getText(), event.start, event.end - event.start, event.text);
        if (event.widget.getText() != newText) {
          event.widget.setText(newText);
        }
    } catch (err) {
      event.doit = true; //Continue here and handle the error in RwtScoutNumberField.java#P_verifyListener
    }
  }
};

function textWasPasted(event) {
  return event.text.length > 1;
}

function isWithinNumberFormatLimits(maxInt, maxFrac, zeroDigit, decimalSeparator, curText, offset, replaceLen, insertText) {
  // !! IMPORTANT NOTE: There is also a Java implementation of this method: org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField.isWithinNumberFormatLimits
  // When changing this implementation also consider updating the Java version!

  if (insertText == null || insertText.length < 1) {
    return true;
  }

  var futureText = null;
  if (curText == null) {
    futureText = insertText;
  } else {
    futureText = curText.substring(0, offset) + insertText + curText.substring(offset + replaceLen);
  }

  var pat = new RegExp('[^1-9'+zeroDigit+']', 'g');
  var parts = futureText.split(decimalSeparator);
  if (parts.length >= 1) {
    var intPartDigits = parts[0].replace(pat, '');
    var intPartValid = intPartDigits == null || intPartDigits.length <= maxInt;
    if (!intPartValid) {
      return false;
    }
  }
  if (parts.length == 2) {
    var fracPartDigits = parts[1].replace(pat, '');
    var fracPartValid = fracPartDigits == null || fracPartDigits.length <= maxFrac;
    if (!fracPartValid) {
      return false;
    }
  }

  return true;
}

function createNumberWithinFormatLimits(maxInt, maxFrac, zeroDigit, decimalSeparator, curText, offset, replaceLen, insertText) {
    // !! IMPORTANT NOTE: There is also a Java implementation of this method: org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField.createNumberWithinFormatLimits
    // When changing this implementation also consider updating the Java version!

    if (insertText == null || insertText.length < 1) {
      return "";
    }

    var result;

    var futureText = null;
    if (curText == null) {
      futureText = insertText;
    } else {
      futureText = curText.substring(0, offset) + insertText + curText.substring(offset + replaceLen);
    }

    var pat = new RegExp('[^1-9'+zeroDigit+']', 'g');
    var parts = futureText.split(decimalSeparator);
    if (parts.length >= 1) {
      var intPartDigits = parts[0].replace(pat, '');
      var intPartValid = intPartDigits == null || intPartDigits.length <= maxInt;
      if (intPartValid) {
        result = intPartDigits;
      } else {
        throw "do not truncate here!";
      }
    }
    if (parts.length == 2) {
      var fracPartDigits = parts[1].replace(pat, '');
      var fracPartValid = fracPartDigits == null || fracPartDigits.length <= maxFrac;
      if (fracPartValid) {
        result += decimalSeparator + fracPartDigits;
      } else {
        result += decimalSeparator + fracPartDigits.substring(0, maxFrac);
      }
    }

    return result;
  }
