package org.eclipse.scout.rt.platform;

import java.lang.annotation.Annotation;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.CreateImmediately;

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
