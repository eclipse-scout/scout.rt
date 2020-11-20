/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.CompactBean;
import org.eclipse.scout.rt.client.ui.basic.table.columns.CompactLine;
import org.eclipse.scout.rt.client.ui.basic.table.columns.CompactLineBuilder;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.TableCompactHandler;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableCompactHandlerTest {
  private P_Table m_table;

  @Before
  public void setUp() {
    m_table = new P_Table();
    ITableRow row = m_table.addRow();
    m_table.getInvisibleFirstColumn().setValue(row, "invisible");
    m_table.getFirstColumn().setValue(row, "first");
    m_table.getSecondColumn().setValue(row, "second");
    m_table.getThirdColumn().setValue(row, "third");
    m_table.getFourthColumn().setValue(row, "fourth");
    m_table.getFifthColumn().setValue(row, "fifth");
  }

  @Test
  public void testBuildBean_Default() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(2)
        .withMoreLinkAvailable(false);
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertFalse(bean.getTitle().contains("lbl"));
    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertFalse(bean.getSubtitle().contains("lbl2"));
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getContent().contains("lbl3"));
    assertTrue(bean.getContent().contains("third"));
    assertTrue(bean.getContent().contains("lbl4"));
    assertTrue(bean.getContent().contains("fourth"));
    assertFalse(bean.getContent().contains("lbl5"));
    assertFalse(bean.getContent().contains("fifth"));

    // Assert order of content
    assertTrue(bean.getContent().contains("third")
        && bean.getContent().indexOf("third") < bean.getContent().indexOf("fourth"));
  }

  @Test
  public void testBuildBean_InvisibleColumns() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(2)
        .withUseOnlyVisibleColumns(false);
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().contains("invisible"));
  }

  @Test
  public void testBuildBean_Title() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(2)
        .withMoreLinkAvailable(false)
        .withTitleColumnSupplier(() -> m_table.getThirdColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertFalse(bean.getTitle().contains("lbl"));
    assertTrue(bean.getTitle().contains("third"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getContentLines().get(0).build().contains("first"));
    assertTrue(bean.getContentLines().get(1).build().contains("fourth"));
    assertFalse(bean.getContent().contains("fifth"));
  }

  @Test
  public void testBuildBean_Subtitle() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(2)
        .withMoreLinkAvailable(false)
        .withSubtitleColumnSupplier(() -> m_table.getThirdColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertFalse(bean.getSubtitle().contains("lbl"));
    assertTrue(bean.getSubtitle().contains("third"));
    assertTrue(bean.getContentLines().get(0).build().contains("second"));
    assertTrue(bean.getContentLines().get(1).build().contains("fourth"));
    assertFalse(bean.getContent().contains("fifth"));
  }

  @Test
  public void testBuildBean_TitleSuffix() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(2)
        .withTitleSuffixColumnSupplier(() -> m_table.getThirdColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("first"));
    assertFalse(bean.getTitleSuffix().contains("lbl"));
    assertTrue(bean.getTitleSuffix().contains("third"));
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getContentLines().get(0).build().contains("fourth"));
    assertTrue(bean.getContentLines().get(1).build().contains("fifth"));
    assertFalse(bean.getContent().contains("third"));
  }

  @Test
  public void testBuildBean_TitleSubtitleContent() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(2)
        .withTitleColumnSupplier(() -> m_table.getFirstColumn())
        .withSubtitleColumnSupplier(() -> m_table.getThirdColumn())
        .addContentColumnSupplier(() -> m_table.getFourthColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().contains("third"));
    assertTrue(bean.getContentLines().get(0).build().contains("fourth"));
    assertTrue(bean.getContentLines().get(1).build().contains("second")); // -> moved from second place to last
  }

  @Test
  public void testBuildBean_Labels() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(2)
        .withTitleBuilder(new CompactLineBuilder(() -> m_table.getFirstColumn()).withShowLabel(true))
        .withSubtitleBuilder(new CompactLineBuilder(() -> m_table.getSecondColumn()).withShowLabel(true))
        .withTitleSuffixBuilder(new CompactLineBuilder(() -> m_table.getThirdColumn()).withShowLabel(true))
        .addContentLineBuilder(new CompactLineBuilder(() -> m_table.getFourthColumn()).withShowLabel(false))
        .addContentLineBuilder(new CompactLineBuilder(() -> m_table.getFifthColumn()).withShowLabel(false));

    CompactBean bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("lbl1"));
    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getSubtitle().contains("lbl2"));
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getTitleSuffix().contains("lbl3"));
    assertTrue(bean.getTitleSuffix().contains("third"));
    assertFalse(bean.getContent().contains("lbl4"));
    assertTrue(bean.getContent().contains("fourth"));
    assertFalse(bean.getContent().contains("lbl5"));
    assertTrue(bean.getContent().contains("fifth"));
  }

  @Test
  public void testBuildBean_NlToBr() {
    m_table.getFirstColumn().setValue(m_table.getRow(0), "first\nnewline");
    TableCompactHandler handler = new TableCompactHandler(m_table);
    CompactBean bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("first\nnewline"));

    // Enable nl to br conversion
    handler = new TableCompactHandler(m_table);
    handler.withTitleBuilder(new CompactLineBuilder(() -> m_table.getFirstColumn()) {
      @Override
      public CompactLine build(IColumn<?> column, ITableRow row) {
        CompactLine line = super.build(column, row);
        line.getTextBlock().setNlToBrEnabled(true);
        return line;
      }
    });
    bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("first<br>newline"));
  }

  @Test
  public void testBuildBean_Custom() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withBeanBuilder((column, rows) -> {
      CompactBean bean = new CompactBean();
      bean.setTitleLine(new CompactLine("txt1", "val1"));
      bean.setSubtitleLine(new CompactLine("txt2", "val2"));
      bean.setTitleSuffixLine(new CompactLine("txt3", "val3"));
      bean.addContentLine(new CompactLine("txt4", "val4"));
      bean.addContentLine(new CompactLine("txt5", "val5"));
      bean.addContentLine(new CompactLine("txt6", "val6"));
      bean.addContentLine(new CompactLine("txt7", "val7"));
      return bean;
    }).withTitleSuffixColumnSupplier(() -> m_table.getFirstColumn()); // <- won't have an effect

    CompactBean bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("txt1"));
    assertTrue(bean.getTitle().contains("val1"));
    assertTrue(bean.getSubtitle().contains("txt2"));
    assertTrue(bean.getSubtitle().contains("val2"));
    assertTrue(bean.getTitleSuffix().contains("txt3"));
    assertTrue(bean.getTitleSuffix().contains("val3"));
    assertTrue(bean.getContent().contains("txt4"));
    assertTrue(bean.getContent().contains("val4"));
    assertTrue(bean.getContent().contains("txt5"));
    assertTrue(bean.getContent().contains("val5"));

    // Assert order of content
    assertTrue(bean.getContent().contains("txt4")
        && bean.getContent().indexOf("txt4") < bean.getContent().indexOf("txt5"));
  }

  @Test
  public void testBuildBean_OrderedContent() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(3) //
        .addContentColumnSupplier(() -> m_table.getFifthColumn()) // <- order will be considered
        .addContentColumnSupplier(() -> m_table.getFourthColumn())
        .addContentColumnSupplier(() -> m_table.getThirdColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getContentLines().get(0).build().contains("fifth"));
    assertTrue(bean.getContentLines().get(1).build().contains("fourth"));
    assertTrue(bean.getContentLines().get(2).build().contains("third"));
  }

  /**
   * Limit content lines to 1 so that remaining columns will be added to more content
   */
  @Test
  public void testBuildBean_MoreContent() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(1) //
        .addContentColumnSupplier(() -> m_table.getFifthColumn())
        .addContentColumnSupplier(() -> m_table.getFourthColumn())
        .addContentColumnSupplier(() -> m_table.getThirdColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getContent().contains("fifth"));
    assertFalse(bean.getContent().contains("fourth"));
    assertFalse(bean.getContent().contains("third"));
    assertTrue(bean.getMoreContent().contains("fourth")
        && bean.getMoreContent().indexOf("fourth") < bean.getMoreContent().indexOf("third"));
  }

  @Test
  public void testBuildBean_MoreContentSingleSupplier() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(1)
        .addContentColumnSupplier(() -> m_table.getFifthColumn()); // even if only one column is provided the remaining ones will be added
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getContent().contains("fifth"));
    assertFalse(bean.getContent().contains("fourth"));
    assertFalse(bean.getContent().contains("third"));
    assertTrue(bean.getMoreContent().contains("third")
        && bean.getMoreContent().indexOf("third") < bean.getMoreContent().indexOf("fourth"));
  }

  @Test
  public void testBuildBean_NoTitles() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(5)
        .withTitleColumnSupplier(() -> null)
        .withSubtitleColumnSupplier(() -> null);
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().isEmpty());
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().isEmpty());
    assertTrue(bean.getContentLines().get(0).build().contains("first"));
    assertTrue(bean.getContentLines().get(1).build().contains("second"));
    assertTrue(bean.getContentLines().get(2).build().contains("third"));
    assertTrue(bean.getContentLines().get(3).build().contains("fourth"));
    assertTrue(bean.getContentLines().get(4).build().contains("fifth"));
  }

  @Test
  public void testBuildBean_DuplicateColumns() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(5)
        .addContentColumnSupplier(() -> m_table.getFirstColumn()); // already used as title column -> no effect
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getSubtitle().contains("second"));
    assertTrue(bean.getContentLines().get(0).build().contains("third"));
    assertTrue(bean.getContentLines().get(1).build().contains("fourth"));
    assertTrue(bean.getContentLines().get(2).build().contains("fifth"));
  }

  @Test
  public void testBuildBean_DuplicateColumns2() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withMaxContentLines(5)
        .withSubtitleColumnSupplier(() -> m_table.getThirdColumn())
        .addContentColumnSupplier(() -> m_table.getThirdColumn()); // already used as subtitle column -> no effect
    CompactBean bean = handler.buildBean(m_table.getRow(0));

    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getSubtitle().contains("third"));
    assertTrue(bean.getContentLines().get(0).build().contains("second"));
    assertTrue(bean.getContentLines().get(1).build().contains("fourth"));
    assertTrue(bean.getContentLines().get(2).build().contains("fifth"));
  }

  @Test
  public void testBuildBean_Exclude() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withColumnFilter(column -> column != m_table.getThirdColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("first"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().contains("second"));
    assertFalse(bean.getContent().contains("third"));
    assertTrue(bean.getContent().contains("fourth"));
    assertTrue(bean.getContent().contains("fifth"));
  }

  /**
   * When the first column is excluded, the second column moves to the top so that it will be used for the title.
   */
  @Test
  public void testBuildBean_ExcludeFirst() {
    TableCompactHandler handler = new TableCompactHandler(m_table);
    handler.withColumnFilter(column -> column != m_table.getFirstColumn());
    CompactBean bean = handler.buildBean(m_table.getRow(0));
    assertTrue(bean.getTitle().contains("second"));
    assertTrue(bean.getTitleSuffix().isEmpty());
    assertTrue(bean.getSubtitle().contains("third"));
    assertTrue(bean.getContent().contains("fourth"));
    assertTrue(bean.getContent().contains("fifth"));
    assertTrue(bean.getContent().contains("fifth"));
    assertFalse(bean.getContent().contains("first"));
    assertFalse(bean.getTitle().contains("first"));
    assertFalse(bean.getSubtitle().contains("first"));
  }

  public static class P_Table extends AbstractTable {

    public InvisibleFirstColumn getInvisibleFirstColumn() {
      return getColumnSet().getColumnByClass(InvisibleFirstColumn.class);
    }

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    public SecondColumn getSecondColumn() {
      return getColumnSet().getColumnByClass(SecondColumn.class);
    }

    public ThirdColumn getThirdColumn() {
      return getColumnSet().getColumnByClass(ThirdColumn.class);
    }

    public FourthColumn getFourthColumn() {
      return getColumnSet().getColumnByClass(FourthColumn.class);
    }

    public FifthColumn getFifthColumn() {
      return getColumnSet().getColumnByClass(FifthColumn.class);
    }

    @Order(1)
    public class InvisibleFirstColumn extends AbstractStringColumn {
      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }

      @Override
      protected String getConfiguredHeaderText() {
        return "lblinvisible";
      }
    }

    @Order(10)
    public class FirstColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "lbl1";
      }
    }

    @Order(20)
    public class SecondColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "lbl2";
      }
    }

    @Order(30)
    public class ThirdColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "lbl3";
      }
    }

    @Order(40)
    public class FourthColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "lbl4";
      }
    }

    @Order(50)
    public class FifthColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "lbl5";
      }
    }
  }
}
