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
package org.eclipse.scout.jaxws.apt.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.MTOM;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.jaxws.apt.internal.util.AnnotationUtil;
import org.eclipse.scout.jaxws.apt.internal.util.AptUtil;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler.HandlerType;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.JaxWsPortTypeDecorator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.IAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;

/**
 * This class represents values declared in {@link JaxWsPortTypeDecorator}.
 *
 * @since 5.1
 */
public class PortTypeProxyDescriptor {

  private final TypeElement m_declaringType;
  private final JaxWsPortTypeDecorator m_annotation;
  private final AnnotationMirror m_annotationMirror;
  private final List<AnnotationMirror> m_siblingAnnotations;

  private final ProcessingEnvironment m_env;

  public PortTypeProxyDescriptor(final Element portTypeProxyElement, final ProcessingEnvironment env) {
    m_declaringType = (TypeElement) env.getTypeUtils().asElement(portTypeProxyElement.asType());
    m_annotation = Assertions.assertNotNull(portTypeProxyElement.getAnnotation(JaxWsPortTypeDecorator.class), "Unexpected: Annotation '%s' not found [class=%s],", JaxWsPortTypeDecorator.class.getName(), portTypeProxyElement);
    m_siblingAnnotations = new ArrayList<>();
    m_env = env;

    AnnotationMirror decoratorAnnotationMirror = null;
    for (final AnnotationMirror _annotationMirror : portTypeProxyElement.getAnnotationMirrors()) {
      if (JaxWsPortTypeDecorator.class.getName().equals(_annotationMirror.getAnnotationType().toString())) {
        decoratorAnnotationMirror = _annotationMirror;
      }
      else {
        m_siblingAnnotations.add(_annotationMirror);
      }
    }

    m_annotationMirror = Assertions.assertNotNull(decoratorAnnotationMirror, "Unexpected: AnnotationMirror for annotation '%s' not found,", JaxWsPortTypeDecorator.class.getName());
  }

  /**
   * Returns the class or interface which contains {@link JaxWsPortTypeDecorator} annotation.
   */
  public TypeElement getDeclaringType() {
    return m_declaringType;
  }

  public Authentication getAuthentication() {
    return m_annotation.authentication();
  }

  /**
   * @return qualified name of the {@link IAuthenticationMethod} used.
   */
  public String getAuthMethod() {
    final AnnotationMirror authenticationAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(m_annotationMirror, "authentication", m_env.getElementUtils()).getValue();
    final AnnotationMirror clazzAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(authenticationAnnotation, "method", m_env.getElementUtils()).getValue();
    return m_env.getElementUtils().getTypeElement(AnnotationUtil.resolveClass(clazzAnnotation, m_env)).getQualifiedName().toString();
  }

  /**
   * @return qualified name of the {@link IAuthenticator} used.
   */
  public String getAuthenticator() {
    final AnnotationMirror authenticationAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(m_annotationMirror, "authentication", m_env.getElementUtils()).getValue();
    final AnnotationMirror clazzAnnotation = (AnnotationMirror) AnnotationUtil.getAnnotationValue(authenticationAnnotation, "authenticator", m_env.getElementUtils()).getValue();
    return m_env.getElementUtils().getTypeElement(AnnotationUtil.resolveClass(clazzAnnotation, m_env)).getQualifiedName().toString();
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
   * @return name of the proxy class.
   */
  public String getProxyName() {
    return m_annotation.portTypeProxyName();
  }

  /**
   * @return <code>true</code> if the proxy name is to be derived from port type interface.
   */
  public boolean isProxyNameDerived() {
    return JaxWsPortTypeDecorator.DERIVED.equals(getProxyName());
  }

  /**
   * @return configured service name.
   */
  public String getServiceName() {
    return m_annotation.serviceName();
  }

  /**
   * @return configured port name.
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
    return JaxWsPortTypeDecorator.DERIVED.equals(getWsdlLocation());
  }

  /**
   * @return {@link List} of additional annotations declared on decorated, like {@link MTOM}.
   */
  public List<AnnotationMirror> getSiblingAnnotations() {
    return m_siblingAnnotations;
  }

  /**
   * @return <code>true</code>, if the decorator contains the given annotation.
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
          throw new IllegalArgumentException(String.format("Unsupported handler type; must implement '%s' for a SOAP protocol-specific handler, or '%s' for a protocol-agnostic handler.", SOAPHandler.class.getSimpleName(), LogicalHandler.class.getSimpleName()));
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
