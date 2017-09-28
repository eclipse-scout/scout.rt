/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
