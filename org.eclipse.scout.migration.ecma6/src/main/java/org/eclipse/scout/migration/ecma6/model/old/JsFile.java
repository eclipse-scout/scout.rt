package org.eclipse.scout.migration.ecma6.model.old;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsFile extends AbstractJsElement {

  private final Path m_path;
  private JsCommentBlock m_copyRight;
  private List<JsClass> m_jsClasses = new ArrayList<>();
  private final Map<JsFile, JsImport> m_imports = new HashMap<>();


  public JsFile(Path path) {
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
    JsClass jsClass, lastJsClass = null;
    if(m_jsClasses.isEmpty()){
      jsClass = new JsClass(fqn, this);
      m_jsClasses.add(jsClass);
      return jsClass;
    }
    lastJsClass = m_jsClasses.get(m_jsClasses.size() - 1);
    if(lastJsClass.getFullyQuallifiedName().equals(fqn)){
      return lastJsClass;
    }
    jsClass = m_jsClasses.stream().filter(c -> c.getFullyQuallifiedName().equals(fqn)).findFirst().orElse(null);
    if(jsClass != null){
      throw new VetoException("Tried to access last class '"+fqn+"' in file '"+getPath().getFileName()+"', but is not last one (last:'"+lastJsClass.getFullyQuallifiedName()+"')!");
    }
    jsClass = new JsClass(fqn, this);
    m_jsClasses.add(jsClass);
    return jsClass;
  }

  public AliasedJsClass getOrCreateImport(JsClass jsClass){
    return m_imports.computeIfAbsent(jsClass.getJsFile(), c -> new JsImport(c, this))
      .computeAliasIfAbsent(jsClass);
  }

//  public JsImport resolveReference(String namespace, String classname){
//
//    String fqn = namespace+"."+classname;
//    JsImport imp = m_imports.get(fqn);
//    if(imp == null){
//      String alias = classname;
//      if(m_jsClasses.stream().filter(c -> c.getName().equalsIgnoreCase(classname)).findFirst().isPresent()){
//        alias = StringUtility.uppercaseFirst(namespace)+classname;
//      }
//      imp = new JsImport(namespace,classname, alias);
//      m_imports.put(fqn, imp);
//    }
//    return imp;
//  }

  public Collection<JsImport> getImports(){
    return Collections.unmodifiableCollection(m_imports.values());
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
