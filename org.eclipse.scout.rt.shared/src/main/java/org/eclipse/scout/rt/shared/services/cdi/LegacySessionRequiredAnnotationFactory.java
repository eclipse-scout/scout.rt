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
package org.eclipse.scout.rt.shared.services.cdi;

import java.lang.annotation.Annotation;

import org.eclipse.scout.rt.shared.ISession;

/**
 *
 */
public final class LegacySessionRequiredAnnotationFactory {

  private LegacySessionRequiredAnnotationFactory() {
  }

  public static SessionRequired createSessionRequiredAnnotation(final Class<? extends ISession> sessionClazz) {
    return new SessionRequired() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return SessionRequired.class;
      }

      @Override
      public Class<? extends ISession> value() {
        return sessionClazz;
      }
    };
  }

}
