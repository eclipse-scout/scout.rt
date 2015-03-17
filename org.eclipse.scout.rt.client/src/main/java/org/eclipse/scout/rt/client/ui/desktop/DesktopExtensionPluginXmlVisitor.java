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
package org.eclipse.scout.rt.client.ui.desktop;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.IBeanContext;
import org.eclipse.scout.rt.platform.pluginxml.IPluginXmlVisitor;
import org.eclipse.scout.rt.platform.pluginxml.internal.IPluginXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class DesktopExtensionPluginXmlVisitor implements IPluginXmlVisitor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DesktopExtensionPluginXmlVisitor.class);
  public static final String ATTR_ACTIVE = "active";
  public static final String ATTR_CLASS = "class";
  private IBeanContext m_context;

  public DesktopExtensionPluginXmlVisitor(IBeanContext context) {
    m_context = context;

  }

  @Override
  public void visit(IPluginXml xmlFile, Document xmlDoc) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//extension[@point='org.eclipse.scout.rt.extension.client.desktopExtensions']/desktopExtension";
    try {
      NodeList desktopExtensions = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
      for (int i = 0; i < desktopExtensions.getLength(); i++) {
        Node desktopExtension = desktopExtensions.item(i);
        if (desktopExtension.getNodeType() == Node.ELEMENT_NODE) {
          Element desktopExtensionElement = (Element) desktopExtension;
          parseDesktopExtensionElement(xmlFile, desktopExtensionElement);
        }
      }
    }
    catch (XPathExpressionException e) {
      LOG.error(String.format("XPath parsing of %s failed.", xmlFile), e);
    }
  }

  protected void parseDesktopExtensionElement(IPluginXml xmlFile, Element e) {
    if (!e.hasAttribute(ATTR_CLASS)) {
      LOG.warn("Invalid desktop extension. Attribute '{0}' is missing.", ATTR_CLASS);
      return;
    }

    String clazz = StringUtility.trim(e.getAttribute(ATTR_CLASS));
    if (!StringUtility.hasText(clazz)) {
      LOG.warn("Invalid desktop extension. Attribute '{0}' is missing.", ATTR_CLASS);
      return;
    }

    boolean isActive = !e.hasAttribute(ATTR_ACTIVE) || !"false".equalsIgnoreCase(e.getAttribute(ATTR_ACTIVE));
    if (!isActive) {
      LOG.info("Skipping inactive desktop extension '{0}'.", clazz);
      return;
    }

    try {
      Class<?> loadClass = xmlFile.loadClass(clazz);
      if (!IDesktopExtension.class.isAssignableFrom(loadClass)) {
        LOG.warn("Invalid desktop extension. Class '{0}' is not instanceof '{1}'.", ATTR_CLASS, IDesktopExtension.class.getName());
        return;
      }

      m_context.registerClass(loadClass);
    }
    catch (ClassNotFoundException ex) {
      LOG.error("Unable to load desktop extension '" + clazz + "'.", ex);
    }
  }
}
