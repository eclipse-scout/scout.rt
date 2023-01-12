/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.logging;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.slf4j.MDC;

/**
 * This class provides the {@link IUiSession#getUiSessionId()} to be set into the <code>diagnostic context map</code>
 * for logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class UiSessionIdContextValueProvider implements IUiRunContextDiagnostics {

  public static final String KEY = "scout.ui.session.id";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    final IUiSession uiSession = UiSession.CURRENT.get();
    return uiSession != null ? uiSession.getUiSessionId() : null;
  }
}
