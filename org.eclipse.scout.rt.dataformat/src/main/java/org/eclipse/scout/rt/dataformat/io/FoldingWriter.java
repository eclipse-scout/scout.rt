/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataformat.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.dataformat.ical.model.PropertyParameter;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Writer used for writing ical/vcard files
 */
public class FoldingWriter extends BufferedWriter {

  public FoldingWriter(Writer out) {
    super(out);
  }

  public void write(Property property) throws IOException {
    if (property == null || property.getName() == null || property.getName().isEmpty()
        || property.getValue() == null || property.getValue().isEmpty()) {
      return;
    }

    append(property.getName());
    for (PropertyParameter parameter : property.getParameters()) {
      append(";");
      append(parameter.getName());
      if (parameter.getValue() != null) {
        append("=");
        append(parameter.getValue());
      }
    }
    append(":");

    /***************
     * WRITE VALUE *
     ***************/

    String value = property.getValue();

    // newlines need to be escaped
    value = value.replace("\r\n", "\\n");
    value = value.replace("\n", "\\n");

    int maxLength = 74;
    if (StringUtility.length(value) < maxLength
        && !property.hasParameter(PropertyParameter.ENCODING_BASE64)) {
      append(value);
    }
    else {
      // do line folding (for further infos see RFC2445 4.1)
      int len = value.length();
      int start = 0;
      int end = (int) NumberUtility.min(maxLength, len);
      while (start < len) {
        // content lines must end with [CR][LF]
        append("\r\n ");
        append(value.substring(start, end));
        start = end;
        end = (int) NumberUtility.min(start + maxLength, len);
      }
    }
    append("\r\n");
  }
}
