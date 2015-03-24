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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.BeanData;
import org.eclipse.scout.rt.platform.IBeanContext;
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
    String expression = "//extension[@point='org.eclipse.scout.service.services']/service | //extension[@point='org.eclipse.scout.service.services']/proxy";
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
    BeanData bean = new BeanData(ref.getService());
    fillServiceAnnotations(bean, xmlFile, ref);
    //TODO imo check to enhance the exiting bean (IService may receive a @Bean anno...) instead of re-registering it
    m_context.registerBean(bean);
  }

  public static void fillServiceAnnotations(BeanData bean, IPluginXml pluginXml, IServiceReference ref) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
    // ranking (@Priority) is migrated to negative @Order
    bean.addAnnotation(org.eclipse.scout.rt.platform.AnnotationFactory.createOrder(-ref.getRanking()));

    // services are always application scoped
    bean.addAnnotation(org.eclipse.scout.rt.platform.AnnotationFactory.createApplicationScoped());

    if (ref.isCreateImmediately()) {
      bean.addAnnotation(org.eclipse.scout.rt.platform.AnnotationFactory.createCreateImmediately());
    }

    //TODO abr: from imo: @Priority is migrated as if it would also be a @Replace. Also add this to the sdk utility
    if (!ref.isProxy() && ref.getRanking() != 0) {
      bean.addAnnotation(AnnotationFactory.createReplace());
    }

    if (ref.isProxy()) {
      bean.addAnnotation((Annotation) pluginXml.loadClass("org.eclipse.scout.rt.shared.AnnotationFactory").getMethod("createTunnelToServer").invoke(null));
    }
    else if (ref.getClientSession() != null) {
      bean.addAnnotation((Annotation) pluginXml.loadClass("org.eclipse.scout.rt.client.AnnotationFactory").getMethod("createClient", Class.class).invoke(null, ref.getClientSession()));
    }
    else if (ref.getServerSession() != null) {
      bean.addAnnotation((Annotation) pluginXml.loadClass("org.eclipse.scout.rt.server.AnnotationFactory").getMethod("createServer", Class.class).invoke(null, ref.getServerSession()));
    }
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
    String clientSessionFqn = null;
    String serverSessionFqn = null;
    String factoryFqn = proxyElement.getAttribute("factory");
    if (StringUtility.hasText(factoryFqn)) {
      String sessionAttrib = proxyElement.getAttribute("session");
      if (StringUtility.hasText(sessionAttrib)) {
        switch (factoryFqn) {
          case ServerServiceFactory:
            serverSessionFqn = sessionAttrib.trim();
            break;
          case ClientServiceFactory:
          case CommonProxyServiceFactory:
          case ClientProxyServiceFactory:
            clientSessionFqn = sessionAttrib.trim();
            break;
        }
      }
      else {
        // init with defaults
        switch (factoryFqn) {
          case ServerServiceFactory:
            serverSessionFqn = IServerSession;
            break;
          case ClientServiceFactory:
          case CommonProxyServiceFactory:
          case ClientProxyServiceFactory:
            clientSessionFqn = IClientSession;
            break;
        }
      }
    }
    if (clientSessionFqn != null) {
      svc.setClientSession(pluginXml.loadClass(clientSessionFqn));
    }
    if (serverSessionFqn != null) {
      svc.setServerSession(pluginXml.loadClass(serverSessionFqn));
    }
    // ranking
    double ranking = 0;
    if (svc.isProxy()) {
      ranking = -2; // default ranking for proxies
    }
    Priority prio = svc.getService().getAnnotation(Priority.class);
    if (prio != null) {
      ranking = prio.value();
    }
    else {
      String rankingString = proxyElement.getAttribute("ranking");
      if (StringUtility.hasText(rankingString)) {
        ranking = Double.parseDouble(rankingString);
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
