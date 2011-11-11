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
package org.eclipse.scout.rt.shared.servicetunnel;

import java.security.Principal;
import java.util.ArrayList;

import javax.security.auth.Subject;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.validation.SchemaFactory;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 *
 */
public final class SoapHandlingUtility {
  private SoapHandlingUtility() {
  }

  public static final String XMLNS_SOAPENV = SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE;
  public static final String XMLNS_WSSE = "http://schemas.xmlsoap.org/ws/2002/04/secext";
  public static final QName SOAPENV_ENVELOPE_ELEMENT = new QName(XMLNS_SOAPENV, "Envelope");
  public static final QName SOAPENV_HEADER_ELEMENT = new QName(XMLNS_SOAPENV, "Header");
  public static final QName WSSE_SECURITY_ELEMENT = new QName(XMLNS_WSSE, "Security");
  public static final QName WSSE_USERNAME_TOKEN_ELEMENT = new QName(XMLNS_WSSE, "UsernameToken");
  public static final QName WSSE_USERNAME_ELEMENT = new QName(XMLNS_WSSE, "Username");
  public static final QName WSSE_PASSWORD_ELEMENT = new QName(XMLNS_WSSE, "Password");

  public static final String WSSE_PASSWORD_TYPE_ATTRIBUTE = "Type";

  public static final String DEFAULT_WSSE_USERNAME_TOKEN = "" +
      "<wsse:Security soapenv:mustUnderstand=\"1\">" +
      "  <wsse:UsernameToken>" +
      "    <wsse:Username>${username}</wsse:Username>" +
      "    <wsse:Password Type=\"http://scout.eclipse.org/security#Token\">${password}</wsse:Password>" +
      "  </wsse:UsernameToken>" +
      "</wsse:Security>";

  /**
   * Create a WS-Security username token containing the first principal {@link Principal#getName()} as username and the
   * second principal as password
   * <p>
   */
  public static String createDefaultWsSecurityElement(ServiceTunnelRequest req) {
    Subject subject = req.getClientSubject();
    if (subject == null || subject.getPrincipals().size() == 0) {
      return null;
    }
    ArrayList<Principal> list = new ArrayList<Principal>(subject.getPrincipals());
    String user = (list.size() > 0 ? list.get(0).getName() : "");
    String pass = (list.size() > 1 ? list.get(1).getName() : "");
    String wsse = DEFAULT_WSSE_USERNAME_TOKEN.replace("${username}", user).replace("${password}", pass);
    return wsse;
  }

  public static SAXParser createSaxParser(SAXParserFactory factory) throws ProcessingException {
    try {
      return factory.newSAXParser();
    }
    catch (Exception e) {
      throw new ProcessingException("building sax parser", e);
    }
  }

  public static SAXParserFactory createSaxParserFactory() throws ProcessingException {
    try {
      // Obtain an instance of a SchemaFactory for W3C Schemas
      SchemaFactory schemaFact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      // Obtain a new instance of a SAXParserFactory.
      SAXParserFactory factory = SAXParserFactory.newInstance();

      // Specifies that the parser produced by this code will provide
      // support for XML namespaces.
      factory.setNamespaceAware(true);

      // disables the XInclude feature
      factory.setXIncludeAware(false);

      // sets the schema validator
      factory.setSchema(schemaFact.newSchema());

      // Specifies that the parser produced by this code will validate
      // documents as they are parsed.
      factory.setValidating(false);

      //disable xml injection
      try {
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      }
      catch (Throwable t) {
        //nop
      }
      try {
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      }
      catch (Throwable t) {
        //nop
      }
      try {
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      }
      catch (Throwable t) {
        //nop
      }
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      return factory;
    }
    catch (Exception e) {
      throw new ProcessingException("building sax parser", e);
    }
  }

}
