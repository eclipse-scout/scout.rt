/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import java.security.AccessController;
import java.security.Principal;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.slf4j.MDC;

/**
 * This class provides the {@link Principal} to be set into the <code>diagnostic context map</code> for logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class PrinicpalContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "subject.principal.name";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    final Subject currentSubject = Subject.getSubject(AccessController.getContext());
    if (currentSubject == null || currentSubject.getPrincipals().isEmpty()) {
      return null;
    }

    final Principal principal = currentSubject.getPrincipals().iterator().next();
    return (principal != null ? principal.getName() : null);
  }
}
