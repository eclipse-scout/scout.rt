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
package org.eclipse.scout.rt.ui.swing.extension;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.XmlUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.IBeanContext;
import org.eclipse.scout.rt.platform.pluginxml.IPluginXmlVisitor;
import org.eclipse.scout.rt.platform.pluginxml.internal.IPluginXml;
import org.eclipse.scout.rt.ui.swing.extension.internal.FormFieldExtension;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class FormFieldsPluginXmlVisitor implements IPluginXmlVisitor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FormFieldsPluginXmlVisitor.class);

  public static final String ATTR_FORMFIELD_NAME = "name";
  public static final String ATTR_FORMFIELD_MODEL_CLASS = "modelClass";
  public static final String ATTR_FORMFIELD_ACTIVE = "active";
  public static final String ATTR_FORMFIELD_SCOPE = "scope";
  public static final String ELEM_UICLASS = "uiClass";
  public static final String ELEM_FACTORY = "factory";
  public static final String ATTR_UICLASS_CLASS = "class";
  public static final String ATTR_FACTORY_CLASS = "class";

  private final IBeanContext m_context;

  public FormFieldsPluginXmlVisitor(IBeanContext context) {
    m_context = context;
  }

  protected IBeanContext getContext() {
    return m_context;
  }

  @Override
  public void visit(IPluginXml xmlFile, Document xmlDoc) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//extension[@point='org.eclipse.scout.rt.ui.swing.formfields']/formField";
    try {
      NodeList formFields = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
      for (int i = 0; i < formFields.getLength(); i++) {
        Node formField = formFields.item(i);
        if (formField.getNodeType() == Node.ELEMENT_NODE) {
          Element formFieldElement = (Element) formField;
          parseFormFieldElement(xmlFile, formFieldElement);
        }
      }
    }
    catch (XPathExpressionException e) {
      LOG.error(String.format("XPath parsing of %s failed.", xmlFile), e);
    }
  }

  @SuppressWarnings("unchecked")
  private void parseFormFieldElement(IPluginXml xmlFile, Element element) {
    String name = element.getAttribute(ATTR_FORMFIELD_NAME);
    boolean active = "true".equalsIgnoreCase(element.getAttribute(ATTR_FORMFIELD_ACTIVE));

    FormFieldExtension formFieldExt = new FormFieldExtension(name);
    formFieldExt.setActive(active);
    formFieldExt.setScope(getScopePriority(element.getAttribute(ATTR_FORMFIELD_SCOPE)));

    String modelClassName = element.getAttribute(ATTR_FORMFIELD_MODEL_CLASS);
    if (!StringUtility.hasText(modelClassName)) {
      //TODO
      return;
    }

    try {
      Class<?> loadClass = xmlFile.loadClass(modelClassName);
      if (!IFormField.class.isAssignableFrom(loadClass)) {
        //TODO
        return;
      }

      formFieldExt.setModelClass((Class<? extends IFormField>) loadClass);
      formFieldExt.setFactoryClass(getClassName(element, ELEM_FACTORY, ATTR_FACTORY_CLASS, IFormFieldFactory.class, xmlFile));
      formFieldExt.setUiClass(getClassName(element, ELEM_UICLASS, ATTR_UICLASS_CLASS, ISwingScoutFormField.class, xmlFile));
      FormFieldExtensions.INSTANCE.add(formFieldExt);
    }
    catch (ClassNotFoundException e) {
      //TODO
    }

  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> getClassName(Element parent, String tagName, String attribute, Class<T> filter, IPluginXml xmlFile) {
    Element child = XmlUtility.getFirstChildElement(parent, tagName);
    if (child == null) {
      return null;
    }

    if (!child.hasAttribute(attribute)) {
      return null;
    }

    String clazzName = child.getAttribute(attribute);
    if (!StringUtility.hasText(clazzName)) {
      return null;
    }

    try {
      Class<?> loadClass = xmlFile.loadClass(clazzName);
      if (filter.isAssignableFrom(loadClass)) {
        return (Class<T>) loadClass;
      }
    }
    catch (ClassNotFoundException e) {
      //TODO
    }

    return null;
  }

  private static int getScopePriority(String scope) {
    if ("global".equalsIgnoreCase(scope)) {
      return IFormFieldExtension.SCOPE_GLOBAL;
    }
    return IFormFieldExtension.SCOPE_DEFAULT;
  }
}
