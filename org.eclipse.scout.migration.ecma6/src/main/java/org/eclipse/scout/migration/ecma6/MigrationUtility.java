package org.eclipse.scout.migration.ecma6;

public final class MigrationUtility {
  private MigrationUtility(){}

  public static void  prependTodo(WorkingCopy workingCopy, String todoText){
    String source = workingCopy.getSource();
    source = prependTodo(source, todoText);
    workingCopy.setSource(source);

  }

  public static String prependTodo(String source, String todoText){
    source = "TODO MIG: "+todoText+System.lineSeparator()+source;
    return source;
  }
}
