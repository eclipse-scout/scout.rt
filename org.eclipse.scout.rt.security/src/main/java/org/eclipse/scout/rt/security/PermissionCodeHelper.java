/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import java.nio.charset.StandardCharsets;
import java.security.Permission;

import org.eclipse.scout.rt.dataobject.exception.IPermissionCodeHelper;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;

/**
 * Creates a short (10‑char) permission code.
 * <p>
 * If the {@link Permission} implements {@link IPermission}, the signature of the permission's {@link IPermission#getId() ID}
 * is hashed; otherwise the permission name is hashed.
 */
public class PermissionCodeHelper implements IPermissionCodeHelper {

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);

  @Override
  public String getPermissionCode(Permission permission) {
    if (permission == null) {
      return null;
    }
    if (permission instanceof IPermission p) {
      return getPermissionCode(p);
    }
    return hash(permission.getName());
  }

  protected String getPermissionCode(IPermission permission) {
    return hash(permission.getId());
  }

  protected String hash(IId id) {
    String idString = m_idCodec.get().toUnqualified(id);
    String signature = m_idCodec.get().createSignature(idString);
    return hash(signature);
  }

  protected String hash(String text) {
    if (text == null) {
      return null;
    }
    return encodeAndShorten(SecurityUtility.hash(text.getBytes(StandardCharsets.UTF_8)));
  }

  protected String encodeAndShorten(byte[] hash) {
    return HexUtility.encode(hash).substring(0, 10);
  }
}
