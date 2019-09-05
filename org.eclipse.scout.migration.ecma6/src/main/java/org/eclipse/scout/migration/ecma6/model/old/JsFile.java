package org.eclipse.scout.migration.ecma6.model.old;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.references.AliasedMember;
import org.eclipse.scout.migration.ecma6.model.references.JsImport;
import org.eclipse.scout.migration.ecma6.model.references.UnresolvedImport;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsFile extends AbstractJsElement {
  private static final Logger LOG = LoggerFactory.getLogger(JsFile.class);

  private final Path m_path;
  private JsCommentBlock m_copyRight;
  private final List<JsClass> m_jsClasses = new ArrayList<>();
  private final List<JsTopLevelEnum> m_jsTopLevelEnums = new ArrayList<>();
  private final Map<String /*module name*/, JsImport> m_imports = new HashMap<>();
  private List<JsAppListener> m_appListeners = new ArrayList<>();

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

  public void addJsClass(JsClass jsClass) {
    m_jsClasses.add(jsClass);
  }

  public List<JsClass> getJsClasses() {
    return Collections.unmodifiableList(m_jsClasses);
  }



  public JsClass firstJsClass() {
    return m_jsClasses.stream().findFirst().orElse(null);
  }

  public JsClass getJsClass(String fqn) {
    return m_jsClasses.stream()
        .filter(cz -> cz.getFullyQualifiedName().equalsIgnoreCase(fqn))
        .findFirst().orElse(null);
  }

  public boolean hasJsClasses() {
    return !m_jsClasses.isEmpty();
  }

  public JsClass getLastOrAppend(String fqn) {
    JsClass jsClass, lastJsClass;
    if (m_jsClasses.isEmpty()) {
      Assertions.assertNotNullOrEmpty(fqn, "fqn is empty");
      jsClass = new JsClass(fqn, this);
      m_jsClasses.add(jsClass);
      return jsClass;
    }
    lastJsClass = m_jsClasses.get(m_jsClasses.size() - 1);
    if (lastJsClass.getFullyQualifiedName().equals(fqn)) {
      return lastJsClass;
    }
    jsClass = m_jsClasses.stream().filter(c -> c.getFullyQualifiedName().equals(fqn)).findFirst().orElse(null);
    if (jsClass != null) {
      throw new VetoException("Tried to access last class '" + fqn + "' in file '" + getPath().getFileName() + "', but is not last one (last:'" + lastJsClass.getFullyQualifiedName() + "')!");
    }
    Assertions.assertNotNullOrEmpty(fqn, "fqn is empty");
    jsClass = new JsClass(fqn, this);
    m_jsClasses.add(jsClass);
    return jsClass;
  }

  public void addJsTopLevelEnum(JsTopLevelEnum jsEnum) {
    m_jsTopLevelEnums.add(jsEnum);
  }

  public List<JsTopLevelEnum> getJsTopLevelEnums() {
    return Collections.unmodifiableList(m_jsTopLevelEnums);
  }

  public List<JsAppListener> getAppListeners(){
    return Collections.unmodifiableList(m_appListeners);
  }
  public void addAppListener(JsAppListener appListener){
    m_appListeners.add(appListener);
  }
  /**
   * @param fullyQualifiedName
   *          e.g. scout.FormField
   * @return
   */
  public AliasedMember getOrCreateImport(String fullyQualifiedName, Context context) {
    // if already unresolved return
    JsImport imp = m_imports.get(fullyQualifiedName);
    if (imp != null) {
      return imp.getDefaultMember();
    }
    // try to find JsClass
    JsClass clazz = context.getJsClass(fullyQualifiedName);
    if (clazz != null) {
      return getOrCreateImport(clazz);
    }
    // try to find in libraries
    INamedElement element = context.getLibraries().getElement(fullyQualifiedName);
    if (element == null) {
      LOG.error("Could not resolve import for '" + fullyQualifiedName + "'. Probably a library is missing.");
      imp = new UnresolvedImport(fullyQualifiedName);
      imp.withDefaultMember(new AliasedMember(MigrationUtility.parseMemberName(fullyQualifiedName)));
      addImport(imp);
      return imp.getDefaultMember();
    }
    return getOrCreateImport(element.getAncestor(e -> e.getType() == Type.Library).getCustomAttributeString(INamedElement.LIBRARY_MODULE_NAME), MigrationUtility.parseMemberName(fullyQualifiedName), false);

  }

  public AliasedMember getOrCreateImport(JsClass jsClass) {
    return getOrCreateImport(jsClass.getName(), jsClass.getJsFile().getPath(), jsClass.isDefault());
  }

  public AliasedMember getOrCreateImport(String memberName, Path fileToImport, boolean defaultIfPossible) {
    Assertions.assertNotNull(fileToImport);
    Assertions.assertNotNull(memberName);
    String moduleName = JsImport.computeRelativePath(this.getPath().getParent(), fileToImport);
    return getOrCreateImport(moduleName, memberName, defaultIfPossible);
  }

  protected AliasedMember getOrCreateImport(String moduleName, String memberName, boolean defaultIfPossible) {
    JsImport libImport = m_imports.get(moduleName);
    if (libImport == null) {
      libImport = new JsImport(moduleName);
      addImport(libImport);
    }
    AliasedMember aliasedMember = libImport.findAliasedMember(memberName);
    if (aliasedMember == null) {
      aliasedMember = ensureUniqueAlias(new AliasedMember(memberName), 1);
      if (defaultIfPossible && libImport.getDefaultMember() == null) {
        libImport.setDefaultMember(aliasedMember);
      }
      else {
        libImport.addMember(aliasedMember);
      }
    }
    return aliasedMember;
  }

  public JsImport getImport(String moduleName) {
    return m_imports.get(moduleName);
  }

  public void addImport(JsImport jsImport) {
    m_imports.put(jsImport.getModuleName(), jsImport);
  }

  private AliasedMember ensureUniqueAlias(AliasedMember member, int index) {
    String alias = Optional.ofNullable(member.getAlias()).orElse(member.getName());
    if (getJsClasses().stream().anyMatch(c -> c.getName().equalsIgnoreCase(alias))) {
      member.setAlias(member.getName() + "_" + index);
      return ensureUniqueAlias(member, index + 1);
    }
    return member;
  }

  public Collection<JsImport> getImports() {
    return Collections.unmodifiableCollection(m_imports.values());
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    builder.append(indent).append(getPath().getFileName())
        .append(" [hasCopyRight:").append(getCopyRight() != null).append("]").append(System.lineSeparator())
        .append(m_jsClasses.stream().map(c -> indent + "- " + c.toString(indent + "  ")).collect(Collectors.joining(System.lineSeparator())))
      .append(m_appListeners.stream().map(c -> indent + "- " + c.toString(indent + "  ")).collect(Collectors.joining(System.lineSeparator())));
    return builder.toString();
  }
}
