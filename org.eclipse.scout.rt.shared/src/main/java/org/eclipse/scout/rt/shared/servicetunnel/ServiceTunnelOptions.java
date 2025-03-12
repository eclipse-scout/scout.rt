/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import static org.eclipse.scout.rt.platform.util.BooleanUtility.nvl;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;

public class ServiceTunnelOptions {
  public static final String ID_SIGNATURE_PROP = "ServiceTunnelOptions.idSignature";

  private Boolean m_idSignature;

  public static ServiceTunnelOptions create() {
    return new ServiceTunnelOptions();
  }

  /**
   * Whether the service tunnel uses signature creation for serialization/deserialization of {@link IDataObject}s or not.
   * When enabled the transferred {@link IDoEntity}s are serialized using signature creation and therefore the transferred data changes.
   * <p>
   * Consider a {@link IDoEntity} which is transferred from the backend server to the ui server and afterward to the browser where it might be used for REST calls directly to a REST endpoint in the backend server.
   * In this case the serialization between the ui server and the browser and between the browser and the REST endpoint use signatures.
   * If now the type of the {@link IDoEntity} is only known to the backend and not the ui server the serialization between the backend and the ui server needs to use signatures or otherwise the REST endpoint will not accept the
   * {@link IDoEntity} as it contains unsigned attributes.
   */
  public ServiceTunnelOptions withIdSignature(Boolean idSignature) {
    m_idSignature = idSignature;
    return this;
  }

  /**
   * see {@link #withIdSignature(Boolean)}
   */
  public Boolean getIdSignature() {
    return m_idSignature;
  }

  /**
   * see {@link #withIdSignature(Boolean)}
   */
  public boolean isIdSignature() {
    return nvl(getIdSignature());
  }
}
