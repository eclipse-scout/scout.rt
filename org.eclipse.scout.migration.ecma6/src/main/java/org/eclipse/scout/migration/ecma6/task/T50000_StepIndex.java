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
package org.eclipse.scout.migration.ecma6.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class T50000_StepIndex extends AbstractTask {

  public static final Pattern SHARED_RESOURCE_PATTERN = Pattern.compile("^@(.*?)\\{(.+)}$");
  public static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("^(.*)(\\.[^.]+)$");
  private static final Logger LOG = LoggerFactory.getLogger(T50000_StepIndex.class);

  private WorkingCopy m_current;

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return PathFilters.inSrcMainJs().test(pathInfo) && pathInfo.getPath().getFileName().toString().endsWith(".index");
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.getWorkingCopy(pathInfo.getPath());
    if (pathInfo.getPath().toString().replace('\\', '/').contains("/_shared/")) {
      // shared index file. this is no longer required
      workingCopy.setDeleted(true);
      return;
    }

    m_current = workingCopy;
    P_Index result = new P_Index();
    parse(workingCopy.getSource(), result, context);
    migrate(result);
    write(result);
    m_current = null;
  }

  protected void migrate(P_Index index) {
    // to not touch STYLE, TEXT, RUN, DATA

    P_IndexType entryPointScript = Assertions.assertNotNull(getEntryPointScript(index), "Cannot find a matching entry-point script for index '{}'.", m_current.getPath());
    Iterator<P_IndexType> iterator = index.m_entries.iterator();
    while (iterator.hasNext()) {
      P_IndexType line = iterator.next();
      if ("INDEX".equals(line.m_name)) {
        iterator.remove(); // references to shared resources are imported in js imports
      }
      else if ("MODEL".equals(line.m_name)) {
        iterator.remove(); // references to model are imported in js
      }
      else if ("SCRIPT".equals(line.m_name) && line != entryPointScript) {
        iterator.remove(); // remove all secondary script references as these are imported now. only keep the run entry point
      }
    }
  }

  protected P_IndexType getEntryPointScript(P_Index index) {
    String run = index.m_run;
    int lastDot = run.lastIndexOf('.');
    if (lastDot > 0) {
      run = run.substring(lastDot + 1);
    }
    final String runClass = run;
    return index.m_entries.stream()
        .filter(type -> "SCRIPT".equals(type.m_name))
        .filter(type -> type.m_entry.m_localPath.endsWith('/' + runClass + ".js"))
        .findAny()
        .orElse(null);
  }

  protected void write(P_Index index) {
    StringBuilder builder = new StringBuilder();
    String nl = m_current.getLineDelimiter();
    index.m_entries.forEach(line -> writeEntry(line, builder, nl));
    m_current.setSource(builder.toString());
  }

  protected void writeEntry(P_IndexType line, StringBuilder builder, String nl) {
    if (!"COMMENT".equals(line.m_name)) {
      builder.append(line.m_name).append(':');
      for (int i = 1; i < 8 - line.m_name.length(); i++) {
        builder.append(' ');
      }
    }
    builder.append(line.m_entry.m_raw).append(nl);
  }

  protected void parse(String content, P_Index result, Context context) {
    try (BufferedReader reader = new BufferedReader(new StringReader(content))) {

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
          P_IndexType comment = new P_IndexType("COMMENT");
          comment.m_entry = new P_FileEntry(line);
          result.m_entries.add(comment);
          continue;
        }
        String[] a = line.split(":", 2);
        if (a.length != 2) {
          LOG.warn("Unparsable line (skipped): {}", line);
          continue;
        }

        String rawType = a[0].trim();
        String value = a[1].trim();
        if (!ObjectUtility.isOneOf(rawType, "INDEX", "SCRIPT", "STYLE", "TEXT", "MODEL", "RUN")) {
          LOG.warn("Unknown resource type '{}' (skipped): {}", rawType, line);
          continue;
        }

        if ("INDEX".equals(rawType)) {
          // Include data from another index file
          for (String file : value.split(">")) {
            Path refIndex = Configuration.get().getSourceModuleDirectory().resolve("src/main/js").resolve(new P_FileEntry(file).m_localPath);
            String nestedSrc = context.ensureWorkingCopy(refIndex).getSource();
            parse(nestedSrc, result, context);
          }
        }
        else {
          P_IndexType t = new P_IndexType(rawType);
          fillFromLine(t, result, value);
          result.m_entries.add(t);
        }
      }
    }
    catch (IOException e) {
      throw new ProcessingException("", e);
    }
  }

  protected void fillFromLine(P_IndexType type, P_Index result, String value) {
    if (type.m_name.equals("RUN")) {
      Assertions.assertNull(result.m_run);
      result.m_run = value;
      type.m_entry = new P_FileEntry(value);
    }
    else if (type.m_name.equals("TEXT")) {
      type.m_entry = new P_FileEntry(value);
    }
    else {
      String[] b = value.split(">");
      type.m_entry = new P_FileEntry(b[b.length - 1]); // only keep the last one. the former ones must be imported by the JS file
    }
  }

  private class P_Index {
    private String m_run;
    private final List<P_IndexType> m_entries = new ArrayList<>();
  }

  private class P_IndexType {
    private final String m_name;
    private P_FileEntry m_entry;

    private P_IndexType(String name) {
      m_name = name;
    }
  }

  private class P_FileEntry {
    private String m_file;
    private final String m_raw;
    private final String m_localPath;

    private P_FileEntry(String raw) {
      m_raw = raw.trim();
      String sharedNamespace;
      Matcher sharedResourceMatcher = SHARED_RESOURCE_PATTERN.matcher(m_raw);
      if (sharedResourceMatcher.matches()) {
        sharedNamespace = sharedResourceMatcher.group(1);
        if (!StringUtility.hasText(sharedNamespace)) {
          sharedNamespace = "studio"; // default namespace
        }
        m_file = sharedResourceMatcher.group(2);
      }
      else if (m_current.getPath().toString().replace('\\', '/').contains("/_shared/")) {
        // Index is a shared resource -> assume everything is a shared resource
        sharedNamespace = Configuration.get().getNamespace();
        m_file = m_raw;
      }
      else {
        m_file = m_raw;
        sharedNamespace = null;
      }
      m_file = m_file.replaceAll("^/", "");

      String fileBase = m_file;
      String fileExt = "";
      Matcher fileExtMatcher = FILE_EXTENSION_PATTERN.matcher(m_file);
      if (fileExtMatcher.matches()) {
        fileBase = fileExtMatcher.group(1);
        fileExt = fileExtMatcher.group(2);
      }

      if (sharedNamespace != null) {
        m_localPath = sharedNamespace + "/" + Configuration.get().getStepConfigTypeName() + "/_shared/" + fileBase + fileExt;
      }
      else {
        Path basePath = Configuration.get().getSourceModuleDirectory().resolve("src/main/js").relativize(m_current.getPath()).getParent();
        m_localPath = basePath.toString().replace('\\', '/') + "/" + fileBase + fileExt;
      }
    }
  }
}
