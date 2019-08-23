/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataformat.vcard;

import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.dataformat.ical.model.PropertyParameter;

public final class VCardProperties {

  public static final String PROP_NAME_N = "N";
  public static final String PROP_NAME_FN = "FN";
  public static final String PROP_NAME_PRODID = "PRODID";
  public static final String PROP_NAME_ORG = "ORG";
  public static final String PROP_NAME_TEL = "TEL";
  public static final String PROP_NAME_ADR = "ADR";
  public static final String PROP_NAME_BDAY = "BDAY";
  public static final String PROP_NAME_EMAIL = "EMAIL";
  public static final String PROP_NAME_TITLE = "TITLE";
  public static final String PROP_NAME_ROLE = "ROLE";
  public static final String PROP_NAME_PHOTO = "PHOTO";
  public static final String PROP_NAME_NOTE = "NOTE";
  public static final String PROP_NAME_URL = "URL";
  public static final String PROP_NAME_VERSION = "VERSION";
  public static final String PROP_NAME_REV = "REV";
  public static final String PROP_NAME_BEGIN = "BEGIN";
  public static final String PROP_NAME_END = "END";

  public static final String PROP_VALUE_VCARD = "VCARD";
  public static final String PROP_VALUE_VERSION_2_1 = "2.1";
  public static final String PROP_VALUE_VERSION_3_0 = "3.0";
  public static final String PROP_VALUE_VERSION_4_0 = "4.0";

  public static final Property PROP_BEGIN_VCARD = new Property(PROP_NAME_BEGIN, PROP_VALUE_VCARD);
  public static final Property PROP_END_VCARD = new Property(PROP_NAME_END, PROP_VALUE_VCARD);
  public static final Property PROP_VERSION_2_1 = new Property(PROP_NAME_VERSION, PROP_VALUE_VERSION_2_1);
  public static final Property PROP_VERSION_3_0 = new Property(PROP_NAME_VERSION, PROP_VALUE_VERSION_3_0);
  public static final Property PROP_VERSION_4_0 = new Property(PROP_NAME_VERSION, PROP_VALUE_VERSION_4_0);

  public static final String PARAM_NAME_TYPE = "TYPE";

  public static final String PARAM_VALUE_WORK = "WORK";
  public static final String PARAM_VALUE_FAX = "FAX";
  public static final String PARAM_VALUE_CELL = "CELL";
  public static final String PARAM_VALUE_CAR = "CAR";
  public static final String PARAM_VALUE_VOICE = "VOICE";
  public static final String PARAM_VALUE_HOME = "HOME";
  public static final String PARAM_VALUE_INTERNET = "INTERNET";

  public static final PropertyParameter PARAM_WORK = new PropertyParameter(PARAM_VALUE_WORK);
  public static final PropertyParameter PARAM_FAX = new PropertyParameter(PARAM_VALUE_FAX);
  public static final PropertyParameter PARAM_CELL = new PropertyParameter(PARAM_VALUE_CELL);
  public static final PropertyParameter PARAM_CAR = new PropertyParameter(PARAM_VALUE_CAR);
  public static final PropertyParameter PARAM_VOICE = new PropertyParameter(PARAM_VALUE_VOICE);
  public static final PropertyParameter PARAM_HOME = new PropertyParameter(PARAM_VALUE_HOME);
  public static final PropertyParameter PARAM_INTERNET = new PropertyParameter(PARAM_VALUE_INTERNET);

  private VCardProperties() {
  }
}
