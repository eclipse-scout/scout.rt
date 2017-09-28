/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.IThrowableWithContextInfo;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.server.admin.inspector.CallInspector;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.admin.inspector.SessionInspector;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceWithoutAuthorization;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides functionality to invoke service operations as described by {@link ServiceTunnelRequest} and to return the
 * operations result in the form of a {@link ServiceTunnelResponse}.
 */
@ApplicationScoped
public class ServiceOperationInvoker {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceOperationInvoker.class);

  /**
   * Invoke the service associated with the {@link ServiceTunnelRequest}. <br>
   * Must be called within a transaction.
   */
  @SuppressWarnings("squid:S1193")
  public ServiceTunnelResponse invoke(final RunContext runContext, final ServiceTunnelRequest serviceReq) {
    final long t0 = System.nanoTime();
    ServiceTunnelResponse response;
    try {
      response = runContext.call(() -> invokeInternal(serviceReq), DefaultExceptionTranslator.class);
    }
    catch (Exception e) {
      // Associate the exception with context information about the service call.
      if (e instanceof PlatformException) {
        ((IThrowableWithContextInfo) e)
            .withContextInfo("service.name", serviceReq.getServiceInterfaceClassName())
            .withContextInfo("service.operation", serviceReq.getOperation());
      }

      // Handle the exception.
      handleException(e);

      // Prepare ServiceTunnelResponse.
      response = new ServiceTunnelResponse(interceptException(e));
    }

    response.setProcessingDuration(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0));
    LOG.debug("TIME {}.{} {}ms", serviceReq.getServiceInterfaceClassName(), serviceReq.getOperation(), response.getProcessingDuration());
    return response;
  }

  protected ServiceTunnelResponse invokeInternal(ServiceTunnelRequest serviceReq) throws ClassNotFoundException {
    IServerSession serverSession = ServerSessionProvider.currentSession();
    if (LOG.isDebugEnabled()) {
      String userId = serverSession != null ? serverSession.getUserId() : "";
      LOG.debug("started {}.{} by {} at {}", serviceReq.getServiceInterfaceClassName(), serviceReq.getOperation(), userId, new Date());
    }
    CallInspector callInspector = getCallInspector(serviceReq, serverSession);
    ServiceTunnelResponse serviceRes = null;
    try {
      ServiceUtility serviceUtility = BEANS.get(ServiceUtility.class);
      Class<?> serviceInterfaceClass = SerializationUtility.getClassLoader().loadClass(serviceReq.getServiceInterfaceClassName());
      Method serviceOp = serviceUtility.getServiceOperation(serviceInterfaceClass, serviceReq.getOperation(), serviceReq.getParameterTypes());
      Object[] args = serviceReq.getArgs();
      Object service = getValidatedServiceAccess(serviceInterfaceClass, serviceOp, args);

      Object data = serviceUtility.invoke(service, serviceOp, args);
      Object[] outParameters = serviceUtility.extractHolderArguments(args);
      serviceRes = new ServiceTunnelResponse(data, outParameters);
      return serviceRes;
    }
    finally {
      updateInspector(callInspector, serviceRes);
    }
  }

  private void updateInspector(CallInspector callInspector, ServiceTunnelResponse serviceRes) {
    if (callInspector != null) {
      try {
        callInspector.update();
      }
      catch (RuntimeException e) {
        LOG.warn("Could not update call inspector", e);
      }
      try {
        callInspector.close(serviceRes);
      }
      catch (RuntimeException e) {
        LOG.warn("Could not close service invocation on call inspector", e);
      }
      try {
        callInspector.getSessionInspector().update();
      }
      catch (RuntimeException e) {
        LOG.warn("Could not update session inspector", e);
      }
    }
  }

  /**
   * Check, if the service can be accessed
   */
  protected Object getValidatedServiceAccess(Class<?> serviceInterfaceClass, Method serviceOp, Object[] args) {
    Object service = BEANS.opt(serviceInterfaceClass);
    checkServiceAvailable(serviceInterfaceClass, service);
    checkRemoteServiceAccessByInterface(serviceInterfaceClass, serviceOp, args);
    checkRemoteServiceAccessByAnnotations(serviceInterfaceClass, service.getClass(), serviceOp, args);
    if (mustAuthorize(serviceInterfaceClass, service.getClass(), serviceOp, args)) {
      checkRemoteServiceAccessByPermission(serviceInterfaceClass, service.getClass(), serviceOp, args);
    }
    return service; // if we come there, the service is available and valid to call
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
    catch (NoSuchMethodException | RuntimeException t) {
      LOG.debug("Could not lookup service method", t);
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
   * <p>
   * Using blacklist {@link RemoteServiceAccessDenied}
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
      catch (NoSuchMethodException | RuntimeException t) {
        LOG.debug("Could not lookup service method", t);
      }
      if (m != null && m.isAnnotationPresent(RemoteServiceAccessDenied.class)) {
        throw new SecurityException("access denied (code 2b).");
      }

      //type level
      if (c.isAnnotationPresent(RemoteServiceAccessDenied.class)) {
        throw new SecurityException("access denied (code 2c).");
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
   * using a remote service proxy.
   * <p>
   * Deny access by default.
   * <p>
   * Accepts when a {@link RemoteServiceAccessPermission} was implied or authorization was waved using whitelist
   * {@link RemoteServiceWithoutAuthorization} in {@link #mustAuthorize(Class, Class, Method, Object[])}
   */
  protected void checkRemoteServiceAccessByPermission(Class<?> interfaceClass, Class<?> implClass, Method interfaceMethod, Object[] args) {
    if (ACCESS.check(new RemoteServiceAccessPermission(interfaceClass.getName(), interfaceMethod.getName()))) {
      //granted
      return;
    }
    throw new SecurityException("access denied (code 3a).");
  }

  /**
   * @return true unless there is a {@link RemoteServiceWithoutAuthorization} on the called method or interface in the
   *         class tree
   * @since 6.1
   */
  protected boolean mustAuthorize(Class<?> interfaceClass, Class<?> implClass, Method interfaceMethod, Object[] args) {
    //check: authorize/no-authorize annotation (type level is base, method level is finegrained)
    Class<?> c = implClass;
    while (c != null) {
      //method level
      Method m = null;
      try {
        m = c.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
      }
      catch (NoSuchMethodException | RuntimeException t) {
        LOG.debug("Could not lookup service method", t);
      }
      if (m != null && m.isAnnotationPresent(RemoteServiceWithoutAuthorization.class)) {
        //granted
        return false;
      }

      //type level
      if (c.isAnnotationPresent(RemoteServiceWithoutAuthorization.class)) {
        //granted
        return false;
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
    return true;
  }

  private CallInspector getCallInspector(ServiceTunnelRequest serviceReq, IServerSession serverSession) {
    if (serverSession != null) {
      SessionInspector sessionInspector = BEANS.get(ProcessInspector.class).getSessionInspector(serverSession, true);
      if (sessionInspector != null) {
        return sessionInspector.requestCallInspector(serviceReq);
      }
    }
    return null;
  }

  /**
   * Method invoked to handle a service exception. This method must not throw an exception.
   * <p>
   * The default implementation handles an exception via {@link ExceptionHandler}, but only if the current context is
   * not cancelled.
   */
  protected void handleException(Throwable t) {
    BEANS.get(ExceptionHandler.class).handle(t);
  }

  /**
   * Method invoked to intercept a service exception before being put into the {@link ServiceTunnelResponse} to be sent
   * to the client. This method must not throw an exception.
   * <p>
   * <p>
   * Security: do not send back original error and stack trace with implementation details.
   * <p>
   * The default implementation returns an empty exception, or in case of a {@link VetoException} only its title,
   * message, htmlMessage, error code and severity.
   */
  protected Throwable interceptException(Throwable t) {
    Throwable p;
    if (t instanceof VetoException) {
      VetoException ve = (VetoException) t;
      p = new VetoException(ve.getStatus().getBody())
          .withTitle(ve.getStatus().getTitle())
          .withHtmlMessage(ve.getHtmlMessage())
          .withCode(ve.getStatus().getCode())
          .withSeverity(ve.getStatus().getSeverity());
    }
    else {
      p = new ProcessingException(TEXTS.get("RequestProblem"));
    }
    p.setStackTrace(new StackTraceElement[0]);
    return p;
  }
}
