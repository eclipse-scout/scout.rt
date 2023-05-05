/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.services.common.code;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeProducer;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link ICodeService#invalidateCodeType(Class)}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class CodeServiceInvalidateTest {

  IBean<?> codeTypeBean;

  @Before
  public void before() {
    codeTypeBean = BeanTestingHelper.get().registerBean(new BeanMetaData(TestCodeType.class).withProducer(new CodeTypeProducer()));
    TestCodeType.INSTANCE_SEQ.set(0);
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBean(codeTypeBean);
  }

  @Test
  public void testInvalidateInsideSubTransaction() {
    RunContexts.copyCurrent().run(() -> CODES.invalidateCodeTypes(new ArrayList<>(CODES.getAllCodeTypeClasses(""))));

    readCodeType(1);
    invalidateCodeType();
    readCodeType(2);

    ServerRunContexts
        .copyCurrent()
        .run(() -> {
          readCodeType(1);
          //this will remove the codeType from the shared map upon commit
          invalidateCodeType();
        });
    readCodeType(2);
    invalidateCodeType();
    readCodeType(3);
  }

  @Test
  public void testReloadInsideSubTransaction() {
    RunContexts.copyCurrent().run(() -> CODES.invalidateCodeTypes(new ArrayList<>(CODES.getAllCodeTypeClasses(""))));

    readCodeType(1);
    invalidateCodeType();
    readCodeType(2);

    ServerRunContexts
        .copyCurrent()
        .run(() -> {
          readCodeType(1);
          //this will remove the codeType from the shared map upon commit
          invalidateCodeType();
          //this will re-insert the codeType into the shared map upon commit
          readCodeType(3);
        });

    readCodeType(2);
    invalidateCodeType();
    readCodeType(4);
  }

  private static TestCodeType readCodeType(int expectedInstanceId) {
    TestCodeType codeType = BEANS.get(TestCodeType.class);
    Assert.assertEquals("TestCodeType.instanceId", expectedInstanceId, codeType.instanceId);
    return codeType;
  }

  private static void invalidateCodeType() {
    CODES.invalidateCodeType(TestCodeType.class);
  }

  @IgnoreBean
  public static final class TestCodeType extends AbstractCodeType<Long, String> {
    private static final long serialVersionUID = 1L;
    public static final AtomicInteger INSTANCE_SEQ = new AtomicInteger();

    public static final Long ID = Long.valueOf(42);

    public int instanceId = INSTANCE_SEQ.incrementAndGet();

    @Override
    public Long getId() {
      return ID;
    }

    @Override
    protected List<? extends ICodeRow<String>> execLoadCodes(Class<? extends ICodeRow<String>> codeRowType) {
      return super.execLoadCodes(codeRowType);
    }
  }
}
