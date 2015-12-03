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
package org.eclipse.scout.rt.platform;

import java.lang.annotation.Annotation;

public final class AnnotationFactory {

  private static final ApplicationScoped APPLICATION_SCOPED = new ApplicationScoped() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return ApplicationScoped.class;
    }
  };

  private static final CreateImmediately CREATE_IMMEDIATELY = new CreateImmediately() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return CreateImmediately.class;
    }
  };

  private static final Replace REPLACE = new Replace() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Replace.class;
    }
  };

  private AnnotationFactory() {
  }

  public static ApplicationScoped createApplicationScoped() {
    return APPLICATION_SCOPED;
  }

  public static CreateImmediately createCreateImmediately() {
    return CREATE_IMMEDIATELY;
  }

  public static Replace createReplace() {
    return REPLACE;
  }

  public static Order createOrder(final double order) {
    return new Order() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Order.class;
      }

      @Override
      public double value() {
        return order;
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
        if (!(obj instanceof Order)) {
          return false;
        }
        Order other = (Order) obj;
        if (this.value() != other.value()) {
          return false;
        }
        return true;
      }
    };
  }

}
