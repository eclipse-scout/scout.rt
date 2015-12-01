/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link ICode#addChildCodeInternal(ICode)}, {@link ICode#removeChildCodeInternal(ICode)},
 * {@link AbstractCodeTypeWithGeneric#addRootCodeInternal(ICode)},
 * {@link AbstractCodeTypeWithGeneric#removeRootCodeInternal(ICode)}
 *
 * @since 4.0
 */
@RunWith(PlatformTestRunner.class)
public class CodeTypeWithAddRemoveCodeTest {

  @Test
  public void testAddRemoveRootCode() throws Exception {
    TestCodeType ct = new TestCodeType();
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11},{id:12,text:Test12}]}", dumpCodeType(ct));

    ct.addRootCodeInternal(-1, new MutableCode<Long>(new CodeRow<Long>(20L, "Root20")));
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11},{id:12,text:Test12}]},{id:20,text:Root20}", dumpCodeType(ct));

    ct.addRootCodeInternal(0, new MutableCode<Long>(new CodeRow<Long>(5L, "Root5")));
    assertEquals("{id:5,text:Root5},{id:10,text:Root10,children:[{id:11,text:Test11},{id:12,text:Test12}]},{id:20,text:Root20}", dumpCodeType(ct));

    ct.addRootCodeInternal(100, new MutableCode<Long>(new CodeRow<Long>(30L, "Root30")));
    assertEquals("{id:5,text:Root5},{id:10,text:Root10,children:[{id:11,text:Test11},{id:12,text:Test12}]},{id:20,text:Root20},{id:30,text:Root30}", dumpCodeType(ct));

    //replace
    ct.addRootCodeInternal(-1, new MutableCode<Long>(new CodeRow<Long>(10L, "Root10b")));
    assertEquals("{id:5,text:Root5},{id:10,text:Root10b},{id:20,text:Root20},{id:30,text:Root30}", dumpCodeType(ct));

    //not part of it
    ct.removeRootCodeInternal(11L);
    assertEquals("{id:5,text:Root5},{id:10,text:Root10b},{id:20,text:Root20},{id:30,text:Root30}", dumpCodeType(ct));

    ct.removeRootCodeInternal(5L);
    assertEquals("{id:10,text:Root10b},{id:20,text:Root20},{id:30,text:Root30}", dumpCodeType(ct));

    ct.removeRootCodeInternal(10L);
    assertEquals("{id:20,text:Root20},{id:30,text:Root30}", dumpCodeType(ct));

    ct.removeRootCodeInternal(30L);
    assertEquals("{id:20,text:Root20}", dumpCodeType(ct));

    ct.removeRootCodeInternal(20L);
    assertEquals("", dumpCodeType(ct));
  }

  @Test
  public void testAddRemoveChildCode() throws Exception {
    TestCodeType ct = new TestCodeType();
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11},{id:12,text:Test12}]}", dumpCodeType(ct));

    ICode<Long> root = ct.getCode(10L);

    root.addChildCodeInternal(-1, new MutableCode<Long>(new CodeRow<Long>(20L, "Child20")));
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11},{id:12,text:Test12},{id:20,text:Child20}]}", dumpCodeType(ct));

    root.addChildCodeInternal(0, new MutableCode<Long>(new CodeRow<Long>(5L, "Child5")));
    assertEquals("{id:10,text:Root10,children:[{id:5,text:Child5},{id:11,text:Test11},{id:12,text:Test12},{id:20,text:Child20}]}", dumpCodeType(ct));

    root.addChildCodeInternal(100, new MutableCode<Long>(new CodeRow<Long>(30L, "Child30")));
    assertEquals("{id:10,text:Root10,children:[{id:5,text:Child5},{id:11,text:Test11},{id:12,text:Test12},{id:20,text:Child20},{id:30,text:Child30}]}", dumpCodeType(ct));

    //replace
    root.addChildCodeInternal(-1, new MutableCode<Long>(new CodeRow<Long>(11L, "Test11b")));
    assertEquals("{id:10,text:Root10,children:[{id:5,text:Child5},{id:11,text:Test11b},{id:12,text:Test12},{id:20,text:Child20},{id:30,text:Child30}]}", dumpCodeType(ct));

    //not part of it
    root.removeChildCodeInternal(10L);
    assertEquals("{id:10,text:Root10,children:[{id:5,text:Child5},{id:11,text:Test11b},{id:12,text:Test12},{id:20,text:Child20},{id:30,text:Child30}]}", dumpCodeType(ct));

    root.removeChildCodeInternal(5L);
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11b},{id:12,text:Test12},{id:20,text:Child20},{id:30,text:Child30}]}", dumpCodeType(ct));

    root.removeChildCodeInternal(12L);
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11b},{id:20,text:Child20},{id:30,text:Child30}]}", dumpCodeType(ct));

    root.removeChildCodeInternal(30L);
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11b},{id:20,text:Child20}]}", dumpCodeType(ct));

    root.removeChildCodeInternal(20L);
    assertEquals("{id:10,text:Root10,children:[{id:11,text:Test11b}]}", dumpCodeType(ct));

    root.removeChildCodeInternal(11L);
    assertEquals("{id:10,text:Root10}", dumpCodeType(ct));
  }

  private static String dumpCodeType(ICodeType<Long, Long> ct) {
    StringBuilder buf = new StringBuilder();
    List<? extends ICode<Long>> list = ct.getCodes();
    if (list.size() > 0) {
      for (ICode<Long> c : list) {
        buf.append(dumpCodeRec(c));
        buf.append(",");
      }
      buf.setLength(buf.length() - 1);
    }
    return buf.toString();
  }

  private static String dumpCodeRec(ICode<Long> c) {
    StringBuilder buf = new StringBuilder();
    buf.append("{");
    buf.append("id:" + c.getId());
    buf.append(",text:" + c.getText());
    List<? extends ICode<Long>> list = c.getChildCodes();
    if (list.size() > 0) {
      buf.append(",children:[");
      for (ICode<Long> x : list) {
        buf.append(dumpCodeRec(x));
        buf.append(",");
      }
      buf.setLength(buf.length() - 1);
      buf.append("]");
    }
    buf.append("}");
    return buf.toString();
  }

  private static class TestCodeType extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;
    public static final long ID = 123L;

    @Override
    public Long getId() {
      return ID;
    }

    @Order(10)
    public class RootCode extends AbstractCode<Long> {
      private static final long serialVersionUID = 1L;
      public static final long ID = 10L;

      @Override
      public Long getId() {
        return ID;
      }

      @Override
      protected String getConfiguredText() {
        return "Root10";
      }

      @Order(10)
      public class Test1Code extends AbstractCode<Long> {
        private static final long serialVersionUID = 1L;
        public static final long ID = 11L;

        @Override
        public Long getId() {
          return ID;
        }

        @Override
        protected String getConfiguredText() {
          return "Test11";
        }
      }

      @Order(20)
      public class Test2Code extends AbstractCode<Long> {
        private static final long serialVersionUID = 1L;
        public static final long ID = 12L;

        @Override
        public Long getId() {
          return ID;
        }

        @Override
        protected String getConfiguredText() {
          return "Test12";
        }
      }

    }
  }
}
