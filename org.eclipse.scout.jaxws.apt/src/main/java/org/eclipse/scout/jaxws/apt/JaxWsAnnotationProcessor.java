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
package org.eclipse.scout.jaxws.apt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Generated;
import javax.annotation.Resource;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.http.HTTPException;

import org.eclipse.scout.jaxws.apt.internal.EntryPointDefinition;
import org.eclipse.scout.jaxws.apt.internal.EntryPointDefinition.HandlerDefinition;
import org.eclipse.scout.jaxws.apt.internal.HandlerArtifactProcessor;
import org.eclipse.scout.jaxws.apt.internal.codemodel.JConditionalEx;
import org.eclipse.scout.jaxws.apt.internal.codemodel.JExprEx;
import org.eclipse.scout.jaxws.apt.internal.codemodel.JTypeParser;
import org.eclipse.scout.jaxws.apt.internal.util.AnnotationUtil;
import org.eclipse.scout.jaxws.apt.internal.util.AptLogger;
import org.eclipse.scout.jaxws.apt.internal.util.AptUtil;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.WebServiceEntryPoint;
import org.eclipse.scout.rt.server.jaxws.provider.context.JaxWsServletRunContexts;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Annotation processor to generate an entry point for endpoint interfaces based on the existence of
 * {@link WebServiceEntryPoint} annotation.
 * <p>
 * Based on the configuration of {@link WebServiceEntryPoint}, other artifacts like handlers are generated.
 *
 * @see WebServiceEntryPoint
 * @since 5.1
 */
@SupportedAnnotationTypes({"javax.jws.WebService", "org.eclipse.scout.rt.server.jaxws.provider.annotation.WebServiceEntryPoint"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JaxWsAnnotationProcessor extends AbstractProcessor {

  protected static final String LOGGER_FIELD_NAME = "LOG";
  protected static final String WEBSERVICE_CONTEXT_FIELD_NAME = "m_webServiceContext";
  protected static final String HANDLE_UNDECLARED_FAULT_METHOD_NAME = "handleUndeclaredFault";
  protected static final String SERVLET_RUN_CONTEXT_FIELD_NAME = "servletRunContext";
  protected static final String RUN_CONTEXT_FIELD_NAME = "requestRunContext";

  private AptLogger m_logger;

  @Override
  public synchronized void init(final ProcessingEnvironment env) {
    m_logger = new AptLogger(env);
    super.init(env);
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    m_logger.info("Annotation processing started...");

    if (roundEnv.processingOver()) {
      return true;
    }

    generateEntryPoints(roundEnv);

    return true;
  }

  /**
   * Generates an <em>entry point port type</em> for all declared {@link WebServiceEntryPoint} annotations.
   */
  protected void generateEntryPoints(final RoundEnvironment roundEnv) {
    // Collect endpoint interfaces.
    final Set<String> endpointInterfaceNames = collectEndpointInterfaceNames(roundEnv);

    // Collect 'entryPointDefinitions' to generate entry point port types.
    for (final Element _element : roundEnv.getElementsAnnotatedWith(WebServiceEntryPoint.class)) {
      final TypeElement _entryPointDefinition = (TypeElement) _element;
      String endpointInterfaceName = null;
      try {
        final AnnotationMirror _entryPointAnnotationMirror = AnnotationUtil.findAnnotationMirror(WebServiceEntryPoint.class.getName(), _entryPointDefinition);

        // Resolve and validate the endpoint interface.
        final TypeElement _endpointInterface = AnnotationUtil.getTypeElement(_entryPointAnnotationMirror, "endpointInterface", processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        Assertions.assertNotNull(_endpointInterface, "Failed to resolve endpoint interface [entryPointDefinition={}]", _entryPointDefinition.getQualifiedName().toString());
        endpointInterfaceName = _endpointInterface.getQualifiedName().toString();

        Assertions.assertNotNull(_endpointInterface.getAnnotation(WebService.class), "Invalid endpoint interface. Must be annoated with {} annotation [entryPointDefinition={}, endpointInterface={}]",
            WebService.class.getSimpleName(),
            _entryPointDefinition.getQualifiedName().toString(),
            endpointInterfaceName);

        // Mark endpoint interface as processed.
        endpointInterfaceNames.remove(endpointInterfaceName);

        final EntryPointDefinition entryPointDefinition = new EntryPointDefinition((TypeElement) _entryPointDefinition, _endpointInterface, processingEnv);
        m_logger.info("Generating entry point for endpoint interface '{}' [entryPoint={}, wsdl:portType={}, wsdl:service={}, wsdl:port={}]",
            endpointInterfaceName,
            entryPointDefinition.getEntryPointQualifiedName(),
            entryPointDefinition.getPortTypeName(),
            entryPointDefinition.getServiceName(),
            entryPointDefinition.getPortName());
        generateEntryPoint(entryPointDefinition, roundEnv);
      }
      catch (final Exception e) {
        m_logger.error("Failed to generate entry point for endpoint interface '{}' [entryPointDefinition={}]", endpointInterfaceName, _entryPointDefinition.getQualifiedName().toString(), e);
      }
    }

    // Log endpoint interfaces for which no entry point was generated.
    for (final String endpointInterfaceName : endpointInterfaceNames) {
      m_logger.info("Skipped entry point generation for endpoint interface '{}', because not configured with {}.", endpointInterfaceName, WebServiceEntryPoint.class.getSimpleName());
    }
  }

  /**
   * Collects the qualified name of all endpoint interfaces annotated with {@link WebService}.
   */
  protected Set<String> collectEndpointInterfaceNames(final RoundEnvironment roundEnv) {
    final Set<String> endpointInterfaceNames = new HashSet<>();

    for (final Element candidate : roundEnv.getElementsAnnotatedWith(WebService.class)) {
      if (!(candidate instanceof TypeElement)) {
        continue; // must be a type
      }

      if (!ElementKind.INTERFACE.equals(candidate.getKind())) {
        continue; // must be an interface
      }

      if (candidate.getAnnotation(WebServiceEntryPoint.class) != null) {
        continue; // ignore entry point definitions
      }

      endpointInterfaceNames.add(((TypeElement) candidate).getQualifiedName().toString());
    }

    return endpointInterfaceNames;
  }

  /**
   * Generates the entry point and associated artifacts for the given definition.
   */
  protected void generateEntryPoint(final EntryPointDefinition entryPointDefinition, final RoundEnvironment roundEnv) throws Exception {
    final JCodeModel model = new JCodeModel();

    // Create EntryPoint class.
    final TypeElement _endpointInterface = entryPointDefinition.getEndpointInterface();
    final JClass endpointInterface = model.ref(_endpointInterface.getQualifiedName().toString());
    final JDefinedClass entryPoint = model._class(entryPointDefinition.getEntryPointQualifiedName())._implements(endpointInterface);

    // Add annotations to the EntryPoint.
    addAnnotations(model, entryPoint, entryPointDefinition, roundEnv);

    // Create handler chain.
    final HandlerChain _handlerChainAnnotation = _endpointInterface.getAnnotation(HandlerChain.class);
    if (_handlerChainAnnotation != null) {
      m_logger.info("Handler file not generated because provided as binding file [file={}, entryPoint={}, endpointInterface={}]",
          _handlerChainAnnotation.file(),
          entryPointDefinition.getEntryPointQualifiedName(),
          entryPointDefinition.getEndpointInterfaceQualifiedName());
    }
    else if (!entryPointDefinition.getHandlerChain().isEmpty() || entryPointDefinition.isAuthenticationEnabled()) {
      entryPoint.annotate(HandlerChain.class).param("file", new HandlerArtifactProcessor().generateHandlerArtifacts(entryPoint, entryPointDefinition, processingEnv, m_logger));
    }

    // Add JavaDoc to the EntryPoint.
    AptUtil.addJavaDoc(entryPoint, createJavaDocForEntryPoint(entryPointDefinition));

    // Create the logger field.
    final JFieldVar logger = entryPoint.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, org.slf4j.Logger.class, LOGGER_FIELD_NAME, model.ref(org.slf4j.LoggerFactory.class).staticInvoke("getLogger").arg(entryPoint.dotclass()));
    // Inject WebServiceContext
    final JFieldVar webServiceContext = entryPoint.field(JMod.PROTECTED, WebServiceContext.class, WEBSERVICE_CONTEXT_FIELD_NAME);
    webServiceContext.annotate(Resource.class);

    // Overwrite all methods declared on the PortType interface.
    for (final Element _element : _endpointInterface.getEnclosedElements()) {
      if (!(ElementKind.METHOD.equals(_element.getKind()))) {
        continue;
      }
      final ExecutableElement _method = (ExecutableElement) _element;

      final String methodName = _method.getSimpleName().toString();
      final JType returnType = JTypeParser.parseType(model, _method.getReturnType());

      // Create the method.
      final JMethod method = entryPoint.method(JMod.PUBLIC, returnType, methodName);
      method.annotate(Override.class);

      // Add the method parameters.
      for (final VariableElement _param : _method.getParameters()) {
        method.param(JMod.FINAL, JTypeParser.parseType(model, _param.asType()), _param.getSimpleName().toString());
      }

      // Add exception throw clauses.
      final List<JClass> throwTypes = new ArrayList<>();
      for (final TypeMirror _throwType : _method.getThrownTypes()) {
        final JClass throwType = model.ref(_throwType.toString());
        throwTypes.add(throwType);
        method._throws(throwType);
      }

      // Create the method implementation.
      addEntryPointMethodImplementation(model, webServiceContext, method, throwTypes, TypeKind.VOID.equals(_method.getReturnType().getKind()), _endpointInterface.getQualifiedName().toString());
    }

    // Create the method to handle undeclared errors.
    addHandleUndeclaredFaultMethod(model, entryPoint, logger);

    // Build and persist this compilation unit.
    AptUtil.buildAndPersist(model, processingEnv.getFiler());

    m_logger.info("Entry point successfully generated. [entryPoint={}, endpointInterface={}, entryPointDefinition={}]", entryPoint.fullName(), endpointInterface.fullName(),
        entryPointDefinition.getQualifiedName());
  }

  /**
   * Creates the implementation of a entry point method.
   */
  @Internal
  protected void addEntryPointMethodImplementation(final JCodeModel model, final JFieldVar webServiceContext, final JMethod method, final List<JClass> throwTypes, final boolean voidMethod, final String endpointInterfaceName) {
    final JBlock methodBody = method.body();

    // Declare RunContext variables.
    final JVar servletRunContext = methodBody
        .decl(JMod.FINAL, model.ref(RunContext.class), SERVLET_RUN_CONTEXT_FIELD_NAME, model.ref(JaxWsServletRunContexts.class)
            .staticInvoke("copyCurrent")
            .invoke("withWebServiceContext")
            .arg(webServiceContext));
    final JVar runContext = methodBody
        .decl(JMod.FINAL, model.ref(RunContext.class), RUN_CONTEXT_FIELD_NAME, model.ref(MessageContexts.class)
            .staticInvoke("getRunContext")
            .arg(webServiceContext.invoke("getMessageContext")));

    final JTryBlock tryBlock = methodBody._try();

    // Invoke port type on behalf of RunContext.
    final JInvocation runContextInvocation = createRunContextInvocation(model, servletRunContext, runContext, voidMethod, method, endpointInterfaceName);
    if (voidMethod) {
      tryBlock.body().add(runContextInvocation);
    }
    else {
      tryBlock.body()._return(runContextInvocation);
    }

    // Create exception handling.
    final JCatchBlock catchBlock = tryBlock._catch(model.ref(Exception.class));
    final JVar caughtException = catchBlock.param("e");
    final JBlock catchBody = catchBlock.body();

    if (throwTypes.isEmpty()) {
      // webservice method has no faults declared.
      catchBody._throw(JExpr.invoke(HANDLE_UNDECLARED_FAULT_METHOD_NAME).arg(caughtException));
    }
    else {
      // handle declared webservice faults.
      final JConditionalEx condition = new JConditionalEx(catchBody);
      for (final JClass throwType : throwTypes) {
        condition._elseif(caughtException._instanceof(throwType))._throw(JExprEx.cast(throwType, caughtException));
      }
      condition._else()._throw(JExpr.invoke(HANDLE_UNDECLARED_FAULT_METHOD_NAME).arg(caughtException));
    }
  }

  /**
   * Creates code to invoke the port type on behalf of the RunContext.
   */
  @Internal
  protected JInvocation createRunContextInvocation(final JCodeModel model, final JVar servletRunContext, final JVar runContext, final boolean voidMethod, final JMethod portTypeMethod, final String endpointInterfaceName) {
    final JType returnType;
    final JDefinedClass servletRunContextCallable;
    final JDefinedClass runContextCallable;
    final String runMethodName;
    if (voidMethod) {
      returnType = model.ref(Void.class).unboxify();
      servletRunContextCallable = model.anonymousClass(IRunnable.class);
      runContextCallable = model.anonymousClass(IRunnable.class);
      runMethodName = "run";
    }
    else {
      returnType = portTypeMethod.type().boxify();
      servletRunContextCallable = model.anonymousClass(model.ref(Callable.class).narrow(returnType));
      runContextCallable = model.anonymousClass(model.ref(Callable.class).narrow(returnType));
      runMethodName = "call";
    }

    // Invoke the bean method.
    final JInvocation beanInvocation = model.ref(BEANS.class).staticInvoke("get").arg(model.ref(endpointInterfaceName).dotclass()).invoke(portTypeMethod.name());
    for (final JVar parameter : portTypeMethod.listParams()) {
      beanInvocation.arg(parameter);
    }

    // Implement RunContext callable.
    final JMethod runContextRunMethod = runContextCallable.method(JMod.PUBLIC | JMod.FINAL, returnType, runMethodName)._throws(Exception.class);
    runContextRunMethod.annotate(Override.class);
    if (voidMethod) {
      runContextRunMethod.body().add(beanInvocation);
    }
    else {
      runContextRunMethod.body()._return(beanInvocation);
    }

    // Create RunContext invocations.
    final JExpression exceptionTranslator = model.ref(DefaultExceptionTranslator.class).dotclass();

    final JInvocation servletRunContextInvocation = servletRunContext.invoke(runMethodName).arg(JExpr._new(servletRunContextCallable)).arg(exceptionTranslator);
    final JInvocation runContextInvocation = runContext.invoke(runMethodName).arg(JExpr._new(runContextCallable)).arg(exceptionTranslator);

    // Implement ServletRunContext callable.
    final JMethod servletRunContextRunMethod = servletRunContextCallable.method(JMod.PUBLIC | JMod.FINAL, returnType, runMethodName)._throws(Exception.class);
    servletRunContextRunMethod.annotate(Override.class);

    final JConditionalEx servletRunContextCondition = new JConditionalEx(servletRunContextRunMethod.body());

    // Assemble the methods.
    if (voidMethod) {
      servletRunContextCondition._if(runContext.eq(JExpr._null())).add(beanInvocation); // directly invoke Bean method.
      servletRunContextCondition._else().add(runContextInvocation); // call RunContext to invoke Bean method.
    }
    else {
      servletRunContextCondition._if(runContext.eq(JExpr._null()))._return(beanInvocation); // directly invoke Bean method.
      servletRunContextCondition._else()._return(runContextInvocation); // call RunContext to invoke Bean method.
    }
    return servletRunContextInvocation;
  }

  /**
   * Adds the method to handle undeclared exceptions which are not declared in the WSDL.
   */
  @Internal
  protected void addHandleUndeclaredFaultMethod(final JCodeModel model, final JDefinedClass entryPoint, final JFieldVar logger) {
    // Create the method to handle undeclared faults.
    final JMethod method = entryPoint.method(JMod.PROTECTED, RuntimeException.class, HANDLE_UNDECLARED_FAULT_METHOD_NAME);
    method.annotate(Internal.class);

    final JVar exceptionParam = method.param(JMod.FINAL, Exception.class, "e");

    final JConditionalEx condition = new JConditionalEx(method.body());

    // Handle RuntimeException
    final JType runtimeException = model._ref(RuntimeException.class);
    condition._if(exceptionParam._instanceof(runtimeException))._throw(JExprEx.cast(runtimeException, exceptionParam));

    // Handle other exception
    final JBlock otherExceptionBlock = condition._else();
    otherExceptionBlock.invoke(logger, "error").arg(JExpr.lit("Undeclared exception while processing webservice request")).arg(exceptionParam);
    otherExceptionBlock._throw(JExpr._new(model.ref(HTTPException.class)).arg(model.ref(HttpServletResponse.class).staticRef("SC_INTERNAL_SERVER_ERROR")));
  }

  /**
   * Adds annotations to the EntryPoint.
   */
  protected void addAnnotations(final JCodeModel model, final JDefinedClass entryPoint, final EntryPointDefinition entryPointDefinition, final RoundEnvironment roundEnv) {
    // Add 'Generated' annotation
    final JAnnotationUse generatedAnnotation = entryPoint.annotate(Generated.class);
    generatedAnnotation.param("value", JaxWsAnnotationProcessor.class.getName());
    generatedAnnotation.param("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ").format(new Date()));
    generatedAnnotation.param("comments", "EntryPoint to run webservice requests on behalf of a RunContext");

    // Add 'WebService' annotation
    if (!entryPointDefinition.containsAnnotation(WebService.class)) {
      final WebService _webServiceAnnotation = entryPointDefinition.getEndpointInterface().getAnnotation(WebService.class);
      final JAnnotationUse webServiceAnnotation = entryPoint.annotate(WebService.class);
      webServiceAnnotation.param("name", _webServiceAnnotation.name());
      webServiceAnnotation.param("targetNamespace", _webServiceAnnotation.targetNamespace());
      webServiceAnnotation.param("endpointInterface", entryPointDefinition.getEndpointInterfaceQualifiedName());

      if (StringUtility.hasText(entryPointDefinition.getServiceName())) {
        webServiceAnnotation.param("serviceName", entryPointDefinition.getServiceName());
      }
      else {
        m_logger.warn("No 'serviceName' specified, which is required if running in a EE container with 'webservice auto-discovery' enabled. [entryPoint={}, endpointInterface={}]",
            entryPointDefinition.getSimpleName(),
            entryPointDefinition.getEndpointInterfaceSimpleName());
      }

      if (StringUtility.hasText(entryPointDefinition.getPortName())) {
        webServiceAnnotation.param("portName", entryPointDefinition.getPortName());
      }
      else {
        m_logger.warn("No 'portName' specified, which is required if running in a EE container with 'webservice auto-discovery' enabled. [entryPoint={}, endpointInterface={}]",
            entryPointDefinition.getSimpleName(),
            entryPointDefinition.getEndpointInterfaceSimpleName());
      }

      if (entryPointDefinition.isWsdlLocationDerived()) {
        final WebServiceClient _webServiceClientAnnotation = findWebServiceClientAnnotation(roundEnv, entryPointDefinition.getServiceName());
        if (_webServiceClientAnnotation != null) {
          webServiceAnnotation.param("wsdlLocation", _webServiceClientAnnotation.wsdlLocation());
        }
        else if (!StringUtility.hasText(entryPointDefinition.getServiceName())) {
          m_logger.warn("Cannot derive 'wsdlLocation' because no 'serviceName'. [entryPoint={}, endpointInterface={}]",
              entryPointDefinition.getSimpleName(),
              entryPointDefinition.getEndpointInterfaceSimpleName());
        }
        else {
          m_logger.warn("Cannot derive 'wsdlLocation' because no Service annotated with '@WebServiceClient(name=\"{}\")' found. [entryPoint={}, endpointInterface={}]",
              entryPointDefinition.getServiceName(),
              entryPointDefinition.getSimpleName(),
              entryPointDefinition.getEndpointInterfaceSimpleName());
        }
      }
      else if (StringUtility.hasText(entryPointDefinition.getWsdlLocation())) {
        webServiceAnnotation.param("wsdlLocation", entryPointDefinition.getWsdlLocation());
      }
    }

    // Add custom annotations
    AnnotationUtil.addAnnotations(model, entryPoint, entryPointDefinition.getSiblingAnnotations());
  }

  /**
   * Returns {@link WebService} for the given service name, else <code>null</code>.
   */
  @Internal
  protected WebServiceClient findWebServiceClientAnnotation(final RoundEnvironment roundEnv, final String serviceName) {
    for (final Element _annotatedElement : roundEnv.getElementsAnnotatedWith(WebServiceClient.class)) {
      final WebServiceClient candidate = _annotatedElement.getAnnotation(WebServiceClient.class);
      if (serviceName.equals(candidate.name())) {
        return candidate;
      }
    }
    return null;
  }

  private String createJavaDocForEntryPoint(final EntryPointDefinition entryPointDefinition) {
    final StringWriter writer = new StringWriter();
    final PrintWriter out = new PrintWriter(writer);

    out.printf("This class is auto-generated by APT triggered by Maven build based on {@link %s}.", entryPointDefinition.getSimpleName()).println();
    out.println("<p>");

    out.println("This entry point ensures webservice requests to run in a {@link RunContext}, and optionally enforces authentication. Following this, the request is propagated to the implementing port type.");
    out.println("<p>");

    out.println("<table>");

    out.printf("<tr><td>Entry point definition:</td><td>{@link %s}</td>", entryPointDefinition.getSimpleName()).println();
    out.printf("<tr><td>Endpoint interface:</td><td>{@link %s}</td>", entryPointDefinition.getEndpointInterfaceSimpleName()).println();

    // Authentication
    if (entryPointDefinition.isAuthenticationEnabled()) {
      out.printf("<tr><td>Authentication method:</td><td>{@link %s}</td>", AptUtil.toSimpleName(entryPointDefinition.getAuthMethod())).println();
      out.printf("<tr><td>Credential verifier:</td><td>{@link %s}</td>", AptUtil.toSimpleName(entryPointDefinition.getAuthVerifier())).println();
    }

    // Handlers
    if (entryPointDefinition.getHandlerChain().isEmpty()) {
      out.println("<tr><td>Handler chain:</td><td>none</td>");
    }
    else {
      final List<String> handlers = new ArrayList<>();
      for (final HandlerDefinition handlerDefinition : entryPointDefinition.getHandlerChain()) {
        handlers.add(String.format("{@link %s}", handlerDefinition.getHandlerSimpleName()));
      }
      out.printf("<tr><td>Handler chain:</td><td>%s</td>", StringUtility.join(", ", handlers)).println();
    }
    out.println("</table>");
    out.println("<ul>");
    out.println("<li>To rebuild <em>stub</em> and <em>entry point</em>, run 'mvn clean compile', or update the Maven Project in Eclipse IDE by pressing 'Ctrl+F5'.</li>");
    out.println("<li>When running an incremental build, <em>stub</em> and <em>entry point</em> are only re-generated if either WSDL, schema or binding files change, or '/target/jaxws/wsartifact-hash' is deleted manually.</li>");
    out.println("</ul>");

    final StringWriter newLine = new StringWriter();
    new PrintWriter(newLine).println();

    return writer.toString().replace(newLine.toString(), "\n"); // remove double new-lines
  }
}
