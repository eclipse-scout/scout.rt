package org.eclipse.scout.migration.ecma6.model.old;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

public class JsFileParser {

  private static final Logger LOG = LoggerFactory.getLogger(JsFileParser.class);

  private static enum READ_SCOPE {
    FUNCTION_COMMENT,
    FUNCTION,
    ENUM
  };

  private static Pattern START_FUNCITON_COMMENT = Pattern.compile("^\\/\\*\\*$");
  private static Pattern FUNCITON_COMMENT = Pattern.compile("^\\ \\*");
  private static Pattern END_FUNCITON_COMMENT = Pattern.compile("^\\ \\*\\/");


  private WorkingCopy m_workingCopy;
  private BufferedReader m_sourceReader;
  private String m_currentLine;
  private int m_currentLineNumber =0;
  private READ_SCOPE m_scope;
  private String m_comment;

  public JsFileParser(WorkingCopy workingCopy){
    m_workingCopy = workingCopy;
    m_sourceReader = new BufferedReader(new StringReader(workingCopy.getSource()));
  }

  public void parse() throws IOException {
    try{
    nextLine();
    while (m_currentLine != null){
      if(START_FUNCITON_COMMENT.matcher(m_currentLine).find()){
        m_comment = readFuncitonComment();
      }
      nextLine();
    }
    }catch (VetoException e){
      MigrationUtility.prependTodo(m_workingCopy, e.getMessage());
      LOG.error(e.getMessage());
    }
  }

  private String readFuncitonComment() throws IOException {
    StringBuilder comment = new StringBuilder(m_currentLine);
    nextLine();

    while(m_currentLine != null) {
      if(END_FUNCITON_COMMENT.matcher(m_currentLine).find()){
        comment.append(System.lineSeparator()).append(m_currentLine);
        break;
      }
      else if(FUNCITON_COMMENT.matcher(m_currentLine).find()){
        comment.append(System.lineSeparator()).append(m_currentLine);
      }else{
        // no comment
        throw new VetoException("Function commentblock could not be parsed ("+m_workingCopy.getPath()+":"+m_currentLineNumber+")! ");
      }
      nextLine();
    }
    System.out.println("comment found: ");
    System.out.println(comment.toString());
    return comment.toString();
  }

  private void nextLine() throws IOException {
    m_currentLine = m_sourceReader.readLine();
    m_currentLineNumber++;
  }
}
