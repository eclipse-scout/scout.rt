/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataformat.vcard.builder;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.dataformat.vcard.VCardBean;
import org.eclipse.scout.rt.dataformat.vcard.VCardProperties;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class VCardBuilderTest {

  @Test
  public void testVCard() {
    VCardBean vcard = BEANS.get(VCardBuilder.class)
        .withDisplayName("displayname")
        .withName("Tommy", "middlename", "Mustermann", "Dr.")
        .withEmail(VCardProperties.PARAM_VALUE_HOME, "tommy@mustermann.de")
        .withJobTitle("Software Engineer")
        .withOrganizations("Mustermann GmbH", "mustermann.de")
        .withProductIdentifier("productidentifiert")
        .withTel(VCardProperties.PARAM_VALUE_WORK, VCardProperties.PARAM_VALUE_CELL, "0776665544")
        .withAddress(VCardProperties.PARAM_VALUE_HOME, "Bahnhofstrasse", "1234", "Entenhausen", "Schweiz")
        .build();

    assertEquals("BEGIN:VCARD\r\n" +
        "VERSION:3.0\r\n" +
        "FN;CHARSET=utf-8:displayname\r\n" +
        "N;CHARSET=utf-8:Mustermann;Tommy;middlename;Dr.;\r\n" +
        "EMAIL;CHARSET=utf-8;TYPE=HOME:tommy@mustermann.de\r\n" +
        "TITLE;CHARSET=utf-8:Software Engineer\r\n" +
        "ORG;CHARSET=utf-8:Mustermann GmbH;mustermann.de\r\n" +
        "PRODID;CHARSET=utf-8:productidentifiert\r\n" +
        "TEL;CHARSET=utf-8;TYPE=WORK,CELL:0776665544\r\n" +
        "ADR;CHARSET=utf-8;TYPE=HOME:;;Bahnhofstrasse;Entenhausen;;1234;Schweiz\r\n" +
        "END:VCARD\r\n", new String(vcard.toBytes("utf-8")));
  }
}
