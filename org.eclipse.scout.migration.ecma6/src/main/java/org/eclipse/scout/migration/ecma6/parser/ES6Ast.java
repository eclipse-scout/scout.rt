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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

import jdk.nashorn.api.tree.CompilationUnitTree;
import jdk.nashorn.api.tree.Tree;

public class ES6Ast {
  private final String m_path;
  private String m_source;
  private ES6Node<CompilationUnitTree> m_root;

  public ES6Ast(File f) {
    this(f.getAbsolutePath(), readFile(f));
  }

  public ES6Ast(String path, String source) {
    m_path = path;
    m_source = source;
    parse();
  }

  private static String readFile(File f) {
    try (FileInputStream in = new FileInputStream(f)) {
      return IOUtility.readStringUTF8(in);
    }
    catch (IOException e) {
      throw new ProcessingException("read file '{}'", f, e);
    }
  }

  public ES6Node<CompilationUnitTree> getRoot() {
    return m_root;
  }

  public String getPath() {
    return m_path;
  }

  public String getSource() {
    return m_source;
  }

  public void setSourceAndParse(String source) {
    m_source = source;
    parse();
  }

  public void parse() {
    Map<Tree, List<Tree>> impls = null;
    try {
      impls = new ES6Parser().parse(m_path, m_source);
    }
    catch (IOException e) {
      throw new ProcessingException("parse {}", m_path, e);
    }
    Map<Tree, ES6Node<?>> nodes = new IdentityHashMap<>();

    //create wrapped nodes with more api logic
    impls
        .values()
        .stream()
        .flatMap(v -> v.stream())
        .filter(impl -> impl != null)
        .forEach(impl -> nodes.put(impl, new ES6Node<Tree>(this, impl)));

    //set wrapped parent and sorted children
    impls.forEach((parent, children) -> {
      if (parent != null) {
        //sort all child nodes by startPos, endPos
        List<ES6Node<?>> sortedChildren = children
            .stream()
            .map(impl -> nodes.get(impl))
            .sorted(Comparator.<ES6Node<?>> comparingLong(n -> n.getImpl().getStartPosition()).thenComparingLong(n -> n.getImpl().getEndPosition()))
            .collect(Collectors.toList());
        nodes.get(parent).setChildren(sortedChildren);
      }
      children.forEach(child -> nodes.get(child).setParent(nodes.get(parent)));
    });

    Tree rootImpl = impls.get(null).get(0);
    m_root = (ES6Node<CompilationUnitTree>) nodes.get(rootImpl);

    //bug fix: correct start and end positions
    visitDepthFirst(node -> {
      node.adjustPositionsPass1();
    });

    visitDepthFirst(node -> {
      if (node.getChildren().isEmpty()) {
        return;
      }
      //max(child.end)
      int end = Math.max(
          node.getEndPosition(),
          node.getChildren().stream().mapToInt(n -> n.getEndPosition()).max().orElse(0));
      node.setEndPosition(end);
    });

    visitDepthFirst(node -> {
      node.adjustPositionsPass2();
    });

    visitDepthFirst(node -> {
      if (node.getChildren().isEmpty()) {
        return;
      }
      //max(child.end)
      int end = Math.max(
          node.getEndPosition(),
          node.getChildren().stream().mapToInt(n -> n.getEndPosition()).max().orElse(0));
      node.setEndPosition(end);
    });

    //bug fix: parse comments
    Map<Integer, Integer> comments = parseBlockComments();
    //find last node top-down that is closest after the comment and its next child has a larger offset
    comments.forEach((commentStart, commentEnd) -> {
      List<ES6Node<?>> candidates = streamBreathFirst()
          .filter(node -> commentEnd < node.getStartPosition() && node.getStartPosition() < commentEnd + 32)
          .collect(Collectors.toList());
      int closestPos = candidates.stream().mapToInt(n -> n.getStartPosition()).min().orElse(0);
      for (int i = 0; i < candidates.size(); i++) {
        ES6Node<?> node = candidates.get(i);
        if (i >= candidates.size() - 1 || node.getStartPosition() < candidates.get(i + 1).getStartPosition()) {
          node.setCommentStartPosition(commentStart);
          break;
        }
      }
    });
  }

  private static Pattern PAT_COMMENT = Pattern.compile("/\\*((?!(\\*/)).)+\\*/", Pattern.DOTALL);

  private Map<Integer/*comment-start-position*/, Integer/*comment-end-position*/> parseBlockComments() {
    Map<Integer, Integer> map = new TreeMap<>();
    Matcher m = PAT_COMMENT.matcher(m_source);
    while (m.find()) {
      map.put(m.start(), m.end());
    }
    return map;
  }

  /**
   * Visit tree using depth first strategy. First children, then node itself.
   */
  public void visitDepthFirst(Consumer<ES6Node<?>> visitor) {
    m_root.visitDepthFirst(visitor);
  }

  public Stream<ES6Node<?>> streamDepthFirst() {
    ArrayList<ES6Node<?>> list = new ArrayList<>();
    m_root.visitDepthFirst(node -> list.add(node));
    return list.stream();
  }

  /**
   * Visit tree using breath first strategy. First node itself then all children.
   */
  public void visitBreathFirst(Consumer<ES6Node<?>> visitor) {
    m_root.visitBreathFirst(visitor);
  }

  public Stream<ES6Node<?>> streamBreathFirst() {
    ArrayList<ES6Node<?>> list = new ArrayList<>();
    m_root.visitBreathFirst(node -> list.add(node));
    return list.stream();
  }

  public String generateOutput() {
    AtomicInteger size = new AtomicInteger();
    visitDepthFirst(node -> size.set(Math.max(size.get(), node.getEndPosition())));
    StringBuilder buf = new StringBuilder(StringUtility.repeat(" ", size.intValue()));
    visitDepthFirst(node -> {
      if (node == getRoot()) {
        return;
      }
      buf.replace(node.getCommentStartPosition(), node.getEndPosition(), node.getComment() + node.getSource());
    });
    return buf.toString();
  }
}
