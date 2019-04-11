/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.job.JobInput;

@IgnoreBean
@Replace
public class FixtureJobInput extends JobInput {
  /**
   * Hint set manually on jobs that are created declaratively in the test class
   */
  public static final String EXPLICIT_HINT = "FixtureJmsJobExplicitHint";
  /**
   * Hint set automatically on all jobs that are created by the scout job manager.
   */
  public static final String IMPLICIT_HINT = "FixtureJmsJobImplicitHint";

  @PostConstruct
  protected void postInit() {
    withExecutionHint(IMPLICIT_HINT);
  }
}
