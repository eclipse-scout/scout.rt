/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

import jdk.nashorn.api.tree.ArrayAccessTree;
import jdk.nashorn.api.tree.ArrayLiteralTree;
import jdk.nashorn.api.tree.AssignmentTree;
import jdk.nashorn.api.tree.BinaryTree;
import jdk.nashorn.api.tree.BlockTree;
import jdk.nashorn.api.tree.BreakTree;
import jdk.nashorn.api.tree.CaseTree;
import jdk.nashorn.api.tree.CatchTree;
import jdk.nashorn.api.tree.ClassDeclarationTree;
import jdk.nashorn.api.tree.ClassExpressionTree;
import jdk.nashorn.api.tree.CompilationUnitTree;
import jdk.nashorn.api.tree.CompoundAssignmentTree;
import jdk.nashorn.api.tree.ConditionalExpressionTree;
import jdk.nashorn.api.tree.ConditionalLoopTree;
import jdk.nashorn.api.tree.ContinueTree;
import jdk.nashorn.api.tree.DebuggerTree;
import jdk.nashorn.api.tree.DoWhileLoopTree;
import jdk.nashorn.api.tree.EmptyStatementTree;
import jdk.nashorn.api.tree.ErroneousTree;
import jdk.nashorn.api.tree.ExportEntryTree;
import jdk.nashorn.api.tree.ExpressionStatementTree;
import jdk.nashorn.api.tree.ForInLoopTree;
import jdk.nashorn.api.tree.ForLoopTree;
import jdk.nashorn.api.tree.ForOfLoopTree;
import jdk.nashorn.api.tree.FunctionCallTree;
import jdk.nashorn.api.tree.FunctionDeclarationTree;
import jdk.nashorn.api.tree.FunctionExpressionTree;
import jdk.nashorn.api.tree.IdentifierTree;
import jdk.nashorn.api.tree.IfTree;
import jdk.nashorn.api.tree.ImportEntryTree;
import jdk.nashorn.api.tree.InstanceOfTree;
import jdk.nashorn.api.tree.LabeledStatementTree;
import jdk.nashorn.api.tree.LiteralTree;
import jdk.nashorn.api.tree.MemberSelectTree;
import jdk.nashorn.api.tree.ModuleTree;
import jdk.nashorn.api.tree.NewTree;
import jdk.nashorn.api.tree.ObjectLiteralTree;
import jdk.nashorn.api.tree.ParenthesizedTree;
import jdk.nashorn.api.tree.Parser;
import jdk.nashorn.api.tree.PropertyTree;
import jdk.nashorn.api.tree.RegExpLiteralTree;
import jdk.nashorn.api.tree.ReturnTree;
import jdk.nashorn.api.tree.SpreadTree;
import jdk.nashorn.api.tree.SwitchTree;
import jdk.nashorn.api.tree.TemplateLiteralTree;
import jdk.nashorn.api.tree.ThrowTree;
import jdk.nashorn.api.tree.Tree;
import jdk.nashorn.api.tree.TryTree;
import jdk.nashorn.api.tree.UnaryTree;
import jdk.nashorn.api.tree.VariableTree;
import jdk.nashorn.api.tree.WhileLoopTree;
import jdk.nashorn.api.tree.WithTree;
import jdk.nashorn.api.tree.YieldTree;

public class ES6Parser {
  private final Map<Tree, List<Tree>> m_tree = new IdentityHashMap<>();
  private String m_source;
  private long m_lastPos;

  public Map<Tree, List<Tree>> parse(String path, String source) throws IOException {
    //ES6
    Parser parser = Parser.create(
        "--language=es6",
        "--es6-module");

    StringBuffer diag = new StringBuffer();
    CompilationUnitTree cut = parser.parse(path, source, (d) -> {
      System.out.println("ES6 parser: " + d);
      diag.append(d).append("\n");
    });
    if (cut == null) {
      throw new ProcessingException(diag.toString());
    }
    m_source = source;
    m_lastPos = 0;
    visitAny(null, cut);
    return m_tree;
  }

  private void add(Tree parent, Tree child) {
    m_tree
        .computeIfAbsent(parent, parent2 -> new ArrayList<>())
        .add(child);
    m_lastPos = child.getStartPosition();
  }

  private void visitAtribute(Tree node, String attributeName, String attributeValue) {
    //nop
  }

  private void visitUnknown(Tree parent, Tree node) {
    if (node == null) return;
    add(parent, node);
    System.out.println("ES6Parser.visitUnknown " + node);
  }

  private void visitAssignment(Tree parent, AssignmentTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getVariable());
    visitAny(node, node.getExpression());
  }

  private void visitCompoundAssignment(Tree parent, CompoundAssignmentTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getVariable());
    visitAny(node, node.getExpression());
  }

  /**
   * Visits a {@code ModuleTree} tree
   */
  private void visitModule(Tree parent, ModuleTree node) {
    if (node == null) return;
    add(parent, node);
    node.getImportEntries().forEach(tree -> visitAny(node, tree));
    node.getStarExportEntries().forEach(tree -> visitAny(node, tree));
    node.getIndirectExportEntries().forEach(tree -> visitAny(node, tree));
    node.getLocalExportEntries().forEach(tree -> visitAny(node, tree));
  }

  /**
   * Visits an {@code ExportEntryTree} tree
   */
  private void visitExportEntry(Tree parent, ExportEntryTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getModuleRequest());
    visitAny(node, node.getLocalName());
    visitAny(node, node.getExportName());
    visitAny(node, node.getImportName());
  }

  /**
   * Visits an {@code ImportEntryTree} tree
   */
  private void visitImportEntry(Tree parent, ImportEntryTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getModuleRequest());
    visitAny(node, node.getImportName());
    visitAny(node, node.getLocalName());
  }

  private void visitBinary(Tree parent, BinaryTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getLeftOperand());
    visitAny(node, node.getRightOperand());
  }

  private void visitBlock(Tree parent, BlockTree node) {
    if (node == null) return;
    add(parent, node);
    node.getStatements().forEach(tree -> visitAny(node, tree));
  }

  private void visitBreak(Tree parent, BreakTree node) {
    if (node == null) return;
    add(parent, node);
    visitAtribute(node, "label", node.getLabel());
  }

  private void visitCase(Tree parent, CaseTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
    node.getStatements().forEach(tree -> visitAny(node, tree));
  }

  private void visitCatch(Tree parent, CatchTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getCondition());
    visitAny(node, node.getParameter());
    visitBlock(node, node.getBlock());
  }

  /**
   * Visits a {@code ClassDeclarationTree} tree
   */
  private void visitClassDeclaration(Tree parent, ClassDeclarationTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getName());
    visitAny(node, node.getClassHeritage());
    visitAny(node, node.getConstructor());
    node.getClassElements().forEach(tree -> visitProperty(node, tree));
  }

  /**
   * Visits a {@code ClassExpressionTree} tree
   */
  private void visitClassExpression(Tree parent, ClassExpressionTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getName());
    visitAny(node, node.getClassHeritage());
    visitAny(node, node.getConstructor());
    node.getClassElements().forEach(tree -> visitProperty(node, tree));
  }

  private void visitConditionalExpression(Tree parent, ConditionalExpressionTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getCondition());
    visitAny(node, node.getTrueExpression());
    visitAny(node, node.getFalseExpression());
  }

  private void visitContinue(Tree parent, ContinueTree node) {
    if (node == null) return;
    add(parent, node);
    visitAtribute(node, "label", node.getLabel());
  }

  private void visitDebugger(Tree parent, DebuggerTree node) {
    if (node == null) return;
    add(parent, node);
  }

  private void visitDoWhileLoop(Tree parent, DoWhileLoopTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getStatement());
    visitAny(node, node.getCondition());
  }

  private void visitErroneous(Tree parent, ErroneousTree node) {
    if (node == null) return;
    add(parent, node);
  }

  private void visitExpressionStatement(Tree parent, ExpressionStatementTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
  }

  private void visitForLoop(Tree parent, ForLoopTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getInitializer());
    visitAny(node, node.getCondition());
    visitAny(node, node.getUpdate());
    visitAny(node, node.getStatement());
  }

  private void visitForInLoop(Tree parent, ForInLoopTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getVariable());
    visitAny(node, node.getExpression());
    visitAny(node, node.getStatement());
  }

  /**
   * Visits a {@code ForOfLoopTree} tree
   */
  private void visitForOfLoop(Tree parent, ForOfLoopTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getVariable());
    visitAny(node, node.getStatement());
    visitAny(node, node.getExpression());
  }

  /**
   * Visits a {@code ConditionalLoopTree} tree
   */
  private void visitConditionalLoop(Tree parent, ConditionalLoopTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getCondition());
    visitAny(node, node.getStatement());
  }

  private void visitFunctionCall(Tree parent, FunctionCallTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getFunctionSelect());
    node.getArguments().forEach(tree -> visitAny(node, tree));
  }

  private void visitFunctionDeclaration(Tree parent, FunctionDeclarationTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getName());
    node.getParameters().forEach(tree -> visitAny(node, tree));
    visitBlock(node, node.getBody());
  }

  private void visitFunctionExpression(Tree parent, FunctionExpressionTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getName());
    node.getParameters().forEach(tree -> visitAny(node, tree));
    visitAny(node, node.getBody());
  }

  private void visitIdentifier(Tree parent, IdentifierTree node) {
    if (node == null) return;
    add(parent, node);
    visitAtribute(node, "name", node.getName());
  }

  private void visitIf(Tree parent, IfTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getCondition());
    visitAny(node, node.getThenStatement());
    visitAny(node, node.getElseStatement());
  }

  private void visitArrayAccess(Tree parent, ArrayAccessTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
    visitAny(node, node.getIndex());
  }

  private void visitArrayLiteral(Tree parent, ArrayLiteralTree node) {
    if (node == null) return;
    add(parent, node);
    node.getElements().forEach(tree -> visitAny(node, tree));
  }

  private void visitLabeledStatement(Tree parent, LabeledStatementTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getStatement());
    visitAtribute(node, "label", node.getLabel());
  }

  private void visitLiteral(Tree parent, LiteralTree node) {
    if (node == null) return;
    add(parent, node);
    visitAtribute(node, "value", String.valueOf(node.getValue()));
  }

  private void visitParenthesized(Tree parent, ParenthesizedTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
  }

  private void visitReturn(Tree parent, ReturnTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
  }

  private void visitMemberSelect(Tree parent, MemberSelectTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
    visitAtribute(node, "identifier", node.getIdentifier());
  }

  private void visitNew(Tree parent, NewTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getConstructorExpression());
  }

  private void visitObjectLiteral(Tree parent, ObjectLiteralTree node) {
    if (node == null) return;
    add(parent, node);
    node.getProperties().forEach(tree -> visitProperty(node, tree));
  }

  private void visitProperty(Tree parent, PropertyTree node) {
    if (node == null) return;
    add(parent, node);
    visitFunctionExpression(node, node.getGetter());
    visitAny(node, node.getKey());
    visitFunctionExpression(node, node.getSetter());
    visitAny(node, node.getValue());
  }

  private void visitRegExpLiteral(Tree parent, RegExpLiteralTree node) {
    if (node == null) return;
    add(parent, node);
    visitAtribute(node, "pattern", node.getPattern());
    visitAtribute(node, "options", node.getOptions());
  }

  /**
   * Visits a {@code TemplateLiteralTree} tree
   */
  private void visitTemplateLiteral(Tree parent, TemplateLiteralTree node) {
    if (node == null) return;
    add(parent, node);
    node.getExpressions().forEach(tree -> visitAny(node, tree));
  }

  private void visitEmptyStatement(Tree parent, EmptyStatementTree node) {
    if (node == null) return;
    add(parent, node);
  }

  /**
   * Visits a {@code SpreadTree} tree
   */
  private void visitSpread(Tree parent, SpreadTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
  }

  private void visitSwitch(Tree parent, SwitchTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
    node.getCases().forEach(tree -> visitCase(node, tree));
  }

  private void visitThrow(Tree parent, ThrowTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
  }

  private void visitCompilationUnit(Tree parent, CompilationUnitTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getModule());
    node.getSourceElements().forEach(tree -> visitAny(node, tree));
  }

  private void visitTry(Tree parent, TryTree node) {
    if (node == null) return;
    add(parent, node);
    visitBlock(node, node.getBlock());
    node.getCatches().forEach(tree -> visitCatch(node, tree));
    visitBlock(node, node.getFinallyBlock());
  }

  private void visitInstanceOf(Tree parent, InstanceOfTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getType());
    visitAny(node, node.getExpression());
  }

  private void visitUnary(Tree parent, UnaryTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
  }

  private void visitVariable(Tree parent, VariableTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getBinding());
    visitAny(node, node.getInitializer());
  }

  private void visitWhileLoop(Tree parent, WhileLoopTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getCondition());
    visitAny(node, node.getStatement());
  }

  private void visitWith(Tree parent, WithTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getScope());
    visitAny(node, node.getStatement());
  }

  /**
   * Visits a {@code YieldTree} tree
   */
  private void visitYield(Tree parent, YieldTree node) {
    if (node == null) return;
    add(parent, node);
    visitAny(node, node.getExpression());
  }

  private void visitAny(Tree parent, Tree node) {
    if (node == null) return;

    else if (node instanceof CaseTree) visitCase(parent, (CaseTree) node);
    else if (node instanceof CatchTree) visitCatch(parent, (CatchTree) node);
    else if (node instanceof PropertyTree) visitProperty(parent, (PropertyTree) node);
    else if (node instanceof CompilationUnitTree) visitCompilationUnit(parent, (CompilationUnitTree) node);
    else if (node instanceof ExportEntryTree) visitExportEntry(parent, (ExportEntryTree) node);
    else if (node instanceof ModuleTree) visitModule(parent, (ModuleTree) node);
    else if (node instanceof ImportEntryTree) visitImportEntry(parent, (ImportEntryTree) node);

    //specialization of ExpressionTree
    else if (node instanceof SpreadTree) visitSpread(parent, (SpreadTree) node);
    else if (node instanceof NewTree) visitNew(parent, (NewTree) node);
    else if (node instanceof BinaryTree) visitBinary(parent, (BinaryTree) node);
    else if (node instanceof YieldTree) visitYield(parent, (YieldTree) node);
    else if (node instanceof CompoundAssignmentTree) visitCompoundAssignment(parent, (CompoundAssignmentTree) node);
    else if (node instanceof FunctionExpressionTree) visitFunctionExpression(parent, (FunctionExpressionTree) node);
    else if (node instanceof UnaryTree) visitUnary(parent, (UnaryTree) node);
    else if (node instanceof LiteralTree) visitLiteral(parent, (LiteralTree) node);
    else if (node instanceof InstanceOfTree) visitInstanceOf(parent, (InstanceOfTree) node);
    else if (node instanceof MemberSelectTree) visitMemberSelect(parent, (MemberSelectTree) node);
    else if (node instanceof TemplateLiteralTree) visitTemplateLiteral(parent, (TemplateLiteralTree) node);
    else if (node instanceof ObjectLiteralTree) visitObjectLiteral(parent, (ObjectLiteralTree) node);
    else if (node instanceof AssignmentTree) visitAssignment(parent, (AssignmentTree) node);
    else if (node instanceof ClassExpressionTree) visitClassExpression(parent, (ClassExpressionTree) node);
    else if (node instanceof IdentifierTree) visitIdentifier(parent, (IdentifierTree) node);
    else if (node instanceof ParenthesizedTree) visitParenthesized(parent, (ParenthesizedTree) node);
    else if (node instanceof ConditionalExpressionTree) visitConditionalExpression(parent, (ConditionalExpressionTree) node);
    else if (node instanceof FunctionCallTree) visitFunctionCall(parent, (FunctionCallTree) node);
    else if (node instanceof RegExpLiteralTree) visitRegExpLiteral(parent, (RegExpLiteralTree) node);
    else if (node instanceof ArrayAccessTree) visitArrayAccess(parent, (ArrayAccessTree) node);
    else if (node instanceof ErroneousTree) visitErroneous(parent, (ErroneousTree) node);
    else if (node instanceof ArrayLiteralTree) visitArrayLiteral(parent, (ArrayLiteralTree) node);
    //end ExpressionTree

    //specialization of StatementTree
    else if (node instanceof WithTree) visitWith(parent, (WithTree) node);
    else if (node instanceof ReturnTree) visitReturn(parent, (ReturnTree) node);
    else if (node instanceof VariableTree) visitVariable(parent, (VariableTree) node);
    else if (node instanceof ClassDeclarationTree) visitClassDeclaration(parent, (ClassDeclarationTree) node);
    else if (node instanceof ThrowTree) visitThrow(parent, (ThrowTree) node);
    else if (node instanceof BlockTree) visitBlock(parent, (BlockTree) node);
    else if (node instanceof LabeledStatementTree) visitLabeledStatement(parent, (LabeledStatementTree) node);

    //specializations of LoopTree
    else if (node instanceof ForOfLoopTree) visitForOfLoop(parent, (ForOfLoopTree) node);
    else if (node instanceof ConditionalLoopTree) visitConditionalLoop(parent, (ConditionalLoopTree) node);
    else if (node instanceof ForInLoopTree) visitForInLoop(parent, (ForInLoopTree) node);
    //end LoopTree

    else if (node instanceof IfTree) visitIf(parent, (IfTree) node);
    else if (node instanceof EmptyStatementTree) visitEmptyStatement(parent, (EmptyStatementTree) node);
    else if (node instanceof ExpressionStatementTree) visitExpressionStatement(parent, (ExpressionStatementTree) node);
    else if (node instanceof FunctionDeclarationTree) visitFunctionDeclaration(parent, (FunctionDeclarationTree) node);

    //specialization of GotoTree
    else if (node instanceof ContinueTree) visitContinue(parent, (ContinueTree) node);
    else if (node instanceof BreakTree) visitBreak(parent, (BreakTree) node);
    //end GotoTree

    else if (node instanceof SwitchTree) visitSwitch(parent, (SwitchTree) node);
    else if (node instanceof DebuggerTree) visitDebugger(parent, (DebuggerTree) node);
    else if (node instanceof TryTree) visitTry(parent, (TryTree) node);
    //end StatementTree

    else {
      throw new ProcessingException("Unknown tree node {}", node);
    }
  }
}
