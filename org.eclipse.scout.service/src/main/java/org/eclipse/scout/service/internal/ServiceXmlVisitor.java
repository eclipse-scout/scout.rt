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
package org.eclipse.scout.service.internal;

import java.lang.reflect.InvocationTargetException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.DynamicAnnotations;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.pluginxml.IPluginXmlVisitor;
import org.eclipse.scout.rt.platform.pluginxml.internal.IPluginXml;
import org.eclipse.scout.service.IServiceReference;
import org.eclipse.scout.service.ServiceReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class ServiceXmlVisitor implements IPluginXmlVisitor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceXmlVisitor.class);

  private static final String ClientServiceFactory = "org.eclipse.scout.rt.client.services.ClientServiceFactory";
  private static final String CommonProxyServiceFactory = "org.eclipse.scout.rt.services.CommonProxyServiceFactory";
  private static final String ClientProxyServiceFactory = "org.eclipse.scout.rt.client.services.ClientProxyServiceFactory";
  private static final String ServerServiceFactory = "org.eclipse.scout.rt.server.services.ServerServiceFactory";

  private static final String IClientSession = "org.eclipse.scout.rt.client.IClientSession";
  private static final String IServerSession = "org.eclipse.scout.rt.server.IServerSession";

  private final IBeanContext m_context;

  public ServiceXmlVisitor(IBeanContext context) {
    m_context = context;
  }

  @Override
  public void visit(IPluginXml xmlFile, Document xmlDoc) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//extension[@point='org.eclipse.scout.service.services']/service";
    try {
      NodeList services = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
      for (int i = 0; i < services.getLength(); i++) {
        Node service = services.item(i);
        if (service.getNodeType() == Node.ELEMENT_NODE) {
          Element sElement = (Element) service;
          try {
            IServiceReference ref = parseEntry(xmlFile, sElement);
            registerService(ref, xmlFile);
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

  private void registerService(IServiceReference ref, IPluginXml xmlFile) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
    Bean<?> bean = new Bean<Object>(ref.getService());
    fillServiceAnnotations(bean, xmlFile, ref);
    m_context.registerBean(bean);
  }

  public static void fillServiceAnnotations(Bean<?> bean, IPluginXml pluginXml, IServiceReference ref) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
    // ranking
    bean.addAnnotation(DynamicAnnotations.createPriority(ref.getRanking()));
  }

  /**
   * @param proxyElement
   * @throws ClassNotFoundException
   */
  public static IServiceReference parseEntry(IPluginXml pluginXml, Element proxyElement) throws IllegalArgumentException, ClassNotFoundException {
    ServiceReference svc = new ServiceReference();
    // proxy/service
    String tagName = proxyElement.getTagName();
    if ("service".equalsIgnoreCase(tagName)) {
      svc.setProxy(false);
    }
    else if ("proxy".equalsIgnoreCase(tagName)) {
      svc.setProxy(true);
    }
    else {
      throw new IllegalArgumentException("unknow element in service extension point: '" + tagName + "'.");
    }
    // class
    String serviceFqn = proxyElement.getAttribute("class");
    if (StringUtility.hasText(serviceFqn)) {
      svc.setService(pluginXml.loadClass(serviceFqn.trim()));
      if (svc.isProxy() && !svc.getService().isInterface()) {
        // only interfaces allowed for proxies
        throw new IllegalArgumentException("proxy class must be an interface: '" + serviceFqn + "'.");
      }
    }
    else {
      throw new IllegalArgumentException("service must have a 'class' attribute (service class)");
    }
    // session
    String sessionFqn = null;
    String factoryFqn = proxyElement.getAttribute("factory");
    if (StringUtility.hasText(factoryFqn)) {
      String sessionAttrib = proxyElement.getAttribute("session");
      if (StringUtility.hasText(sessionAttrib)) {
        sessionFqn = sessionAttrib.trim();
      }
      else {
        // init with defaults
        switch (factoryFqn) {
          case ServerServiceFactory:
            sessionFqn = IServerSession;
            break;
          case ClientServiceFactory:
          case CommonProxyServiceFactory:
          case ClientProxyServiceFactory:
            sessionFqn = IClientSession;
            break;
        }
      }
    }
    if (sessionFqn != null) {
      svc.setSession(pluginXml.loadClass(sessionFqn));
    }
    // ranking
    float ranking = 0;
    if (svc.isProxy()) {
      ranking = -2; // default ranking for proxies
    }
    Priority prio = svc.getService().getAnnotation(Priority.class);
    if (prio != null) {
      ranking = (float) prio.value();
    }
    else {
      String rankingString = proxyElement.getAttribute("ranking");
      if (StringUtility.hasText(rankingString)) {
        ranking = Float.parseFloat(rankingString);
      }
    }
    svc.setRanking(ranking);
    // create immediately
    String createImmediatelyString = proxyElement.getAttribute("createImmediately");
    if (StringUtility.hasText(createImmediatelyString)) {
      svc.setCreateImmediately(Boolean.parseBoolean(createImmediatelyString));
    }
    return svc;
  }
}
