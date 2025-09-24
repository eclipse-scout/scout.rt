/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import static org.eclipse.scout.rt.shared.ISessionVariable.SHARED_CONTEXT_USER_ID;

import java.util.Map;

import org.eclipse.scout.rt.shared.user.UserId;

public class UserIdSharedVariableContributor implements IInitialSharedVariableContributor {

  @Override
  public void contribute(Map<String, Object> variables) {
    variables.put(SHARED_CONTEXT_USER_ID, UserId.CURRENT.get());
  }
}
