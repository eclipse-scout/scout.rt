/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.context.NodeIdentifier;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class NodeIdTest {

  @BeanMock
  protected NodeIdentifier m_nodeIdentifierMock;

  @Test
  public void testCurrent() {
    Mockito.when(m_nodeIdentifierMock.get()).thenReturn("mock-node");
    assertEquals(NodeId.of("mock-node"), NodeId.current());
    assertEquals("mock-node", NodeId.current().unwrap());
  }
}
