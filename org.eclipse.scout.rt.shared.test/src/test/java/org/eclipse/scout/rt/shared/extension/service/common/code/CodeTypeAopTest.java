/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension.service.common.code;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.services.common.code.AbstractCodeTypeExtension;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodeChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericLoadCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericOverwriteCodeChain;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;
import org.junit.Assert;
import org.junit.Test;

public class CodeTypeAopTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testExecValidate() throws Exception {

    BEANS.get(IExtensionRegistry.class).register(CodeTypeExtension.class);

    CountryCodeType ct = new CountryCodeType();
    Assert.assertNotNull(ct.getCode(Long.valueOf(42)));
    Assert.assertEquals(1, ct.getCodes().size());
    Assert.assertEquals(0, execCreateCodeCounter.get());
    Assert.assertEquals(1, execCreateCodesCounter.get());
    Assert.assertEquals(1, execLoadCodesCounter.get());
    Assert.assertEquals(0, execOverwriteCodeCounter.get());

  }

  public static class CountryCodeType extends AbstractCodeType<Long, Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 20000L;

    public CountryCodeType() {
      super();
    }

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Country");
    }

    @Override
    public Long getId() {
      return ID;
    }

    @Order(10)
    public static class USACode extends AbstractCode<Long> {

      private static final long serialVersionUID = 1L;
      public static final Long ID = 20001L;

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("USA");
      }

      @Override
      public Long getId() {
        return ID;
      }
    }

    @Order(20)
    public static class GreatBritainCode extends AbstractCode<Long> {

      private static final long serialVersionUID = 1L;
      public static final Long ID = 20002L;

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("GreatBritain");
      }

      @Override
      public Long getId() {
        return ID;
      }
    }

    @Order(30)
    public static class GermanyCode extends AbstractCode<Long> {

      private static final long serialVersionUID = 1L;
      public static final Long ID = 20003L;

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("Germany");
      }

      @Override
      public Long getId() {
        return ID;
      }
    }

    @Order(40)
    public static class FranceCode extends AbstractCode<Long> {

      private static final long serialVersionUID = 1L;
      public static final Long ID = 20004L;

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("France");
      }

      @Override
      public Long getId() {
        return ID;
      }
    }

    @Order(50)
    public static class SwitzerlandCode extends AbstractCode<Long> {

      private static final long serialVersionUID = 1L;
      public static final Long ID = 20005L;

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("Switzerland");
      }

      @Override
      public Long getId() {
        return ID;
      }
    }
  }

  private static AtomicInteger execCreateCodesCounter = new AtomicInteger(0);
  private static AtomicInteger execCreateCodeCounter = new AtomicInteger(0);
  private static AtomicInteger execLoadCodesCounter = new AtomicInteger(0);
  private static AtomicInteger execOverwriteCodeCounter = new AtomicInteger(0);

  public static class CodeTypeExtension extends AbstractCodeTypeExtension<Long, Long, CountryCodeType> {

    private static final long serialVersionUID = 1L;

    /**
     * @param owner
     */
    public CodeTypeExtension(CountryCodeType owner) {
      super(owner);
    }

    @Override
    public List<? extends ICode<Long>> execCreateCodes(CodeTypeWithGenericCreateCodesChain chain) {
      execCreateCodesCounter.incrementAndGet();
      AbstractCode<Long> burmaCode = new AbstractCode<Long>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected String getConfiguredText() {
          return "Burma";
        }

        @Override
        public Long getId() {
          return Long.valueOf(42);
        }
      };
      return CollectionUtility.arrayList(burmaCode);
    }

    @Override
    public ICode<Long> execCreateCode(CodeTypeWithGenericCreateCodeChain chain, ICodeRow<Long> newRow) {
      execCreateCodeCounter.incrementAndGet();
      return super.execCreateCode(chain, newRow);
    }

    @Override
    public List<? extends ICodeRow<Long>> execLoadCodes(CodeTypeWithGenericLoadCodesChain chain, Class<? extends ICodeRow<Long>> codeRowType) {
      execLoadCodesCounter.incrementAndGet();
      return super.execLoadCodes(chain, codeRowType);
    }

    @Override
    public void execOverwriteCode(CodeTypeWithGenericOverwriteCodeChain chain, ICodeRow<Long> oldCode, ICodeRow<Long> newCode) {
      execOverwriteCodeCounter.incrementAndGet();
      super.execOverwriteCode(chain, oldCode, newCode);
    }

  }

}
