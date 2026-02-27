/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.server.runner.statement;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.SubjectStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.SubjectStatementFactory;
import org.junit.runners.model.Statement;

@Replace
public class ServerSubjectStatementFactory extends SubjectStatementFactory {

  @Override
  public SubjectStatement createSubjectStatement(Statement next, RunWithSubject subjectAnnotation) {
    return new ServerSubjectStatement(next, subjectAnnotation);
  }
}
