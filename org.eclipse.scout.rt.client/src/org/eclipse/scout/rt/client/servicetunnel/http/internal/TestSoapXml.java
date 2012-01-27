/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.servicetunnel.http.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlParser;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.xml.sax.SAXException;

public class TestSoapXml {

  public void run() throws Exception {
    String a = "Hello World";
    System.out.println("a:\n" + a);
    byte[] msg = send(a);
    System.out.println("msg:\n" + new String(msg, "UTF-8"));
    String b = receive(msg);
    System.out.println("b:\n" + b);
  }

  public byte[] send(String text) throws Exception {
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

  public String/* text */receive(byte[] msg) throws IOException, SAXException {
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

  public static void main(String[] args) throws Exception {
    new TestSoapXml().run();
  }
}
