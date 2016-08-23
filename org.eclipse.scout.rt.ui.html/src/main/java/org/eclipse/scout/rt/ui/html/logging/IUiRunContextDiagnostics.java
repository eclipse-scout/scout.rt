package org.eclipse.scout.rt.ui.html.logging;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * Marker interface for {@link IDiagnosticContextValueProvider} which are registered in {@link RunContext} in UI.
 */
public interface IUiRunContextDiagnostics extends IDiagnosticContextValueProvider {
}
