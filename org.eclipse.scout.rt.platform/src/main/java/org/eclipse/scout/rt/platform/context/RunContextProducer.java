/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
    return RunContexts.copyCurrent().withSubject(subject);
  }
}
