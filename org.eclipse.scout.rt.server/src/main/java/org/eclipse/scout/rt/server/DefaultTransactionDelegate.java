/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.service.ServiceUtility;
import org.eclipse.scout.rt.server.admin.inspector.CallInspector;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.admin.inspector.SessionInspector;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.validate.DefaultValidator;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.IValidator;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.rt.shared.validate.OutputValidation;

/**
 * Delegate for scout dynamic business op invocation
 * <p>
 * Subclass this type to change/add validation checks or call {@link #setValidateInput(true)} to activate them.
 * <p>
 * Override {@link #validateInput(IValidationStrategy, Object, Method, Object[])} and/or
 * {@link #validateOutput(IValidationStrategy, Object, Method, Object, Object[])} to modifiy the default central
 * validation. You may use {@link #defaultValidateInput(IValidationStrategy, Object, Method, Object[])} and
 * {@link #defaultValidateOutput(IValidationStrategy, Object, Method, Object, Object[])}
 * <p>
 * Set the config.properties to activate default validation:
 *
 * <pre>
 *   org.eclipse.scout.rt.server.validateInput=true
 *   org.eclipse.scout.rt.server.validateOutput=false
 * </pre>
 */
@ApplicationScoped
public class DefaultTransactionDelegate {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultTransactionDelegate.class);

  public static final Pattern DEFAULT_QUERY_NAMES_PATTERN = Pattern.compile("(get|is|has|load|read|find|select)([A-Z].*)?");
  public static final Pattern DEFAULT_PROCESS_NAMES_PATTERN = Pattern.compile("(set|put|add|remove|store|write|create|insert|update|delete)([A-Z].*)?");

  public DefaultTransactionDelegate() {
    this(false);
  }

  /**
   * @deprecated use DefaultTransactionDelegate, configure debug with logger configuration
   */
  @Deprecated
  public DefaultTransactionDelegate(boolean debug) {
  }

  public ServiceTunnelResponse invoke(ServiceTunnelRequest serviceReq) throws Exception {
    long t0 = System.nanoTime();

    ServiceTunnelResponse response;
    try {
      response = invokeImpl(serviceReq);
    }
    catch (Throwable t) {
      ITransaction.CURRENT.get().addFailure(t);
      handleException(t, serviceReq);
      response = new ServiceTunnelResponse(replaceOutboundException(t));
    }

    long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
    if (LOG.isDebugEnabled()) {
      LOG.debug("TIME {}.{} {}ms", new Object[]{serviceReq.getServiceInterfaceClassName(), serviceReq.getOperation(), elapsedMillis});
    }
    response.setProcessingDuration(elapsedMillis);
    return response;
  }

  protected IValidator createValidator(IValidationStrategy validationStrategy) {
    return new DefaultValidator(validationStrategy);
  }

  /**
   * security: do not send back original error and stack trace with details
   * <p>
   * default returns an empty exception or in case of a {@link VetoException} only its title, message, error code and
   * severity
   */
  protected Throwable replaceOutboundException(Throwable t) {
    Throwable p;
    if (t instanceof VetoException) {
      VetoException ve = (VetoException) t;
      p = new VetoException(ve.getStatus().getTitle(), ve.getStatus().getBody(), ve.getStatus().getCode(), ve.getStatus().getSeverity());
    }
    else {
      p = new ProcessingException(ScoutTexts.get("RequestProblem"));
    }
    p.setStackTrace(new StackTraceElement[0]);
    return p;
  }

  /**
   * This method is executed within a {@link IServerSession} context on behalf of a server job.
   */
  protected ServiceTunnelResponse invokeImpl(ServiceTunnelRequest serviceReq) throws Throwable {
    IServerSession serverSession = ServerSessionProvider.currentSession();
    String authenticatedUser = serverSession.getUserId();
    if (LOG.isDebugEnabled()) {
      LOG.debug("started " + serviceReq.getServiceInterfaceClassName() + "." + serviceReq.getOperation() + " by " + authenticatedUser + " at " + new Date());
    }
    CallInspector callInspector = getCallInspector(serviceReq, serverSession);
    ServiceTunnelResponse serviceRes = null;
    try {
      //do checks
      Class<?> serviceInterfaceClass = SerializationUtility.getClassLoader().loadClass(serviceReq.getServiceInterfaceClassName());
      Method serviceOp = ServiceUtility.getServiceOperation(serviceInterfaceClass, serviceReq.getOperation(), serviceReq.getParameterTypes());
      Object[] args = serviceReq.getArgs();

      checkServiceAccess(serviceInterfaceClass, serviceOp, args);
      //
      Object service = BEANS.get(serviceInterfaceClass);
      validateInput(service, serviceOp, args);
      //
      Object data = ServiceUtility.invoke(serviceOp, service, args);
      Object[] outParameters = ServiceUtility.extractHolderArguments(args);
      //
      //filter output
      validateOutput(service, serviceOp, data, outParameters);
      serviceRes = new ServiceTunnelResponse(data, outParameters);
      return serviceRes;
    }
    finally {
      if (callInspector != null) {
        try {
          callInspector.update();
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
        try {
          callInspector.close(serviceRes);
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
        try {
          callInspector.getSessionInspector().update();
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
      }
    }
  }

  /**
   * Check, if the service can be accessed
   */
  protected void checkServiceAccess(Class<?> serviceInterfaceClass, Method serviceOp, Object[] args) throws ProcessingException {
    Object service = BEANS.opt(serviceInterfaceClass);
    checkServiceAvailable(serviceInterfaceClass, service);
    checkRemoteServiceAccessByInterface(serviceInterfaceClass, serviceOp, args);
    checkRemoteServiceAccessByAnnotations(serviceInterfaceClass, service.getClass(), serviceOp, args);
    checkRemoteServiceAccessByPermission(serviceInterfaceClass, service.getClass(), serviceOp, args);
  }

  /**
   * Check, if an instance is available
   */
  protected void checkServiceAvailable(Class<?> serviceInterfaceClass, Object service) {
    if (service == null) {
      throw new SecurityException("service registry does not contain a service of type " + serviceInterfaceClass.getName());
    }
  }

  /**
   * Check pass 1 on type
   */
  protected void checkRemoteServiceAccessByInterface(Class<?> interfaceClass, Method interfaceMethod, Object[] args) {
    //check: must be an interface
    if (!interfaceClass.isInterface()) {
      throw new SecurityException("access denied (code 1a).");
    }

    //check: method is defined on service interface itself
    Method verifyMethod;
    try {
      verifyMethod = interfaceClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
    }
    catch (Throwable t) {
      throw new SecurityException("access denied (code 1c).");
    }
    //exists
    if (verifyMethod.getDeclaringClass() == IService.class) {
      throw new SecurityException("access denied (code 1d).");
    }
    //continue
  }

  /**
   * Check pass 2 on instance
   */
  protected void checkRemoteServiceAccessByAnnotations(Class<?> interfaceClass, Class<?> implClass, Method interfaceMethod, Object[] args) {
    //check: grant/deny annotation (type level is base, method level is finegrained)
    Class<?> c = implClass;
    while (c != null) {
      //method level
      Method m = null;
      try {
        m = c.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
      }
      catch (Throwable t) {
        //nop
      }
      if (m != null) {
        Annotation[] methodAnnotations = m.getAnnotations();
        for (Annotation ann : methodAnnotations) {
          if (ann.annotationType() == RemoteServiceAccessDenied.class) {
            throw new SecurityException("access denied (code 2b).");
          }
        }
      }
      //type level
      Annotation[] classAnnotations = c.getAnnotations();
      for (Annotation ann : classAnnotations) {
        if (ann.annotationType() == RemoteServiceAccessDenied.class) {
          throw new SecurityException("access denied (code 2c).");
        }
      }
      //next
      if (c == interfaceClass) {
        break;
      }
      c = c.getSuperclass();
      if (c == Object.class) {
        //use interface at last
        c = interfaceClass;
      }
    }
    //continue
  }

  /**
   * Check pass 3 {@link RemoteServiceAccessPermission} if a client (gui) is allowed to call this service from remote
   * using a
   * remote service proxy.
   * <p>
   * Deny access by default.
   * <p>
   * Accepts when a {@link RemoteServiceAccessPermission} was implied.
   */
  protected void checkRemoteServiceAccessByPermission(Class<?> interfaceClass, Class<?> implClass, Method interfaceMethod, Object[] args) {
    if (ACCESS.check(new RemoteServiceAccessPermission(interfaceClass.getName(), interfaceMethod.getName()))) {
      return;
    }
    throw new SecurityException("access denied (code 3a).");
  }

  private CallInspector getCallInspector(ServiceTunnelRequest serviceReq, IServerSession serverSession) {
    CallInspector callInspector = null;
    SessionInspector sessionInspector = ProcessInspector.instance().getSessionInspector(serverSession, true);
    if (sessionInspector != null) {
      callInspector = sessionInspector.requestCallInspector(serviceReq);
    }
    return callInspector;
  }

  protected void validateInput(Object service, Method serviceOp, Object[] args) throws Exception, InstantiationException, IllegalAccessException {
    if (isValidateInput() && args != null && args.length > 0) {
      Class<? extends IValidationStrategy> validationStrategyClass = findInputValidationStrategyByAnnotation(service, serviceOp);
      if (validationStrategyClass == null) {
        validationStrategyClass = findInputValidationStrategyByPolicy(service, serviceOp);
      }
      if (validationStrategyClass == null) {
        throw new SecurityException("input validation failed (no strategy defined)");
      }
      validateInput(validationStrategyClass.newInstance(), service, serviceOp, args);
    }
  }

  protected boolean isValidateInput() {
    return false;
  }

  protected void validateOutput(Object service, Method serviceOp, Object data, Object[] outParameters) throws Exception, InstantiationException, IllegalAccessException {
    if (isValidateOutput() && data != null || (outParameters != null && outParameters.length > 0)) {
      Class<? extends IValidationStrategy> validationStrategyClass = findOutputValidationStrategyByAnnotation(service, serviceOp);
      if (validationStrategyClass == null) {
        validationStrategyClass = findOutputValidationStrategyByPolicy(service, serviceOp);
      }
      if (validationStrategyClass == null) {
        throw new SecurityException("output validation failed");
      }
      validateOutput(validationStrategyClass.newInstance(), service, serviceOp, data, outParameters);
    }
  }

  protected boolean isValidateOutput() {
    return false;
  }

  /**
   * Validate inbound data.Called by {@link #invokeImpl(IServiceTunnelRequest)}.
   * <p>
   * For default handling use
   *
   * <pre>
   * the validation methods of {@link #createValidator(IValidationStrategy)}
   * </pre>
   * <p>
   * Override this method to do central input validation inside the transaction context.
   * <p>
   * This method is part of the protected api and can be overridden.
   *
   * @param validationStrategy
   *          may be null, add corresponding null handling.
   */
  protected void validateInput(IValidationStrategy validationStrategy, Object service, Method op, Object[] args) throws Exception {
    //defaultValidateInput(validationStrategy, service, op, args);
  }

  protected void defaultValidateInput(IValidationStrategy validationStrategy, Object service, Method op, Object[] args) throws Exception {
    createValidator(validationStrategy).validateMethodCall(op, args);
  }

  /**
   * Validate outbound data. Default does nothing. Called by {@link #invokeImpl(IServiceTunnelRequest)}.
   * Override this method to do central output validation inside the transaction context.
   * <p>
   * This method is part of the protected api and can be overridden.
   */
  protected void validateOutput(IValidationStrategy validationStrategy, Object service, Method op, Object returnValue, Object[] outArgs) throws Exception {
    //defaultValidateOutput(validationStrategy, service, op, returnValue, outArgs);
  }

  protected void defaultValidateOutput(IValidationStrategy validationStrategy, Object service, Method op, Object returnValue, Object[] outArgs) throws Exception {
    if ((outArgs != null && outArgs.length > 0) || returnValue != null) {
      IValidator v = createValidator(validationStrategy);
      if (outArgs != null && outArgs.length > 0) {
        for (Object arg : outArgs) {
          v.validateParameter(arg, null);
        }
      }
      if (returnValue != null) {
        v.validateParameter(returnValue, null);
      }
    }
  }

  /**
   * Pass 1 tries to find a {@link InputValidation} annotation
   */
  protected Class<? extends IValidationStrategy> findInputValidationStrategyByAnnotation(Object serviceImpl, Method op) {
    Class<?> c = serviceImpl.getClass();
    while (c != null) {
      //method level
      Method m = null;
      try {
        m = c.getMethod(op.getName(), op.getParameterTypes());
      }
      catch (Throwable t) {
        //nop
      }
      if (m != null) {
        InputValidation ann = m.getAnnotation(InputValidation.class);
        if (ann != null) {
          return ann.value();
        }
      }
      //type level
      InputValidation ann = c.getAnnotation(InputValidation.class);
      if (ann != null) {
        return ann.value();
      }
      //next
      if (c == op.getDeclaringClass()) {
        break;
      }
      c = c.getSuperclass();
      if (c == Object.class) {
        //use interface at last
        c = op.getDeclaringClass();
      }
    }
    //continue
    return null;
  }

  /**
   * Pass 2 decides the strategy by java bean, collections framework and business process naming
   *
   * <pre>
   * <i>Java bean naming</i>
   * {@link IValidationStrategy.QUERY}: get*, is*
   * {@link IValidationStrategy.PROCESS}: set*
   * <p/>
   * <i>Collections framework naming</i>
   * {@link IValidationStrategy.QUERY}: get*
   * {@link IValidationStrategy.PROCESS}: put*, add*, remove*
   * <p/>
   * <i>Business process naming</i>
   * {@link IValidationStrategy.QUERY}: load*, read*, find*, has*, select*
   * {@link IValidationStrategy.PROCESS}: store*, write*, create*, insert*, update*, delete*
   * </pre>
   */
  protected Class<? extends IValidationStrategy> findInputValidationStrategyByPolicy(Object serviceImpl, Method op) {
    if (DEFAULT_QUERY_NAMES_PATTERN.matcher(op.getName()).matches()) {
      return IValidationStrategy.QUERY.class;
    }
    if (DEFAULT_PROCESS_NAMES_PATTERN.matcher(op.getName()).matches()) {
      return IValidationStrategy.PROCESS.class;
    }
    //
    warnMissingInputValidation(serviceImpl, op);
    return IValidationStrategy.QUERY.class;
  }

  protected void warnMissingInputValidation(Object serviceImpl, Method op) {
    LOG.warn("Legacy security hint for: " + op.getDeclaringClass().getName() + "#" + op.getName() + ": missing either annotation " + InputValidation.class.getSimpleName() + " or override of server-side " + getClass().getSimpleName() + "#findInputValidationStrategyByPolicy. To support legacy the QUERY strategy is used.");
  }

  /**
   * Pass 1 tries to find a {@link OutputValidation} annotation
   */
  protected Class<? extends IValidationStrategy> findOutputValidationStrategyByAnnotation(Object serviceImpl, Method op) {
    Class<?> c = serviceImpl.getClass();
    while (c != null) {
      //method level
      Method m = null;
      try {
        m = c.getMethod(op.getName(), op.getParameterTypes());
      }
      catch (Throwable t) {
        //nop
      }
      if (m != null) {
        OutputValidation ann = m.getAnnotation(OutputValidation.class);
        if (ann != null) {
          return ann.value();
        }
      }
      //type level
      OutputValidation ann = c.getAnnotation(OutputValidation.class);
      if (ann != null) {
        return ann.value();
      }
      //next
      if (c == op.getDeclaringClass()) {
        break;
      }
      c = c.getSuperclass();
      if (c == Object.class) {
        //use interface at last
        c = op.getDeclaringClass();
      }
    }
    //continue
    return null;
  }

  /**
   * Pass 2 decides the strategy by policy (custom override recommended)
   * <p>
   * Default does no checks
   */
  protected Class<? extends IValidationStrategy> findOutputValidationStrategyByPolicy(Object serviceImpl, Method op) {
    return IValidationStrategy.NO_CHECK.class;
  }

  /**
   * Method invoked to handle service exceptions.
   */
  protected void handleException(Throwable t, ServiceTunnelRequest serviceTunnelRequest) {
    if (ITransaction.CURRENT.get().isCancelled()) {
      return;
    }

    String serviceOperation = String.format("service=%s, method=%s", serviceTunnelRequest.getServiceInterfaceClassName(), serviceTunnelRequest.getOperation());
    if (t instanceof ProcessingException) {
      ProcessingException pe = (ProcessingException) t;
      pe.addContextMessage(serviceOperation);
      BEANS.get(ExceptionHandler.class).handle(pe);
    }
    else {
      LOG.error(String.format("Unexpected error while invoking service operation [%s]", serviceOperation), t);
    }
  }

}
