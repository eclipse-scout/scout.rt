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

import org.eclipse.scout.jaxws216.session.DefaultServerSessionFactory;
import org.eclipse.scout.jaxws216.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;

/**
 * <p>
 * Annotate handlers to run within a transactional context on behalf of a {@link IServerSession}.<br/>
 * By default, {@link DefaultServerSessionFactory} is used as session strategy.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScoutTransaction {
  Class<? extends IServerSessionFactory> sessionFactory() default DefaultServerSessionFactory.class;
}
