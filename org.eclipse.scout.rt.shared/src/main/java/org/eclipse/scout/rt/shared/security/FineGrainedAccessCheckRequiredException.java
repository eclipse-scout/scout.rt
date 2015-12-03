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
package org.eclipse.scout.rt.shared.security;


/**
 * This exception is thrown by {@link BasicHierarchyPermission#implies(java.security.Permission)} when fine-grained
 * access has to be calculated (on backend) but the permission check is done in the frontend (by a service proxy) The
 * {@link org.eclipse.scout.rt.shared.services.common.security.bsiag.service.ac.IAccessControlService} proxy will then
 * delegate the check to the backend
 *
 * @deprecated Will be removed in Scout 6.1.
 */
@Deprecated
public class FineGrainedAccessCheckRequiredException extends SecurityException {
  private static final long serialVersionUID = 1L;

  public FineGrainedAccessCheckRequiredException() {
    super("fine-grained access-control must be calculated on backend");
  }
}
