/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureForm.MainBoxInWrappedForm;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.FormFieldMenuMenu;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.FormFieldMenuMenu.BigDecimalField;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.InnerGroupBox;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.InnerGroupBox.DateField;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.InnerGroupBox.LastBox;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.InnerGroupBox.LastBox.LongField;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.ListBox;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.StringField;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.TreeBoxField;
import org.eclipse.scout.rt.client.ui.WidgetVisitorTest.FixtureWidget.MainBox.WrappedFormField;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.AbstractFormFieldMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.table.menus.OrganizeColumnsMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox.DefaultListBoxTable;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox.ListBoxFilterBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox.ActiveStateRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox.CheckedStateRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox.CheckedStateRadioButtonGroup.AllButton;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox.DefaultTreeBoxTree;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox.TreeBoxFilterBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.visitor.CollectingVisitor;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class WidgetVisitorTest {

  @Test
  public void testVisitorFull() {
    FixtureWidget root = new FixtureWidget();
    CollectingVisitorWrapper<IWidget> visitor = new CollectingVisitorWrapper<IWidget>(action -> TreeVisitResult.CONTINUE);
    root.visit(visitor);
    visitor.expectCollectedWidgets(FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        TreeBoxFilterBox.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton.class,
        DefaultTreeBoxTree.class,
        ListBox.class,
        ListBoxFilterBox.class,
        CheckedStateRadioButtonGroup.class,
        CheckedButton.class,
        AllButton.class,
        ActiveStateRadioButtonGroup.class,
        ActiveButton.class,
        InactiveButton.class,
        ActiveAndInactiveButton.class,
        DefaultListBoxTable.class,
        OrganizeColumnsMenu.class,
        InnerGroupBox.class,
        LastBox.class,
        LongField.class,
        DateField.class,
        WrappedFormField.class,
        FixtureForm.class,
        MainBoxInWrappedForm.class,
        FormFieldMenuMenu.class,
        BigDecimalField.class);
  }

  @Test
  public void testPostVisit() {
    List<String> visitedWidgets = new ArrayList<>();
    FixtureWidget root = new FixtureWidget();
    root.visit(new IDepthFirstTreeVisitor<IWidget>() {
      @Override
      public TreeVisitResult preVisit(IWidget widget, int level, int index) {
        return TreeVisitResult.CONTINUE;
      }

      @Override
      public boolean postVisit(IWidget widget, int level, int index) {
        visitedWidgets.add(widget.getClass().getName() + " " + level + " " + index);
        return true;
      }
    });

    assertEquals(asList(
        StringField.class.getName() + " 2 0",
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class.getName() + " 5 0",
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class.getName() + " 5 1",
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class.getName() + " 4 0",
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton.class.getName() + " 5 0",
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton.class.getName() + " 5 1",
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton.class.getName() + " 5 2",
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.class.getName() + " 4 1",
        TreeBoxFilterBox.class.getName() + " 3 0",
        DefaultTreeBoxTree.class.getName() + " 3 1",
        TreeBoxField.class.getName() + " 2 1",
        CheckedButton.class.getName() + " 5 0",
        AllButton.class.getName() + " 5 1",
        CheckedStateRadioButtonGroup.class.getName() + " 4 0",
        ActiveButton.class.getName() + " 5 0",
        InactiveButton.class.getName() + " 5 1",
        ActiveAndInactiveButton.class.getName() + " 5 2",
        ActiveStateRadioButtonGroup.class.getName() + " 4 1",
        ListBoxFilterBox.class.getName() + " 3 0",
        OrganizeColumnsMenu.class.getName() + " 4 0",
        DefaultListBoxTable.class.getName() + " 3 1",
        ListBox.class.getName() + " 2 2",
        LongField.class.getName() + " 4 0",
        LastBox.class.getName() + " 3 0",
        DateField.class.getName() + " 3 1",
        InnerGroupBox.class.getName() + " 2 3",
        MainBoxInWrappedForm.class.getName() + " 4 0",
        FixtureForm.class.getName() + " 3 0",
        WrappedFormField.class.getName() + " 2 4",
        BigDecimalField.class.getName() + " 3 0",
        FormFieldMenuMenu.class.getName() + " 2 5",
        MainBox.class.getName() + " 1 0",
        FixtureWidget.class.getName() + " 0 0"), visitedWidgets);
  }

  @Test
  public void testVisitorMenusOnly() {
    FixtureWidget root = new FixtureWidget();
    CollectingVisitorWrapper<IActionNode> visitor = new CollectingVisitorWrapper<IActionNode>(action -> TreeVisitResult.CONTINUE);
    root.visit(visitor, IActionNode.class);
    visitor.expectCollectedWidgets(OrganizeColumnsMenu.class, FormFieldMenuMenu.class);
  }

  @Test
  public void testVisitorWithTerminate() {
    FixtureWidget root = new FixtureWidget();
    CollectingVisitorWrapper<IFormField> visitor = new CollectingVisitorWrapper<IFormField>(formField -> {
      if (org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class == formField.getClass()) {
        return TreeVisitResult.TERMINATE;
      }
      return TreeVisitResult.CONTINUE;
    });
    root.visit(visitor, IFormField.class);
    visitor.expectCollectedWidgets(
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        TreeBoxFilterBox.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class);
  }

  @Test
  public void testLevelOrderTraversalFull() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IWidget>> visitedWidgets = new ArrayList<>();
    root.visit((element, level, index) -> {
      visitedWidgets.add(element.getClass());
      return TreeVisitResult.CONTINUE;
    });
    assertEquals(asList(
        FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        ListBox.class,
        InnerGroupBox.class,
        WrappedFormField.class,
        FormFieldMenuMenu.class,
        TreeBoxFilterBox.class,
        DefaultTreeBoxTree.class,
        ListBoxFilterBox.class,
        DefaultListBoxTable.class,
        LastBox.class,
        DateField.class,
        FixtureForm.class,
        BigDecimalField.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.class,
        CheckedStateRadioButtonGroup.class,
        ActiveStateRadioButtonGroup.class,
        OrganizeColumnsMenu.class,
        LongField.class,
        MainBoxInWrappedForm.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton.class,
        CheckedButton.class,
        AllButton.class,
        ActiveButton.class,
        InactiveButton.class,
        ActiveAndInactiveButton.class), visitedWidgets);
  }

  @Test
  public void testLevelOrderTraversalFiltered() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IMenu>> visitedWidgets = new ArrayList<>();
    root.visit((element, level, index) -> {
      visitedWidgets.add(element.getClass());
      return TreeVisitResult.CONTINUE;
    }, IMenu.class);
    assertEquals(asList(FormFieldMenuMenu.class, OrganizeColumnsMenu.class), visitedWidgets);
  }

  @Test
  public void testLevelOrderTraversalCancelled() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IWidget>> visitedWidgets = new ArrayList<>();
    root.visit((element, level, index) -> {
      visitedWidgets.add(element.getClass());
      return (element instanceof TreeBoxFilterBox) ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
    });
    assertEquals(asList(
        FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        ListBox.class,
        InnerGroupBox.class,
        WrappedFormField.class,
        FormFieldMenuMenu.class,
        TreeBoxFilterBox.class), visitedWidgets);
  }

  @Test
  public void testLevelOrderTraversalSkipSubtree() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IWidget>> visitedWidgets = new ArrayList<>();
    root.visit((element, level, index) -> {
      visitedWidgets.add(element.getClass());
      return (element instanceof ListBox || element instanceof TreeBoxField) ? TreeVisitResult.SKIP_SUBTREE : TreeVisitResult.CONTINUE;
    });
    assertEquals(asList(
        FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        ListBox.class,
        InnerGroupBox.class,
        WrappedFormField.class,
        FormFieldMenuMenu.class,
        LastBox.class,
        DateField.class,
        FixtureForm.class,
        BigDecimalField.class,
        LongField.class,
        MainBoxInWrappedForm.class), visitedWidgets);
  }

  @Test
  public void testPostVisitTerminate() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IFormField>> visitedWidgets = new ArrayList<>();
    root.visit(new DepthFirstTreeVisitor<IFormField>() {
      @Override
      public TreeVisitResult preVisit(IFormField element, int level, int index) {
        visitedWidgets.add(element.getClass());
        return TreeVisitResult.CONTINUE;
      }

      @Override
      public boolean postVisit(IFormField element, int level, int index) {
        return false;
      }
    }, IFormField.class);

    assertEquals(asList(MainBox.class, StringField.class), visitedWidgets);
  }

  @Test
  public void testLevelOrderTraversalSkipSiblings2() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IWidget>> visitedWidgets = new ArrayList<>();
    root.visit((element, level, index) -> {
      visitedWidgets.add(element.getClass());
      return (element instanceof ActiveStateRadioButtonGroup) ? TreeVisitResult.SKIP_SIBLINGS : TreeVisitResult.CONTINUE; // skip siblings on last sibling
    });
    assertEquals(asList(
        FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        ListBox.class,
        InnerGroupBox.class,
        WrappedFormField.class,
        FormFieldMenuMenu.class,
        TreeBoxFilterBox.class,
        DefaultTreeBoxTree.class,
        ListBoxFilterBox.class,
        DefaultListBoxTable.class,
        LastBox.class,
        DateField.class,
        FixtureForm.class,
        BigDecimalField.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.class,
        CheckedStateRadioButtonGroup.class,
        ActiveStateRadioButtonGroup.class,
        OrganizeColumnsMenu.class,
        LongField.class,
        MainBoxInWrappedForm.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton.class,
        CheckedButton.class,
        AllButton.class,
        ActiveButton.class,
        InactiveButton.class,
        ActiveAndInactiveButton.class), visitedWidgets);
  }

  @Test
  public void testLevelOrderTraversalSkipSiblings3() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IWidget>> visitedWidgets = new ArrayList<>();
    root.visit((element, level, index) -> {
      visitedWidgets.add(element.getClass());
      return (element instanceof ActiveAndInactiveButton) ? TreeVisitResult.SKIP_SIBLINGS : TreeVisitResult.CONTINUE; // skip siblings on last sibling
    });
    assertEquals(asList(
        FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        ListBox.class,
        InnerGroupBox.class,
        WrappedFormField.class,
        FormFieldMenuMenu.class,
        TreeBoxFilterBox.class,
        DefaultTreeBoxTree.class,
        ListBoxFilterBox.class,
        DefaultListBoxTable.class,
        LastBox.class,
        DateField.class,
        FixtureForm.class,
        BigDecimalField.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.class,
        CheckedStateRadioButtonGroup.class,
        ActiveStateRadioButtonGroup.class,
        OrganizeColumnsMenu.class,
        LongField.class,
        MainBoxInWrappedForm.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton.class,
        CheckedButton.class,
        AllButton.class,
        ActiveButton.class,
        InactiveButton.class,
        ActiveAndInactiveButton.class), visitedWidgets);
  }

  @Test
  public void testLevelOrderTraversalSkipSiblings() {
    FixtureWidget root = new FixtureWidget();
    List<Class<? extends IWidget>> visitedWidgets = new ArrayList<>();
    root.visit((element, level, index) -> {
      visitedWidgets.add(element.getClass());
      return (element instanceof CheckedStateRadioButtonGroup) ? TreeVisitResult.SKIP_SIBLINGS : TreeVisitResult.CONTINUE;
    });
    assertEquals(asList(
        FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        ListBox.class,
        InnerGroupBox.class,
        WrappedFormField.class,
        FormFieldMenuMenu.class,
        TreeBoxFilterBox.class,
        DefaultTreeBoxTree.class,
        ListBoxFilterBox.class,
        DefaultListBoxTable.class,
        LastBox.class,
        DateField.class,
        FixtureForm.class,
        BigDecimalField.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.class,
        CheckedStateRadioButtonGroup.class,
        OrganizeColumnsMenu.class,
        LongField.class,
        MainBoxInWrappedForm.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton.class,
        CheckedButton.class,
        AllButton.class), visitedWidgets);
  }

  @Test
  public void testVisitorWithSkipSubTree() {
    FixtureWidget root = new FixtureWidget();
    CollectingVisitorWrapper<IFormField> visitor = new CollectingVisitorWrapper<IFormField>(formField -> {
      if (TreeBoxFilterBox.class == formField.getClass() || ListBoxFilterBox.class == formField.getClass()) {
        return TreeVisitResult.SKIP_SUBTREE;
      }
      return TreeVisitResult.CONTINUE;
    });
    root.visit(visitor, IFormField.class);
    visitor.expectCollectedWidgets(
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        TreeBoxFilterBox.class,
        ListBox.class,
        ListBoxFilterBox.class,
        InnerGroupBox.class,
        LastBox.class,
        LongField.class,
        DateField.class,
        WrappedFormField.class,
        MainBoxInWrappedForm.class,
        BigDecimalField.class);
  }

  @Test
  public void testVisitorWithSkipSiblings() {
    FixtureWidget root = new FixtureWidget();
    CollectingVisitorWrapper<IWidget> visitor = new CollectingVisitorWrapper<IWidget>(widget -> {
      if (ActiveButton.class == widget.getClass()) {
        return TreeVisitResult.SKIP_SIBLINGS;
      }
      return TreeVisitResult.CONTINUE;
    });
    root.visit(visitor);
    visitor.expectCollectedWidgets(
        FixtureWidget.class,
        MainBox.class,
        StringField.class,
        TreeBoxField.class,
        TreeBoxFilterBox.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.CheckedButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.CheckedStateRadioButtonGroup.AllButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.InactiveButton.class,
        org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBoxFilterBox.ActiveStateRadioButtonGroup.ActiveAndInactiveButton.class,
        DefaultTreeBoxTree.class,
        ListBox.class,
        ListBoxFilterBox.class,
        CheckedStateRadioButtonGroup.class,
        CheckedButton.class,
        AllButton.class,
        ActiveStateRadioButtonGroup.class,
        ActiveButton.class,
        DefaultListBoxTable.class,
        OrganizeColumnsMenu.class,
        InnerGroupBox.class,
        LastBox.class,
        LongField.class,
        DateField.class,
        WrappedFormField.class,
        FixtureForm.class,
        MainBoxInWrappedForm.class,
        FormFieldMenuMenu.class,
        BigDecimalField.class);
  }

  @Test
  public void testGetWidgetByClass() {
    FixtureWidget root = new FixtureWidget();
    assertNotNull(root.getWidgetByClass(ListBox.class));
    assertNotNull(root.getWidgetByClass(FixtureWidget.class));
    assertNotNull(root.getWidgetByClass(LongField.class));
  }

  private static class CollectingVisitorWrapper<T extends IWidget> extends CollectingVisitor<T> {

    private Function<T, TreeVisitResult> m_visitor;

    private CollectingVisitorWrapper(Function<T, TreeVisitResult> visitor) {
      m_visitor = visitor;
    }

    @Override
    public TreeVisitResult preVisit(T widget, int level, int index) {
      super.preVisit(widget, level, index);
      return m_visitor.apply(widget);
    }

    @SafeVarargs
    public final void expectCollectedWidgets(Class<? extends IWidget>... expected) {
      assertEquals(asList(expected), getCollection().stream().map(Object::getClass).collect(toList()));
    }
  }

  public static class FixtureWidget extends AbstractWidget {

    private final IGroupBox m_mainBox = new MainBox();

    @Override
    public List<? extends IWidget> getChildren() {
      List<IWidget> result = new ArrayList<>();
      result.add(m_mainBox);
      result.add(null);
      return result;
    }

    public class MainBox extends AbstractGroupBox {
      @Order(1000)
      public class FormFieldMenuMenu extends AbstractFormFieldMenu {
        @Order(1000)
        public class BigDecimalField extends AbstractBigDecimalField {
        }
      }

      @Order(1000)
      public class StringField extends AbstractStringField {
      }

      @Order(2000)
      public class TreeBoxField extends AbstractTreeBox<Long> {
      }

      @Order(3000)
      public class ListBox extends AbstractListBox<Long> {
      }

      @Order(5000)
      public class InnerGroupBox extends AbstractGroupBox {

        @Order(1000)
        public class LastBox extends AbstractGroupBox {

          @Order(1000)
          public class LongField extends AbstractLongField {
          }
        }

        @Order(2000)
        public class DateField extends AbstractDateField {
        }
      }

      @Order(6000)
      public class WrappedFormField extends AbstractWrappedFormField<FixtureForm> {
        @Override
        protected Class<? extends IForm> getConfiguredInnerForm() {
          return FixtureForm.class;
        }
      }
    }
  }

  public static final class FixtureForm extends AbstractForm {
    @Order(1000)
    public class MainBoxInWrappedForm extends AbstractGroupBox {
    }
  }
}
