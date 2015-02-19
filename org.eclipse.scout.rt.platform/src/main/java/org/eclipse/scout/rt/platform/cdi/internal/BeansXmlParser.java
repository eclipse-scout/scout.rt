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
package org.eclipse.scout.rt.platform.cdi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.interceptor.InterceptorBinding;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.internal.Activator;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public final class BeansXmlParser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeansXmlParser.class);

  private final Map<Class<? extends Annotation>, ?> m_interceptors;

  public BeansXmlParser() {
    Map<Class<? extends Annotation>, Object> interceptors = new HashMap<>();
    List<IBeansXml> beanXmls = findAllBeanXml();
    for (IBeansXml beanXml : beanXmls) {
      interceptors.putAll(parse(beanXml));
    }
    m_interceptors = interceptors;
  }

  public Map<Class<? extends Annotation>, ?> getInterceptors() {
    return m_interceptors;
  }

  protected List<IBeansXml> findAllBeanXml() {
    if (StringUtility.hasText(System.getProperty("org.osgi.framework.version"))) {
      return findAllBeanXmlOsgi();
    }
    else {
      List<IBeansXml> beanXmls = new ArrayList<>();
      try {
        Enumeration<URL> resources = BeansXmlParser.class.getClassLoader().getResources("META-INF/beans.xml");
        while (resources.hasMoreElements()) {
          URL resourceUrl = resources.nextElement();
          if (resourceUrl != null) {
            beanXmls.add(new BeansXml(resourceUrl));
          }
        }
      }
      catch (IOException e) {
        LOG.error("Could not resolve beans.xml files over classloader.", e);
      }
      return beanXmls;
    }
  }

  /**
   * @return
   */
  protected List<IBeansXml> findAllBeanXmlOsgi() {
    List<IBeansXml> beanXmls = new ArrayList<>();
    for (Bundle bundle : Activator.getBundleContext().getBundles()) {
      URL resourceUrl = bundle.getResource("META-INF/beans.xml");
      if (resourceUrl != null) {
        beanXmls.add(new OsgiBeanXml(bundle, resourceUrl));
      }
    }
    return beanXmls;
  }

  private Map<Class<? extends Annotation>, ?> parse(IBeansXml xmlFile) {
    LOG.debug(String.format("Start parsing xml '%s'.", xmlFile));
    InputStream inputStream = null;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = factory.newDocumentBuilder();
      inputStream = xmlFile.getUrl().openStream();
      Document xmlDoc = docBuilder.parse(inputStream);
      return parseInterceptors(xmlFile, xmlDoc);
    }
    catch (Exception e) {
      LOG.error(String.format("Could not parse '%s'.", xmlFile), e);
      return CollectionUtility.emptyHashMap();
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

  /**
   * @param xmlDoc
   */
  private Map<Class<? extends Annotation>, ?> parseInterceptors(IBeansXml beansXml, Document xmlDoc) {
    Map<Class<? extends Annotation>, Object> interceptors = new HashMap<>();
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//beans/interceptors/class";
    try {
      NodeList services = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
      for (int i = 0; i < services.getLength(); i++) {
        Node interceptorNode = services.item(i);
        String interceptorFqn = interceptorNode.getTextContent();
        if (StringUtility.hasText(interceptorFqn)) {
          try {
            Class<?> interceptorClass = beansXml.loadClass(interceptorFqn);
            Object interceptor = BeanContext.createInstance(interceptorClass);
            for (Annotation a : readInterceptorAnnotations(interceptorClass)) {
              interceptors.put(a.annotationType(), interceptor);
            }
          }
          catch (ClassNotFoundException e) {
            LOG.error(String.format("Could not instanicate %s.", interceptorFqn), e);
          }
        }
      }
    }
    catch (XPathExpressionException e) {
      LOG.error(String.format("XPath parsing of %s failed.", beansXml), e);
    }
    return interceptors;

  }

  /**
   * @param interceptorClass
   * @return
   */
  private List<Annotation> readInterceptorAnnotations(Class<?> interceptorClass) {
    List<Annotation> result = new ArrayList<Annotation>();
    for (Annotation a : interceptorClass.getAnnotations()) {
      if (a.annotationType().getAnnotation(InterceptorBinding.class) != null) {
        result.add(a);
      }
    }
    return result;
  }

  private class OsgiBeanXml extends BeansXml {

    private final Bundle m_bundle;

    /**
     * @param beanXmlUrl
     */
    public OsgiBeanXml(Bundle bundle, URL beanXmlUrl) {
      super(beanXmlUrl);
      m_bundle = bundle;
    }

    public Bundle getBundle() {
      return m_bundle;
    }

    @Override
    public Class<?> loadClass(String fullyQuallifiedName) throws ClassNotFoundException {
      Assertions.assertNotNullOrEmpty(fullyQuallifiedName);
      return getBundle().loadClass(fullyQuallifiedName);
    }

  }
}
