/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.vcard;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.eclipse.scout.rt.dataformat.ical.ICalBean;
import org.eclipse.scout.rt.dataformat.ical.model.ICalVCardHelper;
import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.dataformat.ical.model.PropertyParameter;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class VCardBeanTest {
  @Test
  public void testWrite() {
    VCardBean vcard = BEANS.get(VCardBean.class);
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    vcard.addProperty(VCardProperties.PROP_BEGIN_VCARD);
    vcard.addProperty(VCardProperties.PROP_VERSION_3_0);
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_PRODID, "PRODID"));
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_FN, "John Doe"));
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_N, helper.composeStructuredValueFromSingleValues("Doe", "John", "Hannes", "Dr.", null)));
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_TEL, Arrays.asList(new PropertyParameter(VCardProperties.PARAM_NAME_TYPE, VCardProperties.PARAM_VALUE_VOICE, VCardProperties.PARAM_VALUE_WORK)), "0998887766"));
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_TITLE, "Software Engineer"));
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_EMAIL, Arrays.asList(new PropertyParameter(VCardProperties.PARAM_NAME_TYPE, VCardProperties.PARAM_VALUE_WORK)), "john.doe@email.com"));
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_ORG, helper.composeStructuredValueFromSingleValues("Doe Corporation")));
    vcard.addProperty(new Property(VCardProperties.PROP_NAME_ADR, Arrays.asList(new PropertyParameter(VCardProperties.PARAM_NAME_TYPE, VCardProperties.PARAM_VALUE_HOME)),
        helper.composeStructuredValueFromSingleValues(null, null, "Bahnhofweg", "Zurich", null, "8000", "Switzerland")));

    vcard.addProperty(VCardProperties.PROP_END_VCARD);

    StringWriter w = new StringWriter();
    vcard.write(w, "utf-8");
    String writtenVCard = w.toString();

    assertEquals("BEGIN:VCARD\r\n" +
        "VERSION:3.0\r\n" +
        "PRODID;CHARSET=utf-8:PRODID\r\n" +
        "FN;CHARSET=utf-8:John Doe\r\n" +
        "N;CHARSET=utf-8:Doe;John;Hannes;Dr.;\r\n" +
        "TEL;CHARSET=utf-8;TYPE=VOICE,WORK:0998887766\r\n" +
        "TITLE;CHARSET=utf-8:Software Engineer\r\n" +
        "EMAIL;CHARSET=utf-8;TYPE=WORK:john.doe@email.com\r\n" +
        "ORG;CHARSET=utf-8:Doe Corporation\r\n" +
        "ADR;CHARSET=utf-8;TYPE=HOME:;;Bahnhofweg;Zurich;;8000;Switzerland\r\n" +
        "END:VCARD\r\n" +
        "", writtenVCard);
  }

  @Test
  public void testParse() {
    final String vcard = "BEGIN:VCARD\r\n" +
        "VERSION:3.0\r\n" +
        "PRODID:PRODID\r\n" +
        "FN:John Doe\r\n" +
        "N:Doe;John;Hannes;Dr.;\r\n" +
        "TEL;TYPE=VOICE,WORK:0998887766\r\n" +
        "TITLE:Software Engineer\r\n" +
        "EMAIL;TYPE=WORK:john.doe@email.com\r\n" +
        "ORG:Doe Corporation\r\n" +
        "ADR;TYPE=HOME:;;Bahnhofweg;Zurich;;8000;Switzerland\r\n" +
        "END:VCARD\r\n";

    ICalBean bean = ICalBean.parse(new StringReader(vcard), "utf-8");

    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_BEGIN));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_VERSION));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_PRODID));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_FN));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_N));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_TEL));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_TITLE));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_EMAIL));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_ORG));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_ADR));
    assertNotNull(bean.getProperty(VCardProperties.PROP_NAME_END));

    assertEquals(VCardProperties.PROP_VALUE_VCARD, bean.getProperty(VCardProperties.PROP_NAME_BEGIN).getValue());
    assertEquals(VCardProperties.PROP_VALUE_VERSION_3_0, bean.getProperty(VCardProperties.PROP_NAME_VERSION).getValue());
    assertEquals("PRODID", bean.getProperty(VCardProperties.PROP_NAME_PRODID).getValue());
    assertEquals("John Doe", bean.getProperty(VCardProperties.PROP_NAME_FN).getValue());
    assertEquals("Doe;John;Hannes;Dr.;", bean.getProperty(VCardProperties.PROP_NAME_N).getValue());
    assertEquals("0998887766", bean.getProperty(VCardProperties.PROP_NAME_TEL).getValue());
    assertTrue(bean.getProperty(VCardProperties.PROP_NAME_TEL).hasParameter(VCardProperties.PARAM_NAME_TYPE));
    assertEquals(VCardProperties.PARAM_VALUE_VOICE + "," + VCardProperties.PARAM_VALUE_WORK, bean.getProperty(VCardProperties.PROP_NAME_TEL).getParameter(VCardProperties.PARAM_NAME_TYPE).getValue());
    assertEquals("Software Engineer", bean.getProperty(VCardProperties.PROP_NAME_TITLE).getValue());
    assertEquals("john.doe@email.com", bean.getProperty(VCardProperties.PROP_NAME_EMAIL).getValue());
    assertTrue(bean.getProperty(VCardProperties.PROP_NAME_EMAIL).hasParameter(VCardProperties.PARAM_NAME_TYPE));
    assertEquals(VCardProperties.PARAM_VALUE_WORK, bean.getProperty(VCardProperties.PROP_NAME_EMAIL).getParameter(VCardProperties.PARAM_NAME_TYPE).getValue());
    assertTrue(bean.getProperty(VCardProperties.PROP_NAME_EMAIL).hasParameter(VCardProperties.PARAM_NAME_TYPE));
    assertEquals("Doe Corporation", bean.getProperty(VCardProperties.PROP_NAME_ORG).getValue());
    assertEquals(";;Bahnhofweg;Zurich;;8000;Switzerland", bean.getProperty(VCardProperties.PROP_NAME_ADR).getValue());
    assertTrue(bean.getProperty(VCardProperties.PROP_NAME_ADR).hasParameter(VCardProperties.PARAM_NAME_TYPE));
    assertEquals(VCardProperties.PARAM_VALUE_HOME, bean.getProperty(VCardProperties.PROP_NAME_ADR).getParameter(VCardProperties.PARAM_NAME_TYPE).getValue());
    assertEquals(VCardProperties.PROP_VALUE_VCARD, bean.getProperty(VCardProperties.PROP_NAME_END).getValue());
  }
}
