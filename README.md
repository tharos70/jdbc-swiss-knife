![logo](/assets/img/logo.png)

# jdbc-swiss-knife
> A simple set of utilities for handling jdbc accessible metadata. 

It includes:
 - Database metadata reader
 - Database metadata based java code generator supporting:
   - spring-jdbc Dao basic implementation
   - spring-jdbc Dao advanced implementation (COMING SOON)
 - Database metadata based yaml skeleton generator (COMING SOON)
  
> So far, the only supported database is [Posgresql](https://www.postgresql.org/). Anyway, it's quite easy to extend the support to othes jdbc compliant platforms

## Installing / Getting started

Simply add the following snippet to you `pom.xml`

```xml
<dependency>
  <groupId>com.tharos</groupId>
  <artifactId>jdbc-swiss-knife</artifactId>
  <version>${version}</version>
</dependency>
```

## How to

### - Read metadata from DB

```java
DatabaseMetadataExtractor dbme = new DatabaseMetadataExtractor(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "user",
      "password",
      "schema-name"
    );
//Retrieves all tables in schema    
ArrayList<Table> tableList = dbme.getTablesList(); 
....
//Retrieves all columns for tableName    
ArrayList<Column> columnList = dbme.extractColumnsInfo("tableName");
```

### - Generate spring-jdbc framework based Dao Classes

```java
...
Table t = dbme.getTablesList().get(0);
new DaoPatternStrategy()
      .generate(
          t,
          "base.package" // base package for generated classes
        );
```

## Developing

If you want to extend this library just run the following command

```shell
git clone https://github.com/tharos70/jdbc-swiss-knife.git
```

### Building

In order to build the project just install [maven](https://maven.apache.org/) and a java 11 compatible sdk (such as [adoptOpenJdk](https://adoptopenjdk.net/)) in your system and run the following command

```shell
cd jdbc-swiss-knife
mvn clean install
```

Now you can add you own functionality to the library with your favourite IDE.

## Contributing

If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.

## Links

- Project homepage: https://github.com/tharos70/jdbc-swiss-knife
- Repository: https://github.com/tharos70/jdbc-swiss-knife
- Issue tracker: https://github.com/tharos70/jdbc-swiss-knife/issues
  - In case of sensitive bugs like security vulnerabilities, please contact
    tharos70@gmail.com directly instead of using issue tracker. We value your effort to improve the security and privacy of this project!
- Related projects:
  
> This project relies on the amazing [Javapoet](https://github.com/square/javapoet) library. A huge thanks to *square* for creating such an amazing tool.

## Project status

This project is considered *under construction* so use it carefully and at your own risk.

## Licensing

All the code in this project is licensed under Apache License 2.0 license.