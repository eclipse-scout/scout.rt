/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.eclipse.scout.rt.dataformat.ical.model.ICalVCardHelper;
import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.dataformat.ical.model.PropertyParameter;
import org.eclipse.scout.rt.dataformat.vcard.VCardProperties;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Reader used for reading ical/vcard files
 */
public class UnfoldingReader extends BufferedReader {

  private String m_charsetName;

  public UnfoldingReader(Reader in, String charsetName) {
    super(in);
    m_charsetName = charsetName;
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  /**
   * See {@link String#indexOf(String)}. The only difference is that occurrences within double quotes do not count. If
   * there are an odd number of double quotes, all occurrences after the last double quote do not count (i.e. the last
   * double quote is considered an opening double quote).
   *
   * @return See {@link String#indexOf(String)}
   */
  private int indexOfNonQuoted(String s, String substring) {
    int index = s.indexOf(substring);
    while (index >= 0) {
      if (s.substring(0, index).replaceAll("[^\"]", "").length() % 2 == 0) {
        // odd number of double quotes before substring => substring is not within double quotes
        break;
      }
      index = s.indexOf(substring, index + 1);
    }
    return index;
  }

  public Property readProperty() throws IOException {
    String s;
    // Skip empty lines
    do {
      s = readLine();
    }
    while (s != null && !StringUtility.hasText(s));

    if (s == null) {
      // eof
      return null;
    }

    /*====================================
     * SEPARATE PARAMETER LIST FROM VALUE *
     =====================================*/

    /*
     * NOTE: Use indexOfNonQuoted(String string, String substring) to find the separators for parameters,
     * parameter values and the end of the parameter list!
     *
     * According to RFC-2425/5545 the following contentline is correct and contains the parameters TYPE with
     * two values, BAR with three values, 42 with value hello\ and the last parameter, world, has no values:
     *
     * FOO;TYPE=ä&ö,§.<;BAR=,"hello: world; bye, world!",-;42=hello\;world:Fold
     *  ing after 75 octets (bytes) could also occur in the middle of the paramete
     *  r list. The property value can contain any textual characters, but dependi
     *  ng on the definition of the specific property further restrictions may app
     *  ly
     */
    int indexOfColon = indexOfNonQuoted(s, ":");

    // if end of parameter list is not found on first line, continue search on further (folded) lines
    if (indexOfColon < 0) {
      // name not found
      mark(1024);
      String tmp = readLine();
      StringBuilder sb = new StringBuilder();
      sb.append(s);
      while (tmp != null && tmp.matches("^\\s.*")) {
        sb.append(tmp.replaceAll("^\\s", ""));
        mark(1024);
        if (indexOfNonQuoted(sb.toString(), ":") >= 0) {
          break;
        }
        tmp = readLine();
      }
      s = sb.toString();
      reset();
      if (!StringUtility.hasText(s)) {
        // the file parsing will stop here
        return null;
      }
      else {
        // get the position of the colon in the unfolded content line
        indexOfColon = indexOfNonQuoted(s, ":");
        if (indexOfColon < 0) {
          // unable to determine a folded property
          // the file parsing will stop here
          return null;
        }
      }
    }

    String parametersPart = s.substring(0, indexOfColon);

    // find the parameters
    List<String> parameterList = new ArrayList<>();
    int relativeIndexOfNextSemicolon = indexOfNonQuoted(parametersPart, ";");
    int absoluteIndexOfNextSemicolon;
    int nextParamStart = 0;
    while (relativeIndexOfNextSemicolon >= 0) {
      absoluteIndexOfNextSemicolon = nextParamStart + relativeIndexOfNextSemicolon;
      parameterList.add(parametersPart.substring(nextParamStart, absoluteIndexOfNextSemicolon));
      nextParamStart = absoluteIndexOfNextSemicolon + 1;
      relativeIndexOfNextSemicolon = indexOfNonQuoted(parametersPart.substring(nextParamStart), ";");
    }
    parameterList.add(parametersPart.substring(nextParamStart));

    Property property = parseParameters(parameterList.toArray(new String[0]));

    /*====================================
     * READ VALUE *
     =====================================*/

    StringBuilder value = new StringBuilder();
    String tmp = s.substring(indexOfColon + 1);

    // unfold the value
    if (property.hasParameter(PropertyParameter.ENCODING_QUOTED_PRINTABLE)) {
      if (tmp != null && tmp.matches("^.*=$")) {
        // unfold according to quoted-printable folding scheme
        value.append(tmp, 0, tmp.length() - 1);
        tmp = readLine();
        while (tmp != null && tmp.matches("^.*=$")) {
          value.append(tmp, 0, tmp.length() - 1);
          tmp = readLine();
        }
        // append last line (not containing a '=' at the end)
      }
      value.append(tmp);
    }
    else {
      // normally unfold the value
      value.append(tmp);
      mark(1024);
      tmp = readLine();
      while (tmp != null && tmp.matches("^\\s.*")) {
        value.append(tmp.replaceAll("^\\s", ""));
        mark(1024);
        tmp = readLine();
      }
      reset();
    }

    // change charset according to charset parameter (if available and supported)
    if (value.length() > 0) {
      PropertyParameter charsetParameter = property.getParameter(PropertyParameter.PARAM_CHARSET);
      if (charsetParameter != null && charsetParameter.getValue() != null
          && BEANS.get(ICalVCardHelper.class).isCharacterSetSupported(charsetParameter.getValue())) {
        value = new StringBuilder(new String(value.toString().getBytes(m_charsetName), charsetParameter.getValue()));
      }
    }

    // decode the value
    if (PropertyParameter.ENCODING_BASE64.equals(property.getParameter(PropertyParameter.PARAM_ENCODING)) && value.length() > 0 && !"PHOTO".equals(property.getName())) {
      property.setValue(new String(Base64Utility.decode(value.toString())));
    }
    else if (property.hasParameter(PropertyParameter.ENCODING_QUOTED_PRINTABLE)) {
      try {
        property.setValue(new String(QuotedPrintableCodec.decodeQuotedPrintable(value.toString().getBytes(StandardCharsets.UTF_8))));
      }
      catch (DecoderException e) {
        throw new IOException(e);
      }
    }
    else {
      property.setValue(value.toString());
    }

    return property;
  }

  /**
   * @param parameters
   *          with parameters[0] being the name of the property
   * @return property object containing the property name, property parameters and their values
   */
  private Property parseParameters(String[] parameters) {
    // consider the optional group part for vCards => parameters[0] could be "FOO.BAR" => the name is only "BAR"
    Property property = new Property(parameters[0].replaceAll("^[^.]*\\.", ""));

    for (int i = 1; i < parameters.length; i++) {
      String name = null;
      HashSet<String> values = new HashSet<>();
      int indexOfEqualSign = parameters[i].indexOf('=');
      if (indexOfEqualSign >= 0) {
        name = parameters[i].substring(0, indexOfEqualSign);
        if (parameters[i].length() > indexOfEqualSign) {
          // parse parameter values (first parse all values within double quotes, then the rest)
          String valuesPart = parameters[i].substring(indexOfEqualSign + 1);
          Matcher m = Pattern.compile("(\"[^\"]*\")").matcher(valuesPart);
          int start = 0;
          int end = 0;
          int sofar = 0;
          StringBuilder rest = new StringBuilder();
          while (m.find(end)) {
            start = m.start();
            end = m.end();
            if (sofar < start) {
              rest.append(valuesPart, sofar, start);
            }
            sofar = end;
            values.add(valuesPart.substring(start + 1, end - 1));
          }
          if (sofar < valuesPart.length()) {
            rest.append(valuesPart.substring(sofar));
          }
          // since rest does not contain any values within double quotes, we can simply use the split method
          for (String v : rest.toString().split(",")) {
            if (StringUtility.hasText(v)) {
              values.add(v);
            }
          }
        }
      }
      else {
        name = parameters[i];
      }

      // add parameter to the property
      if ("B".equalsIgnoreCase(name)) {
        property.addParameters(PropertyParameter.ENCODING_BASE64);
      }
      else if (VCardProperties.PARAM_NAME_TYPE.equalsIgnoreCase(name)) {
        // add values for parameter TYPE directly as property parameters for easier accessibility
        for (String v : values.toArray(new String[0])) {
          // the parameter values of the parameter "TYPE" are case-insensitive for all properties defined in RFC-2426
          v = v.toUpperCase();
          switch (v) {
            case VCardProperties.PARAM_VALUE_CAR:
              property.addParameters(VCardProperties.PARAM_CAR);
              break;
            case VCardProperties.PARAM_VALUE_CELL:
              property.addParameters(VCardProperties.PARAM_CELL);
              break;
            case VCardProperties.PARAM_VALUE_FAX:
              property.addParameters(VCardProperties.PARAM_FAX);
              break;
            case VCardProperties.PARAM_VALUE_HOME:
              property.addParameters(VCardProperties.PARAM_HOME);
              break;
            case VCardProperties.PARAM_VALUE_INTERNET:
              property.addParameters(VCardProperties.PARAM_INTERNET);
              break;
            case VCardProperties.PARAM_VALUE_VOICE:
              property.addParameters(VCardProperties.PARAM_VOICE);
              break;
            case VCardProperties.PARAM_VALUE_WORK:
              property.addParameters(VCardProperties.PARAM_WORK);
              break;
          }
        }
        property.addParameters(new PropertyParameter(VCardProperties.PARAM_NAME_TYPE, values.isEmpty() ? null : values.toArray(new String[0])));
      }
      else {
        PropertyParameter p = new PropertyParameter(name, values.isEmpty() ? null : values.toArray(new String[0]));
        property.addParameters(p);
      }
    }

    return property;
  }
}
