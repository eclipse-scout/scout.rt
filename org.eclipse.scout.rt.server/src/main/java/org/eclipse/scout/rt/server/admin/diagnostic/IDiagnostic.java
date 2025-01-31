/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.diagnostic;

import java.util.List;

public interface IDiagnostic {

  void addDiagnosticItemToList(List<List<String>> result);

  String[] getPossibleActions();

  void addSubmitButtonsHTML(List<List<String>> result);

  void call(String action, Object[] values);
}
