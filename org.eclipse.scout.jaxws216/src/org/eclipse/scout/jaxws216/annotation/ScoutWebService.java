/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws216.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.jaxws216.internal.resolver.ScoutInstanceResolver;
import org.eclipse.scout.jaxws216.security.provider.BasicAuthenticationHandler;
import org.eclipse.scout.jaxws216.security.provider.ConfigIniCredentialValidationStrategy;
import org.eclipse.scout.jaxws216.security.provider.IAuthenticationHandler;
import org.eclipse.scout.jaxws216.security.provider.ICredentialValidationStrategy;
import org.eclipse.scout.jaxws216.session.DefaultServerSessionFactory;
import org.eclipse.scout.jaxws216.session.IServerSessionFactory;

import com.sun.xml.internal.ws.api.server.InstanceResolverAnnotation;

/**
 * <p>
 * Annotation to specify session, authentication and credential validation strategy.<br/>
 * By default, the following configuration is applied:
 * </p>
 * <table border="1">
 * <tr>
 * <td><b>property</b></td>
 * <td><b>default</b></td>
 * </tr>
 * <tr>
 * <td>sessionFactory</td>
 * <td>{@link DefaultServerSessionFactory}</td>
 * </tr>
 * <tr>
 * <td>authenticationHandler</td>
 * <td>{@link BasicAuthenticationHandler}</td>
 * </tr>
 * <tr>
 * <td>credentialValidationStrategy</td>
 * <td>{@link ConfigIniCredentialValidationStrategy}</td>
 * </tr>
 * </table>
 */
@Target(ElementType.TYPE)
@InstanceResolverAnnotation(ScoutInstanceResolver.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScoutWebService {
  Class<? extends IServerSessionFactory> sessionFactory() default DefaultServerSessionFactory.class;

  Class<? extends IAuthenticationHandler> authenticationHandler() default BasicAuthenticationHandler.class;

  Class<? extends ICredentialValidationStrategy> credentialValidationStrategy() default ConfigIniCredentialValidationStrategy.class;
}
