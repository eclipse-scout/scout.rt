/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.xmlparser;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Soap round trip as JUnit test, using {@link ScoutXmlDocument}, {@link ScoutXmlParser} and {@link ScoutXmlElement}
 */
public class ScoutXmlSoapTest {

  @Test
  public void testName() throws Exception {
    String a = "Hello World";
    byte[] msg = send(a);
    String b = receive(msg);
    assertEquals("Hello World", b);
  }

  private byte[] send(String text) throws Exception {
    ScoutXmlDocument doc = new ScoutXmlDocument();
    doc.setXmlVersion("1.0");
    doc.setXmlEncoding("UTF-8");
    ScoutXmlElement env = doc.setRoot("");
    env.setNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
    env.setName("SOAP-ENV:Envelope");
    env.setAttribute("SOAP-ENV:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
    ScoutXmlElement body = env.addChild("SOAP-ENV:Body");
    ScoutXmlElement dataPart = body.addChild("data");
    dataPart.addContent(text);
    ScoutXmlElement infoPart = body.addChild("info");
    infoPart.addContent("For maximal performance, data is reduced, compressed, signed and base64 encoded.");
    //
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    doc.write(out);
    out.close();
    return out.toByteArray();
  }

  private String/* text */receive(byte[] msg) throws IOException, SAXException {
    ScoutXmlDocument doc = new ScoutXmlParser().parse(new ByteArrayInputStream(msg));
    ScoutXmlElement env = doc.getChild("{http://schemas.xmlsoap.org/soap/envelope/}Envelope");
    ScoutXmlElement body = env.getChild("{http://schemas.xmlsoap.org/soap/envelope/}Body");
    ScoutXmlElement dataPart = body.getChild("data");
    String text = dataPart.getText();
    ScoutXmlElement infoPart = body.getChild("info");
    String info = infoPart.getText();
    System.out.println("Info: " + info);
    return text;
  }
}
