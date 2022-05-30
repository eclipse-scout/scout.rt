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
package org.eclipse.scout.rt.dataobject.migration;

import java.util.function.Function;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic test implementation for a single {@link IDoStructureMigrationTargetContextData}.
 */
public abstract class AbstractDoStructureMigrationTargetContextDataTest<T extends IDoStructureMigrationTargetContextData> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDoStructureMigrationTargetContextDataTest.class);

  /**
   * @return {@link DoEntity} instance (not a subclass) having a "_type" attribute with one of the values in
   *         {@link DoStructureMigrationContextDataTarget#typeNames()} and which will result in
   *         {@link IDoStructureMigrationTargetContextData#initialize} returning true.
   */
  protected abstract IDoEntity getValidContextDoEntityTyped();

  /**
   * @return {@link IDoEntity} which is an instance of one of the classes in
   *         {@link DoStructureMigrationContextDataTarget#doEntityClasses()} and which will result in
   *         {@link IDoStructureMigrationTargetContextData#initialize} returning true.
   */
  protected IDoEntity getValidContextDoEntityRaw() {
    return BEANS.get(DataObjectHelper.class).cloneRaw(getValidContextDoEntityTyped());
  }

  /**
   * @return {@link IDoEntity} which will result in {@link IDoStructureMigrationTargetContextData#initialize} returning
   *         false.
   */
  protected IDoEntity getInvalidContextDoEntity() {
    return new DoEntity();
  }

  protected T m_targetContext;
  protected DoStructureMigrationContext m_ctx;

  @Before
  public void before() {
    m_ctx = BEANS.get(DoStructureMigrationContext.class);
    @SuppressWarnings("unchecked")
    Class<? extends T> targetContextClazz = (Class<? extends T>) TypeCastUtility.getGenericsParameterClass(getClass(), AbstractDoStructureMigrationTargetContextDataTest.class);
    m_targetContext = BEANS.get(targetContextClazz);
  }

  @Test
  public void testValidContextForMatchingDoEntityTyped() {
    IDoEntity doEntity = getValidContextDoEntityTyped();
    Assertions.assertNotEquals(DoEntity.class, doEntity.getClass());
    Assertions.assertTrue(m_targetContext.initialize(m_ctx, doEntity));
  }

  @Test
  public void testValidContextForMatchingDoEntityRaw() {
    IDoEntity doEntity = getValidContextDoEntityRaw();
    Assertions.assertEquals(DoEntity.class, doEntity.getClass());
    Assertions.assertTrue(m_targetContext.initialize(m_ctx, doEntity));
  }

  @Test
  public void testNonMatchingTypedDoEntity() {
    IDoEntity doEntity = getInvalidContextDoEntity();
    Assertions.assertFalse(m_targetContext.initialize(m_ctx, doEntity));
  }

  protected <V> void assertContextValue(V expected, Function<T, V> actualGetter, IDoEntity doEntity) {
    Assertions.assertTrue(m_targetContext.initialize(m_ctx, doEntity));
    Assertions.assertEquals(expected, actualGetter.apply(m_targetContext));
  }
}
