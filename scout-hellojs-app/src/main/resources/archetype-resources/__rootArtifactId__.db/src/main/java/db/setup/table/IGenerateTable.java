#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup.table;

import java.sql.Date;

import org.jooq.DataType;
import org.jooq.impl.SQLDataType;

import ${package}.db.setup.IDatabaseObject;

public interface IGenerateTable extends IDatabaseObject {

  DataType<String> TYPE_ID = SQLDataType.VARCHAR.length(36).nullable(false);

  DataType<Boolean> TYPE_BOOLEAN = SQLDataType.BOOLEAN;
  DataType<Integer> TYPE_INTEGER = SQLDataType.INTEGER;
  DataType<Double> TYPE_DOUBLE = SQLDataType.DOUBLE;
  DataType<byte[]> TYPE_BLOB = SQLDataType.BLOB;
  DataType<Date> TYPE_DATE = SQLDataType.DATE;

  DataType<String> TYPE_STRING_S = SQLDataType.VARCHAR.length(64).nullable(false);
  DataType<String> TYPE_STRING_S_OPTIONAL = SQLDataType.VARCHAR.length(64).nullable(true);

  String getSchemaName();

  String createSQLInternal();

}
