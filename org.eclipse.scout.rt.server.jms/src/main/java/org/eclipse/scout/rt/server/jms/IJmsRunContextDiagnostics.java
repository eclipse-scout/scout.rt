package org.eclipse.scout.rt.server.jms;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * Marker interface for {@link IDiagnosticContextValueProvider} which are registered in {@link RunContext} in JMS
 * communication.
 */
public interface IJmsRunContextDiagnostics extends IDiagnosticContextValueProvider {
}
