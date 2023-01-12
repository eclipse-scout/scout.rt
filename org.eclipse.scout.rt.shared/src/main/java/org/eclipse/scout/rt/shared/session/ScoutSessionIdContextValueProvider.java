/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.session;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.slf4j.MDC;

/**
 * This class provides the {@link ISession#getId()} to be set into the <code>diagnostic context map</code> for logging
 * purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class ScoutSessionIdContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "scout.session.id";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    final ISession session = ISession.CURRENT.get();
    return session != null ? session.getId() : null;
  }
}
