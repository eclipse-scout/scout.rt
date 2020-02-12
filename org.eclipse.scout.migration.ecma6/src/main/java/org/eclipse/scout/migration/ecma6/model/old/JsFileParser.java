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
package org.eclipse.scout.migration.ecma6.model.old;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.task.T1100_ObjectFactories;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsFileParser {

  private static final Logger LOG = LoggerFactory.getLogger(JsFileParser.class);

  private static Pattern START_COPY_RIGHT = Pattern.compile("^/\\*{1}$");
  private static Pattern END_COPY_RIGHT = Pattern.compile("^ \\*{1}/$");

  private static Pattern START_FUNCTION_COMMENT = Pattern.compile("^/\\*\\*$");
  private static Pattern FUNCTION_COMMENT = Pattern.compile("^ \\*");
  private static Pattern END_FUNCTION_COMMENT = Pattern.compile("^ \\*(\\s*\\*)?/");

  /**
   * <pre>
    * scout.Device = function(model) {
    * Groups:
    *  1 indent (empty)
   *  2 namespace = scope.Device
   *  3 field name (empty)
    *  4 params
    *  5 open bracket
    *  6 closing bracket (opt)
   * </pre>
   */
  private static Pattern START_CONSTRUCTOR = Pattern.compile("^()([^ .]+\\.[^ .]+)()\\s*=\\s*function\\(([^)]*)\\)\\s*(\\{)\\s*(\\}\\;)?");

  /**
   * <pre>
   * scout.KeyStroke.Mode = {
   * Groups:
   *  1 indent
   *  2 namespace = scope.class
   *  3 field name
   *  4 open bracket
   *  5 closing bracket (opt)
   * </pre>
   */
  private static Pattern START_CLASS_ENUM = Pattern.compile("^(\\s*)([^ .]+\\.[A-Z][^ .]+)\\.([A-Z][^ .]+)\\s*=\\s*(\\{)\\s*(\\}\\;)?");

  /**
   * <pre>
   * scout.TreeVisitResult = {
   * Groups:
   *  1 indent (empty)
   *  2 namespace = scope
   *  3 field name
   *  4 open bracket
   *  5 closing bracket (opt)
   * </pre>
   */
  private static Pattern START_TOP_LEVEL_ENUM1 = Pattern.compile("^()([^ .]+)\\.([^ .]+)\\s*=\\s*(\\{)\\s*(\\}\\;)?");

  /**
   * <pre>
   * scout.numbers.RoundingMode = {
   * Groups:
   *  1 indent (empty)
   *  2 namespace = scope
   *  3 field name
   *  4 open bracket
   *  5 closing bracket (opt)
   * </pre>
   */
  private static Pattern START_TOP_LEVEL_ENUM2 = Pattern.compile("^()([^ .]+\\.[^ .]+)\\.([^ .]+)\\s*=\\s*(\\{)\\s*(\\}\\;)?");

  /**
   * <pre>
    * Groups:
    *  1 indent (empty)
   *  2 namespace = scope.class
    *  3 name
    *  4 params
    *  5 open bracket
    *  6 closing bracket (opt)
   * </pre>
   */
  private static Pattern START_FUNCTION = Pattern.compile("^()([^ .]+\\.[^ .]+)\\.prototype\\.([^ .]+)\\s*=\\s*function\\(([^)]*)\\)\\s*(\\{)\\s*(\\}\\;)?");

  /**
   * <pre>
    * Groups:
    *  1 indent (empty)
   *  2 namespace = scope.class
   *  3 field name
    *  4 params
    *  5 open bracket
    *  6 closing bracket (opt)
   * </pre>
   */
  private static Pattern START_STATIC_FUNCTION = Pattern.compile("^()([^ .]+\\.[^ .]+)\\.([^ .]+)\\s*=\\s*function\\(([^)]*)\\)\\s*(\\{)\\s*(\\}\\;)?");

  /**
   * <pre>
   *    scout.Status._warnDeprecationLogged = false;
   * Groups:
   *  1 namespace
   *  2 field name
   *  3 allocation
   * </pre>
   */
  private static Pattern START_CLASS_VARIABLE = Pattern.compile("^([^ .]+\\.[^ .]+)\\.([_$a-z][^ .]+)\\s*=\\s*(?!\\s)(?!(?:function))(.*);?$"); //FULL MATCH

  /**
   * <pre>
   * Groups:
   *  1 namespace = scope.class
   *  2 field name
   *  3 allocation (opt)
   *  4 delimiter semicolumn (opt)
   * </pre>
   */
  private static Pattern START_CLASS_CONSTANT_MULTI_LINE = Pattern.compile("(?s)\\n([^. \\n]+\\.[^ .]+)\\.([A-Z0-9_]+)(\\s*=\\s*.*?)?(;)", Pattern.DOTALL);

  /**
   * <pre>
   * scout.strings = {
   * Groups:
   *  1 namespace = 'scout' or empty for the utility scout.js
   *  2 name 'strings' or 'scout' for the utility scout.js
   * </pre>
   */
  private static Pattern START_UTILITY_BLOCK = Pattern.compile("^(?:([a-z][^ .]+)\\.)?([a-z][^ .]+)\\s*=\\s*\\{");

  /**
   * <pre>
   * SPACE SPACE insertAt: function(text, insertText, position) {
   * Groups:
   *  1 name
   * </pre>
   */
  private static Pattern START_UTILITY_FUNCTION = Pattern.compile("^  ([_$a-z][^ .]+)\\s*:\\s*function");

  /**
   * <pre>
   *    sessions : [],
   *    appListeners : [],
   *    $activeElements : null,
   * Groups:
   *  1 name
   *  2 value or first line
   * </pre>
   */
  private static Pattern START_UTILITY_VARIABLE = Pattern.compile("^  ([_$a-z][^ .]+)\\s*:\\s*(?!\\s)(?!(?:function))(.*)$"); //FULL MATCH

  private static Pattern START_UTILITY_CONST = Pattern.compile("^  ([_$A-Z][^ .]+)\\s*:\\s*(?!\\s)(?!(?:function))(.*)$"); //FULL MATCH

  private static Pattern END_UTILITY_BLOCK = Pattern.compile("^\\}");

  /**
   * <pre>
   * scout.addAppListener = function(type, func) {
   * Groups:
   *  1 namespace
   *  2 name
   * </pre>
   */
  private static Pattern START_UTILITY_FUNCTION_STANDALONE = Pattern.compile("^([a-z][^ .]+)\\.([_$a-z][^ .]+)\\s*=\\s*function");

  /**
   * <pre>
   * scout.sessions = [];
   * Groups:
   *  1 namespace
   *  2 name
   * </pre>
   */
  private static Pattern START_UTILITY_VARIABLE_STANDALONE = Pattern.compile("^([a-z][^ .]+)\\.([_$a-z][^ .]+)\\s*=\\s*(?!\\s)(?!(?:function))");

  private static Pattern START_UTILITY_CONST_STANDALONE = Pattern.compile("^([A-Z][^ .]+)\\.([_$A-Z][^ .]+)\\s*=\\s*(?!\\s)(?!(?:function))");

  /**
   * <pre>
   * Groups:
   *  none
   * </pre>
   */
  private static Pattern START_APP_LISTENER = Pattern.compile("^" + Pattern.quote("scout.addAppListener('") + "(bootstrap|prepare)" + Pattern.quote("', function() {") + "$");

  private static Pattern END_APP_LISTENER = Pattern.compile("\\}\\)\\;");

  /**
   * <pre>
    * Groups:
    * 1 indent
    * 2 closing bracket
    * 3 termination character ';' or ','
   * </pre>
   */
  private static Pattern END_BLOCK = Pattern.compile("^(\\s*)(\\}\\s*)([;,])");

  private static Pattern SUPER_BLOCK = Pattern.compile("scout\\.inherits\\(([^,]+),\\s*([^,]+)\\);");

  private WorkingCopy m_workingCopy;
  private final JsFile m_jsFile;
  private LineReaderWithPositionTracking m_sourceReader;
  private String m_currentLine;
  private int m_currentLineNumber = 0;
  private final String m_lineSeparator;
  @FrameworkExtensionMarker
  private JsUtility m_curUtility;
  private boolean m_logNoContent = true;

  public JsFileParser(WorkingCopy workingCopy, boolean logNoContent) {
    m_workingCopy = workingCopy;
    m_logNoContent = logNoContent;
    String source = workingCopy.getInitialSource();
    m_lineSeparator = workingCopy.getLineDelimiter();
    m_sourceReader = new LineReaderWithPositionTracking(new StringReader(source));
    m_jsFile = new JsFile(workingCopy);
  }

  @FrameworkExtensionMarker
  public JsFile parse() throws IOException {
    if (T1100_ObjectFactories.isObjectFactories(m_jsFile.getPath(), m_workingCopy)) {
      //  do not parse objectFactories.js it is a special file and handled in T1100_ObjectFactories
      return m_jsFile;
    }
    if (PathFilters.inSrcTestJs().test(m_jsFile.getPathInfo())) {
      // do not parse specs. these files cannot be handled by this parser anyway.
      return m_jsFile;
    }

    JsFunction instanceGetter = null;
    Matcher matcher = null;
    try {
      nextLine();
      JsCommentBlock comment = null;
      while (m_currentLine != null) {

        if (m_currentLineNumber == 1) {
          matcher = START_COPY_RIGHT.matcher(m_currentLine);
          if (matcher.find()) {
            readCopyRight();
            continue;
          }
        }
        matcher = START_FUNCTION_COMMENT.matcher(m_currentLine);
        if (matcher.find()) {
          comment = readFunctionComment();
          continue;
        }
        matcher = START_CONSTRUCTOR.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isClass().test(m_jsFile.getPathInfo())) {
          readFunction(matcher, comment, true, false);
          comment = null;
          continue;
        }
        matcher = START_CLASS_VARIABLE.matcher(m_currentLine);
        if (matcher.matches() && PathFilters.isClass().test(m_jsFile.getPathInfo())) {//uses matcher.MATCHES() instead of find()
          readClassVariable(matcher);
          comment = null;
          continue;
        }
        matcher = START_CLASS_ENUM.matcher(m_currentLine);
        if (matcher.find()) {
          readEnum(matcher);
          continue;
        }
        matcher = START_APP_LISTENER.matcher(m_currentLine);
        if (matcher.find()) {
          instanceGetter = readAppListener();
          continue;
        }
        matcher = START_TOP_LEVEL_ENUM1.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isTopLevelEnum(m_jsFile.getPath())) {
          readTopLevelEnum(matcher);
          continue;
        }
        else {
          //detect top level enums in other files
          matcher = START_TOP_LEVEL_ENUM1.matcher(m_currentLine);
          if (matcher.find() && Character.isUpperCase(matcher.group(3).charAt(0))) {
            LOG.warn("Potential top-level enum found. Every top-level enum should be defined in its own file and added to PathFilters.isTopLevelEnum(). {}:{} {}", m_jsFile.getPath(), m_currentLineNumber, matcher.group());
          }
        }
        matcher = START_TOP_LEVEL_ENUM2.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isTopLevelEnum(m_jsFile.getPath())) {
          readTopLevelEnum(matcher);
          continue;
        }
        else {
          //detect top level enums in other files
          matcher = START_TOP_LEVEL_ENUM2.matcher(m_currentLine);
          if (matcher.find() && Character.isUpperCase(matcher.group(3).charAt(0))) {
            LOG.warn("Potential top-level enum found. Every top-level enum should be defined in its own file and added to PathFilters.isTopLevelEnum(). {}:{} {}", m_jsFile.getPath(), m_currentLineNumber, matcher.group());
          }
        }
        matcher = START_UTILITY_BLOCK.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isUtility(m_jsFile.getPath())) {
          beginUtility(matcher);
          comment = null;
          continue;
        }
        matcher = START_UTILITY_FUNCTION.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isUtility(m_jsFile.getPath())) {
          readUtilityFunction(matcher);
          comment = null;
          continue;
        }
        matcher = START_UTILITY_VARIABLE.matcher(m_currentLine);
        if (matcher.matches() && PathFilters.isUtility(m_jsFile.getPath())) {//uses matcher.MATCHES() instead of find()
          readUtilityVariable(matcher, false);
          comment = null;
          continue;
        }
        matcher = START_UTILITY_CONST.matcher(m_currentLine);
        if (matcher.matches() && PathFilters.isUtility(m_jsFile.getPath())) {//uses matcher.MATCHES() instead of find()
          readUtilityVariable(matcher, true);
          comment = null;
          continue;
        }
        matcher = END_UTILITY_BLOCK.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isUtility(m_jsFile.getPath()) && m_curUtility != null) {
          endUtility(matcher);
          comment = null;
          continue;
        }
        matcher = START_UTILITY_FUNCTION_STANDALONE.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isUtility(m_jsFile.getPath())) {
          readUtilityFunctionStandalone(matcher);
          comment = null;
          continue;
        }
        matcher = START_UTILITY_VARIABLE_STANDALONE.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isUtility(m_jsFile.getPath())) {
          readUtilityVariableStandalone(matcher, false);
          comment = null;
          continue;
        }
        matcher = START_UTILITY_CONST_STANDALONE.matcher(m_currentLine);
        if (matcher.find() && PathFilters.isUtility(m_jsFile.getPath())) {
          readUtilityVariableStandalone(matcher, true);
          comment = null;
          continue;
        }
        matcher = START_FUNCTION.matcher(m_currentLine);
        if (matcher.find()) {
          readFunction(matcher, comment, false, false);
          comment = null;
          continue;
        }
        matcher = START_STATIC_FUNCTION.matcher(m_currentLine);
        if (matcher.find()) {
          readFunction(matcher, comment, false, true);
          comment = null;
          continue;
        }
        matcher = SUPER_BLOCK.matcher(m_currentLine);
        if (matcher.find()) {
          JsClass clazz = m_jsFile.getLastClassOrAppend(matcher.group(1));
          clazz.setSuperCall(readSuperCall(matcher));
          continue;
        }
        nextLine();
      }
    }
    catch (VetoException e) {
      LOG.error("Could not parse file '" + m_jsFile.getPath() + ":" + m_currentLineNumber + "'.", e);
      throw e;
    }

    Matcher constMatcher = START_CLASS_CONSTANT_MULTI_LINE.matcher(m_workingCopy.getSource());
    while (constMatcher.find()) {
      readClassConstant(constMatcher);
    }

    List<JsClass> jsClasses = m_jsFile.getJsClasses();
    if (jsClasses.size() == 1) {
      jsClasses.get(0).setDefault(true);
    }
    @FrameworkExtensionMarker
    List<JsUtility> jsUtilities = m_jsFile.getJsUtilities();
    List<JsTopLevelEnum> jsTopLevelEnums = m_jsFile.getJsTopLevelEnums();
    if (instanceGetter != null) {
      if (jsClasses.size() == 1) {
        instanceGetter.setJsClass(jsClasses.get(0));
        jsClasses.get(0).addFunction(instanceGetter);
      }
      else {
        LOG.error("Could not create instance getter of singleton '" + m_jsFile.getPath() + "'");
      }
    }
    // log
    String fileName = m_jsFile.getPath().getFileName().toString();
    if (jsClasses.isEmpty() && jsTopLevelEnums.isEmpty() && jsUtilities.isEmpty() && fileName.endsWith(".js") && !fileName.endsWith("-module.js")) {
      if (isLogNoContent()) {
        LOG.error("No content found in file '" + m_jsFile.getPath() + "'.");
      }
    }
    else if (jsClasses.size() > 1) {
      LOG.warn("More than 1 class found in file '" + m_jsFile.getPath() + "'. Every classfile should be defined in its own file.");
    }
    return m_jsFile;
  }

  public void setLogNoContent(boolean logNoContent) {
    m_logNoContent = logNoContent;
  }

  public boolean isLogNoContent() {
    return m_logNoContent;
  }

  private void readCopyRight() throws IOException {
    JsCommentBlock comment = new JsCommentBlock();
    StringBuilder commentBody = new StringBuilder(m_currentLine).append(m_lineSeparator);
    nextLine();

    while (m_currentLine != null) {
      if (END_COPY_RIGHT.matcher(m_currentLine).find()) {
        commentBody.append(m_currentLine).append(m_lineSeparator);
        break;
      }
      else if (FUNCTION_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(m_currentLine).append(m_lineSeparator);
      }
      else {
        // no comment
        throw new VetoException("Function commentblock could not be parsed (" + m_workingCopy.getPath() + ":" + m_currentLineNumber + ") [line: '" + m_currentLine + "']! ");
      }
      nextLine();
    }
    comment.setSource(commentBody.toString());
    m_jsFile.setCopyRight(comment);
    nextLine();
  }

  private JsCommentBlock readFunctionComment() throws IOException {
    JsCommentBlock comment = new JsCommentBlock();
    StringBuilder commentBody = new StringBuilder(m_currentLine).append(m_lineSeparator);
    nextLine();

    while (m_currentLine != null) {
      if (END_FUNCTION_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(m_currentLine).append(m_lineSeparator);
        break;
      }
      else if (FUNCTION_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(m_currentLine).append(m_lineSeparator);
      }
      else {
        // no comment
        throw new VetoException("Function commentblock could not be parsed (" + m_workingCopy.getPath() + ":" + m_currentLineNumber + ") [line: '" + m_currentLine + "']! ");
      }
      nextLine();
    }
    comment.setSource(commentBody.toString());
    nextLine();
    return comment;
  }

  private JsFunction readFunction(Matcher matcher, JsCommentBlock comment, boolean constructor, boolean isStatic) throws IOException {
    String indent = matcher.group(1);
    JsClass clazz = m_jsFile.getLastClassOrAppend(matcher.group(2));
    JsFunction function = new JsFunction(clazz, matcher.group(3));
    function.setComment(comment);
    function.setConstructor(constructor);
    function.setStatic(isStatic);
    function.setArgs(matcher.group(4));
    StringBuilder functionBody = new StringBuilder(m_currentLine).append(m_lineSeparator);
    if (StringUtility.hasText(matcher.group(6))) {
      function.setSource(functionBody.toString());
      clazz.addFunction(function);
      nextLine();
      return function;
    }
    nextLine();
    while (m_currentLine != null) {
      Matcher endMatcher = END_BLOCK.matcher(m_currentLine);
      if (endMatcher.matches() && endMatcher.group(1).equals(indent)) {
        functionBody.append(endMatcher.group(1) + endMatcher.group(2));
        break;
      }

      if (StringUtility.hasText(m_currentLine) && !m_currentLine.startsWith(" ")) {
        throw new VetoException("Could not parse function body (" + m_workingCopy.getPath() + ":" + m_currentLineNumber + ")");
      }
      functionBody.append(m_currentLine).append(m_lineSeparator);
      nextLine();
    }
    function.setSource(functionBody.toString());

    clazz.addFunction(function);
    return function;
  }

  @FrameworkExtensionMarker
  private JsUtility beginUtility(Matcher matcher) throws IOException {
    String namespace = matcher.group(1);
    String name = matcher.group(2);
    Assertions.assertNull(m_curUtility);
    m_curUtility = new JsUtility(m_jsFile, namespace, name, matcher.group());
    m_jsFile.addJsUtility(m_curUtility);
    nextLine();
    return m_curUtility;
  }

  @FrameworkExtensionMarker
  private JsUtilityFunction readUtilityFunction(Matcher matcher) throws IOException {
    String name = matcher.group(1);
    if (m_curUtility == null) {
      LOG.warn("wrong utility-style function detected. {}:{} {}", m_workingCopy.getPath(), m_currentLineNumber, matcher.group());
      nextLine();
      return null;
    }
    JsUtilityFunction f = m_curUtility.addFunction(name, !name.startsWith("_"), false, matcher.group());
    nextLine();
    return f;
  }

  @FrameworkExtensionMarker
  private JsUtilityFunction readUtilityFunctionStandalone(Matcher matcher) throws IOException {
    String namespace = matcher.group(1);
    String name = matcher.group(2);
    JsUtility util = m_jsFile.getJsUtilities().stream().filter(u -> u.getFullyQualifiedName().equals(namespace)).findFirst().orElse(null);
    if (util == null) {
      util = new JsUtility(m_jsFile, null, namespace, null);
      m_jsFile.addJsUtility(util);
    }
    JsUtilityFunction f = util.addFunction(name, !name.startsWith("_"), true, matcher.group());
    nextLine();
    return f;
  }

  @FrameworkExtensionMarker
  private JsUtilityVariable readUtilityVariable(Matcher matcher, boolean isConst) throws IOException {
    String name = matcher.group(1);
    String value = matcher.group(2);
    if (value.endsWith(",")) {
      value = value.substring(0, value.length() - 1);
    }
    if (m_curUtility == null) {
      LOG.warn("wrong utility-style variable detected. {}:{} {}", m_workingCopy.getPath(), m_currentLineNumber, matcher.group());
      nextLine();
      return null;
    }
    JsUtilityVariable v = m_curUtility.addVariable(name, value, !name.startsWith("_"), false, isConst, matcher.group());
    nextLine();
    return v;
  }

  @FrameworkExtensionMarker
  private JsUtilityVariable readUtilityVariableStandalone(Matcher matcher, boolean isConst) throws IOException {
    String namespace = matcher.group(1);
    String name = matcher.group(2);
    JsUtility util = m_jsFile.getJsUtilities().stream().filter(u -> u.getFullyQualifiedName().equals(namespace)).findFirst().orElse(null);
    if (util == null) {
      util = new JsUtility(m_jsFile, null, namespace, null);
      m_jsFile.addJsUtility(util);
    }
    JsUtilityVariable v = util.addVariable(name, null, !name.startsWith("_"), true, isConst, matcher.group());
    nextLine();
    return v;
  }

  @FrameworkExtensionMarker
  private void endUtility(Matcher matcher) throws IOException {
    Assertions.assertNotNull(m_curUtility);
    String source = m_workingCopy.getInitialSource();
    int a = source.indexOf(m_curUtility.getStartTag());
    int b = m_sourceReader.getStartOfLine() + matcher.end();
    m_curUtility.setSource(source.substring(a, b));
    m_curUtility = null;
    nextLine();
  }

  private JsEnum readEnum(Matcher matcher) throws IOException {
    String indent = matcher.group(1);
    JsClass clazz = m_jsFile.getLastClassOrAppend(matcher.group(2));
    JsEnum jsEnum = new JsEnum(clazz, matcher.group(3));
    StringBuilder bodyBuilder = new StringBuilder(matcher.group(4));
    if (StringUtility.hasText(matcher.group(5))) {
      // take care dynamic values can not be implemented as cons
      LOG.warn("Dynamic enum '" + jsEnum.getName() + "' found in " + m_workingCopy.getPath() + ":" + m_currentLineNumber);
      jsEnum.addParseError("Looks like a dynamic jsEnum. Must be migrated by hand or added to T70010_ManualFixes.");
      clazz.addEnum(jsEnum);
      nextLine();
      return jsEnum;
    }
    nextLine();
    while (m_currentLine != null) {
      Matcher endMatcher = END_BLOCK.matcher(m_currentLine);
      if (endMatcher.matches() && endMatcher.group(1).equals(indent)) {
        bodyBuilder.append("}");
        break;
      }
      bodyBuilder.append(m_currentLine);
      if (StringUtility.hasText(m_currentLine) && !m_currentLine.startsWith(" ")) {
        throw new VetoException("Could not parse enum body (" + m_workingCopy.getPath() + ":" + m_currentLineNumber + ")");
      }
      nextLine();
    }
    jsEnum.setSource(bodyBuilder.toString());

    clazz.addEnum(jsEnum);
    return jsEnum;
  }

  private JsTopLevelEnum readTopLevelEnum(Matcher matcher) throws IOException {
    String indent = "";
    JsTopLevelEnum jsEnum = new JsTopLevelEnum(matcher.group(2), matcher.group(3), m_jsFile);
    StringBuilder bodyBuilder = new StringBuilder(matcher.group(4));
    if (StringUtility.hasText(matcher.group(5))) {
      // take care dynamic values can not be implemented as cons
      LOG.warn("Dynamic enum '" + jsEnum.getName() + "' found in " + m_workingCopy.getPath() + ":" + m_currentLineNumber);
      jsEnum.addParseError("Looks like a dynamic jsEnum. Must be migrated by hand or added to T70010_ManualFixes.");
      m_jsFile.addJsTopLevelEnum(jsEnum);
      nextLine();
      return jsEnum;
    }
    nextLine();
    while (m_currentLine != null) {
      Matcher endMatcher = END_BLOCK.matcher(m_currentLine);
      if (endMatcher.matches() && endMatcher.group(1).equals(indent)) {
        bodyBuilder.append(endMatcher.group(1) + endMatcher.group(2));
        break;
      }
      bodyBuilder.append(m_currentLine).append(m_lineSeparator);
      if (StringUtility.hasText(m_currentLine) && !m_currentLine.startsWith(" ")) {
        throw new VetoException("Could not parse enum body (" + m_workingCopy.getPath() + ":" + m_currentLineNumber + ")");
      }
      nextLine();
    }
    jsEnum.setSource(bodyBuilder.toString());
    m_jsFile.addJsTopLevelEnum(jsEnum);
    return jsEnum;
  }

  private JsClassVariable readClassVariable(Matcher matcher) throws IOException {
    JsClass clazz = m_jsFile.getLastClassOrAppend(matcher.group(1));
    JsClassVariable v = new JsClassVariable(clazz, matcher.group(2), false);
    v.setSource(matcher.group());
    clazz.addVariable(v);
    nextLine();
    return v;
  }

  private JsClassVariable readClassConstant(Matcher matcher) throws IOException {
    JsClass clazz = m_jsFile.getClassOrAppend(matcher.group(1));
    JsClassVariable v = new JsClassVariable(clazz, matcher.group(2), true);
    v.setSource(matcher.group());
    clazz.addVariable(v);
    //no call to nextLine(), this method is called outside line loop
    return v;
  }

  private JsFunction readAppListener() throws IOException {
    JsAppListener appListener = new JsAppListener(m_jsFile);
    StringBuilder functionBody = new StringBuilder(m_currentLine).append(m_lineSeparator);
    Pattern namePattern = Pattern.compile("(" + Configuration.get().getNamespace() + "\\.[^ \\=]*)\\s*\\=\\s*scout\\.create\\(");

    nextLine();
    while (m_currentLine != null) {
      Matcher endMatcher = END_APP_LISTENER.matcher(m_currentLine);
      if (endMatcher.matches()) {
        functionBody.append(m_currentLine);
        break;
      }
      Matcher nameMatcher = namePattern.matcher(m_currentLine);
      if (nameMatcher.find()) {
        if (appListener.getInstanceName() != null) {
          LOG.error("Found more than one assignments in appListener of '" + m_jsFile.getPath() + "'.");
          appListener.addParseError("Found more than one assignments in appListener - hand migration required!");
        }
        appListener.setInstanceFqn(nameMatcher.group(1));
      }
      functionBody.append(m_currentLine).append(m_lineSeparator);
      if (StringUtility.hasText(m_currentLine) && !m_currentLine.startsWith(" ")) {
        throw new VetoException("Could not parse appListener body (" + m_workingCopy.getPath() + ":" + m_currentLineNumber + ")");
      }
      nextLine();
    }
    // skip when no assignment has been found.
    if (appListener.getInstanceName() == null) {
      return null;
    }
    appListener.setSource(functionBody.toString());

    m_jsFile.addAppListener(appListener);

    // create instance getter function
    StringBuilder instanceGetterBuilder = new StringBuilder();
    instanceGetterBuilder.append("static get() {").append(m_lineSeparator)
        .append("  return instance;").append(m_lineSeparator)
        .append("}").append(m_lineSeparator);

    JsFunction instanceGetter = new JsFunction(null, "get");
    instanceGetter.setMemoryOnly(true);
    instanceGetter.setStatic(true);
    instanceGetter.addSingletonReference(appListener.getInstanceNamespace() + "." + appListener.getInstanceName());
    instanceGetter.setSource(instanceGetterBuilder.toString());

    return instanceGetter;

  }

  private JsSuperCall readSuperCall(Matcher matcher) throws IOException {
    JsSuperCall superCall = new JsSuperCall(matcher.group(2));
    superCall.setSource(matcher.group());
    nextLine();
    return superCall;
  }

  private void nextLine() throws IOException {
    m_currentLine = m_sourceReader.readLine();
    m_currentLineNumber++;
  }

}
