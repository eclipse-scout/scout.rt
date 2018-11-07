#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup.table;

import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ${package}.db.setup.AbstractTable;

public class PersonTable extends AbstractTable {

  public static final String TABLE = "person";

  public static final String PERSON_ID = "person_id";
  public static final String LAST_NAME = "last_name";
  public static final String FIRST_NAME = "first_name";
  public static final String SALARY = "salary";
  public static final String EXTERNAL = "external";

  @Override
  public String createSQLInternal() {
    return getContext().createTable(getName())
        .column(PERSON_ID, SQLDataType.VARCHAR.length(36).nullable(false))
        .column(FIRST_NAME, SQLDataType.VARCHAR.length(200).nullable(true))
        .column(LAST_NAME, SQLDataType.VARCHAR.length(200).nullable(false))
        .column(SALARY, SQLDataType.DECIMAL(9).nullable(true))
        .column(EXTERNAL, SQLDataType.BOOLEAN.nullable(true))
        .constraints(DSL.constraint(getPKName()).primaryKey(PERSON_ID))
        .getSQL();
  }

  @Override
  public String getName() {
    return TABLE;
  }

  @Override
  public Logger getLogger() {
    return LoggerFactory.getLogger(PersonTable.class);
  }
}
