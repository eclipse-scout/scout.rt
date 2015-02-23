/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.pluginxml.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.pluginxml.IPluginXmlParser;
import org.eclipse.scout.rt.platform.pluginxml.IPluginXmlVisitor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The {@link PluginXmlParser} will be triggered from {@link Platform#start()}. All plugin.xml and fragment.xml files
 * are visited and passed to all registered visitors.
 *
 * @since 5.1
 */
public final class PluginXmlParser implements IPluginXmlParser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PluginXmlParser.class);

  private static final PluginXmlParser instance = new PluginXmlParser();

  private Map<IPluginXml, Document> m_documents;

  private PluginXmlParser() {
    LOG.setLevel(LOG.LEVEL_DEBUG);
  }

  /**
   * The accesser to the singleton.
   *
   * @return the single instance of the {@link IPluginXmlParser}
   */
  public static IPluginXmlParser get() {
    return instance;
  }

  @Override
  public void visit(IPluginXmlVisitor visitor) {
    visitInternal(visitor);
  }

  private void visitInternal(IPluginXmlVisitor visitor) {
    for (Entry<IPluginXml, Document> entry : ensureDucumentsLoaded().entrySet()) {

      visitor.visit(entry.getKey(), entry.getValue());
    }
  }

  private Map<IPluginXml, Document> ensureDucumentsLoaded() {
    if (m_documents == null) {
      Map<IPluginXml, Document> documents = new HashMap<>();
      List<IPluginXml> xmlFiles = PluginXmlResolver.resolvePluginXml();
      for (IPluginXml xmlFile : xmlFiles) {
        LOG.debug(String.format("resolved plugin.xml '%s'", xmlFile));
        try {
          documents.put(xmlFile, getDocument(xmlFile));
        }
        catch (IOException | SAXException | ParserConfigurationException e) {
          LOG.error(String.format("Could not parse '%s'.", xmlFile), e);
        }
      }
      m_documents = documents;
    }
    return m_documents;
  }

  private Document getDocument(IPluginXml xmlFile) throws IOException, SAXException, ParserConfigurationException {
    LOG.debug(String.format("Start parsing xml '%s'.", xmlFile));
    InputStream inputStream = null;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = factory.newDocumentBuilder();
      inputStream = xmlFile.getUrl().openStream();
      Document xmlDoc = docBuilder.parse(inputStream);
      return xmlDoc;

    }

    finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        }
        catch (IOException e) {
          // void
        }
      }
    }
  }
}
