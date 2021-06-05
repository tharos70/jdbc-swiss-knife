![logo](https://raw.githubusercontent.com/tharos70/jdbc-swiss-knife/master/.github/images/logo.png)

# jdbc-swiss-knife
> A simple set of utilities for handling jdbc accessible metadata. 

It includes:
 - metadata reader
 - metadata based java code generator
 - metadata based yaml skeleton generator

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

Now you can extend the library on your favourite IDE

## Contributing

If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.

## Links

Even though this information can be found inside the project on machine-readable
format like in a .json file, it's good to include a summary of most useful
links to humans using your project. You can include links like:

- Project homepage: https://your.github.com/awesome-project/
- Repository: https://github.com/your/awesome-project/
- Issue tracker: https://github.com/your/awesome-project/issues
  - In case of sensitive bugs like security vulnerabilities, please contact
    my@email.com directly instead of using issue tracker. We value your effort
    to improve the security and privacy of this project!
- Related projects:
  - Your other project: https://github.com/your/other-project/
  - Someone else's project: https://github.com/someones/awesome-project/


## Licensing

One really important part: Give your project a proper license. Here you should
state what the license is and how to find the text version of the license.
Something like:

"The code in this project is licensed under MIT license."