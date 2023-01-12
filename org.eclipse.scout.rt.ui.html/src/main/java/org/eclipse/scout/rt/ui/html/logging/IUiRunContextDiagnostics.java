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

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * Marker interface for {@link IDiagnosticContextValueProvider} which are registered in {@link RunContext} in UI.
 */
public interface IUiRunContextDiagnostics extends IDiagnosticContextValueProvider {
}
