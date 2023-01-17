/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared;

import java.lang.annotation.Annotation;

@SuppressWarnings("squid:S2162") // instanceof comparison ok here
public final class AnnotationFactory {

  private static final TunnelToServer TUNNEL_TO_SERVER = new TunnelToServer() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return TunnelToServer.class;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return obj instanceof TunnelToServer;
    }

    @Override
    public int hashCode() {
      return 0;
    }
  };

  private AnnotationFactory() {
  }

  public static TunnelToServer createTunnelToServer() {
    return TUNNEL_TO_SERVER;
  }
}
