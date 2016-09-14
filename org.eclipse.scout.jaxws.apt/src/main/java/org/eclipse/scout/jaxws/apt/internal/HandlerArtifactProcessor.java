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

import static java.lang.String.format;

import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.jaxws.apt.JaxWsAnnotationProcessor;
import org.eclipse.scout.jaxws.apt.internal.EntryPointDefinition.HandlerDefinition;
import org.eclipse.scout.jaxws.apt.internal.util.AptLogger;
import org.eclipse.scout.jaxws.apt.internal.util.AptUtil;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.WebServiceEntryPoint;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.AuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.provider.handler.HandlerDelegate;
import org.eclipse.scout.rt.server.jaxws.provider.handler.SOAPHandlerDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

/**
 * Processor to generate authentication handler, handler entry points and the handler chain XML file.
 *
 * @since 5.1
 */
public class HandlerArtifactProcessor {

  private static final String AUTH_HANDLER_NAME = "AuthHandler";
  private static final String HANDLER_SUFFIX = "";
  private static final String HANDLERS_FILE_NAME = "handler-chain.xml";

  /**
   * @return relative filename of the XML file.
   */
  public String generateHandlerArtifacts(final JClass portTypeEntryPoint, final EntryPointDefinition portTypeEntryPointDefinition, final ProcessingEnvironment env, final AptLogger logger) {
    final String endpointInterface = portTypeEntryPointDefinition.getEndpointInterfaceQualifiedName();

    final List<String> handlers = new ArrayList<>();

    // Add configured handlers.
    final List<HandlerDefinition> handlerChain = portTypeEntryPointDefinition.getHandlerChain();
    for (int idx = 0; idx < handlerChain.size(); idx++) {
      final HandlerDefinition handlerDefinition = handlerChain.get(idx);

      try {
        final String handlerDelegateQualifiedName = createAndPersistHandlerDelegate(portTypeEntryPoint, portTypeEntryPointDefinition, handlerDefinition, idx, env);
        logger.info("Generate handler delegate [handler={}, handlerDelegate={}, portTypeDefinition={}, endpointInterface={}]",
            handlerDefinition.getHandlerQualifiedName(),
            handlerDelegateQualifiedName,
            portTypeEntryPointDefinition.getQualifiedName(),
            endpointInterface);
        handlers.add(handlerDelegateQualifiedName);
      }
      catch (final Exception e) {
        throw new JaxWsProcessorException(
            format("Failed to generate handler delegate [handler=%s, portTypeDefinition=%s, endpointInterface=%s]", handlerDefinition.getHandlerQualifiedName(), portTypeEntryPointDefinition.getQualifiedName(), endpointInterface),
            e);
      }
    }

    // Generate AuthHandler
    if (portTypeEntryPointDefinition.isAuthenticationEnabled()) {
      // Ensure valid order.
      int order = portTypeEntryPointDefinition.getAuthentication().order();
      if (order < 0) {
        logger.info("Illegal position for authentication handler specified. Register the handler as first handler in the handler chain.  [position={}, portTypeDefinition={}, endpointInterface={}]",
            order,
            portTypeEntryPointDefinition.getQualifiedName(),
            endpointInterface);
        order = 0;
      }
      else if (order > handlerChain.size()) {
        logger.info("Illegal position for authentication handler specified. Register the handler as last handler in the handler chain.  [position={}, portTypeDefinition={}, endpointInterface={}]",
            order,
            portTypeEntryPointDefinition.getQualifiedName(),
            endpointInterface);
        order = handlerChain.size();
      }

      // Generate the AuthHandler
      try {
        final String authHandlerQualifiedName = createAndPersistAuthHandler(portTypeEntryPoint, portTypeEntryPointDefinition, env);
        logger.info("Generate AuthHandler [authHandler={}, portTypeDefinition={}, endpointInterface={}]",
            authHandlerQualifiedName,
            portTypeEntryPointDefinition.getQualifiedName(),
            endpointInterface);
        handlers.add(order, authHandlerQualifiedName);
      }
      catch (final Exception e) {
        throw new JaxWsProcessorException(format("Failed to generate AuthHandler [portTypeDefinition=%s, endpointInterface=%s]", portTypeEntryPointDefinition.getQualifiedName(), endpointInterface), e);
      }
    }

    // Generate handler-chain.xml.
    try {
      final String handlerChainFile = createAndPersistHandlerXmlFile(portTypeEntryPoint, portTypeEntryPointDefinition, handlers, env.getFiler());
      logger.info("Generate handler chain XML-file [file={}, portTypeDefinition={}, endpointInterface={}]",
          handlerChainFile,
          portTypeEntryPointDefinition.getQualifiedName(),
          endpointInterface);

      return handlerChainFile;
    }
    catch (final Exception e) {
      throw new JaxWsProcessorException(format("Failed to generate handler chain XML-file [portTypeDefinition=%s, endpointInterface=%s]", portTypeEntryPointDefinition.getQualifiedName(), endpointInterface), e);
    }
  }

  /**
   * Generates the AuthHandler.
   */
  public String createAndPersistAuthHandler(final JClass portTypeEntryPoint, final EntryPointDefinition entryPointDefinition, final ProcessingEnvironment processingEnv) throws IOException, JClassAlreadyExistsException {
    final JCodeModel model = new JCodeModel();

    final String fullName = entryPointDefinition.getEntryPointQualifiedName() + "_" + AUTH_HANDLER_NAME;
    final JDefinedClass authHandler = model._class(fullName)._extends(model.ref(AuthenticationHandler.class));

    // Add 'Generated' annotation
    final JAnnotationUse generatedAnnotation = authHandler.annotate(Generated.class);
    generatedAnnotation.param("value", JaxWsAnnotationProcessor.class.getName());
    generatedAnnotation.param("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ").format(new Date()));
    generatedAnnotation.param("comments", format("Authentication Handler for [method=%s, verifier=%s]", AptUtil.toSimpleName(entryPointDefinition.getAuthMethod()), AptUtil.toSimpleName(entryPointDefinition.getAuthVerifier())));

    // Add default constructor with super call to provide authentication annotation.
    final JMethod defaultConstructor = authHandler.constructor(JMod.PUBLIC);
    final JClass entryPointDefinitionClass = model.ref(entryPointDefinition.getQualifiedName());
    defaultConstructor.body().invoke("super").arg(JExpr.dotclass(entryPointDefinitionClass).invoke("getAnnotation").arg(JExpr.dotclass(model.ref(WebServiceEntryPoint.class))).invoke("authentication"));

    AptUtil.addJavaDoc(authHandler,
        format("This class is auto-generated by APT triggered by Maven build and is based on the authentication configuration declared in {@link %s}.", entryPointDefinition.getSimpleName()));

    AptUtil.buildAndPersist(model, processingEnv.getFiler());

    return authHandler.fullName();
  }

  /**
   * Generates the entry point for a handler.
   */
  public String createAndPersistHandlerDelegate(final JClass portTypeEntryPoint, final EntryPointDefinition entryPointDefinition, final HandlerDefinition handler, final int idx, final ProcessingEnvironment env)
      throws IOException, JClassAlreadyExistsException {
    final JCodeModel model = new JCodeModel();

    final String fullName = entryPointDefinition.getEntryPointQualifiedName() + "_" + handler.getHandlerSimpleName() + HANDLER_SUFFIX;
    final JDefinedClass handlerDelegate = model._class(fullName);

    // Add 'Generated' annotation
    final JAnnotationUse generatedAnnotation = handlerDelegate.annotate(Generated.class);
    generatedAnnotation.param("value", JaxWsAnnotationProcessor.class.getName());
    generatedAnnotation.param("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ").format(new Date()));
    generatedAnnotation.param("comments", "Handler delegate for " + handler.getHandlerQualifiedName());

    AptUtil.addJavaDoc(handlerDelegate, format("This class is auto-generated by APT triggered by Maven build and is based on the handler configuration declared in {@link %s}.", entryPointDefinition.getSimpleName()));

    switch (handler.getHandlerType()) {
      case SOAP:
        handlerDelegate._extends(model.ref(SOAPHandlerDelegate.class));
        break;
      case LOGICAL:
        handlerDelegate._extends(model.ref(HandlerDelegate.class).narrow(LogicalMessageContext.class));
        handlerDelegate._implements(model.ref(LogicalHandler.class).narrow(LogicalMessageContext.class));
        break;
      default:
        handlerDelegate._extends(model.ref(HandlerDelegate.class).narrow(MessageContext.class));
        break;
    }

    // Add default constructor with super call to provide handler annotation.
    final JClass entryPointDefinitionClass = model.ref(entryPointDefinition.getQualifiedName());
    final JMethod defaultConstructor = handlerDelegate.constructor(JMod.PUBLIC);
    defaultConstructor.body().invoke("super").arg(JExpr.dotclass(entryPointDefinitionClass).invoke("getAnnotation").arg(JExpr.dotclass(model.ref(WebServiceEntryPoint.class))).invoke("handlerChain").component(JExpr.lit(idx)));

    AptUtil.buildAndPersist(model, env.getFiler());

    return handlerDelegate.fullName();
  }

  protected String createAndPersistHandlerXmlFile(final JClass portTypeEntryPoint, final EntryPointDefinition entryPointDefinition, final List<String> handlers, final Filer filer) throws ParserConfigurationException, IOException {
    final DocumentBuilder builder = XmlUtility.newDocumentBuilder();

    // Create the comment.
    final StringWriter comment = new StringWriter();
    final PrintWriter out = new PrintWriter(comment);
    out.println();
    out.printf("This file is auto-generated by APT triggered by Maven build, and is populated with handlers declared in '%s'. This file is referenced in @%s annotation in '%s'.<br/>", entryPointDefinition.getSimpleName(),
        HandlerChain.class.getSimpleName(), portTypeEntryPoint.name()).println();
    out.printf("Please note that handlers for webservice providers are to be declared in reverse order (JAX-WS JSR 224).<br/>").println();
    out.printf("This file is updated once any WSDL artifact or %s changes. If providing a handler chain binding file yourself, this file will not be generated.", entryPointDefinition.getSimpleName()).println();
    out.println();

    // Create the XML content. For webservice providers, handlers are to be registered in the reverse order (JAX-WS JSR 224).
    final Document xmlDocument = builder.newDocument();
    xmlDocument.appendChild(xmlDocument.createComment(comment.toString()));
    final Node xmlHandlerChains = xmlDocument.appendChild(xmlDocument.createElementNS("http://java.sun.com/xml/ns/javaee", "handler-chains"));
    for (int i = handlers.size() - 1; i >= 0; i--) {
      final Node xmlHandlerChain = xmlHandlerChains.appendChild(xmlDocument.createElement("handler-chain"));
      xmlHandlerChain.appendChild(xmlDocument.createComment(format(" Executed as %s. handler", i + 1)));
      final Node xmlHandlerNode = xmlHandlerChain.appendChild(xmlDocument.createElement("handler"));
      final Node xmlHandlerClassNode = xmlHandlerNode.appendChild(xmlDocument.createElement("handler-class"));
      xmlHandlerClassNode.setTextContent(handlers.get(i));
    }

    final String xmlContent = XmlUtility.wellformDocument(xmlDocument);

    // Determine the package and file name.
    final String path = portTypeEntryPoint._package().name();
    final String fileName = AptUtil.toSimpleName(entryPointDefinition.getEntryPointQualifiedName()) + "_" + HANDLERS_FILE_NAME;

    // Persist the handler file.
    try (PrintWriter w = new PrintWriter(filer.createResource(StandardLocation.SOURCE_OUTPUT, path, fileName).openWriter())) {
      w.write(xmlContent);
      w.flush();
    }

    return fileName;
  }
}
