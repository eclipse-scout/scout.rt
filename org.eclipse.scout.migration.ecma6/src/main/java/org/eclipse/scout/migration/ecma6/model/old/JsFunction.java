package org.eclipse.scout.migration.ecma6.model.old;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsFunction extends AbstractJsElement{

  private final JsClass m_jsClass;
  private final String m_name;
  private String m_args;
  private String m_body;
  private boolean m_static;
  private JsCommentBlock m_comment;
  private boolean m_constructor;


  public JsFunction(JsClass jsClass, String name){
    m_jsClass = jsClass;
    m_name = name;
  }

  public JsClass getJsClass() {
    return m_jsClass;
  }

  public String getName() {
    return m_name;
  }

  public void setConstructor(boolean constructor) {
    m_constructor = constructor;
  }

  public boolean isConstructor() {
    return m_constructor;
  }

  public void setStatic(boolean aStatic) {
    m_static = aStatic;
  }

  public boolean isStatic() {
    return m_static;
  }

  public void setArgs(String args) {
    m_args = args;
  }

  public String getArgs() {
    return m_args;
  }

  public void setBody(String body) {
    m_body = body;
  }

  public String getBody() {
    return m_body;
  }

  public void setComment(JsCommentBlock comment) {
    m_comment = comment;
  }

  public JsCommentBlock getComment() {
    return m_comment;
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent){
    StringBuilder builder = new StringBuilder();
    builder.append(getName()).append(" [constuctor:").append(isConstructor()).append(", static:").append(isStatic()).append(", hasComment:").append(getComment()!= null).append("]");
    return builder.toString();
  }

}
