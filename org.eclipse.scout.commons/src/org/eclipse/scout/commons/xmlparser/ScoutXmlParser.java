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
package org.eclipse.scout.commons.xmlparser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Title : Scout XML Parser Description: Copyright : Copyright (c) 2006 BSI AG,
 * ETH Zürich, Stefan Vogt Company : BSI AG www.bsiag.com
 * 
 * @version 1.0
 */
public class ScoutXmlParser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutXmlParser.class);
  private static boolean warnedDefaultParserNotFound;

  private static final boolean DEFAULT_IGNORE_EXTERNAL_ENTITIES = false;
  private static final boolean DEFAULT_IGNORE_SAX_ERRORS = false;
  private static final boolean DEFAULT_IGNORE_SAX_WARNINGS = true;
  private static final String DEFAULT_ENTITY_EXPANSION_LIMIT = "1000000000";

  private boolean m_ignoreExternalEntities;
  private boolean m_ignoreSaxErrors;
  private boolean m_ignoreSaxWarnings;
  private int m_numberOfIgnoredErrors;
  private int m_numberOfIgnoredWarnings;
  private ScoutXmlDocument m_xmlDocument;
  private ScoutXmlElement m_initialElement;
  private XMLReader m_xmlReader;

  /**
   * @since 1.0
   */
  public ScoutXmlParser() {
    this(new ScoutXmlDocument());
  }

  /**
   * @since 1.0
   */
  public ScoutXmlParser(boolean ignoreExternalEntities) {
    this();
    this.setIgnoreExternalEntities(ignoreExternalEntities);
  }

  /**
   * @since 1.0
   */
  public ScoutXmlParser(ScoutXmlDocument document) {
    this(document, null);
  }

  /**
   * @since 1.0
   */
  protected ScoutXmlParser(ScoutXmlDocument document, ScoutXmlElement root) {
    m_initialElement = root;
    m_xmlDocument = document;

    this.setIgnoreExternalEntities(DEFAULT_IGNORE_EXTERNAL_ENTITIES);
    this.setIgnoreSaxErrors(DEFAULT_IGNORE_SAX_ERRORS);
    this.setIgnoreSaxWarnings(DEFAULT_IGNORE_SAX_WARNINGS);

    try {
      m_xmlReader = new org.apache.xerces.parsers.SAXParser();
    }
    catch (Throwable t) {
      try {
        m_xmlReader = XMLReaderFactory.createXMLReader();
      }
      catch (SAXException e) {
        throw new UnsupportedOperationException("Cannot find an xml parser. Check dependency to org.apache.xerces");
      }
      if (!warnedDefaultParserNotFound) {
        warnedDefaultParserNotFound = true;
        if (LOG.isInfoEnabled()) {
          LOG.info("Missing dependency to org.apache.xerces. Using alternative " + m_xmlReader.getClass().getName());
        }
      }
    }

    m_xmlReader.setContentHandler(new P_SaxContentHandler());
    m_xmlReader.setErrorHandler(new P_SaxErrorHandler());
    m_xmlReader.setEntityResolver(new P_SaxEntityResolver());

    try {
      m_xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", new P_SaxLexicalHandler());
      m_xmlReader.setProperty("http://xml.org/sax/properties/declaration-handler", new P_SaxDeclarationHandler());

      m_xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", true);
      m_xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
    }
    catch (SAXNotRecognizedException e) {
      // Do nothing
    }
    catch (SAXNotSupportedException e) {
      // Do nothing
    }
  }

  /**
   * Returns whether this parser is ignoring external entities. This is
   * especially useful if you want to ignore an external DTD which is currently
   * unavailable. But keep in mind that if you ignore the DTD it is possible
   * that some texts differ because entities declared in the DTD can't be
   * resolved.
   * 
   * @since 1.0
   */
  public boolean isIgnoreExternalEntities() {
    return m_ignoreExternalEntities;
  }

  /**
   * Returns whether this parser is ignoring recoverable SAX errors.
   * 
   * @since 1.0
   */
  public boolean isIgnoreSaxErrors() {
    return m_ignoreSaxErrors;
  }

  /**
   * Returns whether this parser is ignoring SAX warnings.
   * 
   * @since 1.0
   */
  public boolean isIgnoreSaxWarnings() {
    return m_ignoreSaxWarnings;
  }

  /**
   * Returns whether this parser is validating.
   * 
   * @since 1.0
   */
  public boolean isValidating() {
    try {
      return m_xmlReader.getFeature("http://xml.org/sax/features/validation");
    }
    catch (SAXNotRecognizedException exception) {
      return false;
    }
    catch (SAXNotSupportedException exception) {
      return false;
    }
  }

  /**
   * @param file
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   * @throws SAXException
   * @since 1.0
   */
  public ScoutXmlDocument parse(File file) throws IOException, SAXException {
    return this.parse(new FileInputStream(file), file.getAbsolutePath());
  }

  /**
   * Parses the given stream and returns the resulting XML document.
   * 
   * @param stream
   *          The stream to be parsed.
   * @return ScoutXmlDocument
   * @throws IOException
   * @throws SAXException
   * @since 1.0
   */
  public ScoutXmlDocument parse(InputStream stream) throws IOException, SAXException {
    return this.parse(stream, null);
  }

  /**
   * Parses the given stream and returns the resulting XML document.
   * 
   * @param stream
   *          The stream to be parsed.
   * @param systemId
   *          The system identifier for this source.
   * @return ScoutXmlDocument
   * @throws IOException
   * @throws SAXException
   * @since 1.0
   */
  public ScoutXmlDocument parse(InputStream stream, String systemId) throws IOException, SAXException {
    InputSource inputSource = new InputSource(stream);

    if (systemId != null) {
      inputSource.setSystemId(new File(systemId).toURI().toString());
    }

    m_xmlReader.parse(inputSource);

    return m_xmlDocument;
  }

  /**
   * Parses the given source and returns the resulting XML document.
   * 
   * @param source
   *          The source can be a file's name, an URI or the XML document
   *          itself. Examples for a valid source are: &quot;C:\\test.xml&quot;,
   *          &quot;http://www.bsiag.com/test.xml&quot; or
   *          &quot;&lt;test&gt;Hello&lt;/test&gt;&quot;
   * @return ScoutXmlDocument
   * @throws IOException
   * @throws SAXException
   * @since 1.0
   */
  public ScoutXmlDocument parse(String source) throws IOException, SAXException {
    return this.parse(source, null);
  }

  /**
   * Parses the given source and returns the resulting XML document.
   * 
   * @param source
   *          The source can be a file's name, an URI or the XML document
   *          itself. Examples for a valid source are: &quot;C:\\test.xml&quot;,
   *          &quot;http://www.bsiag.com/test.xml&quot; or
   *          &quot;&lt;test&gt;Hello&lt;/test&gt;&quot;
   * @param systemId
   *          The system identifier for this source. Only used if the source is
   *          XML code.
   * @return ScoutXmlDocument
   * @throws IOException
   * @throws SAXException
   * @since 1.0
   */
  public ScoutXmlDocument parse(String source, String systemId) throws IOException, SAXException {
    if (source.equals("")) {
      return m_xmlDocument;
    }
    else {
      try {
        if (new File(source).exists()) {
          source = new File(source).toURI().toString();
        }
        else {
          new URI(source);
        }

        m_xmlReader.parse(source);
      }
      catch (URISyntaxException exception) {
        if (source.trim().startsWith("<")) {
          this.parse(new ByteArrayInputStream(source.getBytes()), systemId);
        }
        else {
          throw new ScoutXmlException("Unknown source. If it is a file path the file doesn't exist.");
        }
      }
    }

    return m_xmlDocument;
  }

  /**
   * @since 1.0
   */
  public ScoutXmlDocument parse(URL url) throws IOException, SAXException {
    return this.parse(url.toExternalForm());
  }

  /**
   * Sets whether to ignore all external entities This is especially useful if
   * you want to ignore an external DTD which is currently unavailable. But keep
   * in mind that if you ignore the DTD it is possible that some text values
   * differ because entities declared in the DTD can't be resolved or that the
   * 
   * @param ignoreExternalEntities
   *          If true all external entities are ignored.
   * @since 1.0
   */
  public void setIgnoreExternalEntities(boolean ignoreExternalEntities) {
    m_ignoreExternalEntities = ignoreExternalEntities;
  }

  /**
   * Sets whether to ignore recoverable SAX parser errors.
   * 
   * @param ignoreSaxErrors
   *          If true all recoverable parser errors are ignored, otherwise an
   *          exception is thrown.
   * @since 1.0
   */
  public void setIgnoreSaxErrors(boolean ignoreSaxErrors) {
    m_ignoreSaxErrors = ignoreSaxErrors;
  }

  /**
   * Sets whether to ignore SAX parser warnings.
   * 
   * @param ignoreParserErrors
   *          If true all parser warnings are ignored, otherwise the warning is
   *          written to the log.
   * @since 1.0
   */
  public void setIgnoreSaxWarnings(boolean ignoreSaxWarnings) {
    m_ignoreSaxWarnings = ignoreSaxWarnings;
  }

  /**
   * Sets whether to validate the document which is to be parsed.
   * 
   * @param validating
   *          If true the document is validated, otherwise not.
   * @throws ConfigurationException
   *           Is thrown if the underlying SAX parser doesn't support
   *           validation.
   * @since 1.0
   */
  public void setValidating(boolean validating) {
    try {
      m_xmlReader.setFeature("http://xml.org/sax/features/validation", validating);
    }
    catch (Exception exception) {
      if (validating) {
        throw new ScoutXmlException("The currently instantiated SAX parser doesn't allow to turn validation on.", exception);
      }
    }
  }

  public void setXmlEncoding(String encoding) {
    m_xmlDocument.setXmlEncoding(encoding);
  }

  private class P_SaxContentHandler implements org.xml.sax.ContentHandler {
    private ScoutXmlElement m_ancestor;
    private ScoutXmlElement m_current;
    private Map<String, String> m_namespaceBuffer;
    private StringBuffer m_textBuffer;

    @Override
    public void startDocument() throws SAXException {
      m_ancestor = m_initialElement != null ? m_initialElement.getParent() : null;
      m_current = m_initialElement;
      m_namespaceBuffer = new Hashtable<String, String>();
      m_textBuffer = new StringBuffer();

      m_numberOfIgnoredErrors = 0;
      m_numberOfIgnoredWarnings = 0;

      m_xmlDocument.setStrictlyChecking(false);
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
      m_namespaceBuffer.put(prefix, namespaceURI);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qnamePrefixed, Attributes attributes) throws SAXException {
      String text = m_textBuffer.toString().trim(); // Other wise problems with
      // mixed content
      if (text.length() > 0) {
        m_current.addText(text);
      }
      if (m_textBuffer.length() > 0) {
        m_textBuffer = new StringBuffer();
      }

      m_ancestor = m_current;
      m_current = m_xmlDocument.new ScoutXmlElement();

      m_current.setName(qnamePrefixed.equals("") ? localName : qnamePrefixed);

      if (m_xmlDocument.hasRoot()) {
        m_ancestor.addChild(m_current);
      }
      else {
        m_xmlDocument.setRoot(m_current);
      }

      if (m_namespaceBuffer.size() > 0) {
        m_current.setNamespaces(m_namespaceBuffer);
        m_namespaceBuffer = new Hashtable<String, String>();
      }

      if (attributes.getLength() > 0) {
        m_current.setAttributes(attributes);
      }
    }

    @Override
    public void characters(char[] characters, int start, int length) throws SAXException {
      if (length > 0) {
        m_textBuffer.append(characters, start, length);
      }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
      // Ignored
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      String text = m_textBuffer.toString().trim();
      if (text.length() > 0) {
        m_current.addText(text);
      }
      if (m_textBuffer.length() > 0) {
        m_textBuffer = new StringBuffer();
      }

      m_current.optimize();

      if (!m_current.isRoot()) {
        m_current = m_ancestor;
        m_ancestor = m_ancestor.getParent();
      }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
      // Ignored
    }

    @Override
    public void endDocument() throws SAXException {
      m_ancestor = null;
      m_current = null;
      m_namespaceBuffer = null;
      m_textBuffer = null;

      if (m_numberOfIgnoredErrors > 0) {
        LOG.warn(m_numberOfIgnoredErrors + " recoverable error(s) were ignored.");
      }

      if (m_numberOfIgnoredWarnings > 0) {
        LOG.warn(m_numberOfIgnoredWarnings + " warnings(s) were ignored.");
      }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
      // Ignored
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      // Ignored
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
      // Ignored
    }
  }

  private class P_SaxDeclarationHandler implements org.xml.sax.ext.DeclHandler {

    @Override
    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
      // Ignored
    }

    @Override
    public void elementDecl(String name, String model) throws SAXException {
      // Ignored
    }

    @Override
    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
      // Ignored
    }

    @Override
    public void internalEntityDecl(String name, String value) throws SAXException {
      // Ignored
    }
  }

  private class P_SaxEntityResolver implements org.xml.sax.EntityResolver {
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
      if (ScoutXmlParser.this.isIgnoreExternalEntities()) {
        return new InputSource(new ByteArrayInputStream(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").getBytes()));
      }
      else {
        return null;
      }
    }
  }

  private class P_SaxErrorHandler implements org.xml.sax.ErrorHandler {
    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
      if (ScoutXmlParser.this.isIgnoreSaxErrors()) {
        m_numberOfIgnoredErrors++;
      }
      else {
        throw exception;
      }
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      if (ScoutXmlParser.this.isIgnoreSaxWarnings()) {
        m_numberOfIgnoredWarnings++;
      }
      else {
        LOG.warn(exception.getMessage());
      }
    }
  }

  private class P_SaxLexicalHandler implements org.xml.sax.ext.LexicalHandler {
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
      // Ignored
    }

    @Override
    public void startCDATA() throws SAXException {
      // Ignored
    }

    @Override
    public void endCDATA() throws SAXException {
      // Ignored
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
      m_xmlDocument.setExternalDTD(publicId, systemId);
    }

    @Override
    public void endDTD() throws SAXException {
      // Ignored
    }

    @Override
    public void startEntity(String name) throws SAXException {
      // Ignored
    }

    @Override
    public void endEntity(String name) throws SAXException {
      // Ignored
    }
  }
}
