package org.tharos.jdbc.swissknife.core;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts database types to Java class types.
 */
public class SQLTypeMap {

  /**
   * Translates a data type from an integer (java.sql.Types value) to a string
   * that represents the corresponding class.
   *
   * @param type
   *            The java.sql.Types value to convert to its corresponding class.
   * @return The class that corresponds to the given java.sql.Types
   *         value, or Object.class if the type has no known mapping.
   */
  public static Class<?> toClass(int type) {
    Class<?> result = Object.class;

    switch (type) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
        result = String.class;
        break;
      case Types.NUMERIC:
      case Types.DECIMAL:
        result = java.math.BigDecimal.class;
        break;
      case Types.BIT:
        result = Boolean.class;
        break;
      case Types.TINYINT:
        result = Byte.class;
        break;
      case Types.SMALLINT:
        result = Short.class;
        break;
      case Types.INTEGER:
        result = Integer.class;
        break;
      case Types.BIGINT:
        result = Long.class;
        break;
      case Types.REAL:
      case Types.FLOAT:
        result = Float.class;
        break;
      case Types.DOUBLE:
        result = Double.class;
        break;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        result = Byte[].class;
        break;
      case Types.DATE:
        result = java.sql.Date.class;
        break;
      case Types.TIME:
        result = java.sql.Time.class;
        break;
      case Types.TIMESTAMP:
        result = java.sql.Timestamp.class;
        break;
    }

    return result;
  }

  private static Map<Integer, String> jdbcTypeNames = new HashMap<Integer, String>();

  public static Map<Integer, String> getAllJdbcTypeNames()
    throws IllegalArgumentException, IllegalAccessException {
    if (SQLTypeMap.jdbcTypeNames.isEmpty()) {
      for (Field field : Types.class.getFields()) {
        SQLTypeMap.jdbcTypeNames.put(
          (Integer) field.get(null),
          field.getName()
        );
      }
    }
    return SQLTypeMap.jdbcTypeNames;
  }
}
