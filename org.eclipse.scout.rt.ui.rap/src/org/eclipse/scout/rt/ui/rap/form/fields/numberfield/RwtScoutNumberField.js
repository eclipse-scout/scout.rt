var handleEvent = function(event) {
  // keys as defined in org.eclipse.scout.rt.ui.rap.form.fields.numberfield.RwtScoutNumberField
  var maxInt = event.widget.getData('RwtScoutNumberField.maxInt');
  var maxFrac = event.widget.getData('RwtScoutNumberField.maxFra');
  var zeroDigit = event.widget.getData('RwtScoutNumberField.zeroDig');
  var decimalSeparator = event.widget.getData('RwtScoutNumberField.decSep');
  event.doit = isWithinNumberFormatLimits(maxInt, maxFrac, zeroDigit, decimalSeparator, event.widget.getText(), event.start, event.end - event.start, event.text);
};

function isWithinNumberFormatLimits(maxInt, maxFrac, zeroDigit, decimalSeparator, curText, offset, replaceLen, insertText) {
  // !! IMPORTANT NOTE: There is also a Java implementation of this method: org.eclipse.scout.commons.StringUtility.isWithinNumberFormatLimits
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
