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
package org.eclipse.scout.rt.platform;

import java.lang.annotation.Annotation;

import org.eclipse.scout.commons.annotations.Priority;

public final class AnnotationFactory {
  private AnnotationFactory() {
  }

  public static ApplicationScoped createApplicationScoped() {
    return AnnotationFactory.Dummy.class.getAnnotation(ApplicationScoped.class);
  }

  public static CreateImmediately createCreateImmediately() {
    return AnnotationFactory.Dummy.class.getAnnotation(CreateImmediately.class);
  }

  public static Priority createPriority(final double priority) {
    return new Priority() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Priority.class;
      }

      @Override
      public double value() {
        return priority;
      }

      @Override
      public int hashCode() {
        return (int) Double.doubleToLongBits(value());
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null) {
          return false;
        }
        if (!(obj instanceof Priority)) {
          return false;
        }
        Priority other = (Priority) obj;
        if (this.value() != other.value()) {
          return false;
        }
        return true;
      }
    };
  }

  @ApplicationScoped
  @CreateImmediately
  private static class Dummy {
  }
}
