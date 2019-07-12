package org.eclipse.scout.migration.ecma6.model.old;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

  private WorkingCopy m_workingCopy;
  private final JsFile m_jsFile;
  private BufferedReader m_sourceReader;
  private String m_currentLine;
  private int m_currentLineNumber = 0;
  private int m_offsetStartLine = 0;

  public JsFileParser(WorkingCopy workingCopy) {
    m_workingCopy = workingCopy;
    m_jsFile = new JsFile(workingCopy.getPath());
    m_sourceReader = new BufferedReader(new StringReader(workingCopy.getInitialSource()));
  }

  public JsFile parse() throws IOException {
    try {
      nextLine();
      JsCommentBlock comment = null;
      while (m_currentLine != null) {
        Matcher matcher = START_COPY_RIGHT.matcher(m_currentLine);
        if(matcher.find()){
          readCopyRight();
          continue;
        }
        matcher = START_FUNCITON_COMMENT.matcher(m_currentLine);
        if (matcher.find()) {
          comment = readFunctionComment();
          continue;
        }
        matcher = START_CONSTRUCTOR.matcher(m_currentLine);
        if(matcher.find()){
          readFunction(matcher,comment, true, false);
          comment = null;
          continue;
        }
        matcher = START_FUNCTION.matcher(m_currentLine);
        if (matcher.find()) {
          readFunction(matcher,comment, false, false);
          comment = null;
          continue;
        }
        matcher = START_STATIC_FUNCTION.matcher(m_currentLine);
        if(matcher.find()){
          readFunction(matcher,comment, false, true);
          comment = null;
          continue;
        }
        matcher = SUPER_BLOCK.matcher(m_currentLine);
        if(matcher.find()){
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
    // debug
    if(LOG.isInfoEnabled()){
      List<JsClass> jsClasses = m_jsFile.getJsClasses();
      if(jsClasses.size() != 1){
        LOG.info("JsFile '"+m_jsFile.getPath().getFileName()+"' does have a strange amount of classes["+
          jsClasses.stream().map(jsC -> "'"+jsC.getFullyQuallifiedName()+"'").collect(Collectors.joining(", "))+"]");
      }
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
        commentBody.append(System.lineSeparator()).append(m_currentLine);
        break;
      }
      else if (FUNCITON_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(System.lineSeparator()).append(m_currentLine);
      }
      else {
        // no comment
        throw new VetoException("Function commentblock could not be parsed (" + m_workingCopy.getPath().getFileName() + ":" + m_currentLineNumber + ") [line: '"+m_currentLine+"']! ");
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
        commentBody.append(System.lineSeparator()).append(m_currentLine);
        break;
      }
      else if (FUNCITON_COMMENT.matcher(m_currentLine).find()) {
        commentBody.append(System.lineSeparator()).append(m_currentLine);
      }
      else {
        // no comment
        throw new VetoException("Function commentblock could not be parsed (" + m_workingCopy.getPath().getFileName() + ":" + m_currentLineNumber + ") [line: '"+m_currentLine+"']! ");
      }
      nextLine();
    }
    comment.setSource(commentBody.toString());
    comment.setEndOffset(m_offsetStartLine + m_currentLine.length());
    nextLine();
    return comment;
  }

  private JsFunction readFunction(Matcher matcher, JsCommentBlock comment, boolean constructor, boolean isStatic) throws IOException {
    JsFunction function = new JsFunction(matcher.group(2));
    function.setComment(comment);
    function.setStartOffset(m_offsetStartLine);
    function.setConstructor(constructor);
    function.setStatic(isStatic);
    function.setArgs(matcher.group(3));
    StringBuilder functionBody = new StringBuilder(matcher.group(4));
    if(StringUtility.hasText(matcher.group(5))){
      functionBody.append(matcher.group(5));
      function.setBody(functionBody.toString());
      nextLine();
      return function;
    }
    nextLine();
    while (m_currentLine != null) {
      if (END_BLOCK.matcher(m_currentLine).find()) {
        functionBody.append(m_currentLine);
        break;
      }
      if (StringUtility.hasText(m_currentLine) && !m_currentLine.startsWith(" ")) {
        throw new VetoException("Could not parse function body (" + m_workingCopy.getPath().getFileName() + ":" + m_currentLineNumber + ")");
      }
      nextLine();
    }
    function.setBody(functionBody.toString());
    function.setEndOffset(m_offsetStartLine + m_currentLine.length());

    JsClass clazz = m_jsFile.getLastOrAppend(matcher.group(1));
    clazz.addFunction(function);
    return function;
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
      m_offsetStartLine += (m_currentLine.length()+1);
    }
    m_currentLine = m_sourceReader.readLine();
    m_currentLineNumber++;
  }


}
