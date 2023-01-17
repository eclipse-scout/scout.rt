/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TypeCastUtility {

  private static final Logger LOG = LoggerFactory.getLogger(TypeCastUtility.class);

  // singleton
  private static final TypeCastUtility instance = new TypeCastUtility();

  private static final int CHARACTER = 1;
  private static final int BYTE = 2;
  private static final int BOOLEAN = 3;
  private static final int SHORT = 4;
  private static final int INTEGER = 5;
  private static final int LONG = 6;
  private static final int FLOAT = 7;
  private static final int DOUBLE = 8;
  private static final int STRING = 9;
  private static final int OBJECT = 10;
  private static final int BIGINTEGER = 11;
  private static final int BIGDECIMAL = 12;
  private static final int DATE = 13;
  private static final int CALENDAR = 14;
  private static final int SQLDATE = 15;
  private static final int SQLTIME = 16;
  private static final int SQLTIMESTAMP = 17;
  private static final int TRISTATE = 18;
  private static final int UTCDATE = 19;
  private static final int VOID = 9999;

  private static final String ZULU_DATE_TO_STRING_FORMAT = "dd.MM.yyyy'T'HH:mm:ss.sss'Z'";

  /**
   * Casts an object into a specific type.
   *
   * @throws IllegalArgumentException
   *           if the cast failed.
   */
  @SuppressWarnings("unchecked")
  public static <T> T castValue(Object object, Class<T> castType) {
    return (T) instance.castValueImpl(object, castType);
  }

  /**
   * @param type
   *          not null
   * @return either the same type or if the type is a primitive type, the wrapper type of that primitive type
   */
  public static Class<?> getNonPrimitiveType(Class<?> type) {
    if (type.isPrimitive()) {
      return instance.getWrappedType(type);
    }
    return type;
  }

  /**
   * @param type
   *          not null
   * @return either the same type or if the type is a wrapper type, the primitive type of that wrapper type. May return
   *         null if there is no primitive type for the given type
   */
  public static Class<?> getPrimitiveType(Class<?> type) {
    if (!type.isPrimitive()) {
      return instance.getPrimitiveTypeInternal(type);
    }
    return type;
  }

  // instance
  /**
   * fast access to debug flag
   */
  private final Map<Class, Class> m_wrapperTypeMap = new HashMap<>();
  private final Map<Class, Integer> m_typeMap = new HashMap<>();
  private final Map<Class, Integer> m_primitiveTypeMap = new HashMap<>();
  private final Map<GPCKey, Class<?>> m_genericsParameterClassCache = new ConcurrentHashMap<>();

  private TypeCastUtility() {
    m_wrapperTypeMap.put(char.class, Character.class);
    m_wrapperTypeMap.put(byte.class, Byte.class);
    m_wrapperTypeMap.put(boolean.class, Boolean.class);
    m_wrapperTypeMap.put(short.class, Short.class);
    m_wrapperTypeMap.put(int.class, Integer.class);
    m_wrapperTypeMap.put(long.class, Long.class);
    m_wrapperTypeMap.put(float.class, Float.class);
    m_wrapperTypeMap.put(double.class, Double.class);
    m_wrapperTypeMap.put(void.class, Void.class);
    //
    m_typeMap.put(Character.class, CHARACTER);
    m_typeMap.put(Byte.class, BYTE);
    m_typeMap.put(Boolean.class, BOOLEAN);
    m_typeMap.put(TriState.class, TRISTATE);
    m_typeMap.put(Short.class, SHORT);
    m_typeMap.put(Integer.class, INTEGER);
    m_typeMap.put(Long.class, LONG);
    m_typeMap.put(Float.class, FLOAT);
    m_typeMap.put(Double.class, DOUBLE);
    m_typeMap.put(String.class, STRING);
    m_typeMap.put(Object.class, OBJECT);
    m_typeMap.put(BigInteger.class, BIGINTEGER);
    m_typeMap.put(BigDecimal.class, BIGDECIMAL);
    m_typeMap.put(UTCDate.class, UTCDATE);
    m_typeMap.put(Date.class, DATE);
    m_typeMap.put(Calendar.class, CALENDAR);
    m_typeMap.put(GregorianCalendar.class, CALENDAR);
    m_typeMap.put(java.sql.Date.class, SQLDATE);
    m_typeMap.put(Time.class, SQLTIME);
    m_typeMap.put(Timestamp.class, SQLTIMESTAMP);
    m_typeMap.put(Void.class, VOID);
    //
    m_primitiveTypeMap.put(boolean.class, BOOLEAN);
    m_primitiveTypeMap.put(byte.class, BYTE);
    m_primitiveTypeMap.put(char.class, CHARACTER);
    m_primitiveTypeMap.put(short.class, SHORT);
    m_primitiveTypeMap.put(int.class, INTEGER);
    m_primitiveTypeMap.put(long.class, LONG);
    m_primitiveTypeMap.put(float.class, FLOAT);
    m_primitiveTypeMap.put(double.class, DOUBLE);
  }

  @SuppressWarnings("unchecked")
  private Object castValueImpl(Object o, Class castType) {
    Class toType = castType;
    // null check
    if (o == null) {
      // primitive null-representation
      if (toType.isPrimitive()) {
        return getPrimitiveNull(toType);
      }
      return null;
    }
    // get non-primitive type
    if (toType.isPrimitive()) {
      toType = getWrappedType(toType);
    }
    // direct check
    if (toType.isInstance(o)) {
      return o;
    }

    // need conversion
    Class fromType = o.getClass();
    // array check
    if (toType.isArray()) {
      return castArrayValueImpl(o, fromType, toType);
    }
    else {
      return castBasicValueImpl(o, fromType, toType);
    }
  }

  /**
   * type to typeId
   */
  private int getTypeId(Class type) {
    Integer id = m_typeMap.get(type);
    if (id == null) {
      return 0;
    }
    else {
      return id.intValue();
    }
  }

  /**
   * type to typeId
   */
  private int getPrimitiveTypeId(Class type) {
    Integer id = m_primitiveTypeMap.get(type);
    if (id == null) {
      return 0;
    }
    else {
      return id.intValue();
    }
  }

  /**
   * wrapper type for primitive types
   */
  private Class getWrappedType(Class primitiveType) {
    return m_wrapperTypeMap.get(primitiveType);
  }

  /**
   * primitive type for wrapper types
   */
  private Class getPrimitiveTypeInternal(Class wrappedType) {
    int id = m_typeMap.get(wrappedType);
    Map<Integer, Class> primitiveIdMap = m_primitiveTypeMap
        .entrySet()
        .stream()
        .collect(StreamUtility.toMap(Entry::getValue, Entry::getKey));
    if (!primitiveIdMap.containsKey(id)) {
      return null;
    }
    return primitiveIdMap.get(id);
  }

  /**
   * Null representation of a primitive type (0,false)
   */
  @SuppressWarnings("unchecked")
  private <T> T getPrimitiveNull(Class<T> primitiveType) {
    int fromId = getPrimitiveTypeId(primitiveType);
    if (fromId == 0) {
      throw new IllegalArgumentException(primitiveType + " no primitive type");
    }
    switch (fromId) {
      case BOOLEAN: {
        return (T) Boolean.FALSE;
      }
      case BYTE: {
        return (T) Byte.valueOf((byte) 0);
      }
      case CHARACTER: {
        return (T) Character.valueOf('\u0000');
      }
      case SHORT: {
        return (T) Short.valueOf((short) 0);
      }
      case INTEGER: {
        return (T) Integer.valueOf(0);
      }
      case LONG: {
        return (T) Long.valueOf(0L);
      }
      case FLOAT: {
        return (T) Float.valueOf(0.0f);
      }
      case DOUBLE: {
        return (T) Double.valueOf(0.0);
      }
      default: {
        throw new IllegalArgumentException(primitiveType + " no primitive type");
      }
    }
  }

  /**
   * exception builder
   */
  private IllegalArgumentException createException(Object o, Class fromType, Class toType, int code, String msg) {
    return new IllegalArgumentException(
        "converting "
            + VerboseUtility.dumpObject(o)
            + " from "
            + VerboseUtility.dumpType(fromType)
            + " to "
            + VerboseUtility.dumpType(toType)
            + " failed with code "
            + code
            + " (" + msg + ")");
  }

  private Object castArrayValueImpl(Object o, Class fromType, Class toType) {
    if (!fromType.isArray()) {
      // integrate collections framework. collections can be transformed to
      // arrays
      if (o instanceof Collection) {
        o = ((Collection) o).toArray();
      }
      else if (o instanceof Map) {
        o = ((Map) o).values().toArray();
      }
      else {
        throw createException(o, fromType, toType, 1, "object is not an array");
      }
    }
    Class toCompType = toType.getComponentType();
    // calculate dimension
    int dim = 0;
    Class t = toType;
    while (t.isArray()) {
      // next
      dim++;
      t = t.getComponentType();
    }
    // create array dims
    int[] dims = new int[dim];
    dim = 0;
    Object od = o;
    t = toType;
    while (t.isArray()) {
      dims[dim] = Array.getLength(od);
      t = t.getComponentType();
      if (dims[dim] == 0) {
        break;// if array length is 0 break up
      }
      // next
      od = Array.get(od, 0);
      dim++;
    }
    // create array
    Object newArray = Array.newInstance(t, dims);
    for (int i = 0, ni = dims[0]; i < ni; i++) {
      Object castedElement = castValueImpl(Array.get(o, i), toCompType);
      Array.set(newArray, i, castedElement);
    }
    return newArray;
  }

  @SuppressWarnings("squid:S138")
  private Object castBasicValueImpl(Object o, Class fromType, Class toType) {
    // null check
    if (o == null) {
      return null;
    }
    // from type in map
    int fromId = getTypeId(fromType);
    if (fromId == 0) {
      throw createException(o, fromType, toType, 2, "no from-mapping");
    }
    // to type in map
    int toId = getTypeId(toType);
    if (toId == 0) {
      throw createException(o, fromType, toType, 3, "no to-mapping");
    }
    switch (fromId) {
      case CHARACTER: {
        switch (toId) {
          case CHARACTER: {
            return o;
          }
          case BYTE: {
            return txCharToByte(((Character) o).charValue());
          }
          case BOOLEAN: {
            return txCharToBoolean(((Character) o).charValue());
          }
          case TRISTATE: {
            return txCharToTriState(((Character) o).charValue());
          }
          case SHORT: {
            return txCharToShort(((Character) o).charValue());
          }
          case INTEGER: {
            return txCharToInt(((Character) o).charValue());
          }
          case LONG: {
            return txCharToLong(((Character) o).charValue());
          }
          case FLOAT: {
            return txCharToFloat(((Character) o).charValue());
          }
          case DOUBLE: {
            return txCharToDouble(((Character) o).charValue());
          }
          case STRING: {
            return txCharToString(((Character) o).charValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txCharToBigInteger(((Character) o).charValue());
          }
          case BIGDECIMAL: {
            return txCharToBigDecimal(((Character) o).charValue());
          }
        }
        break;
      }
      case BYTE: {
        switch (toId) {
          case CHARACTER: {
            return txByteToChar(((Byte) o).byteValue());
          }
          case BYTE: {
            return o;
          }
          case BOOLEAN: {
            return txByteToBoolean(((Byte) o).byteValue());
          }
          case TRISTATE: {
            return txByteToTriState(((Byte) o).byteValue());
          }
          case SHORT: {
            return txByteToShort(((Byte) o).byteValue());
          }
          case INTEGER: {
            return txByteToInt(((Byte) o).byteValue());
          }
          case LONG: {
            return txByteToLong(((Byte) o).byteValue());
          }
          case FLOAT: {
            return txByteToFloat(((Byte) o).byteValue());
          }
          case DOUBLE: {
            return txByteToDouble(((Byte) o).byteValue());
          }
          case STRING: {
            return txByteToString(((Byte) o).byteValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txByteToBigInteger(((Byte) o).byteValue());
          }
          case BIGDECIMAL: {
            return txByteToBigDecimal(((Byte) o).byteValue());
          }
        }
        break;
      }
      case BOOLEAN: {
        switch (toId) {
          case CHARACTER: {
            return txBooleanToChar(((Boolean) o).booleanValue());
          }
          case BYTE: {
            return txBooleanToByte(((Boolean) o).booleanValue());
          }
          case BOOLEAN: {
            return o;
          }
          case TRISTATE: {
            return txBooleanToTriState(((Boolean) o).booleanValue());
          }
          case SHORT: {
            return txBooleanToShort(((Boolean) o).booleanValue());
          }
          case INTEGER: {
            return txBooleanToInt(((Boolean) o).booleanValue());
          }
          case LONG: {
            return txBooleanToLong(((Boolean) o).booleanValue());
          }
          case FLOAT: {
            return txBooleanToFloat(((Boolean) o).booleanValue());
          }
          case DOUBLE: {
            return txBooleanToDouble(((Boolean) o).booleanValue());
          }
          case STRING: {
            return txBooleanToString(((Boolean) o).booleanValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txBooleanToBigInteger(((Boolean) o).booleanValue());
          }
          case BIGDECIMAL: {
            return txBooleanToBigDecimal(((Boolean) o).booleanValue());
          }
        }
        break;
      }
      case TRISTATE: {
        switch (toId) {
          case CHARACTER: {
            return txTriStateToChar((TriState) o);
          }
          case BYTE: {
            return txTriStateToByte((TriState) o);
          }
          case BOOLEAN: {
            return txTriStateToBoolean((TriState) o);
          }
          case TRISTATE: {
            return o;
          }
          case SHORT: {
            return txTriStateToShort((TriState) o);
          }
          case INTEGER: {
            return txTriStateToInt((TriState) o);
          }
          case LONG: {
            return txTriStateToLong((TriState) o);
          }
          case FLOAT: {
            return txTriStateToFloat((TriState) o);
          }
          case DOUBLE: {
            return txTriStateToDouble((TriState) o);
          }
          case STRING: {
            return txTriStateToString((TriState) o);
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txTriStateToBigInteger((TriState) o);
          }
          case BIGDECIMAL: {
            return txTriStateToBigDecimal((TriState) o);
          }
        }
        break;
      }
      case SHORT: {
        switch (toId) {
          case CHARACTER: {
            return txShortToChar(((Short) o).shortValue());
          }
          case BYTE: {
            return txShortToByte(((Short) o).shortValue());
          }
          case BOOLEAN: {
            return txShortToBoolean(((Short) o).shortValue());
          }
          case TRISTATE: {
            return txShortToTriState(((Short) o).shortValue());
          }
          case SHORT: {
            return o;
          }
          case INTEGER: {
            return txShortToInt(((Short) o).shortValue());
          }
          case LONG: {
            return txShortToLong(((Short) o).shortValue());
          }
          case FLOAT: {
            return txShortToFloat(((Short) o).shortValue());
          }
          case DOUBLE: {
            return txShortToDouble(((Short) o).shortValue());
          }
          case STRING: {
            return txShortToString(((Short) o).shortValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txShortToBigInteger(((Short) o).shortValue());
          }
          case BIGDECIMAL: {
            return txShortToBigDecimal(((Short) o).shortValue());
          }
        }
        break;
      }
      case INTEGER: {
        switch (toId) {
          case CHARACTER: {
            return txIntToChar(((Integer) o).intValue());
          }
          case BYTE: {
            return txIntToByte(((Integer) o).intValue());
          }
          case BOOLEAN: {
            return txIntToBoolean(((Integer) o).intValue());
          }
          case TRISTATE: {
            return txIntToTriState(((Integer) o).intValue());
          }
          case SHORT: {
            return txIntToShort(((Integer) o).intValue());
          }
          case INTEGER: {
            return o;
          }
          case LONG: {
            return txIntToLong(((Integer) o).intValue());
          }
          case FLOAT: {
            return txIntToFloat(((Integer) o).intValue());
          }
          case DOUBLE: {
            return txIntToDouble(((Integer) o).intValue());
          }
          case STRING: {
            return txIntToString(((Integer) o).intValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txIntToBigInteger(((Integer) o).intValue());
          }
          case BIGDECIMAL: {
            return txIntToBigDecimal(((Integer) o).intValue());
          }
        }
        break;
      }
      case LONG: {
        switch (toId) {
          case CHARACTER: {
            return txLongToChar(((Long) o).longValue());
          }
          case BYTE: {
            return txLongToByte(((Long) o).longValue());
          }
          case BOOLEAN: {
            return txLongToBoolean(((Long) o).longValue());
          }
          case TRISTATE: {
            return txLongToTriState(((Long) o).longValue());
          }
          case SHORT: {
            return txLongToShort(((Long) o).longValue());
          }
          case INTEGER: {
            return txLongToInt(((Long) o).longValue());
          }
          case LONG: {
            return o;
          }
          case FLOAT: {
            return txLongToFloat(((Long) o).longValue());
          }
          case DOUBLE: {
            return txLongToDouble(((Long) o).longValue());
          }
          case STRING: {
            return txLongToString(((Long) o).longValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txLongToBigInteger(((Long) o).longValue());
          }
          case BIGDECIMAL: {
            return txLongToBigDecimal(((Long) o).longValue());
          }
        }
        break;
      }
      case FLOAT: {
        switch (toId) {
          case CHARACTER: {
            return txFloatToChar(((Float) o).floatValue());
          }
          case BYTE: {
            return txFloatToByte(((Float) o).floatValue());
          }
          case BOOLEAN: {
            return txFloatToBoolean(((Float) o).floatValue());
          }
          case TRISTATE: {
            return txFloatToTriState(((Float) o).floatValue());
          }
          case SHORT: {
            return txFloatToShort(((Float) o).floatValue());
          }
          case INTEGER: {
            return txFloatToInt(((Float) o).floatValue());
          }
          case LONG: {
            return txFloatToLong(((Float) o).floatValue());
          }
          case FLOAT: {
            return o;
          }
          case DOUBLE: {
            return txFloatToDouble(((Float) o).floatValue());
          }
          case STRING: {
            return txFloatToString(((Float) o).floatValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txFloatToBigInteger(((Float) o).floatValue());
          }
          case BIGDECIMAL: {
            return txFloatToBigDecimal(((Float) o).floatValue());
          }
        }
        break;
      }
      case DOUBLE: {
        switch (toId) {
          case CHARACTER: {
            return txDoubleToChar(((Double) o).doubleValue());
          }
          case BYTE: {
            return txDoubleToByte(((Double) o).doubleValue());
          }
          case BOOLEAN: {
            return txDoubleToBoolean(((Double) o).doubleValue());
          }
          case TRISTATE: {
            return txDoubleToTriState(((Double) o).doubleValue());
          }
          case SHORT: {
            return txDoubleToShort(((Double) o).doubleValue());
          }
          case INTEGER: {
            return txDoubleToInt(((Double) o).doubleValue());
          }
          case LONG: {
            return txDoubleToLong(((Double) o).doubleValue());
          }
          case FLOAT: {
            return txDoubleToFloat(((Double) o).doubleValue());
          }
          case DOUBLE: {
            return o;
          }
          case STRING: {
            return txDoubleToString(((Double) o).doubleValue());
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return txDoubleToBigInteger(((Double) o).doubleValue());
          }
          case BIGDECIMAL: {
            return txDoubleToBigDecimal(((Double) o).doubleValue());
          }
        }
        break;
      }
      case STRING: {
        if (((String) o).isEmpty()) {
          return null;// special handling for empty
        }
        // strings
        switch (toId) {
          case CHARACTER: {
            return txStringToChar((String) o);
          }
          case BYTE: {
            return txStringToByte((String) o);
          }
          case BOOLEAN: {
            return txStringToBoolean((String) o);
          }
          case TRISTATE: {
            return txStringToTriState((String) o);
          }
          case SHORT: {
            return txStringToShort((String) o);
          }
          case INTEGER: {
            return txStringToInt((String) o);
          }
          case LONG: {
            return txStringToLong((String) o);
          }
          case FLOAT: {
            return txStringToFloat((String) o);
          }
          case DOUBLE: {
            return txStringToDouble((String) o);
          }
          case STRING: {
            return o;
          }
          case OBJECT: {
            return o;
          }
          case DATE: {
            return txStringToDate((String) o);
          }
          case UTCDATE: {
            return txStringToUTCDate((String) o);
          }
          case CALENDAR: {
            return txStringToCalendar((String) o);
          }
          case SQLDATE: {
            return txStringToSqlDate((String) o);
          }
          case SQLTIME: {
            return txStringToSqlTime((String) o);
          }
          case SQLTIMESTAMP: {
            return txStringToSqlTimestamp((String) o);
          }
          case BIGINTEGER: {
            return txStringToBigInteger((String) o);
          }
          case BIGDECIMAL: {
            return txStringToBigDecimal((String) o);
          }
        }
        break;
      }
      case OBJECT: {
        switch (toId) {
          case STRING: {
            return txObjectToString(o);
          }
          case OBJECT: {
            return o;
          }
        }
        break;
      }
      case BIGINTEGER: {
        switch (toId) {
          case CHARACTER: {
            return txBigIntegerToChar((BigInteger) o);
          }
          case BYTE: {
            return txBigIntegerToByte((BigInteger) o);
          }
          case BOOLEAN: {
            return txBigIntegerToBoolean((BigInteger) o);
          }
          case TRISTATE: {
            return txBigIntegerToTriState((BigInteger) o);
          }
          case SHORT: {
            return txBigIntegerToShort((BigInteger) o);
          }
          case INTEGER: {
            return txBigIntegerToInt((BigInteger) o);
          }
          case LONG: {
            return txBigIntegerToLong((BigInteger) o);
          }
          case FLOAT: {
            return txBigIntegerToFloat((BigInteger) o);
          }
          case DOUBLE: {
            return txBigIntegerToDouble((BigInteger) o);
          }
          case STRING: {
            return txBigIntegerToString((BigInteger) o);
          }
          case OBJECT: {
            return o;
          }
          case BIGINTEGER: {
            return o;
          }
          case BIGDECIMAL: {
            return txBigIntegerToBigDecimal((BigInteger) o);
          }
        }
        break;
      }
      case BIGDECIMAL: {
        switch (toId) {
          case CHARACTER: {
            return txBigDecimalToChar((BigDecimal) o);
          }
          case BYTE: {
            return txBigDecimalToByte((BigDecimal) o);
          }
          case BOOLEAN: {
            return txBigDecimalToBoolean((BigDecimal) o);
          }
          case TRISTATE: {
            return txBigDecimalToTriState((BigDecimal) o);
          }
          case SHORT: {
            return txBigDecimalToShort((BigDecimal) o);
          }
          case INTEGER: {
            return txBigDecimalToInt((BigDecimal) o);
          }
          case LONG: {
            return txBigDecimalToLong((BigDecimal) o);
          }
          case FLOAT: {
            return txBigDecimalToFloat((BigDecimal) o);
          }
          case DOUBLE: {
            return txBigDecimalToDouble((BigDecimal) o);
          }
          case STRING: {
            return txBigDecimalToString((BigDecimal) o);
          }
          case OBJECT: {
            return o;
          }
          case BIGDECIMAL: {
            return o;
          }
          case BIGINTEGER: {
            return txBigDecimalToBigInteger((BigDecimal) o);
          }
        }
        break;
      }
      case DATE: {
        switch (toId) {
          case DATE: {
            return o;
          }
          case UTCDATE: {
            return txDateToUTCDate((Date) o);
          }
          case CALENDAR: {
            return txDateToCalendar((Date) o);
          }
          case SQLDATE: {
            return txDateToSqlDate((Date) o);
          }
          case SQLTIME: {
            return txDateToSqlTime((Date) o);
          }
          case SQLTIMESTAMP: {
            return txDateToSqlTimestamp((Date) o);
          }
          case STRING: {
            return txDateToString((Date) o);
          }
          case OBJECT: {
            return o;
          }
        }
        break;
      }
      case UTCDATE: {
        switch (toId) {
          case UTCDATE: {
            return o;
          }
          case DATE: {
            return txUTCDateToDate((UTCDate) o);
          }
          case CALENDAR: {
            return txUTCDateToCalendar((UTCDate) o);
          }
          case SQLDATE: {
            return txUTCDateToSqlDate((UTCDate) o);
          }
          case SQLTIME: {
            return txUTCDateToSqlTime((UTCDate) o);
          }
          case SQLTIMESTAMP: {
            return txUTCDateToSqlTimestamp((UTCDate) o);
          }
          case STRING: {
            return txUTCDateToString((UTCDate) o);
          }
          case OBJECT: {
            return o;
          }
        }
        break;
      }
      case CALENDAR: {
        switch (toId) {
          case DATE: {
            return txCalendarToDate((Calendar) o);
          }
          case UTCDATE: {
            return txCalendarToUTCDate((Calendar) o);
          }
          case CALENDAR: {
            return o;
          }
          case SQLDATE: {
            return txCalendarToSqlDate((Calendar) o);
          }
          case SQLTIME: {
            return txCalendarToSqlTime((Calendar) o);
          }
          case SQLTIMESTAMP: {
            return txCalendarToSqlTimestamp((Calendar) o);
          }
          case STRING: {
            return txCalendarToString((Calendar) o);
          }
          case OBJECT: {
            return o;
          }
        }
        break;
      }
      case SQLDATE: {
        switch (toId) {
          case DATE: {
            return o;
          }
          case UTCDATE: {
            return txSqlDateToUTCDate((java.sql.Date) o);
          }
          case CALENDAR: {
            return txSqlDateToCalendar((java.sql.Date) o);
          }
          case SQLDATE: {
            return o;
          }
          case SQLTIME: {
            return txSqlDateToSqlTime((java.sql.Date) o);
          }
          case SQLTIMESTAMP: {
            return txSqlDateToSqlTimestamp((java.sql.Date) o);
          }
          case STRING: {
            return txSqlDateToString((java.sql.Date) o);
          }
          case OBJECT: {
            return o;
          }
        }
        break;
      }
      case SQLTIME: {
        switch (toId) {
          case DATE: {
            return o;
          }
          case UTCDATE: {
            return txSqlTimeToUTCDate((Time) o);
          }
          case CALENDAR: {
            return txSqlTimeToCalendar((Time) o);
          }
          case SQLDATE: {
            return txSqlTimeToSqlDate((Time) o);
          }
          case SQLTIME: {
            return o;
          }
          case SQLTIMESTAMP: {
            return txSqlTimeToSqlTimestamp((Time) o);
          }
          case STRING: {
            return txSqlTimeToString((Time) o);
          }
          case OBJECT: {
            return o;
          }
        }
        break;
      }
      case SQLTIMESTAMP: {
        switch (toId) {
          case DATE: {
            return o;
          }
          case UTCDATE: {
            return txSqlTimestampToUTCDate((Timestamp) o);
          }
          case CALENDAR: {
            return txSqlTimestampToCalendar((Timestamp) o);
          }
          case SQLDATE: {
            return txSqlTimestampToSqlDate((Timestamp) o);
          }
          case SQLTIME: {
            return txSqlTimestampToSqlTime((Timestamp) o);
          }
          case SQLTIMESTAMP: {
            return o;
          }
          case STRING: {
            return txSqlTimestampToString((Timestamp) o);
          }
          case OBJECT: {
            return o;
          }
        }
        break;
      }
    }
    throw createException(o, fromType, toType, 4, "not implementated");
  }

  /**
   * Base Transformations
   */

  private byte txBooleanToByte(boolean o) {
    return (byte) (o ? 1 : 0);
  }

  private char txBooleanToChar(boolean o) {
    return (o ? 'X' : ' ');
  }

  private double txBooleanToDouble(boolean o) {
    return (o ? 1 : 0);
  }

  private float txBooleanToFloat(boolean o) {
    return (o ? 1 : 0);
  }

  private int txBooleanToInt(boolean o) {
    return (o ? 1 : 0);
  }

  private long txBooleanToLong(boolean o) {
    return (o ? 1 : 0);
  }

  private short txBooleanToShort(boolean o) {
    return (short) (o ? 1 : 0);
  }

  private String txBooleanToString(boolean o) {
    return String.valueOf(o);
  }

  private boolean txByteToBoolean(byte o) {
    return (o == 1);
  }

  private char txByteToChar(byte o) {
    return (char) (o);
  }

  private double txByteToDouble(byte o) {
    return (o);
  }

  private float txByteToFloat(byte o) {
    return (o);
  }

  private int txByteToInt(byte o) {
    return (o);
  }

  private long txByteToLong(byte o) {
    return (o);
  }

  private short txByteToShort(byte o) {
    return (o);
  }

  private String txByteToString(byte o) {
    return String.valueOf((char) o);
  }

  private boolean txCharToBoolean(char o) {
    return (o == 'X' || o == 'x' || o == '1');
  }

  private byte txCharToByte(char o) {
    return (byte) (o);
  }

  private double txCharToDouble(char o) {
    return Double.parseDouble(String.valueOf(o));
  }

  private float txCharToFloat(char o) {
    return Float.parseFloat(String.valueOf(o));
  }

  private int txCharToInt(char o) {
    return Integer.parseInt(String.valueOf(o));
  }

  private long txCharToLong(char o) {
    return Long.parseLong(String.valueOf(o));
  }

  private short txCharToShort(char o) {
    return Short.parseShort(String.valueOf(o));
  }

  private String txCharToString(char o) {
    return String.valueOf(o);
  }

  private boolean txDoubleToBoolean(double o) {
    return (o == 1.0);
  }

  private byte txDoubleToByte(double o) {
    return (byte) (o);
  }

  private char txDoubleToChar(double o) {
    return (char) (o);
  }

  private float txDoubleToFloat(double o) {
    return Float.parseFloat(String.valueOf(o));
  }

  private int txDoubleToInt(double o) {
    return (int) (o);
  }

  private long txDoubleToLong(double o) {
    return (long) (o);
  }

  private short txDoubleToShort(double o) {
    return (short) (o);
  }

  private String txDoubleToString(double o) {
    return String.valueOf(o);
  }

  private boolean txFloatToBoolean(float o) {
    return (o == 1f);
  }

  private byte txFloatToByte(float o) {
    return (byte) (o);
  }

  private char txFloatToChar(float o) {
    return (char) (o);
  }

  private double txFloatToDouble(float o) {
    return Double.parseDouble(String.valueOf(o));
  }

  private int txFloatToInt(float o) {
    return (int) (o);
  }

  private long txFloatToLong(float o) {
    return (long) (o);
  }

  private short txFloatToShort(float o) {
    return (short) (o);
  }

  private String txFloatToString(float o) {
    return String.valueOf(o);
  }

  private boolean txIntToBoolean(int o) {
    return (o == 1);
  }

  private byte txIntToByte(int o) {
    return (byte) (o);
  }

  private char txIntToChar(int o) {
    return (char) (o);
  }

  private double txIntToDouble(int o) {
    return (o);
  }

  private float txIntToFloat(int o) {
    return (o);
  }

  private long txIntToLong(int o) {
    return (o);
  }

  private short txIntToShort(int o) {
    return (short) (o);
  }

  private String txIntToString(int o) {
    return String.valueOf(o);
  }

  private boolean txLongToBoolean(long o) {
    return (o == 1L);
  }

  private byte txLongToByte(long o) {
    return (byte) (o);
  }

  private char txLongToChar(long o) {
    return (char) (o);
  }

  private double txLongToDouble(long o) {
    return (o);
  }

  private float txLongToFloat(long o) {
    return (o);
  }

  private int txLongToInt(long o) {
    return (int) (o);
  }

  private short txLongToShort(long o) {
    return (short) (o);
  }

  private String txLongToString(long o) {
    return String.valueOf(o);
  }

  private String txObjectToString(Object o) {
    return o.toString();
  }

  private boolean txShortToBoolean(short o) {
    return (o == 1);
  }

  private byte txShortToByte(short o) {
    return (byte) (o);
  }

  private char txShortToChar(short o) {
    return (char) (o);
  }

  private double txShortToDouble(short o) {
    return (o);
  }

  private float txShortToFloat(short o) {
    return (o);
  }

  private int txShortToInt(short o) {
    return (o);
  }

  private long txShortToLong(short o) {
    return (o);
  }

  private String txShortToString(short o) {
    return String.valueOf(o);
  }

  private boolean txStringToBoolean(String o) {
    o = o.toLowerCase();
    if ("true".equals(o)) {
      return true;
    }
    if ("1".equals(o)) {
      return true;
    }
    if ("yes".equals(o)) {
      return true;
    }
    if ("x".equals(o)) {
      return true;
    }
    if ("on".equals(o)) {
      return true;
    }
    return false;
  }

  private byte txStringToByte(String o) {
    try {
      return Byte.parseByte(o);
    }
    catch (NumberFormatException nfe1) {
      return (byte) Float.parseFloat(o);
    }
  }

  private char txStringToChar(String o) {
    return o.charAt(0);
  }

  private double txStringToDouble(String o) {
    return Double.parseDouble(o);
  }

  private float txStringToFloat(String o) {
    return Float.parseFloat(o);
  }

  private int txStringToInt(String o) {
    try {
      return Integer.parseInt(o);
    }
    catch (NumberFormatException nfe1) {
      return (int) Float.parseFloat(o);
    }
  }

  private long txStringToLong(String o) {
    try {
      return Long.parseLong(o);
    }
    catch (NumberFormatException nfe1) {
      return (long) Double.parseDouble(o);
    }
  }

  private short txStringToShort(String o) {
    try {
      return Short.parseShort(o);
    }
    catch (NumberFormatException nfe1) {
      return (short) Float.parseFloat(o);
    }
  }

  // Phase 2

  private BigDecimal txBigIntegerToBigDecimal(BigInteger o) {
    return new BigDecimal(o);
  }

  private boolean txBigIntegerToBoolean(BigInteger o) {
    return o.equals(BigInteger.ONE);
  }

  private byte txBigIntegerToByte(BigInteger o) {
    return o.byteValue();
  }

  private char txBigIntegerToChar(BigInteger o) {
    return (char) o.intValue();
  }

  private double txBigIntegerToDouble(BigInteger o) {
    return o.doubleValue();
  }

  private float txBigIntegerToFloat(BigInteger o) {
    return o.floatValue();
  }

  private int txBigIntegerToInt(BigInteger o) {
    return o.intValue();
  }

  private long txBigIntegerToLong(BigInteger o) {
    return o.longValue();
  }

  private short txBigIntegerToShort(BigInteger o) {
    return o.shortValue();
  }

  private String txBigIntegerToString(BigInteger o) {
    return o.toString();
  }

  private BigInteger txBigDecimalToBigInteger(BigDecimal o) {
    return o.toBigInteger();
  }

  private boolean txBigDecimalToBoolean(BigDecimal o) {
    return BigInteger.ONE.equals(o.toBigInteger());
  }

  private byte txBigDecimalToByte(BigDecimal o) {
    return o.byteValue();
  }

  private char txBigDecimalToChar(BigDecimal o) {
    return (char) o.intValue();
  }

  private double txBigDecimalToDouble(BigDecimal o) {
    return o.doubleValue();
  }

  private float txBigDecimalToFloat(BigDecimal o) {
    return o.floatValue();
  }

  private int txBigDecimalToInt(BigDecimal o) {
    return o.intValue();
  }

  private long txBigDecimalToLong(BigDecimal o) {
    return o.longValue();
  }

  private short txBigDecimalToShort(BigDecimal o) {
    return o.shortValue();
  }

  private String txBigDecimalToString(BigDecimal o) {
    return o.toString();
  }

  private BigDecimal txBooleanToBigDecimal(boolean o) {
    return new BigDecimal((o ? BigInteger.ONE : BigInteger.ZERO));
  }

  private BigInteger txBooleanToBigInteger(boolean o) {
    return (o ? BigInteger.ONE : BigInteger.ZERO);
  }

  private BigDecimal txByteToBigDecimal(byte o) {
    return BigDecimal.valueOf(o);
  }

  private BigInteger txByteToBigInteger(byte o) {
    return BigInteger.valueOf(o);
  }

  private Date txCalendarToDate(Calendar o) {
    return o.getTime();
  }

  private UTCDate txCalendarToUTCDate(Calendar o) {
    return new UTCDate(o.getTime().getTime());
  }

  private java.sql.Date txCalendarToSqlDate(Calendar o) {
    return new java.sql.Date(o.getTimeInMillis());
  }

  private Time txCalendarToSqlTime(Calendar o) {
    return new Time(o.getTimeInMillis());
  }

  private Timestamp txCalendarToSqlTimestamp(Calendar o) {
    return new Timestamp(o.getTimeInMillis());
  }

  private String txCalendarToString(Calendar o) {
    return new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).format(o.getTime());
  }

  private BigDecimal txCharToBigDecimal(char o) {
    return BigDecimal.valueOf(o);
  }

  private BigInteger txCharToBigInteger(char o) {
    return BigInteger.valueOf(o);
  }

  private Calendar txDateToCalendar(Date o) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(o);
    return cal;
  }

  private UTCDate txDateToUTCDate(Date o) {
    return new UTCDate(o.getTime());
  }

  private java.sql.Date txDateToSqlDate(Date o) {
    return new java.sql.Date(o.getTime());
  }

  private Time txDateToSqlTime(Date o) {
    return new Time(o.getTime());
  }

  private Timestamp txDateToSqlTimestamp(Date o) {
    return new Timestamp(o.getTime());
  }

  private String txDateToString(Date o) {
    return new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).format(o);
  }

  private Date txUTCDateToDate(UTCDate o) {
    return o;
  }

  private Calendar txUTCDateToCalendar(UTCDate o) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(o);
    return cal;
  }

  private java.sql.Date txUTCDateToSqlDate(UTCDate o) {
    return new java.sql.Date(o.getTime());
  }

  private Time txUTCDateToSqlTime(UTCDate o) {
    return new Time(o.getTime());
  }

  private Timestamp txUTCDateToSqlTimestamp(UTCDate o) {
    return new Timestamp(o.getTime());
  }

  private String txUTCDateToString(UTCDate o) {
    return new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).format(o);
  }

  private BigDecimal txDoubleToBigDecimal(double o) {
    return new BigDecimal(Double.toString(o));
  }

  private BigInteger txDoubleToBigInteger(double o) {
    return BigInteger.valueOf((long) o);
  }

  private BigDecimal txFloatToBigDecimal(float o) {
    return new BigDecimal(String.valueOf(o));
  }

  private BigInteger txFloatToBigInteger(float o) {
    return BigInteger.valueOf((long) o);
  }

  private BigDecimal txIntToBigDecimal(int o) {
    return BigDecimal.valueOf(o);
  }

  private BigInteger txIntToBigInteger(int o) {
    return BigInteger.valueOf(o);
  }

  private BigDecimal txLongToBigDecimal(long o) {
    return BigDecimal.valueOf(o);
  }

  private BigInteger txLongToBigInteger(long o) {
    return BigInteger.valueOf(o);
  }

  private BigDecimal txShortToBigDecimal(short o) {
    return BigDecimal.valueOf(o);
  }

  private BigInteger txShortToBigInteger(short o) {
    return BigInteger.valueOf(o);
  }

  private Calendar txSqlDateToCalendar(java.sql.Date o) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(o);
    return cal;
  }

  private Time txSqlDateToSqlTime(java.sql.Date o) {
    return new Time(o.getTime());
  }

  private Timestamp txSqlDateToSqlTimestamp(java.sql.Date o) {
    return new Timestamp(o.getTime());
  }

  private UTCDate txSqlDateToUTCDate(java.sql.Date o) {
    return new UTCDate(o.getTime());
  }

  private String txSqlDateToString(java.sql.Date o) {
    return new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).format(o);
  }

  private Calendar txSqlTimestampToCalendar(Timestamp o) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(o);
    return cal;
  }

  private java.sql.Date txSqlTimestampToSqlDate(Timestamp o) {
    return new java.sql.Date(o.getTime());
  }

  private Time txSqlTimestampToSqlTime(Timestamp o) {
    return new Time(o.getTime());
  }

  private UTCDate txSqlTimestampToUTCDate(Timestamp o) {
    return new UTCDate(o.getTime());
  }

  private String txSqlTimestampToString(Timestamp o) {
    return new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).format(o);
  }

  private Calendar txSqlTimeToCalendar(Time o) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(o);
    return cal;
  }

  private java.sql.Date txSqlTimeToSqlDate(Time o) {
    return new java.sql.Date(o.getTime());
  }

  private Timestamp txSqlTimeToSqlTimestamp(Time o) {
    return new Timestamp(o.getTime());
  }

  private UTCDate txSqlTimeToUTCDate(Time o) {
    return new UTCDate(o.getTime());
  }

  private String txSqlTimeToString(Time o) {
    return new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).format(o);
  }

  private Date txStringToDate(String o) {
    try {
      return new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).parse(o);
    }
    catch (ParseException pe) {
      throw createException(o, String.class, Date.class, 5, pe.getMessage());
    }
  }

  private UTCDate txStringToUTCDate(String o) {
    try {
      Date d = new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).parse(o);
      return new UTCDate(d.getTime());
    }
    catch (ParseException pe) {
      throw createException(o, String.class, UTCDate.class, 5, pe.getMessage());
    }
  }

  private Calendar txStringToCalendar(String o) {
    try {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).parse(o));
      return cal;
    }
    catch (ParseException pe) {
      throw createException(o, String.class, Calendar.class, 6, pe.getMessage());
    }
  }

  private java.sql.Date txStringToSqlDate(String o) {
    try {
      return new java.sql.Date(new SimpleDateFormat("dd.MM.yyyy").parse(o).getTime());
    }
    catch (ParseException pe) {
      throw createException(o, String.class, java.sql.Date.class, 7, pe.getMessage());
    }
  }

  private Time txStringToSqlTime(String o) {
    try {
      return new Time(new SimpleDateFormat("HH:mm:ss.sss").parse(o).getTime());
    }
    catch (ParseException pe) {
      throw createException(o, String.class, Time.class, 8, pe.getMessage());
    }
  }

  private Timestamp txStringToSqlTimestamp(String o) {
    try {
      return new Timestamp(new SimpleDateFormat(ZULU_DATE_TO_STRING_FORMAT).parse(o).getTime());
    }
    catch (ParseException pe) {
      throw createException(o, String.class, Timestamp.class, 8, pe.getMessage());
    }
  }

  private BigDecimal txStringToBigDecimal(String o) {
    return new BigDecimal(o);
  }

  private BigInteger txStringToBigInteger(String o) {
    return new BigDecimal(o).toBigInteger();
  }

  private byte txTriStateToByte(TriState o) {
    return o.isUndefined() ? -1 : o.getIntegerValue().byteValue();
  }

  private char txTriStateToChar(TriState o) {
    return o.isUndefined() ? '?' : o.getBooleanValue() ? 'X' : ' ';
  }

  private double txTriStateToDouble(TriState o) {
    return o.isUndefined() ? -1 : o.getIntegerValue().doubleValue();
  }

  private float txTriStateToFloat(TriState o) {
    return o.isUndefined() ? -1 : o.getIntegerValue().floatValue();
  }

  private int txTriStateToInt(TriState o) {
    return o.isUndefined() ? -1 : o.getIntegerValue().intValue();
  }

  private long txTriStateToLong(TriState o) {
    return o.isUndefined() ? -1 : o.getIntegerValue().longValue();
  }

  private short txTriStateToShort(TriState o) {
    return o.isUndefined() ? -1 : o.getIntegerValue().shortValue();
  }

  private String txTriStateToString(TriState o) {
    return o.toString();
  }

  private TriState txByteToTriState(byte o) {
    return TriState.parse(o);
  }

  private TriState txCharToTriState(char o) {
    return TriState.parse(o);
  }

  private TriState txDoubleToTriState(double o) {
    return TriState.parse(o);
  }

  private TriState txFloatToTriState(float o) {
    return TriState.parse(o);
  }

  private TriState txIntToTriState(int o) {
    return TriState.parse(o);
  }

  private TriState txLongToTriState(long o) {
    return TriState.parse(o);
  }

  private TriState txShortToTriState(short o) {
    return TriState.parse(o);
  }

  private TriState txStringToTriState(String o) {
    return TriState.parse(o);
  }

  private TriState txBigIntegerToTriState(BigInteger o) {
    return TriState.parse(o);
  }

  private TriState txBigDecimalToTriState(BigDecimal o) {
    return TriState.parse(o);
  }

  private BigDecimal txTriStateToBigDecimal(TriState o) {
    if (o.isUndefined()) {
      return null;
    }
    else if (o.getBooleanValue()) {
      return BigDecimal.ONE;
    }
    else {
      return BigDecimal.ZERO;
    }
  }

  private BigInteger txTriStateToBigInteger(TriState o) {
    if (o.isUndefined()) {
      return null;
    }
    else if (o.getBooleanValue()) {
      return BigInteger.ONE;
    }
    else {
      return BigInteger.ZERO;
    }
  }

  private TriState txBooleanToTriState(boolean o) {
    return TriState.parse(o);
  }

  private boolean txTriStateToBoolean(TriState o) {
    return o.isUndefined() ? false : o.getBooleanValue().booleanValue();
  }

  @SuppressWarnings("squid:S00116")
  private static class TypeDesc {
    int parameterizedTypeIndex;
    TypeVariable<?> typeVariable;
    Class resolvedClazz;
    int arrayDim;
  }

  /**
   * key for GenericsParameterClass with weak references so to allow for class unloading
   */
  private static class GPCKey {
    private final Class<?> m_queryClass;
    private final Class<?> m_genericsOwnerClass;
    private final int m_genericsParameterIndex;

    public GPCKey(Class queryClass, Class genericsOwnerClass, int genericsParameterIndex) {
      m_queryClass = queryClass;
      m_genericsOwnerClass = genericsOwnerClass;
      m_genericsParameterIndex = genericsParameterIndex;
    }

    @Override
    public int hashCode() {
      return (m_queryClass != null ? m_queryClass.hashCode() : 0) ^ (m_genericsOwnerClass != null ? m_genericsOwnerClass.hashCode() : 0) ^ m_genericsParameterIndex;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      GPCKey o = (GPCKey) obj;
      return (this.m_queryClass == o.m_queryClass) && (this.m_genericsOwnerClass == o.m_genericsOwnerClass) && (this.m_genericsParameterIndex == o.m_genericsParameterIndex);
    }
  }

  /**
   * {@link TypeCastUtility#getGenericsParameterClass(Class, Class, int)}
   */
  public static Class getGenericsParameterClass(Class queryClass, Class genericsOwnerClass) {
    return instance.getGenericsParameterClassImpl(queryClass, genericsOwnerClass, 0);
  }

  /**
   * @return the actual (declared) type of the generics parameter with index genericsParameterIndex on
   *         genericsOwnerClass.
   *         <p>
   *         The generics parameter is defined on the genericsOwnerClass
   *         <p>
   *         The actual type must be declared on the queryClass
   *         <p>
   *         Example: queryType=LongArrayHolder, genericsOwnerClass=IHolder.class, genericsParameterIndex=0 since
   *         IHolder has only one, returns Long[].class
   */
  public static Class getGenericsParameterClass(Class queryClass, Class genericsOwnerClass, int genericsParameterIndex) {
    return instance.getGenericsParameterClassImpl(queryClass, genericsOwnerClass, genericsParameterIndex);
  }

  private Class getGenericsParameterClassImpl(Class queryClass, Class genericsOwnerClass, int genericsParameterIndex) {
    GPCKey key = new GPCKey(queryClass, genericsOwnerClass, genericsParameterIndex);
    Class result = m_genericsParameterClassCache.get(key);
    if (result != null) {
      return result;
    }
    //
    final boolean debugEnabled = LOG.isDebugEnabled();
    if (debugEnabled) {
      LOG.debug("queryClass={}, genericsOwnerClass={}, genericsParameterIndex={}", queryClass, genericsOwnerClass, genericsParameterIndex);
    }
    TypeDesc desc = new TypeDesc();
    Set<Type> loopDetector = new HashSet<>();
    visitGenericsHierarchy(queryClass, genericsOwnerClass, genericsParameterIndex, desc, loopDetector, debugEnabled);
    if (desc.resolvedClazz == null) {
      StringBuilder s = new StringBuilder(desc.typeVariable != null ? desc.typeVariable.getName() : "null");

      for (int i = 0; i < desc.arrayDim; i++) {
        s.append("[]");
      }
      throw new IllegalArgumentException("cannot resolve " + s + " to a 'Class' type on " + queryClass);
    }
    if (desc.arrayDim > 0) {
      result = Array.newInstance(desc.resolvedClazz, new int[desc.arrayDim]).getClass();
    }
    else {
      result = desc.resolvedClazz;
    }
    m_genericsParameterClassCache.put(key, result);
    return result;
  }

  private boolean/* foundStopType */ visitGenericsHierarchy(Type type, Class<?> stopType, int stopTypeGenericsParameterIndex, TypeDesc desc, Set<Type> loopDetector, boolean debugEnabled) {
    if (loopDetector.contains(type)) {
      return false;
    }
    if (type instanceof Class) {
      Class c = (Class) type;
      if (c == stopType) {
        //stop class, set generics index
        desc.parameterizedTypeIndex = stopTypeGenericsParameterIndex;
        if (debugEnabled) {
          LOG.debug("foundStopClass {}, using generics index {}", c, desc.parameterizedTypeIndex);
        }
        return true;
      }
      boolean foundStopClass = false;
      if (!c.isInterface()) {
        Type a = c.getGenericSuperclass();
        if (a != null) {
          try {
            loopDetector.add(type);
            foundStopClass = visitGenericsHierarchy(a, stopType, stopTypeGenericsParameterIndex, desc, loopDetector, debugEnabled);
          }
          finally {
            loopDetector.remove(type);
          }
        }
      }
      if (!foundStopClass) {
        Type[] a = c.getGenericInterfaces();
        if (a.length > 0) {
          for (int i = 0; i < a.length && !foundStopClass; i++) {
            try {
              loopDetector.add(type);
              foundStopClass = visitGenericsHierarchy(a[i], stopType, stopTypeGenericsParameterIndex, desc, loopDetector, debugEnabled);
            }
            finally {
              loopDetector.remove(type);
            }
          }
        }
      }
      if (foundStopClass) {
        if (debugEnabled) {
          LOG.debug("visit {}", VerboseUtility.dumpGenerics(c));
        }
        if (desc.resolvedClazz == null && desc.typeVariable != null) {
          //find the index of the typeParameter
          desc.parameterizedTypeIndex = -1;
          TypeVariable<?>[] vars = c.getTypeParameters();
          for (int i = 0; i < vars.length; i++) {
            if (Objects.equals(vars[i], desc.typeVariable)) {
              if (debugEnabled) {
                LOG.debug("{} has index {}", desc.typeVariable, i);
              }
              desc.parameterizedTypeIndex = i;
              break;
            }
          }
          if (desc.parameterizedTypeIndex == -1) {
            throw new IllegalStateException("Unable to resolve type variable " + desc.typeVariable + " for " + c + ".");
          }
        }
        return true;
      }
      return false;
    }
    //
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      Type rawType = pt.getRawType();
      try {
        loopDetector.add(pt);
        boolean b = visitGenericsHierarchy(rawType, stopType, stopTypeGenericsParameterIndex, desc, loopDetector, debugEnabled);
        if (!b) {
          return false;
        }
      }
      finally {
        loopDetector.remove(pt);
      }
      //found path to stopClass
      if (debugEnabled) {
        LOG.debug("visit {}", VerboseUtility.dumpGenerics(pt));
      }
      if (desc.resolvedClazz == null) {
        //find next type variable
        Type actualArg = pt.getActualTypeArguments()[desc.parameterizedTypeIndex];
        if (debugEnabled) {
          LOG.debug("index {} matches to {}", desc.parameterizedTypeIndex, actualArg);
        }
        while (actualArg instanceof GenericArrayType) {
          //it is something like <T[]>
          actualArg = ((GenericArrayType) actualArg).getGenericComponentType();
          desc.arrayDim++;
        }
        if (actualArg instanceof ParameterizedType) {
          //it is something like <Map<String,String>>
          actualArg = ((ParameterizedType) actualArg).getRawType();
        }
        if (actualArg instanceof Class) {
          desc.resolvedClazz = (Class) actualArg;
        }
        else if (actualArg instanceof TypeVariable) {
          desc.typeVariable = (TypeVariable) actualArg;
        }
        else {
          if (debugEnabled) {
            LOG.debug("failed with {}", actualArg);
          }
          throw new IllegalArgumentException("expected ParameterizedType with actual argument of type Class or TypeVariable when encountered " + VerboseUtility.dumpGenerics(type));
        }
      }
      return true;
    }
    return false;
  }
}
