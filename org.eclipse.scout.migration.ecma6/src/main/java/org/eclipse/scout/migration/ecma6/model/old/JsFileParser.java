package org.eclipse.scout.migration.ecma6.model.old;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsFileParser {

  private static final Logger LOG = LoggerFactory.getLogger(JsFileParser.class);

  private static Pattern START_COPY_RIGHT = Pattern.compile("^\\/\\*{5,}$");
  private static Pattern END_COPY_RIGHT = Pattern.compile("^\\ \\*{5,}\\/$");
  private static Pattern START_FUNCITON_COMMENT = Pattern.compile("^\\/\\*\\*$");
  private static Pattern FUNCITON_COMMENT = Pattern.compile("^\\ \\*");
  private static Pattern END_FUNCITON_COMMENT = Pattern.compile("^\\ \\*(\\s*\\*)?\\/");
  private static Pattern START_CONSTRUCTOR = Pattern.compile("^([^\\.]+\\.[^\\ \\.]+)()\\s*\\=\\s*function\\(([^\\)]*)\\)\\s*(\\{)\\s*(\\}\\;)?");
  private static Pattern START_FUNCTION = Pattern.compile("^([^\\.]+\\.[^\\.]+)\\.prototype\\.([^\\ ]+)\\ \\=\\s*function\\(([^\\)]*)\\)\\s*(\\{)\\s*(\\}\\;)?");
  private static Pattern START_STATIC_FUNCTION = Pattern.compile("^([^\\.]+\\.[^\\.]+)\\.([^\\ ]+)\\ \\=\\s*function\\(([^\\)]*)\\)\\s*(\\{)\\s*(\\}\\;)?");
  private static Pattern END_BLOCK = Pattern.compile("^\\}\\;");
  private static Pattern SUPER_BLOCK = Pattern.compile("scout\\.inherits\\(([^\\,]+)\\,\\s*([^\\,]+)\\)\\;");
//  scout.Menu.MenuStyle = {
  private static Pattern CONST_START = Pattern.compile("^([^\\.]+\\.[^\\.]+)\\.([^\\ ]+)\\s*\\=\\s*(\\{)\\s*(\\}\\;)?");

  private WorkingCopy m_workingCopy;
  private final JsFile m_jsFile;
  private BufferedReader m_sourceReader;
  private String m_currentLine;
  private int m_currentLineNumber = 0;
  private int m_offsetStartLine = 0;
  private final String m_lineSeparator;

  public JsFileParser(WorkingCopy workingCopy) {
    m_workingCopy = workingCopy;
    String source = workingCopy.getInitialSource();
    m_lineSeparator = workingCopy.getLineSeparator();
    m_sourceReader = new BufferedReader(new StringReader(source));
    m_jsFile = new JsFile(workingCopy.getPath());
  }

  public JsFile parse() throws IOException {
    try {
      nextLine();
      JsCommentBlock comment = null;
      while (m_currentLine != null) {
        Matcher matcher = START_COPY_RIGHT.matcher(m_currentLine);
        if (matcher.find()) {
          readCopyRight();
          continue;
        }
        matcher = START_FUNCITON_COMMENT.matcher(m_currentLine);
        if (matcher.find()) {
          comment = readFunctionComment();
          continue;
        }
        matcher = START_CONSTRUCTOR.matcher(m_currentLine);
        if (matcher.find()) {
          readFunction(matcher, comment, true, false);
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
        matcher = CONST_START.matcher(m_currentLine);
        if(matcher.find()){
          readConst(matcher);
        }
        matcher = SUPER_BLOCK.matcher(m_currentLine);
        if (matcher.find()) {
          JsClass clazz = m_jsFile.getLastOrAppend(matcher.group(1));
          clazz.setSuperCall(readSuperCall(matcher));

          continue;
        }
        nextLine();
      }
    }
    catch (VetoException e) {
      MigrationUtility.prependTodo(m_workingCopy, e.getMessage());
      throw e;
    }

    List<JsClass> jsClasses = m_jsFile.getJsClasses();
    if (jsClasses.size() == 0) {
      LOG.error("No classes found in file '" + m_jsFile.getPath().getFileName() + "'.");
    }
    else if (jsClasses.size() > 1) {
      LOG.warn("More than 1 class found in file '" + m_jsFile.getPath().getFileName() + "'. Every classfile should be defined in its own file.");
    }
    return m_jsFile;
  }



  private void readCopyRight() throws IOException {
    JsCommentBlock comment = new JsCommentBlock();
    comment.setStartOffset(m_offsetStartLine);
    StringBuilder commentBody = new StringBuilder(m_currentLine);
    nextLine();

    while (m_currentLine != null) {
      if (END_COPY_RIGHT.matcher(m_currentLine).find()) {
        commentBody.append(m_workingCopy.getLineSeparator()).append(m_currentLine);
        break;
      }
      else if (FUNCITON_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(m_workingCopy.getLineSeparator()).append(m_currentLine);
      }
      else {
        // no comment
        throw new VetoException("Function commentblock could not be parsed (" + m_workingCopy.getPath().getFileName() + ":" + m_currentLineNumber + ") [line: '" + m_currentLine + "']! ");
      }
      nextLine();
    }
    comment.setSource(commentBody.toString());
    comment.setEndOffset(m_offsetStartLine + m_currentLine.length());
    m_jsFile.setCopyRight(comment);
    nextLine();
  }

  private JsCommentBlock readFunctionComment() throws IOException {
    JsCommentBlock comment = new JsCommentBlock();
    comment.setStartOffset(m_offsetStartLine);
    StringBuilder commentBody = new StringBuilder(m_currentLine);
    nextLine();

    while (m_currentLine != null) {
      if (END_FUNCITON_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(m_workingCopy.getLineSeparator()).append(m_currentLine);
        break;
      }
      else if (FUNCITON_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(m_workingCopy.getLineSeparator()).append(m_currentLine);
      }
      else {
        // no comment
        throw new VetoException("Function commentblock could not be parsed (" + m_workingCopy.getPath().getFileName() + ":" + m_currentLineNumber + ") [line: '" + m_currentLine + "']! ");
      }
      nextLine();
    }
    comment.setSource(commentBody.toString());
    comment.setEndOffset(m_offsetStartLine + m_currentLine.length());
    nextLine();
    return comment;
  }

  private JsFunction readFunction(Matcher matcher, JsCommentBlock comment, boolean constructor, boolean isStatic) throws IOException {
    JsClass clazz = m_jsFile.getLastOrAppend(matcher.group(1));
    JsFunction function = new JsFunction(clazz, matcher.group(2));
    function.setComment(comment);
    function.setStartOffset(m_offsetStartLine);
    function.setConstructor(constructor);
    function.setStatic(isStatic);
    function.setArgs(matcher.group(3));
    StringBuilder functionBody = new StringBuilder(matcher.group(4));
    if (StringUtility.hasText(matcher.group(5))) {
      functionBody.append(matcher.group(5));
      function.setBody(functionBody.toString());
      nextLine();
      clazz.addFunction(function);
      return function;
    }
    nextLine();
    while (m_currentLine != null) {
      functionBody.append(m_currentLine);
      if (END_BLOCK.matcher(m_currentLine).find()) {
        break;
      }
      if (StringUtility.hasText(m_currentLine) && !m_currentLine.startsWith(" ")) {
        throw new VetoException("Could not parse function body (" + m_workingCopy.getPath().getFileName() + ":" + m_currentLineNumber + ")");
      }
      nextLine();
    }
    function.setBody(functionBody.toString());
    function.setEndOffset(m_offsetStartLine + m_currentLine.length());

    clazz.addFunction(function);
    return function;
  }

  protected JsConstant readConst(Matcher matcher) throws IOException {
    JsClass clazz = m_jsFile.getLastOrAppend(matcher.group(1));
    JsConstant constant = new JsConstant(clazz, matcher.group(2));
    constant.setStartOffset(m_offsetStartLine);
    StringBuilder bodyBuilder = new StringBuilder(matcher.group(3));
    if (StringUtility.hasText(matcher.group(4))) {
      // take care dynamic values can not be implemented as cons
      LOG.warn("Dynamic const '"+constant.getName()+"' found in "+m_workingCopy.getPath().getFileName()+":"+m_currentLineNumber);
      constant.addParseError("Looks like a dynamic constant. Must be migrated by hand.");
      clazz.addConstant(constant);
      return constant;
    }
    nextLine();
    while (m_currentLine != null) {
      bodyBuilder.append(m_currentLine);
      if (END_BLOCK.matcher(m_currentLine).find()) {
        break;
      }
      if (StringUtility.hasText(m_currentLine) && !m_currentLine.startsWith(" ")) {
        throw new VetoException("Could not parse constant body (" + m_workingCopy.getPath().getFileName() + ":" + m_currentLineNumber + ")");
      }
      nextLine();
    }
    constant.setBody(bodyBuilder.toString());
    constant.setEndOffset(m_offsetStartLine + m_currentLine.length());

    clazz.addConstant(constant);
    return constant;

  }

  protected JsSuperCall readSuperCall(Matcher matcher) throws IOException {
    JsSuperCall superCall = new JsSuperCall(matcher.group(2));
    superCall.setStartOffset(m_offsetStartLine);
    superCall.setEndOffset(m_offsetStartLine + m_currentLine.length());
    nextLine();
    return superCall;
  }

  private void nextLine() throws IOException {
    if (m_currentLine != null) {
      m_offsetStartLine += (m_currentLine.length() + m_lineSeparator.length());
    }
    m_currentLine = m_sourceReader.readLine();
    m_currentLineNumber++;
  }

}
