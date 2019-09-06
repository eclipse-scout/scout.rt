package org.eclipse.scout.migration.ecma6.model.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsFunction extends AbstractJsElement {

  private JsClass m_jsClass;
  private final String m_name;
  private String m_args;
  private boolean m_constructor;
  private boolean m_static;
  private JsCommentBlock m_comment;
  private List<String> m_singletonReferences = new ArrayList<>();

  public JsFunction(JsClass jsClass, String name) {
    m_jsClass = jsClass;
    m_name = name;
  }

  public JsClass getJsClass() {
    return m_jsClass;
  }

  public void setJsClass(JsClass jsClass) {
    m_jsClass = jsClass;
  }

  public String getName() {
    return m_name;
  }

  public String getFqn(){
    if(isConstructor()){
      return getJsClass().getFullyQualifiedName();
    }
    return getJsClass().getFullyQualifiedName()+"."+getName();
  }

  public void setConstructor(boolean b) {
    m_constructor = b;
  }

  public boolean isConstructor() {
    return m_constructor;
  }

  public void setStatic(boolean b) {
    m_static = b;
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


  public void setComment(JsCommentBlock comment) {
    m_comment = comment;
  }

  public JsCommentBlock getComment() {
    return m_comment;
  }

  public void addSingletonReference(String fqn){
    m_singletonReferences.add(fqn);
  }
  public List<String> getSingletonReferences() {
    return Collections.unmodifiableList(m_singletonReferences);
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    builder
        .append(getName())
        .append(" [constuctor:")
        .append(isConstructor())
        .append(", static:")
        .append(isStatic())
        .append(", hasComment:")
        .append(getComment() != null)
        .append("]");
    return builder.toString();
  }

}
