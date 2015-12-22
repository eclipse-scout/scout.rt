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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.jws.WebService;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.MTOM;

import org.eclipse.scout.jaxws.apt.internal.util.AnnotationUtil;
import org.eclipse.scout.jaxws.apt.internal.util.AptUtil;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.authentication.ICredentialVerifier;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication.NullAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler.HandlerType;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.JaxWsPortTypeProxy;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;

/**
 * This class represents values declared in {@link JaxWsPortTypeProxy}.
 *
 * @since 5.1
 */
public class PortTypeProxyDescriptor {

  public static final String PORT_TYPE_PROXY_SUFFIX = "Proxy";

  private final TypeElement m_endpointInterface;
  private final TypeElement m_descriptor;
  private final JaxWsPortTypeProxy m_annotation;
  private final AnnotationMirror m_annotationMirror;
  private final List<AnnotationMirror> m_siblingAnnotations;
  private String m_proxyNameSuffix;

  private final ProcessingEnvironment m_env;

  public PortTypeProxyDescriptor(final TypeElement _descriptor, final TypeElement _endpointInterface, final ProcessingEnvironment env) {
    m_endpointInterface = _endpointInterface;
    m_descriptor = _descriptor;
    m_annotation = Assertions.assertNotNull(_descriptor.getAnnotation(JaxWsPortTypeProxy.class), "Unexpected: Annotation '{}' not found [class={}],", JaxWsPortTypeProxy.class.getName(), _descriptor);
    m_siblingAnnotations = new ArrayList<>();
    m_env = env;

    AnnotationMirror descriptorAnnotationMirror = null;
    for (final AnnotationMirror _annotationMirror : _descriptor.getAnnotationMirrors()) {
      if (JaxWsPortTypeProxy.class.getName().equals(_annotationMirror.getAnnotationType().toString())) {
        descriptorAnnotationMirror = _annotationMirror;
      }
      else {
        m_siblingAnnotations.add(_annotationMirror);
      }
    }

    m_annotationMirror = Assertions.assertNotNull(descriptorAnnotationMirror, "Unexpected: AnnotationMirror for annotation '{}' not found,", JaxWsPortTypeProxy.class.getName());
  }

  public TypeElement getEndpointInterface() {
    return m_endpointInterface;
  }

  /**
   * @return the fully qualified name of the port type proxy.
   */
  public String getProxyQualifiedName() {
    final boolean derived = JaxWsPortTypeProxy.DERIVED.equals(m_annotation.portTypeProxyName());

    final String suffix = StringUtility.nvl(m_proxyNameSuffix, "");
    final String pck = m_env.getElementUtils().getPackageOf(m_descriptor).getQualifiedName().toString();
    if (derived) {
      return StringUtility.join(".", pck, m_endpointInterface.getSimpleName() + PORT_TYPE_PROXY_SUFFIX + suffix);
    }
    else {
      return StringUtility.join(".", pck, m_annotation.portTypeProxyName() + suffix);
    }
  }

  /**
   * Sets a suffix to be appended to the PortTypeProxyName, or <code>null</code> for no suffix. The suffix is used for
   * unique names.
   */
  public void setProxyNameSuffix(final String suffix) {
    m_proxyNameSuffix = suffix;
  }

  /**
   * Returns the class or interface which contains {@link JaxWsPortTypeProxy} annotation.
   */
  public TypeElement getDescriptor() {
    return m_descriptor;
  }

  public Authentication getAuthentication() {
    return m_annotation.authentication();
  }

  /**
   * @return <code>true</code> if there is an authentication handler to be installed.
   */
  public boolean isAuthenticationEnabled() {
    return !NullAuthenticationMethod.class.getName().replaceAll("\\$", "\\.").equals(getAuthMethod().replaceAll("\\$", "\\."));
  }

  /**
   * @return qualified name of the {@link IAuthenticationMethod} used.
   */
  public String getAuthMethod() {
    final AnnotationMirror authenticationAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(m_annotationMirror, "authentication", m_env.getElementUtils()).getValue();
    final AnnotationMirror clazzAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(authenticationAnnotation, "method", m_env.getElementUtils()).getValue();
    return AnnotationUtil.resolveClass(clazzAnnotation, m_env);
  }

  /**
   * @return qualified name of the {@link ICredentialVerifier} used.
   */
  public String getAuthVerifier() {
    final AnnotationMirror authenticationAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(m_annotationMirror, "authentication", m_env.getElementUtils()).getValue();
    final AnnotationMirror clazzAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(authenticationAnnotation, "verifier", m_env.getElementUtils()).getValue();
    return AnnotationUtil.resolveClass(clazzAnnotation, m_env);
  }

  /**
   * @return {@link List} of declared handlers.
   */
  public List<HandlerDescriptor> getHandlerChain() {
    final List<HandlerDescriptor> handlerChain = new ArrayList<>();

    @SuppressWarnings("unchecked")
    final List<AnnotationValue> handlerAnnotationValues = (List<AnnotationValue>) AnnotationUtil.getAnnotationValue(m_annotationMirror, "handlerChain", m_env.getElementUtils()).getValue();

    for (final AnnotationValue handlerAnnotationValue : handlerAnnotationValues) {
      final AnnotationMirror handlerAnnotation = (AnnotationMirror) handlerAnnotationValue.getValue();
      handlerChain.add(new HandlerDescriptor(handlerAnnotation));
    }

    return handlerChain;
  }

  /**
   * @return the name of the Web Service (wsdl:portType).
   */
  public String getPortTypeName() {
    return m_endpointInterface.getAnnotation(WebService.class).name();
  }

  /**
   * @return the service name of the Web Service (wsdl:service).
   */
  public String getServiceName() {
    return m_annotation.serviceName();
  }

  /**
   * @return the port name of the Web Service (wsdl:port).
   */
  public String getPortName() {
    return m_annotation.portName();
  }

  /**
   * @return configured WSDL location.
   */
  public String getWsdlLocation() {
    return m_annotation.wsdlLocation();
  }

  /**
   * @return <code>true</code> if the WSDL location is to be derived from {@link WebServiceClient} annotation on service
   *         class.
   */
  public boolean isWsdlLocationDerived() {
    return JaxWsPortTypeProxy.DERIVED.equals(getWsdlLocation());
  }

  /**
   * @return {@link List} of additional annotations declared on decorated, like {@link MTOM}.
   */
  public List<AnnotationMirror> getSiblingAnnotations() {
    return m_siblingAnnotations;
  }

  /**
   * @return <code>true</code>, if the descriptor contains the given annotation.
   */
  public boolean containsAnnotation(final Class<? extends Annotation> annotationClass) {
    for (final AnnotationMirror siblingAnnotation : m_siblingAnnotations) {
      if (annotationClass.getName().equals(siblingAnnotation.getAnnotationType().toString())) {
        return true;
      }
    }
    return false;
  }

  public class HandlerDescriptor {
    private final HandlerType m_handlerType;
    private final String m_qualifiedName;

    public HandlerDescriptor(final AnnotationMirror handlerAnnotationMirror) {
      final AnnotationMirror clazzMirror = (AnnotationMirror) AnnotationUtil.getAnnotationValue(handlerAnnotationMirror, "value", m_env.getElementUtils()).getValue();
      m_qualifiedName = AnnotationUtil.resolveClass(clazzMirror, m_env);

      final TypeElement handlerType = m_env.getElementUtils().getTypeElement(m_qualifiedName);
      if (handlerType != null) {
        if (AptUtil.isSubtype(handlerType, SOAPHandler.class, m_env)) {
          m_handlerType = HandlerType.SOAP;
        }
        else if (AptUtil.isSubtype(handlerType, LogicalHandler.class, m_env)) {
          m_handlerType = HandlerType.LOGICAL;
        }
        else {
          throw new PlatformException("Unsupported handler type; must implement '{}' for a SOAP protocol-specific handler, or '{}' for a protocol-agnostic handler.", SOAPHandler.class.getSimpleName(), LogicalHandler.class.getSimpleName());
        }
      }
      else {
        // Handler is not visible yet.
        final VariableElement enumElement = (VariableElement) AnnotationUtil.getAnnotationValue(handlerAnnotationMirror, "handlerType", m_env.getElementUtils()).getValue();
        m_handlerType = HandlerType.valueOf(enumElement.getSimpleName().toString());
      }
    }

    public HandlerType getHandlerType() {
      return m_handlerType;
    }

    public String getQualifiedName() {
      return m_qualifiedName;
    }

    public String getSimpleName() {
      return AptUtil.toSimpleName(m_qualifiedName);
    }
  }
}
