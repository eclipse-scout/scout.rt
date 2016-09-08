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
import org.eclipse.scout.jaxws.apt.internal.util.Assertions;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication.NullAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler.HandlerType;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.IgnoreWebServiceEntryPoint;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.WebServiceEntryPoint;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;

/**
 * This class represents values declared via {@link WebServiceEntryPoint}.
 *
 * @since 5.1
 */
@SuppressWarnings("squid:S00117")
public class EntryPointDefinition {

  public static final String ENTRY_POINT_SUFFIX = "EntryPoint";

  private boolean m_ignore;
  private final TypeElement m_endpointInterface;
  private final TypeElement m_definition;
  private final WebServiceEntryPoint m_annotation;
  private final AnnotationMirror m_annotationMirror;
  private final List<AnnotationMirror> m_siblingAnnotations;

  private final ProcessingEnvironment m_env;

  public EntryPointDefinition(final TypeElement _definition, final TypeElement _endpointInterface, final ProcessingEnvironment env) {
    m_endpointInterface = _endpointInterface;
    m_definition = _definition;
    m_annotation = Assertions.assertNotNull(_definition.getAnnotation(WebServiceEntryPoint.class), "Unexpected: Annotation '{}' not found [class={}],", WebServiceEntryPoint.class.getName(), _definition);
    m_siblingAnnotations = new ArrayList<>();
    m_env = env;

    AnnotationMirror _entryPointAnnotationMirror = null;
    for (final AnnotationMirror _annotationMirror : _definition.getAnnotationMirrors()) {
      final String candidate = _annotationMirror.getAnnotationType().toString();

      if (WebServiceEntryPoint.class.getName().equals(candidate)) {
        _entryPointAnnotationMirror = _annotationMirror;
      }
      else if (IgnoreWebServiceEntryPoint.class.getName().equals(candidate)) {
        m_ignore = true;
      }
      else {
        m_siblingAnnotations.add(_annotationMirror);
      }
    }

    m_annotationMirror = Assertions.assertNotNull(_entryPointAnnotationMirror, "Unexpected: AnnotationMirror for annotation '{}' not found,", WebServiceEntryPoint.class.getName());
  }

  /**
   * Returns <code>true</code> to not generate an entry point for this definition, or else <code>false</code>.
   */
  public boolean isIgnore() {
    return m_ignore;
  }

  public TypeElement getEndpointInterface() {
    return m_endpointInterface;
  }

  /**
   * @return the fully qualified name of the entry point.
   */
  public String getEntryPointQualifiedName() {
    final boolean nameDerived = WebServiceEntryPoint.DERIVED.equals(m_annotation.entryPointName());
    final boolean packageDerived = WebServiceEntryPoint.DERIVED.equals(m_annotation.entryPointPackage());

    final String pck = (packageDerived ? m_env.getElementUtils().getPackageOf(m_definition).getQualifiedName().toString() : m_annotation.entryPointPackage());
    final String name = (nameDerived ? m_endpointInterface.getSimpleName() + ENTRY_POINT_SUFFIX : m_annotation.entryPointName());

    return StringUtility.join(".", pck, name);
  }

  /**
   * Returns the qualified name of the element declaring {@link WebServiceEntryPoint} annotation.
   */
  public String getQualifiedName() {
    return m_definition.getQualifiedName().toString();
  }

  /**
   * Returns the simple name of the element declaring {@link WebServiceEntryPoint} annotation.
   */
  public String getSimpleName() {
    return m_definition.getSimpleName().toString();
  }

  /**
   * Returns the qualified name of the endpoint interface.
   */
  public String getEndpointInterfaceQualifiedName() {
    return m_endpointInterface.getQualifiedName().toString();
  }

  /**
   * Returns the simple name of the endpoint interface.
   */
  public String getEndpointInterfaceSimpleName() {
    return m_endpointInterface.getSimpleName().toString();
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
  public List<HandlerDefinition> getHandlerChain() {
    final List<HandlerDefinition> handlerChain = new ArrayList<>();

    @SuppressWarnings("unchecked")
    final List<AnnotationValue> handlerAnnotationValues = (List<AnnotationValue>) AnnotationUtil.getAnnotationValue(m_annotationMirror, "handlerChain", m_env.getElementUtils()).getValue();

    for (final AnnotationValue handlerAnnotationValue : handlerAnnotationValues) {
      final AnnotationMirror handlerAnnotation = (AnnotationMirror) handlerAnnotationValue.getValue();
      handlerChain.add(new HandlerDefinition(handlerAnnotation));
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
    return WebServiceEntryPoint.DERIVED.equals(getWsdlLocation());
  }

  /**
   * @return {@link List} of additional annotations declared on decorated, like {@link MTOM}.
   */
  public List<AnnotationMirror> getSiblingAnnotations() {
    return m_siblingAnnotations;
  }

  /**
   * @return <code>true</code>, if the <em>entry point definition</em> contains the given annotation.
   */
  public boolean containsAnnotation(final Class<? extends Annotation> annotationClass) {
    for (final AnnotationMirror siblingAnnotation : m_siblingAnnotations) {
      if (annotationClass.getName().equals(siblingAnnotation.getAnnotationType().toString())) {
        return true;
      }
    }
    return false;
  }

  public class HandlerDefinition {
    private final HandlerType m_handlerType;
    private final String m_qualifiedName;

    public HandlerDefinition(final AnnotationMirror handlerAnnotationMirror) {
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
          throw new IllegalStateException(
              format("Unsupported handler type; must implement '%s' for a SOAP protocol-specific handler, or '%s' for a protocol-agnostic handler.", SOAPHandler.class.getSimpleName(), LogicalHandler.class.getSimpleName()));
        }
      }
      else {
        // Handler is not visible yet.
        final VariableElement enumElement = (VariableElement) AnnotationUtil.getAnnotationValue(handlerAnnotationMirror, "handlerType", m_env.getElementUtils()).getValue();
        m_handlerType = HandlerType.valueOf(enumElement.getSimpleName().toString());
      }
    }

    /**
     * @return handler type, and is one of {@link HandlerType#LOGICAL} or {@link HandlerType#SOAP}.
     */
    public HandlerType getHandlerType() {
      return m_handlerType;
    }

    /**
     * @return qualified name of the handler.
     */
    public String getHandlerQualifiedName() {
      return m_qualifiedName;
    }

    /**
     * @return simple name of the handler.
     */
    public String getHandlerSimpleName() {
      return AptUtil.toSimpleName(m_qualifiedName);
    }
  }
}
