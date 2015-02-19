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
package org.eclipse.scout.rt.client.services;

import java.lang.reflect.InvocationTargetException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.pluginxml.IPluginXmlVisitor;
import org.eclipse.scout.rt.platform.pluginxml.internal.IPluginXml;
import org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.service.IServiceReference;
import org.eclipse.scout.service.internal.ServiceXmlVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class ProxyXmlVisitor implements IPluginXmlVisitor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ProxyXmlVisitor.class);

  private final IBeanContext m_context;

  public ProxyXmlVisitor(IBeanContext context) {
    m_context = context;
  }

  @Override
  public void visit(IPluginXml xmlFile, Document xmlDoc) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//extension[@point='org.eclipse.scout.service.services']/proxy";
    try {
      NodeList services = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
      for (int i = 0; i < services.getLength(); i++) {
        Node service = services.item(i);
        if (service.getNodeType() == Node.ELEMENT_NODE) {
          Element sElement = (Element) service;
          try {
            IServiceReference ref = ServiceXmlVisitor.parseEntry(xmlFile, sElement);
            registerProxy(ref, xmlFile);
          }
          catch (Exception e) {
            LOG.error(String.format("Could not parse services of %s", xmlFile), e);
          }
        }
      }
    }
    catch (XPathExpressionException e) {
      LOG.error(String.format("XPath parsing of %s failed.", xmlFile), e);
    }
  }

  private void registerProxy(final IServiceReference ref, IPluginXml xmlFile) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
    final Class<?> service = ref.getService();
    Bean<?> bean = new Bean<Object>(ref.getService()) {
      @Override
      protected Object createNewInstance() {
        return ServiceTunnelUtility.createProxy(service, new ClientServiceTunnelInvocationHandler(service));
      }
    };
    ServiceXmlVisitor.fillServiceAnnotations(bean, xmlFile, ref);
    m_context.register(bean);
  }
}
