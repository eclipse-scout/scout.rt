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

@SuppressWarnings("squid:S2162") // instanceof comparison ok here
public final class AnnotationFactory {

  private static final ApplicationScoped APPLICATION_SCOPED = new ApplicationScoped() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return ApplicationScoped.class;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return obj instanceof ApplicationScoped;
    }

    @Override
    public int hashCode() {
      return 0;
    }
  };

  private static final CreateImmediately CREATE_IMMEDIATELY = new CreateImmediately() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return CreateImmediately.class;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return obj instanceof CreateImmediately;
    }

    @Override
    public int hashCode() {
      return 0;
    }
  };

  private static final Replace REPLACE = new Replace() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Replace.class;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return obj instanceof Replace;
    }

    @Override
    public int hashCode() {
      return 0;
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
        // hash of the double (can be replaced with Double.hashCode() as soon as JRE 1.8 is used).
        long bits = Double.doubleToLongBits(value());
        int hashOfValue = (int) (bits ^ (bits >>> 32));

        // implementation according to java.lang.annotation.Annotation.hashCode() specification
        return 127 * "value".hashCode() ^ hashOfValue;
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
        return this.value() == other.value();
      }
    };
  }

}
