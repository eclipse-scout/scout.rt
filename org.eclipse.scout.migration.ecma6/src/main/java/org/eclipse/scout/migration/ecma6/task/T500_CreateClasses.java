package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.regex.Function;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(500)
public class T500_CreateClasses extends AbstractTask{

//  scout.FormField = function() {
  private static Pattern CONSTUCTOR_REGEX = Pattern.compile("(\\s+)([^\\.\\s]*)\\.([^\\.\\s]*)\\s*\\=\\s*function\\(\\)\\s*\\{");

  private Predicate<Path> m_filter = PathFilters.and(PathFilters.inSrcMainJs(), PathFilters.withExtension("js"));
//  scout.inherits(scout.FormField, scout.Widget);
  private Pattern m_functionOrConstructorRegex;
  private static Pattern INHERIT_REGEX = Pattern.compile("scout\\.inherits\\([^\\,]*\\,\\s*([^\\)]*)\\)\\;");
  private String m_constuctorName = "TODO MIG [constuctorName]";
  private List<Function> m_functions = new ArrayList<>();
  private String m_superClass = null;


  @Override
  public void setup(Context context) {
    m_functionOrConstructorRegex = Pattern.compile("\\s"+context.getNamespace()+"\\.([^\\.\\s]*)(\\.prototype\\.([^\\s]*))?\\s*\\=\\s*function\\(");
  }

  @Override
  public boolean accept(Path file, Path moduleRelativeFile, Context context) {
    return m_filter.test(file);
  }

  @Override
  public void process(Path file, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(file);
    try {

//      parseClasses(workingCopy, context);
//      readSuperClass(workingCopy,context);
      createClasses(workingCopy, context);
    }catch (VetoException e){
      MigrationUtility.prependTodo(workingCopy, e.getMessage());
      System.out.println("ERROR ["+file.getFileName()+"]: "+e.getMessage());
    }
//    String source = workingCopy.getSource();
//    Matcher matcher = CONSTUCTOR_REGEX.matcher(source);
//    while(matcher.find()){
//      System.out.println(matcher.group(0)+" - "+matcher.group(2)+" - "+matcher.group(3));
//    }
//    scout.FormField.prototype._createLoadingSupport = function() {

  }

  protected void parseClasses(WorkingCopy workingCopy, Context context){
    List<String> orderedClassNames = new ArrayList<>();
//    List<String> classNames = new ArrayList<>();
    String source = workingCopy.getSource();
    Matcher matcher = m_functionOrConstructorRegex.matcher(source);
    while(matcher.find()){
      Function f = new Function(context.getNamespace(), matcher.group(1),matcher.group(3));
      if("this".equals(f.getClassname())){
        continue;
      }
      if(f.isConstructor()){
        if(f.isConstructor() && !orderedClassNames.isEmpty() && orderedClassNames.contains(f.getClassname())){
          throw new VetoException("Constructors must be the first function of a class (ensure to move the constructor to the top of the class functions).");
        }
      }
      if(orderedClassNames.isEmpty() || !orderedClassNames.contains(f.getClassname())){
        orderedClassNames.add(f.getClassname());
      }else if(!orderedClassNames.get(orderedClassNames.size()-1).equals(f.getClassname())){
        throw new VetoException("In case of multiple classes in one file, functions must be in classblocks (all X functions then all Y functions).");
      }
      m_functions.add(f);
    }
  }

  protected void readSuperClass(WorkingCopy workingCopy, Context context){
    String source = workingCopy.getSource();
    Matcher matcher = INHERIT_REGEX.matcher(source);
    if(matcher.find()){
      m_superClass = matcher.group(1);
    }

  }

  protected void createClasses(WorkingCopy workingCopy, Context context){
    JsFile jsFile = context.ensureJsFile(workingCopy);

  }
}
