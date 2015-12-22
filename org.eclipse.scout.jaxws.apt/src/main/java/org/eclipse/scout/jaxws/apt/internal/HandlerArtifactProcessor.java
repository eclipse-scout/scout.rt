/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.apt.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.jws.HandlerChain;
import javax.tools.StandardLocation;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.jaxws.apt.JaxWsAnnotationProcessor;
import org.eclipse.scout.jaxws.apt.internal.PortTypeProxyDescriptor.HandlerDescriptor;
import org.eclipse.scout.jaxws.apt.internal.util.AptUtil;
import org.eclipse.scout.jaxws.apt.internal.util.Logger;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.JaxWsPortTypeProxy;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.AuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.provider.handler.HandlerProxy;
import org.eclipse.scout.rt.server.jaxws.provider.handler.SOAPHandlerProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

/**
 * Processor to generate authentication handler, proxy handlers and the handler-chain XML file.
 *
 * @since 5.1
 */
public class HandlerArtifactProcessor {

  private static final String AUTH_HANDLER_NAME = "AuthHandler";
  private static final String HANDLER_PROXY_SUFFIX = "Proxy";
  private static final String HANDLERS_FILE_NAME = "handler-chain.xml";

  /**
   * Creates the following artifacts:
   * <ul>
   * <li>Authentication handler (if applicable)</li>
   * <li>HandlerProxies for configured handlers</li>
   * <li>XML file to describe the handler-chain</li>
   * </ul>
   *
   * @return relative filename of the XML file.
   */
  public String generateHandlerArtifacts(final JClass portTypeProxy, final PortTypeProxyDescriptor descriptor, final ProcessingEnvironment env, final Logger logger) {
    final List<String> handlers = new ArrayList<>();

    // Generate AuthHandler
    if (descriptor.isAuthenticationEnabled()) {
      try {
        final String authHandlerQualifiedName = createAndPersistAuthHandler(portTypeProxy, descriptor, env);
        logger.logInfo("Generating AuthHandler for '%s': %s", portTypeProxy.fullName(), authHandlerQualifiedName);
        handlers.add(authHandlerQualifiedName);
      }
      catch (final Exception e) {
        throw new RuntimeException(String.format("Failed to generate AuthHandler for '%s'", portTypeProxy.fullName()), e);
      }
    }

    // Add configured handlers.
    final List<HandlerDescriptor> handlerChain = descriptor.getHandlerChain();
    for (int idx = 0; idx < handlerChain.size(); idx++) {
      final HandlerDescriptor handler = handlerChain.get(idx);

      try {
        String proxyHandlerQualifiedName = createAndPersistProxyHandler(portTypeProxy, descriptor, handler, idx, env);
        logger.logInfo("Generating proxy handler for '%s': %s", handler.getQualifiedName(), proxyHandlerQualifiedName);
        handlers.add(proxyHandlerQualifiedName);
      }
      catch (final Exception e) {
        throw new RuntimeException(String.format("Failed to generate proxy handler for '%s'", handler.getQualifiedName()), e);
      }
    }

    // Generate handler-chain.xml.
    try {
      String handlerChainFile = createAndPersistHandlerXmlFile(portTypeProxy, descriptor, handlers, env.getFiler());
      logger.logInfo("Generating XML-file for handler-chain for '%s': %s", portTypeProxy.fullName(), handlerChainFile);

      return handlerChainFile;
    }
    catch (final Exception e) {
      throw new RuntimeException(String.format("Failed to generate XML-file for handler-chain for '%s'", portTypeProxy.fullName()), e);
    }
  }

  /**
   * Generates the AuthHandler.
   */
  public String createAndPersistAuthHandler(final JClass portTypeProxy, final PortTypeProxyDescriptor descriptor, final ProcessingEnvironment processingEnv) throws Exception {
    final JCodeModel model = new JCodeModel();

    final String fullName = descriptor.getProxyQualifiedName() + "_" + AUTH_HANDLER_NAME;
    final JDefinedClass authHandler = model._class(fullName)._extends(model.ref(AuthenticationHandler.class));

    // Add 'Generated' annotation
    final JAnnotationUse generatedAnnotation = authHandler.annotate(Generated.class);
    generatedAnnotation.param("value", JaxWsAnnotationProcessor.class.getName());
    generatedAnnotation.param("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ").format(new Date()));
    generatedAnnotation.param("comments", String.format("Authentication Handler for [method=%s, verifier=%s]", AptUtil.toSimpleName(descriptor.getAuthMethod()), AptUtil.toSimpleName(descriptor.getAuthVerifier())));

    // Add default constructor with super call to provide authentication annotation.
    final JMethod defaultConstructor = authHandler.constructor(JMod.PUBLIC);
    final JClass descriptorClass = model.ref(descriptor.getDescriptor().getQualifiedName().toString());
    defaultConstructor.body().invoke("super").arg(JExpr.dotclass(descriptorClass).invoke("getAnnotation").arg(JExpr.dotclass(model.ref(JaxWsPortTypeProxy.class))).invoke("authentication"));

    AptUtil.addJavaDoc(authHandler,
        String.format("This class is auto-generated by APT triggered by Maven build and is based on the authentication configuration declared in {@link %s}.", descriptor.getDescriptor().getSimpleName().toString()));

    AptUtil.buildAndPersist(model, processingEnv.getFiler());

    return authHandler.fullName();
  }

  /**
   * Generates a ProxyHandler.
   */
  public String createAndPersistProxyHandler(final JClass portTypeProxy, final PortTypeProxyDescriptor descriptor, final HandlerDescriptor handler, final int idx, final ProcessingEnvironment env) throws Exception {
    final JCodeModel model = new JCodeModel();

    final String fullName = descriptor.getProxyQualifiedName() + "_" + handler.getSimpleName() + HANDLER_PROXY_SUFFIX;
    final JDefinedClass handlerProxy = model._class(fullName);

    // Add 'Generated' annotation
    final JAnnotationUse generatedAnnotation = handlerProxy.annotate(Generated.class);
    generatedAnnotation.param("value", JaxWsAnnotationProcessor.class.getName());
    generatedAnnotation.param("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ").format(new Date()));
    generatedAnnotation.param("comments", "Handler proxy for " + handler.getQualifiedName());

    AptUtil.addJavaDoc(handlerProxy, String.format("This class is auto-generated by APT triggered by Maven build and is based on the handler configuration declared in {@link %s}.", descriptor.getDescriptor().getSimpleName().toString()));

    switch (handler.getHandlerType()) {
      case SOAP:
        handlerProxy._extends(model.ref(SOAPHandlerProxy.class));
        break;
      case LOGICAL:
        handlerProxy._extends(model.ref(HandlerProxy.class).narrow(LogicalMessageContext.class));
        handlerProxy._implements(model.ref(LogicalHandler.class).narrow(LogicalMessageContext.class));
        break;
      default:
        handlerProxy._extends(model.ref(HandlerProxy.class).narrow(MessageContext.class));
        break;
    }

    // Add default constructor with super call to provide handler annotation.
    final JClass descriptorClass = model.ref(descriptor.getDescriptor().getQualifiedName().toString());
    final JMethod defaultConstructor = handlerProxy.constructor(JMod.PUBLIC);
    defaultConstructor.body().invoke("super").arg(JExpr.dotclass(descriptorClass).invoke("getAnnotation").arg(JExpr.dotclass(model.ref(JaxWsPortTypeProxy.class))).invoke("handlerChain").component(JExpr.lit(idx)));

    AptUtil.buildAndPersist(model, env.getFiler());

    return handlerProxy.fullName();
  }

  protected String createAndPersistHandlerXmlFile(final JClass portTypeProxy, final PortTypeProxyDescriptor descriptor, final List<String> handlers, final Filer filer) throws Exception {
    final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    // Create the comment.
    final StringWriter comment = new StringWriter();
    final PrintWriter out = new PrintWriter(comment);
    out.println();
    out.printf("This file is auto-generated by APT triggered by Maven build, and is populated with handlers declared in '%s'. This file is referenced in @%s annotation in '%s'.<br/>", descriptor.getDescriptor().getSimpleName(),
        HandlerChain.class.getSimpleName(), portTypeProxy.name()).println();
    out.printf("Please note that handlers for webservice providers are to be declared in reverse order (JAX-WS JSR 224).<br/>").println();
    out.printf("This file is updated once any WSDL artifact or %s changes. If providing a handler chain binding file yourself, this file will not be generated.", descriptor.getDescriptor().getSimpleName()).println();
    out.println();

    // Create the XML content. For webservice providers, handlers are to be registered in the reverse order (JAX-WS JSR 224).
    final Document xmlDocument = builder.newDocument();
    xmlDocument.appendChild(xmlDocument.createComment(comment.toString()));
    final Node xmlHandlerChains = xmlDocument.appendChild(xmlDocument.createElementNS("http://java.sun.com/xml/ns/javaee", "handler-chains"));
    for (int i = handlers.size() - 1; i >= 0; i--) {
      final Node xmlHandlerChain = xmlHandlerChains.appendChild(xmlDocument.createElement("handler-chain"));
      xmlHandlerChain.appendChild(xmlDocument.createComment(String.format(" Executed as %s. handler", i + 1)));
      final Node xmlHandlerNode = xmlHandlerChain.appendChild(xmlDocument.createElement("handler"));
      final Node xmlHandlerClassNode = xmlHandlerNode.appendChild(xmlDocument.createElement("handler-class"));
      xmlHandlerClassNode.setTextContent(handlers.get(i));
    }

    final String xmlContent = XmlUtility.wellformDocument(xmlDocument);

    // Determine the package and file name.
    final String path = portTypeProxy._package().name();
    final String fileName = AptUtil.toSimpleName(descriptor.getProxyQualifiedName()) + "_" + HANDLERS_FILE_NAME;

    // Persist the handler file.
    try (PrintWriter w = new PrintWriter(filer.createResource(StandardLocation.SOURCE_OUTPUT, path, fileName).openWriter())) {
      w.write(xmlContent);
      w.flush();
    }

    return fileName;
  }
}
