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
package org.eclipse.scout.rt.server.admin.diagnostic;

import java.util.List;

public interface IDiagnostic {

  void addDiagnosticItemToList(List<List<String>> result);

  String[] getPossibleActions();

  void addSubmitButtonsHTML(List<List<String>> result);

  void call(String action, Object[] values);

}
