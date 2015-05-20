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
package org.eclipse.scout.jaxws.apt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.xml.ws.soap.MTOM;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.apt.internal.HandlerArtifactProcessor;
import org.eclipse.scout.jaxws.apt.internal.PortTypeProxyDescriptor;
import org.eclipse.scout.jaxws.apt.internal.PortTypeProxyDescriptor.HandlerDescriptor;
import org.eclipse.scout.jaxws.apt.internal.codemodel.JConditionalEx;
import org.eclipse.scout.jaxws.apt.internal.codemodel.JExprEx;
import org.eclipse.scout.jaxws.apt.internal.codemodel.JTypeParser;
import org.eclipse.scout.jaxws.apt.internal.util.AnnotationUtil;
import org.eclipse.scout.jaxws.apt.internal.util.AptUtil;
import org.eclipse.scout.jaxws.apt.internal.util.Logger;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.JaxWsPortTypeDecorator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.AuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.provider.context.JaxWsRunContexts;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Annotation processor to generate a proxy for each webservice endpoint, so that webservice requests are run on behalf
 * of a {@link RunContext}. Based on the existence of an associated {@link JaxWsPortTypeDecorator}, other artifacts like
 * authentication handler, proxies for handlers and handler-chain XML file are generated.
 *
 * @since 5.1
 */
@SupportedAnnotationTypes({"javax.jws.WebService", "org.eclipse.scout.rt.server.jaxws.provider.annotation.JaxWsPortTypeDecorator"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PortTypeProxyAnnotationProcessor extends AbstractProcessor {

  public static final String PORT_TYPE_PROXY_SUFFIX = "Proxy";

  protected static final String LOGGER_FIELD_NAME = "LOG";
  protected static final String WEBSERVICE_CONTEXT_FIELD_NAME = "m_webServiceContext";
  protected static final String HANDLE_UNDECLARED_FAULT_METHOD_NAME = "handleUndeclaredFault";
  protected static final String JAXWS_RUN_CONTEXT_FIELD_NAME = "jaxwsRunContext";
  protected static final String HANDLER_RUN_CONTEXT_FIELD_NAME = "handlerRunContext";

  private Logger m_logger;

  @Override
  public synchronized void init(final ProcessingEnvironment env) {
    m_logger = new Logger(env);
    super.init(env);
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    m_logger.logInfo("Annotation processing started...");

    if (roundEnv.processingOver()) {
      return true;
    }

    generatePortTypeProxyForEachEndpoint(roundEnv);

    return true;
  }

  /**
   * Generates a PortTypeProxy for each port type found.
   */
  protected void generatePortTypeProxyForEachEndpoint(final RoundEnvironment roundEnv) {
    for (final Element candidate : roundEnv.getElementsAnnotatedWith(WebService.class)) {
      if (candidate instanceof TypeElement && ElementKind.INTERFACE.equals(candidate.getKind()) && candidate.getAnnotation(JaxWsPortTypeDecorator.class) == null /* skip decorators */) {
        final TypeElement portTypeInterface = (TypeElement) candidate;
        try {
          m_logger.logInfo("Generating PortTypeProxy for '%s'", portTypeInterface.getQualifiedName());
          generatePortTypeProxy(portTypeInterface, roundEnv);
        }
        catch (final Exception e) {
          m_logger.logError(e, e.getMessage());
        }
      }
    }
  }

  /**
   * Generates the PortTypeProxy and associated artifacts for the given port type.
   */
  protected void generatePortTypeProxy(final TypeElement _interface, final RoundEnvironment roundEnv) throws Exception {
    final JCodeModel model = new JCodeModel();

    final JDefinedClass portTypeProxy = createPortTypeProxyClass(model, _interface, roundEnv);

    // Create the logger field.
    final JFieldVar logger = portTypeProxy.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, IScoutLogger.class, LOGGER_FIELD_NAME, model.ref(ScoutLogManager.class).staticInvoke("getLogger").arg(portTypeProxy.dotclass()));
    // Inject WebServiceContext
    final JFieldVar webServiceContext = portTypeProxy.field(JMod.PROTECTED, WebServiceContext.class, WEBSERVICE_CONTEXT_FIELD_NAME);
    webServiceContext.annotate(Resource.class);

    // Overwrite all methods declared on the PortType interface.
    for (final Element _element : _interface.getEnclosedElements()) {
      if (!(ElementKind.METHOD.equals(_element.getKind()))) {
        continue;
      }
      final ExecutableElement _method = (ExecutableElement) _element;

      final String methodName = _method.getSimpleName().toString();
      final JType returnType = JTypeParser.parseType(model, _method.getReturnType());

      m_logger.logInfo("Generating PortTypeProxy implementation for method '%s#%s'", _interface.getQualifiedName().toString(), methodName);

      // Create the method.
      final JMethod method = portTypeProxy.method(JMod.PUBLIC, returnType, methodName);
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

      // Create the proxy implementation.
      addMethodProxyImplementation(model, webServiceContext, method, throwTypes, TypeKind.VOID.equals(_method.getReturnType().getKind()), _interface.getQualifiedName().toString());
    }

    // Create the method to handle undeclared errors.
    addHandleUndeclaredFaultMethod(model, portTypeProxy, logger);

    // Build and persist this compilation unit.
    AptUtil.buildAndPersist(model, processingEnv.getFiler());

    m_logger.logInfo("PortTypeProxy '%s' successfully generated.'", portTypeProxy.fullName());
  }

  /**
   * Creates the skeleton class for the port type proxy.
   */
  @Internal
  protected JDefinedClass createPortTypeProxyClass(final JCodeModel model, final TypeElement _portTypeInterface, final RoundEnvironment roundEnv) throws JClassAlreadyExistsException {
    final JClass portTypeInterface = model.ref(_portTypeInterface.getQualifiedName().toString());

    final String portTypeName = _portTypeInterface.getAnnotation(WebService.class).name();
    final PortTypeProxyDescriptor descriptor = findPortTypeProxyDescriptor(roundEnv, portTypeName);
    if (descriptor == null) {
      m_logger.logInfo("No endpoint decorator class annotated with '%s' found. Using derived values for @%s annotation. [portType=%s]", JaxWsPortTypeDecorator.class.getSimpleName(), WebService.class.getSimpleName(), portTypeName);
    }

    // Create PortTypeProxy class.
    final JDefinedClass portTypeProxy;

    final String pck;
    final String name;
    if (descriptor != null) {
      pck = processingEnv.getElementUtils().getPackageOf(descriptor.getDeclaringType()).getQualifiedName().toString();
      name = (descriptor.isProxyNameDerived() ? _portTypeInterface.getSimpleName() + PORT_TYPE_PROXY_SUFFIX : descriptor.getProxyName());
    }
    else {
      pck = processingEnv.getElementUtils().getPackageOf(_portTypeInterface).getQualifiedName().toString();
      name = _portTypeInterface.getSimpleName() + PORT_TYPE_PROXY_SUFFIX;
    }

    portTypeProxy = model._class(StringUtility.join(".", pck, name));
    portTypeProxy._implements(portTypeInterface);

    // Add annotations to the PortTypeProxy.
    addAnnotations(model, portTypeProxy, _portTypeInterface, descriptor, roundEnv);

    // Add JavaDoc to the PortTypeProxy.
    AptUtil.addJavaDoc(portTypeProxy, createJavaDocForPortTypeProxy(_portTypeInterface, descriptor));
    return portTypeProxy;
  }

  /**
   * Creates the implementation of a port type method.
   */
  @Internal
  protected void addMethodProxyImplementation(final JCodeModel model, final JFieldVar webServiceContext, final JMethod method, final List<JClass> throwTypes, final boolean voidMethod, final String portTypeQualifiedName) {
    final JBlock methodBody = method.body();

    // Declare variables 'jaxwsRunContext' and 'handlerRunContext'.
    final JVar jaxwsRunContext = methodBody.decl(JMod.FINAL, model.ref(RunContext.class), JAXWS_RUN_CONTEXT_FIELD_NAME, model.ref(JaxWsRunContexts.class).staticInvoke("empty").invoke("webServiceContext").arg(webServiceContext));
    final JVar handlerRunContext = methodBody.decl(JMod.FINAL, model.ref(RunContext.class), HANDLER_RUN_CONTEXT_FIELD_NAME, model.ref(MessageContexts.class).staticInvoke("getRunContext").arg(webServiceContext.invoke("getMessageContext")));

    final JTryBlock tryBlock = methodBody._try();

    // Invoke port type on behalf of RunContext.
    final JInvocation runContextInvocation = createRunContextInvocation(model, jaxwsRunContext, handlerRunContext, voidMethod, method, portTypeQualifiedName);
    if (voidMethod) {
      tryBlock.body().add(runContextInvocation);
    }
    else {
      tryBlock.body()._return(runContextInvocation);
    }

    // Create exception handling logic.
    final JCatchBlock catchBlock = tryBlock._catch(model.ref(ProcessingException.class));
    final JVar caughtException = catchBlock.param("e");
    final JBlock catchBody = catchBlock.body();

    final JInvocation rootCauseInvocation = model.ref(ExceptionHandler.class).staticInvoke("getRootCause").arg(caughtException);

    // Create exception handling.
    if (throwTypes.isEmpty()) {
      // webservice method has not faults declared.
      catchBody._throw(JExpr.invoke(HANDLE_UNDECLARED_FAULT_METHOD_NAME).arg(rootCauseInvocation));
    }
    else {
      // handle declared webservice faults.
      final JVar cause = catchBody.decl(model.ref(Throwable.class), "cause", rootCauseInvocation);

      final JConditionalEx condition = new JConditionalEx(catchBody);
      for (final JClass throwType : throwTypes) {
        condition._elseif(cause._instanceof(throwType))._throw(JExprEx.cast(throwType, cause));
      }
      condition._else()._throw(JExpr.invoke(HANDLE_UNDECLARED_FAULT_METHOD_NAME).arg(cause));
    }
  }

  /**
   * Creates code to invoke the real port type on behalf of the given RunContext.
   *
   * @return
   */
  @Internal
  protected JInvocation createRunContextInvocation(final JCodeModel model, final JVar jaxwsRunContext, final JVar handlerRunContext, final boolean voidMethod, final JMethod portTypeMethod, final String portTypeName) {
    final JDefinedClass jaxwsRunContextCallable;
    final JDefinedClass handlerRunContextCallable;
    final String runMethodName;
    if (voidMethod) {
      jaxwsRunContextCallable = model.anonymousClass(IRunnable.class);
      handlerRunContextCallable = model.anonymousClass(IRunnable.class);
      runMethodName = "run";
    }
    else {
      jaxwsRunContextCallable = model.anonymousClass(model.ref(Callable.class).narrow(portTypeMethod.type()));
      handlerRunContextCallable = model.anonymousClass(model.ref(Callable.class).narrow(portTypeMethod.type()));
      runMethodName = "call";
    }

    // Invoke the bean method.
    final JInvocation beanInvocation = model.ref(BEANS.class).staticInvoke("get").arg(model.ref(portTypeName).dotclass()).invoke(portTypeMethod.name());
    for (final JVar parameter : portTypeMethod.listParams()) {
      beanInvocation.arg(parameter);
    }

    // Implement HandlerRunContext callable.
    final JMethod handlerRunContextRunMethod = handlerRunContextCallable.method(JMod.PUBLIC | JMod.FINAL, portTypeMethod.type(), runMethodName)._throws(Exception.class);
    handlerRunContextRunMethod.annotate(Override.class);
    if (voidMethod) {
      handlerRunContextRunMethod.body().add(beanInvocation);
    }
    else {
      handlerRunContextRunMethod.body()._return(beanInvocation);
    }

    // Create RunContext invocations.
    final JInvocation jaxwsRunContextInvocation = jaxwsRunContext.invoke(runMethodName).arg(JExpr._new(jaxwsRunContextCallable));
    final JInvocation handlerRunContextInvocation = handlerRunContext.invoke(runMethodName).arg(JExpr._new(handlerRunContextCallable));

    // Implement JaxWsRunContext callable.
    final JMethod jaxwsRunContextRunMethod = jaxwsRunContextCallable.method(JMod.PUBLIC | JMod.FINAL, portTypeMethod.type(), runMethodName)._throws(Exception.class);
    jaxwsRunContextRunMethod.annotate(Override.class);

    final JConditionalEx jaxWsRunContextCondition = new JConditionalEx(jaxwsRunContextRunMethod.body());

    // Assemble the methods.
    if (voidMethod) {
      jaxWsRunContextCondition._if(handlerRunContext.eq(JExpr._null())).add(beanInvocation); // directly invoke Bean method.
      jaxWsRunContextCondition._else().add(handlerRunContextInvocation); // call HandlerRunContext to invoke Bean method.
    }
    else {
      jaxWsRunContextCondition._if(handlerRunContext.eq(JExpr._null()))._return(beanInvocation); // directly invoke Bean method.
      jaxWsRunContextCondition._else()._return(handlerRunContextInvocation); // call HandlerRunContext to invoke Bean method.
    }
    return jaxwsRunContextInvocation;
  }

  /**
   * Adds the method to handle undeclared exceptions which are not declared in the WSDL.
   */
  @Internal
  protected void addHandleUndeclaredFaultMethod(final JCodeModel model, final JDefinedClass portTypeProxy, final JFieldVar logger) {
    // Create the method to handle undeclared faults.
    final JMethod method = portTypeProxy.method(JMod.PROTECTED, RuntimeException.class, HANDLE_UNDECLARED_FAULT_METHOD_NAME);
    method.annotate(Internal.class);

    final JVar throwableParam = method.param(JMod.FINAL, Throwable.class, "t");

    final JConditionalEx condition = new JConditionalEx(method.body());

    // Handle RuntimeException
    final JType runtimeException = model._ref(RuntimeException.class);
    condition._if(throwableParam._instanceof(runtimeException))._throw(JExprEx.cast(runtimeException, throwableParam));

    // Handle Error
    final JType error = model._ref(Error.class);
    condition._elseif(throwableParam._instanceof(error))._throw(JExprEx.cast(error, throwableParam));

    // Handle other exception
    final JBlock otherExceptionBlock = condition._else();
    otherExceptionBlock.invoke(logger, "error").arg(JExpr.lit("Undeclared exception while processing webservice request")).arg(throwableParam);
    otherExceptionBlock._throw(JExpr._new(model.ref(HTTPException.class)).arg(model.ref(HttpServletResponse.class).staticRef("SC_INTERNAL_SERVER_ERROR")));
  }

  /**
   * Adds annotations to the PortTypeProxy.
   */
  protected void addAnnotations(final JCodeModel model, final JDefinedClass portTypeProxy, final TypeElement _portTypeInterface, final PortTypeProxyDescriptor descriptor, final RoundEnvironment roundEnv) {
    // Add 'Generated' annotation
    final JAnnotationUse generatedAnnotation = portTypeProxy.annotate(Generated.class);
    generatedAnnotation.param("value", PortTypeProxyAnnotationProcessor.class.getName());
    generatedAnnotation.param("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ").format(new Date()));
    generatedAnnotation.param("comments", "Proxy to run webservice requests on behalf of a RunContext");

    if (descriptor != null) {
      // Add 'WebService' annotation
      if (!descriptor.containsAnnotation(WebService.class)) {
        final WebService _webServiceAnnotation = _portTypeInterface.getAnnotation(WebService.class);
        final JAnnotationUse webServiceAnnotation = portTypeProxy.annotate(WebService.class);
        webServiceAnnotation.param("name", _webServiceAnnotation.name());
        webServiceAnnotation.param("targetNamespace", _webServiceAnnotation.targetNamespace());
        webServiceAnnotation.param("endpointInterface", _portTypeInterface.getQualifiedName().toString());

        if (descriptor != null) {
          if (StringUtility.hasText(descriptor.getServiceName())) {
            webServiceAnnotation.param("serviceName", descriptor.getServiceName());
          }
          if (StringUtility.hasText(descriptor.getPortName())) {
            webServiceAnnotation.param("portName", descriptor.getPortName());
          }
          if (descriptor.isWsdlLocationDerived()) {
            final WebServiceClient _webServiceClientAnnotation = findWebServiceClientAnnotation(roundEnv, descriptor.getServiceName());
            if (_webServiceClientAnnotation != null) {
              webServiceAnnotation.param("wsdlLocation", _webServiceClientAnnotation.wsdlLocation());
            }
            else {
              m_logger.logWarn("Cannot derive 'wsdlLocation' because no Service annotated with '@WebServiceClient(name=\"%s\")' found.", descriptor.getServiceName());
            }
          }
          else if (StringUtility.hasText(descriptor.getWsdlLocation())) {
            webServiceAnnotation.param("wsdlLocation", descriptor.getWsdlLocation());
          }
        }
      }

      // Add custom annotations
      AnnotationUtil.addAnnotations(model, portTypeProxy, descriptor.getSiblingAnnotations());

      // Add handler chain annotation.
      final HandlerChain _handlerChainAnnotation = _portTypeInterface.getAnnotation(HandlerChain.class);
      if (_handlerChainAnnotation == null) {
        portTypeProxy.annotate(HandlerChain.class).param("file", new HandlerArtifactProcessor().generateHandlerArtifacts(portTypeProxy, descriptor, processingEnv, m_logger));
      }
      else {
        m_logger.logInfo("Handler file not generated because provided as binding file [file=%s]", _handlerChainAnnotation.file());
      }
    }
  }

  /**
   * Returns the descriptor representing {@link JaxWsPortTypeDecorator}, or <code>null</code> if not found.
   */
  @Internal
  protected PortTypeProxyDescriptor findPortTypeProxyDescriptor(final RoundEnvironment roundEnv, final String portTypeName) {
    for (final Element _portTypeProxyElement : roundEnv.getElementsAnnotatedWith(JaxWsPortTypeDecorator.class)) {
      final JaxWsPortTypeDecorator _portTypeProxyAnnotation = _portTypeProxyElement.getAnnotation(JaxWsPortTypeDecorator.class);
      if (portTypeName.equals(_portTypeProxyAnnotation.belongsToPortType())) {
        return new PortTypeProxyDescriptor(_portTypeProxyElement, processingEnv);
      }
    }
    return null;
  }

  /**
   * Returns {@link WebService} for the given service name, or <code>null</code> if not found.
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

  private String createJavaDocForPortTypeProxy(final TypeElement _portTypeInterface, final PortTypeProxyDescriptor descriptor) {
    final StringWriter writer = new StringWriter();
    final PrintWriter out = new PrintWriter(writer);

    out.printf("This class is auto-generated by APT triggered by Maven build based on {@link %s} annotation on {@link %s}", WebService.class.getSimpleName(), _portTypeInterface.getSimpleName());
    if (descriptor != null) {
      out.printf(", and is customized by {@link %s}.", descriptor.getDeclaringType().getSimpleName()).println();
    }
    else {
      out.println(".");
    }
    out.println("<p>");
    out.printf("This proxy intercepts webservice requests and runs them on behalf of a {@link RunContext}, before being dispatched to the implementing PortType bean. Typically, the RunContext is configured by a preceding handler, like {@link %s}.", AuthenticationHandler.class.getSimpleName()).println();
    out.println("<p>");
    if (descriptor != null) {
      out.println("<table>");

      // Authentication
      if (descriptor.getAuthentication().enabled()) {
        out.printf("<tr><td>Authentication Method:</td><td>{@link %s}</td>", AptUtil.toSimpleName(descriptor.getAuthMethod())).println();
        out.printf("<tr><td>Authenticator:</td><td>{@link %s}</td>", AptUtil.toSimpleName(descriptor.getAuthenticator())).println();
      }
      else {
        out.println("<tr><td>Authentication:</td><td>none</td>");
      }

      // Handlers
      if (descriptor.getHandlerChain().isEmpty()) {
        out.println("<tr><td>Handler chain:</td><td>none</td>");
      }
      else {
        final List<String> handlers = new ArrayList<>();
        for (final HandlerDescriptor handler : descriptor.getHandlerChain()) {
          handlers.add(String.format("{@link %s}", handler.getSimpleName()));
        }
        out.printf("<tr><td>Handler chain:</td><td>%s</td>", StringUtility.join(", ", handlers)).println();
      }

      out.println("</table>");
    }
    out.println("<ul>");
    out.println("<li>To disable proxy generation, set Maven property 'jaxws.porttypeproxy.enabled' to <code>false</code>, or disable APT.</li>");
    out.println("<li>To rebuild stub and proxy, run 'mvn clean compile', or update the Maven Project in Eclipse IDE (Ctrl+F5 with 'clean projects' checked).</li>");
    out.println("<li>If not cleaning 'target' folder, stub and proxy is only re-generated if either WSDL, schema or binding files change, or '/target/jaxws/wsartifact-hash' is deleted manually.</li>");
    out.printf("<li>Provide an interface annotated with {@link %s} to customize proxy and artifact generation, e.g. to configure authentication, install handlers, or to contribute additional annotations like {@link %s}.</li>", JaxWsPortTypeDecorator.class.getSimpleName(), MTOM.class.getSimpleName()).println();
    out.printf("<li>Any annotation declared on interface annotated with {@link %s} is contributed to the PortTypeProxy.</li>", JaxWsPortTypeDecorator.class.getSimpleName()).println();
    out.printf("<li>By contributing a {@link %s} annotation on proxy decorator, webservice configuration can be overwritten.</li>", WebService.class.getSimpleName()).println();
    out.printf("<li>If providing a binding file with a handler-chain definition, handler definition and authentication on proxy decorator is ignored.</li>", WebService.class.getSimpleName()).println();
    if (descriptor != null && _portTypeInterface.getAnnotation(HandlerChain.class) == null) {
      out.println("<li>JAX-WS handlers are registered in 'handler-chain.xml'.</li>");
    }

    out.println("</ul>");

    final StringWriter newLine = new StringWriter();
    new PrintWriter(newLine).println();

    return writer.toString().replace(newLine.toString(), "\n"); // remove double new-lines
  }
}
