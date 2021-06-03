package org.tharos.jdbc.swissknife.dto;

public class Column {

  private String name;
  private Class<?> type;
  private boolean nullable;
  private Integer size;
  private Integer decimalDigits;
  private boolean autoincrement;
  private boolean isPrimaryKey;
  private boolean isForeignKey;
  private String foreignTableName;
  private String foreignColumnName;

  public String getForeignTableName() {
    return foreignTableName;
  }

  public void setForeignTableName(String foreignTableName) {
    this.foreignTableName = foreignTableName;
  }

  public String getForeignColumnName() {
    return foreignColumnName;
  }

  public void setForeignColumnName(String foreignColumnName) {
    this.foreignColumnName = foreignColumnName;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getDecimalDigits() {
    return decimalDigits;
  }

  public void setDecimalDigits(Integer decimalDigits) {
    this.decimalDigits = decimalDigits;
  }

  public boolean isAutoincrement() {
    return autoincrement;
  }

  public void setAutoincrement(boolean autoincrement) {
    this.autoincrement = autoincrement;
  }

  public boolean isPrimaryKey() {
    return isPrimaryKey;
  }

  public void setPrimaryKey(boolean isPrimaryKey) {
    this.isPrimaryKey = isPrimaryKey;
  }

  public boolean isForeignKey() {
    return isForeignKey;
  }

  public void setForeignKey(boolean isForeignKey) {
    this.isForeignKey = isForeignKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Class<?> getType() {
    return type;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\t\tNAME [" + getName() + "]\n");
    sb.append("\t\tTYPE [" + getType() + "]\n");
    sb.append("\t\tSIZE [" + getSize() + "]\n");
    sb.append("\t\tDECIMAL DIGITS [" + getDecimalDigits() + "]\n");
    sb.append("\t\tNULLABLE [" + isNullable() + "]\n");
    sb.append("\t\tAUTOINCREMENT [" + isAutoincrement() + "]\n");
    sb.append("\t\tPRIMARYKEY [" + isPrimaryKey() + "]\n");
    sb.append("\t\tFOREIGN KEY [" + isForeignKey() + "]\n");
    sb.append(
      "\t\tFOREIGN INFO [" +
      getForeignTableName() +
      "." +
      getForeignColumnName() +
      "]\n"
    );
    return sb.toString();
  }
}
