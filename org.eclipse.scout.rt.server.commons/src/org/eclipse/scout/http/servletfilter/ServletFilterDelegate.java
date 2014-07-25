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
package org.eclipse.scout.http.servletfilter;

/**
 * @deprecated use org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection instead. Will be removed
 *             in the M-Release.
 */
@Deprecated
public final class ServletFilterDelegate extends org.eclipse.scout.rt.server.commons.servletfilter.ServletFilterDelegate {

  /**
   * @deprecated use {@link org.eclipse.scout.rt.server.commons.servletfilter.ServletFilterDelegate.IServiceCallback}
   *             instead. Will be removed in the M-Release.
   */
  @Deprecated
  public interface IServiceCallback extends org.eclipse.scout.rt.server.commons.servletfilter.ServletFilterDelegate.IServiceCallback {
  }

}
