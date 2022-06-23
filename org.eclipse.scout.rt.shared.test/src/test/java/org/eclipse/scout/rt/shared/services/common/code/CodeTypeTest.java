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
package org.eclipse.scout.rt.shared.services.common.code;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link AbstractCode}
 */
@RunWith(PlatformTestRunner.class)
public class CodeTypeTest {

  @Test
  public void testOverwriteCode_overwriteValues() {
    TestCodeType ct = new TestCodeType();
    ICode c = ct.getCode(TestCodeType.Test1Code.ID);
    assertEquals(TestCodeType.DYNAMIC_TEXT, c.getText());
    assertEquals(TestCodeType.CONFIGURED_ICON, c.getIconId());
    assertNull(c.getTooltipText());
    assertEquals(TestCodeType.CONFIGURED_BACKGROUND_COLOR, c.getBackgroundColor());
    assertEquals(TestCodeType.CONFIGURED_FOREGROUND_COLOR, c.getForegroundColor());
    assertEquals(TestCodeType.CONFIGURED_FONT, c.getFont().toPattern());
    assertEquals(TestCodeType.CONFIGURED_CSS_CLASS, c.getCssClass());
    assertEquals(TestCodeType.DYNAMIC_ENABLED, c.isEnabled());
    // parent key
    assertEquals(TestCodeType.DYNAMIC_ACTIVE, c.isActive());
    assertEquals(TestCodeType.CONFIGURED_EXT_KEY, c.getExtKey());
    assertEquals(TestCodeType.CONFIGURED_VALUE, c.getValue());
    assertEquals(TestCodeType.DYNAMIC_PARTITION_ID, c.getPartitionId());
  }

  @Test
  public void testOverwriteCode_keepValues() {
    TestCodeType ct = new TestCodeType();
    ICode c = ct.getCode(TestCodeType.Test2Code.ID);
    assertEquals(TestCodeType.DYNAMIC_TEXT, c.getText());
    assertEquals(TestCodeType.DYNAMIC_ICON, c.getIconId());
    assertEquals(TestCodeType.DYNAMIC_TOOLTIP, c.getTooltipText());
    assertEquals(TestCodeType.DYNAMIC_BACKGROUND_COLOR, c.getBackgroundColor());
    assertEquals(TestCodeType.DYNAMIC_FOREGROUND_COLOR, c.getForegroundColor());
    assertEquals(TestCodeType.DYNAMIC_FONT, c.getFont().toPattern());
    assertEquals(TestCodeType.DYNAMIC_CSS_CLASS, c.getCssClass());
    assertEquals(TestCodeType.DYNAMIC_ENABLED, c.isEnabled());
    // parent key
    assertEquals(TestCodeType.DYNAMIC_ACTIVE, c.isActive());
    assertEquals(TestCodeType.DYNAMIC_EXT_KEY, c.getExtKey());
    assertEquals(TestCodeType.DYNAMIC_VALUE, c.getValue());
    assertEquals(TestCodeType.DYNAMIC_PARTITION_ID, c.getPartitionId());
  }

  private static class TestCodeType extends AbstractCodeType<String, String> {
    private static final long serialVersionUID = 1L;
    public static final String ID = "2";

    public static final String CONFIGURED_TEXT = "configuredText";
    public static final String CONFIGURED_ICON = "configuredIcon";
    public static final String CONFIGURED_TOOLTIP = "configuredTooltip";
    public static final String CONFIGURED_BACKGROUND_COLOR = "configuredBackgroundColor";
    public static final String CONFIGURED_FOREGROUND_COLOR = "configuredForegroundColor";
    public static final String CONFIGURED_FONT = "null-ITALIC-0";
    public static final String CONFIGURED_CSS_CLASS = "configuredCssClass";
    public static final boolean CONFIGURED_ENABLED = false;
    public static final boolean CONFIGURED_ACTIVE = false;
    public static final String CONFIGURED_EXT_KEY = "configuredExtKey";
    public static final double CONFIGURED_VALUE = 42d;

    public static final String DYNAMIC_TEXT = "dynamicText";
    public static final String DYNAMIC_ICON = "dynamicIcon";
    public static final String DYNAMIC_TOOLTIP = "dynamicTooltip";
    public static final String DYNAMIC_BACKGROUND_COLOR = "dynamicBackgroundColor";
    public static final String DYNAMIC_FOREGROUND_COLOR = "dynamicForegroundColor";
    public static final String DYNAMIC_FONT = "null-BOLD-0";
    public static final String DYNAMIC_CSS_CLASS = "dynamicCssClass";
    public static final boolean DYNAMIC_ENABLED = true;
    public static final String DYNAMIC_PARENT_KEY = "dynamicParentKey";
    public static final boolean DYNAMIC_ACTIVE = true;
    public static final String DYNAMIC_EXT_KEY = "dynamicExtKey";
    public static final double DYNAMIC_VALUE = 10d;
    public static final long DYNAMIC_PARTITION_ID = 0L;

    private List<String> m_overwrittenCodes;

    @Override
    public String getId() {
      return ID;
    }

    public List<String> getOverwrittenCodes() {
      return m_overwrittenCodes;
    }

    @Override
    protected List<ICodeRow<String>> execLoadCodes(Class<? extends ICodeRow<String>> codeRowType) {
      return Arrays.asList(
          new CodeRow<>(
              RootCode.ID,
              "Root",
              null, // icon
              null, // tooltip
              null, // background color
              null, // foreground color
              null, // font
              null, // css class
              DYNAMIC_ENABLED,
              null,
              DYNAMIC_ACTIVE,
              null, // ext key
              null, // value
              DYNAMIC_PARTITION_ID),
          new CodeRow<>(
              RootCode.ChildCode.ID,
              "Child",
              null, // icon
              null, // tooltip
              null, // background color
              null, // foreground color
              null, // font
              null, // css class
              DYNAMIC_ENABLED,
              DYNAMIC_PARENT_KEY,
              DYNAMIC_ACTIVE,
              null, // ext key
              null, // value
              DYNAMIC_PARTITION_ID),
          new CodeRow<>(
              Test1Code.ID,
              DYNAMIC_TEXT,
              null, // icon
              null, // tooltip
              null, // background color
              null, // foreground color
              null, // font
              null, // css class
              DYNAMIC_ENABLED,
              DYNAMIC_PARENT_KEY,
              DYNAMIC_ACTIVE,
              null, // ext key
              null, // value
              DYNAMIC_PARTITION_ID),
          new CodeRow<>(
              Test2Code.ID,
              DYNAMIC_TEXT,
              DYNAMIC_ICON,
              DYNAMIC_TOOLTIP,
              DYNAMIC_BACKGROUND_COLOR,
              DYNAMIC_FOREGROUND_COLOR,
              FontSpec.parse(DYNAMIC_FONT),
              DYNAMIC_CSS_CLASS,
              DYNAMIC_ENABLED,
              DYNAMIC_PARENT_KEY,
              DYNAMIC_ACTIVE,
              DYNAMIC_EXT_KEY,
              DYNAMIC_VALUE,
              DYNAMIC_PARTITION_ID));
    }

    @Override
    protected void execOverwriteCode(ICodeRow<String> oldCode, ICodeRow<String> newCode) {
      if (oldCode != null) {
        m_overwrittenCodes.add(oldCode.getKey());
      }
      super.execOverwriteCode(oldCode, newCode);
    }

    @Override
    protected void loadCodes() {
      m_overwrittenCodes = new ArrayList<>();
      super.loadCodes();
    }

    @Order(10)
    public class RootCode extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "dynamicParentKey";

      @Override
      public String getId() {
        return ID;
      }

      @Override
      protected String getConfiguredText() {
        return "Root";
      }

      @Order(10)
      public class ChildCode extends AbstractCode<String> {
        private static final long serialVersionUID = 1L;
        public static final String ID = "75b3ede5-065a-4869-a4a7-c477af43abce";

        @Override
        public String getId() {
          return ID;
        }

        @Override
        protected String getConfiguredText() {
          return "Child";
        }
      }
    }

    @Order(10)
    public class Test1Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Test1Code";

      @Override
      public String getId() {
        return ID;
      }

      @Override
      protected String getConfiguredText() {
        return CONFIGURED_TEXT;
      }

      @Override
      protected String getConfiguredIconId() {
        return CONFIGURED_ICON;
      }

      @Override
      protected String getConfiguredTooltipText() {
        return CONFIGURED_TOOLTIP;
      }

      @Override
      protected String getConfiguredBackgroundColor() {
        return CONFIGURED_BACKGROUND_COLOR;
      }

      @Override
      protected String getConfiguredForegroundColor() {
        return CONFIGURED_FOREGROUND_COLOR;
      }

      @Override
      protected String getConfiguredFont() {
        return CONFIGURED_FONT;
      }

      @Override
      protected String getConfiguredCssClass() {
        return CONFIGURED_CSS_CLASS;
      }

      @Override
      protected boolean getConfiguredEnabled() {
        return CONFIGURED_ENABLED;
      }

      @Override
      protected boolean getConfiguredActive() {
        return CONFIGURED_ACTIVE;
      }

      @Override
      protected String getConfiguredExtKey() {
        return CONFIGURED_EXT_KEY;
      }

      @Override
      protected Double getConfiguredValue() {
        return CONFIGURED_VALUE;
      }
    }

    @Order(20)
    public class Test2Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Test2Code";

      @Override
      public String getId() {
        return ID;
      }

      @Override
      protected String getConfiguredText() {
        return CONFIGURED_TEXT;
      }

      @Override
      protected String getConfiguredIconId() {
        return CONFIGURED_ICON;
      }

      @Override
      protected String getConfiguredTooltipText() {
        return CONFIGURED_TOOLTIP;
      }

      @Override
      protected String getConfiguredBackgroundColor() {
        return CONFIGURED_BACKGROUND_COLOR;
      }

      @Override
      protected String getConfiguredForegroundColor() {
        return CONFIGURED_FOREGROUND_COLOR;
      }

      @Override
      protected String getConfiguredFont() {
        return CONFIGURED_FONT;
      }

      @Override
      protected boolean getConfiguredEnabled() {
        return CONFIGURED_ENABLED;
      }

      @Override
      protected boolean getConfiguredActive() {
        return CONFIGURED_ACTIVE;
      }

      @Override
      protected String getConfiguredExtKey() {
        return CONFIGURED_EXT_KEY;
      }

      @Override
      protected Double getConfiguredValue() {
        return CONFIGURED_VALUE;
      }
    }
  }

  /**
   * If a code is defined by {@link AbstractCodeTypeWithGeneric#execCreateCodes()} and overwritten by
   * {@link AbstractCodeTypeWithGeneric#execLoadCodes(Class)} the method
   * {@link AbstractCodeTypeWithGeneric#execOverwriteCode(ICodeRow, ICodeRow)} must be called also for the code as well
   * (root and child codes).
   */
  @Test
  public void testExecOverwriteWasForRootAndChildCodes() {
    TestCodeType ct = new TestCodeType();
    List<String> overwrittenCodes = ct.getOverwrittenCodes();
    assertEquals(4, overwrittenCodes.size());
    assertTrue(overwrittenCodes.contains(TestCodeType.RootCode.ID));
    assertTrue(overwrittenCodes.contains(TestCodeType.RootCode.ChildCode.ID));
    assertTrue(overwrittenCodes.contains(TestCodeType.Test1Code.ID));
    assertTrue(overwrittenCodes.contains(TestCodeType.Test2Code.ID));
  }
}
