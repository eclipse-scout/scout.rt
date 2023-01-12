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

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Producer for {@link RunContext} objects.
 * <p>
 * The default implementation creates a copy of the current calling {@link RunContext}.
 *
 * @since 5.1
 */
@ApplicationScoped
public class RunContextProducer {

  /**
   * Produces a {@link RunContext} for the given {@link Subject}.
   */
  public RunContext produce(final Subject subject) {
    return RunContexts.copyCurrent(true).withSubject(subject);
  }
}
