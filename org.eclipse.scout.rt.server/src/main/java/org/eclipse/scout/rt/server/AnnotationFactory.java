package org.eclipse.scout.rt.server;

import java.lang.annotation.Annotation;

public final class AnnotationFactory {
  private AnnotationFactory() {
  }

  public static Server createServer(final Class<? extends IServerSession> clazz) {
    return new Server() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Server.class;
      }

      @Override
      public Class<? extends IServerSession> value() {
        return clazz;
      }

      @Override
      public int hashCode() {
        return value().hashCode();
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null) {
          return false;
        }
        if (!(obj instanceof Server)) {
          return false;
        }
        Server other = (Server) obj;
        if (this.value() != other.value()) {
          return false;
        }
        return true;
      }
    };
  }
}
