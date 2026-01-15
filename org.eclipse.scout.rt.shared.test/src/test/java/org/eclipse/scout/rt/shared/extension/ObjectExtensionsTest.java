/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ObjectExtensionsTest {

  @BeanMock
  public IInternalExtensionRegistry m_internalExtensionRegistry;

  @SuppressWarnings("unchecked")
  @Test
  public void testLoadExtensions() {
    testLoadExtensions_internal(Collections.emptyList(), "java.util.Collections$SingletonList");
    testLoadExtensions_internal(Collections.singletonList(mock(IExtension.class)), "java.util.ImmutableCollections$List12");
    testLoadExtensions_internal(Collections.nCopies(2, mock(IExtension.class)), "java.util.ImmutableCollections$ListN");
    testLoadExtensions_internal(Collections.nCopies(3, mock(IExtension.class)), "java.util.ImmutableCollections$ListN");
    testLoadExtensions_internal(Collections.nCopies(4, mock(IExtension.class)), "java.util.ImmutableCollections$ListN");
    testLoadExtensions_internal(new LinkedList<>(Collections.nCopies(5, mock(IExtension.class))), "java.util.Collections$UnmodifiableRandomAccessList");
  }

  protected void testLoadExtensions_internal(List<IExtension<?>> extensionRegistryList, String expectedListClassName) {
    @SuppressWarnings("unchecked") IExtension<IExtensibleObject> localExtension = (IExtension<IExtensibleObject>) mock(IExtension.class);
    when(m_internalExtensionRegistry.createExtensionsFor(any())).thenReturn(new ArrayList<>(extensionRegistryList));

    ObjectExtensions<IExtensibleObject, IExtension<? extends IExtensibleObject>> objectExtensions = new ObjectExtensions<>(mock(IExtensibleObject.class), false);
    List<IExtension<? extends IExtensibleObject>> extensions = objectExtensions.loadExtensions(localExtension);
    assertEquals(extensionRegistryList.size() + 1, extensions.size());
    assertEquals(expectedListClassName, extensions.getClass().getName());
  }
}
