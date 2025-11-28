/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.AbstractAccessControlService;
import org.eclipse.scout.rt.security.AllPermissionCollection;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;

/**
 * Test implementation of {@link IAccessControlService} providing fixed subject.
 */
public class JerseyTestAccessControlService extends AbstractAccessControlService<String> {

  @Override
  protected String getCurrentUserCacheKey() {
    return "user.mock";
  }

  @Override
  public String getUserIdOfCurrentSubject() {
    return "user.mock";
  }

  @Override
  protected IPermissionCollection execLoadPermissions(String cacheKey) {
    return BEANS.get(AllPermissionCollection.class);
  }
}
