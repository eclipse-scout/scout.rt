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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;

public final class AnnotationFactory {
  private AnnotationFactory() {
  }

  public static ApplicationScoped createApplicationScoped() {
    return AnnotationFactory.Dummy.class.getAnnotation(ApplicationScoped.class);
  }

  public static CreateImmediately createCreateImmediately() {
    return AnnotationFactory.Dummy.class.getAnnotation(CreateImmediately.class);
  }

  public static Replace createReplace() {
    return AnnotationFactory.Dummy.class.getAnnotation(Replace.class);
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

  @ApplicationScoped
  @CreateImmediately
  @Replace
  private static class Dummy {
  }
}
