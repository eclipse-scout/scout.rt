/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public class EntityMapperSubPeerFixture extends EntityMapperFixture {

  private Long m_subPeerValue;

  public Long getSubPeerValue() {
    return m_subPeerValue;
  }

  public void setSubPeerValue(Long contributedSubPeerValue) {
    m_subPeerValue = contributedSubPeerValue;
  }
}
