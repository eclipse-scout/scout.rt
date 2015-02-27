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
package org.eclipse.scout.rt.platform.cdi;

import java.lang.annotation.Annotation;

import org.eclipse.scout.commons.annotations.Priority;

/**
 *
 */
public final class DynamicAnnotations {

  private DynamicAnnotations() {
  }

  public static Priority createPriority(final float priority) {
    return new Priority() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Priority.class;
      }

      @Override
      public float value() {
        return priority;
      }

      @Override
      public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotationType() == null) ? 0 : annotationType().hashCode());
        result = prime * result + Float.floatToIntBits(value());
        return result;
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
        if (annotationType() == null) {
          if (other.annotationType() != null) {
            return false;
          }
        }
        else if (!annotationType().equals(other.annotationType())) {
          return false;
        }
        if (Float.floatToIntBits(value()) != Float.floatToIntBits(other.value())) {
          return false;
        }
        return true;
      }
    };
  }

  public static ApplicationScoped createApplicationScoped() {
    return new ApplicationScoped() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ApplicationScoped.class;
      }

      @Override
      public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotationType() == null) ? 0 : annotationType().hashCode());
        return result;
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null) {
          return false;
        }
        if (!(obj instanceof ApplicationScoped)) {
          return false;
        }
        ApplicationScoped other = (ApplicationScoped) obj;
        if (annotationType() == null) {
          if (other.annotationType() != null) {
            return false;
          }
        }
        else if (!annotationType().equals(other.annotationType())) {
          return false;
        }
        return true;
      }
    };
  }

  public static CreateImmediately createCreateImmediately() {
    return new CreateImmediately() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return CreateImmediately.class;
      }

      @Override
      public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotationType() == null) ? 0 : annotationType().hashCode());
        return result;
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null) {
          return false;
        }
        if (!(obj instanceof CreateImmediately)) {
          return false;
        }
        CreateImmediately other = (CreateImmediately) obj;
        if (annotationType() == null) {
          if (other.annotationType() != null) {
            return false;
          }
        }
        else if (!annotationType().equals(other.annotationType())) {
          return false;
        }
        return true;
      }
    };
  }

}
