/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.services.common.code;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeProducer;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
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
    codeTypeBean = TestingUtility.registerBean(new BeanMetaData(TestCodeType.class).withProducer(new CodeTypeProducer()));
    TestCodeType.INSTANCE_SEQ.set(0);
  }

  @After
  public void after() {
    TestingUtility.unregisterBean(codeTypeBean);
  }

  @Test
  public void testInvalidateInsideSubTransaction() {
    CODES.invalidateCodeTypes(new ArrayList<>(CODES.getAllCodeTypeClasses("")));

    readCodeType(1);
    invalidateCodeType();
    readCodeType(2);

    ServerRunContexts
        .copyCurrent()
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            readCodeType(1);
            //this will remove the codeType from the shared map upon commit
            invalidateCodeType();
          }
        });
    readCodeType(2);
    invalidateCodeType();
    readCodeType(3);
  }

  @Test
  public void testReloadInsideSubTransaction() {
    CODES.invalidateCodeTypes(new ArrayList<>(CODES.getAllCodeTypeClasses("")));

    readCodeType(1);
    invalidateCodeType();
    readCodeType(2);

    ServerRunContexts
        .copyCurrent()
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            readCodeType(1);
            //this will remove the codeType from the shared map upon commit
            invalidateCodeType();
            //this will re-insert the codeType into the shared map upon commit
            readCodeType(3);
          }
        });

    readCodeType(2);
    invalidateCodeType();
    readCodeType(4);
  }

  private static TestCodeType readCodeType(int expectedInstanceId) {
    TestCodeType codeType = BEANS.get(TestCodeType.class);
    System.out.println("" + codeType.instanceId);
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
