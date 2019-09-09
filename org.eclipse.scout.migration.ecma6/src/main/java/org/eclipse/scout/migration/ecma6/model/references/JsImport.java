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
package org.eclipse.scout.migration.ecma6.model.references;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.task.T40030_IndexJs;

public class JsImport implements IImport {

  private final String m_moduleName;
  private final Path m_pathToIndex;
  private AliasedMember m_defaultMember;
  private List<AliasedMember> m_members = new ArrayList<>();

  public JsImport(String moduleName) {
    this(moduleName, null);
  }

  public JsImport(String moduleName, Path pathToIndex) {
    m_moduleName = moduleName;
    m_pathToIndex = pathToIndex;
  }

  public JsImport withMember(AliasedMember member) {
    m_members.add(member);
    return this;
  }

  @Override
  public void addMember(AliasedMember member) {
    withMember(member);
  }

  public List<AliasedMember> getMembers() {
    return Collections.unmodifiableList(m_members);
  }

  public JsImport withDefaultMember(AliasedMember defaultMember) {
    m_defaultMember = defaultMember;
    return this;
  }

  @Override
  public AliasedMember getDefaultMember() {
    return m_defaultMember;
  }

  @Override
  public AliasedMember findAliasedMember(String member) {
    if (m_defaultMember != null && m_defaultMember.getName().equals(member)) {
      return m_defaultMember;
    }
    return m_members.stream().filter(am -> am.getName().equals(member))
        .findFirst().orElse(null);
  }

  public void setDefaultMember(AliasedMember defaultMember) {
    withDefaultMember(defaultMember);
  }

  public String getModuleName() {
    return m_moduleName;
  }

  @Override
  public String toSource(Context context) {
    StringBuilder sourceBuilder = new StringBuilder();
    sourceBuilder.append("import");
    if (m_defaultMember != null) {
      sourceBuilder.append(" ").append(getDefaultMember().getName());
      if (getDefaultMember().getAlias() != null) {
        sourceBuilder.append(" as ").append(getDefaultMember().getAlias());
      }
    }
    if (m_members.size() > 0) {
      sourceBuilder.append(" {");
      sourceBuilder.append(m_members.stream().map(m -> {
        StringBuilder b = new StringBuilder();
        b.append(m.getName());
        if (m.getAlias() != null) {
          b.append(" as ").append(m.getAlias());
        }
        return b.toString();
      }).collect(Collectors.joining(", ")));
      sourceBuilder.append("}");
    }
    sourceBuilder.append(" from")
        .append(" '");
    if (m_pathToIndex != null) {
      String relPathToIndex = T40030_IndexJs.nameWithoutJsExtension(m_pathToIndex.toString().replace('\\', '/'));
      sourceBuilder.append(ensureIsRelativePath(relPathToIndex));
    }
    else {
      sourceBuilder.append(getModuleName());
    }
    sourceBuilder.append("';");
    return sourceBuilder.toString();
  }

  public static String computeRelativePath(Path targetFile, Path modulePath) {
    if (!Files.isDirectory(targetFile)) {
      targetFile = targetFile.getParent();
    }
    Path relPath = FileUtility.removeFileExtensionJs(targetFile.relativize(modulePath));
    return ensureIsRelativePath(relPath.toString().replace('\\', '/'));
  }

  private static String ensureIsRelativePath(String p) {
    if (p.startsWith("../")) {
      return p;
    }
    return "./" + p;
  }
}
