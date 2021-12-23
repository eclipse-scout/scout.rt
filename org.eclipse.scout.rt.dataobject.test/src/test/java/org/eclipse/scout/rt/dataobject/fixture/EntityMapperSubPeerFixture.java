/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
