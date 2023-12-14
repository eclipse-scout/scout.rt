/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import jakarta.annotation.PostConstruct;

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
