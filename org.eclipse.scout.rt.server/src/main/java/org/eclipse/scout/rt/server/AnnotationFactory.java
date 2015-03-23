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
