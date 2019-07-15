package org.eclipse.scout.migration.ecma6.model.old;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JsFile extends AbstractSourceRange {

  private final Path m_path;
  private JsCommentBlock m_copyRight;
  private List<JsClass> m_jsClasses = new ArrayList<>();


  public JsFile(Path path){
    Assertions.assertNotNull(path);
    m_path = path;
  }

  public Path getPath() {
    return m_path;
  }

  public void setCopyRight(JsCommentBlock copyRight) {
    m_copyRight = copyRight;
  }

  public JsCommentBlock getCopyRight() {
    return m_copyRight;
  }

  public void addJsClass(JsClass jsClass){
    m_jsClasses.add(jsClass);
  }

  public List<JsClass> getJsClasses() {
    return Collections.unmodifiableList(m_jsClasses);
  }

  public JsClass getJsClass(String fqn){
    return m_jsClasses.stream()
      .filter(cz -> cz.getFullyQuallifiedName().equalsIgnoreCase(fqn))
      .findFirst().orElse(null);
  }

  public boolean hasJsClasses(){
    return !m_jsClasses.isEmpty();
  }


  public JsClass getLastOrAppend(String fqn){
    JsClass jsClass = null;
    if(m_jsClasses.isEmpty()){
      jsClass = new JsClass(fqn, this);
      m_jsClasses.add(jsClass);
      return jsClass;
    }
    jsClass = m_jsClasses.get(m_jsClasses.size() - 1);
    if(jsClass.getFullyQuallifiedName().equals(fqn)){
      return jsClass;
    }
    jsClass = m_jsClasses.stream().filter(c -> c.getFullyQuallifiedName().equals(fqn)).findFirst().orElse(null);
    if(jsClass != null){
      throw new VetoException("Tried to access last class '"+fqn+"' in file '"+getPath().getFileName()+"', but is not last one!");
    }
    jsClass = new JsClass(fqn, this);
    m_jsClasses.add(jsClass);
    return jsClass;
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent){
    StringBuilder builder = new StringBuilder();
    builder.append(indent).append(getPath().getFileName())
      .append(" [hasCopyRight:").append(getCopyRight() !=null).append("]").append(System.lineSeparator())
    .append(m_jsClasses.stream().map(c -> indent+"- "+c.toString(indent+"  ")).collect(Collectors.joining(System.lineSeparator())));
    return builder.toString();
  }
}
