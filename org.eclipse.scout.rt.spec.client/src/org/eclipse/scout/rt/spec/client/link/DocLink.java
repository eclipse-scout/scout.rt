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
package org.eclipse.scout.rt.spec.client.link;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocLink {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DocLink.class);

  public static final String REPLACE_TAG_NAME = "replace";
  public static final String START_TAG = "<";

  public static final String LINK_TAG_NAME = "link";
  public static final String ID_TAG_NAME = "id";
  public static final String NAME_TAG_NAME = "name";

  private final String m_targetId;
  private final String m_displayName;

  public DocLink(String targetId, String displayName) {
    m_targetId = targetId;
    m_displayName = displayName;
  }

  public String getTargetId() {
    return m_targetId;
  }

  public String getDisplayName() {
    return m_displayName;
  }

  /**
   * Converts a link to XML
   * 
   * @param displayName
   * @param targetId
   * @return the link in XML format, e.g. {@code
   *         <link>
   *         <id>FilterImportTestForm.MainBox.TestStringField</id>
   *         <name>TestStringField</name>
   *         </link>}
   */
  public String toXML() {
    String taggedName = encloseInTags(m_displayName, NAME_TAG_NAME);
    String taggedId = encloseInTags(m_targetId, ID_TAG_NAME);
    return encloseInTags(taggedId + taggedName, LINK_TAG_NAME);
  }

  public static String encloseInTags(String text, String tagName) {
    return getStartTag(tagName) + text + getEndTag(tagName);
  }

  private static String getStartTag(String tagName) {
    return "<" + tagName + ">";
  }

  private static String getEndTag(String tagName) {
    return "</" + tagName + ">";
  }

  /**
   * @param xml
   * @throws Exception
   */
  public static List<DocLink> parse(String xml) throws ProcessingException {
    Document doc = loadXMLFromString(xml);
    NodeList links = doc.getElementsByTagName(LINK_TAG_NAME);
    List<DocLink> specLinks = new ArrayList<DocLink>();
    for (int i = 0; i < links.getLength(); i++) {
      Node l = links.item(i);
      DocLink specLink2 = getSpecLink(l);
      specLinks.add(specLink2);
    }
    return specLinks;
  }

  private static DocLink getSpecLink(Node link) {
    NodeList linkChildren = link.getChildNodes();
    String id = null;
    String name = null;

    for (int j = 0; j < linkChildren.getLength(); j++) {
      Node node = linkChildren.item(j);
      if (node.getNodeName().equals(ID_TAG_NAME)) {
        id = node.getTextContent();
      }
      if (node.getNodeName().equals("name")) {
        name = node.getTextContent();
      }
    }
    if (id == null || name == null) {
      LOG.error("Invalid spec link ignored.");
      return null;
    }

    return new DocLink(id, name);
  }

  private static Document loadXMLFromString(String xml) throws ProcessingException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xml));
      return builder.parse(is);
    }
    catch (SAXException e) {
      throw new ProcessingException("", e);
    }
    catch (IOException e) {
      throw new ProcessingException("", e);
    }
    catch (ParserConfigurationException e) {
      throw new ProcessingException("", e);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_displayName == null) ? 0 : m_displayName.hashCode());
    result = prime * result + ((m_targetId == null) ? 0 : m_targetId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DocLink other = (DocLink) obj;
    if (m_displayName == null) {
      if (other.m_displayName != null) {
        return false;
      }
    }
    else if (!m_displayName.equals(other.m_displayName)) {
      return false;
    }
    if (m_targetId == null) {
      if (other.m_targetId != null) {
        return false;
      }
    }
    else if (!m_targetId.equals(other.m_targetId)) {
      return false;
    }
    return true;
  }

}
