package org.eclipse.scout.rt.server.commons.servlet.logging;

import javax.servlet.Servlet;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * Marker interface for {@link IDiagnosticContextValueProvider} which are registered in {@link RunContext} for a
 * {@link Servlet}.
 */
public interface IServletRunContextDiagnostics extends IDiagnosticContextValueProvider {
}
