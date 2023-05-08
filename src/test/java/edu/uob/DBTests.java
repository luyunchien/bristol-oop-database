package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

// PLEASE READ:
// The tests in this file will fail by default for a template skeleton, your job is to pass them
// and maybe write some more, read up on how to write tests at
// https://junit.org/junit5/docs/current/user-guide/#writing-tests
final class DBTests {

  private DBServer server;

  // we make a new server for every @Test (i.e. this method runs before every @Test test case)
  @BeforeEach
  void setup(@TempDir File dbDir) {
    // Notice the @TempDir annotation, this instructs JUnit to create a new temp directory somewhere
    // and proceeds to *delete* that directory when the test finishes.
    // You can read the specifics of this at
    // https://junit.org/junit5/docs/5.4.2/api/org/junit/jupiter/api/io/TempDir.html

    // If you want to inspect the content of the directory during/after a test run for debugging,
    // simply replace `dbDir` here with your own File instance that points to somewhere you know.
    // IMPORTANT: If you do this, make sure you rerun the tests using `dbDir` again to make sure it
    // still works and keep it that way for the submission.

    server = new DBServer(dbDir);
  }

  // Here's a basic test for spawning a new server and sending an invalid command,
  // the spec dictates that the server respond with something that starts with `[ERROR]`
  @Test
  void testInvalidCommandIsAnError() {
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("CREATE DATABASE db;").startsWith("[OK]"));

    //the database being created already exists
    assertTrue(server.handleCommand("CREATE DATABASE db;").startsWith("[ERROR]"));

    assertTrue(server.handleCommand("USE db;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks(name, grade);").startsWith("[OK]"));

    //the table being created already exists
    assertTrue(server.handleCommand("CREATE TABLE marks;").startsWith("[ERROR]"));

    //the number of values being inserted is different from the number of attributes in the table
    assertTrue(server.handleCommand("INSERT INTO marks VALUES('Bob');").startsWith("[ERROR]"));

    //TABLE crew does not exist
    assertTrue(server.handleCommand("SELECT * FROM crew;").startsWith("[ERROR]"));

    //'WHERE' is missing in grammar
    assertTrue(server.handleCommand("SELECT * FROM marks pass == TRUE;").startsWith("[ERROR]"));

    //semicolon is missing
    assertTrue(server.handleCommand("SELECT * FROM marks").startsWith("[ERROR]"));

    //TABLE name is missing
    assertTrue(server.handleCommand("SELECT * FROM;").startsWith("[ERROR]"));

  }

  // Add more unit tests or integration tests here.
  // Unit tests would test individual methods or classes whereas integration tests are geared
  // towards a specific usecase (i.e. creating a table and inserting rows and asserting whether the
  // rows are actually inserted)


  @Test
  void testTranscript(){
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE name != 'Dave';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE pass == TRUE;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE coursework(task, grade);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('DB', 1);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 4);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('STAG', 2);").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM coursework;").startsWith("[OK]"));
    assertTrue(server.handleCommand("JOIN coursework AND marks ON grade AND id;").startsWith("[OK]"));
    assertTrue(server.handleCommand("UPDATE marks SET mark = 38 WHERE name == 'Clive';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE name == 'Clive';").startsWith("[OK]"));
    assertTrue(server.handleCommand("DELETE FROM marks WHERE name == 'Dave';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE name LIKE 've';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT id FROM marks WHERE pass == FALSE;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT name FROM marks WHERE mark>60;").startsWith("[OK]"));
    assertTrue(server.handleCommand("DELETE FROM marks WHERE mark<40;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks;").startsWith("[OK]"));

  }


  @Test
  void testIfMaxIDIsCorrect() throws FileNotFoundException {
    //create and use DATABASE data
    assertTrue(server.handleCommand("CREATE DATABASE data;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE data;").startsWith("[OK]"));

    //create TABLE people
    assertTrue(server.handleCommand("CREATE TABLE people (name, age, email);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO people VALUES ('Bob', 21, 'bob@bob.net');").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO people VALUES ('Harry', 32, 'harry@harry.com');").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO people VALUES ('Chris', 42, 'chris@chris.ac.uk');").startsWith("[OK]"));

    //test condition
    assertTrue(server.handleCommand("SELECT * FROM people WHERE (id>1) and (age<=40);").startsWith("[OK]"));

    //test string comparison
    assertTrue(server.handleCommand("SELECT * FROM people WHERE (name>'Bob') and (age<=40);").startsWith("[OK]"));

    //test nested condition
    assertTrue(server.handleCommand("SELECT * FROM people WHERE ((id==1) and (Name=='Bob')) or (id == 2);").startsWith("[OK]"));

    //delete the last row of the table (id==3)
    assertTrue(server.handleCommand("DELETE FROM people WHERE id == 3;").startsWith("[OK]"));

    //insert into a new row
    assertTrue(server.handleCommand("INSERT INTO people VALUES ('Sam', 22, 'sam@gmail.com');").startsWith("[OK]"));

    //test if the id of the newly inserted row is 4
    File fileToRead = new File(server.getRootBase().getCurrentTablePath());
    FileReader reader = new FileReader(fileToRead);
    BufferedReader br = new BufferedReader(reader);
    String line = null;
    try {
      line = br.readLine();
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    assert line != null;
    String[] attribute = line.split("\\t");
    int maxID = Integer.parseInt(attribute[0].substring(2));
    assertEquals(4, maxID);


    assertTrue(server.handleCommand("SELECT * FROM people;").startsWith("[OK]"));
    //drop TABLE people
    assertTrue(server.handleCommand("DROP TABLE people;").startsWith("[OK]"));
    //check if TABLE people is successfully dropped
    assertTrue(server.handleCommand("SELECT * FROM people;").startsWith("[ERROR]"));


  }

  @Test
  void testDropDatabase(){
    //check DATABASE data does not exist
    assertTrue(server.handleCommand("USE data;").startsWith("[ERROR]"));

    //create DATABASE data and drop it
    assertTrue(server.handleCommand("create DATABASE data;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE data;").startsWith("[OK]"));
    assertTrue(server.handleCommand("DROP DATABASE data;").startsWith("[OK]"));

    //check if DATABASE data is successfully dropped
    assertTrue(server.handleCommand("USE data;").startsWith("[ERROR]"));
  }


  @Test
  void testAlterAndUpdate(){
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));

    //create table marks
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks;").startsWith("[OK]"));

    //create table grade
    assertTrue(server.handleCommand("CREATE TABLE coursework(task, grade);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('DB', 1);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 4);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('STAG', 2);").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM coursework;").startsWith("[OK]"));

    assertTrue(server.handleCommand("ALTER TABLE marks ADD class;").startsWith("[OK]"));

    //test command with tab
    assertTrue(server.handleCommand("SELECT *   FROM marks;").startsWith("[OK]"));

    assertTrue(server.handleCommand("ALTER TABLE marks DROP class;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks;").startsWith("[OK]"));
    assertTrue(server.handleCommand("delete from marks where id==4;").startsWith("[OK]"));
    assertTrue(server.handleCommand("insert into marks values('Clive', 20, FALSE);").startsWith("[OK]"));

    //test nested condition
    assertTrue(server.handleCommand("select * from marks where ((id>1) and (name=='Dave')) or ((mark<=35) and (id==3));").startsWith("[OK]"));
    //test like operator
    assertTrue(server.handleCommand("select * from marks where name like 've';").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE marks ADD class;").startsWith("[OK]"));
    //update all the classes
    assertTrue(server.handleCommand("UPDATE marks set class='CS2022' where id>=1 ;").startsWith("[OK]"));
    assertTrue(server.handleCommand("UPDATE marks set class='CS2021' where name>'Dave';").startsWith("[OK]"));
    assertTrue(server.handleCommand("select * from marks;").startsWith("[OK]"));

    //test invalid condition
    assertTrue(server.handleCommand("select * from marks where id==4 and name=='Steve';").startsWith("[ERROR]"));
    //test nested condition
    assertTrue(server.handleCommand("select * from marks WHERE ((mark>+30.99)AND(mark<=+60)) AND (( (id==2)OR(pass!=TRUE) )AND(name LIKE 've'));").startsWith("[OK]"));
    //null cannot be compared
    assertTrue(server.handleCommand("SELECT * FROM marks where name>null;").startsWith("[ERROR]"));
    //string and number cannot be compared
    assertTrue(server.handleCommand("SELECT * FROM marks where name>6;").startsWith("[ERROR]"));
    //test float number
    assertTrue(server.handleCommand("SELECT * FROM marks where mark > 35.5;").startsWith("[OK]"));
    //cannot add an attribute that already exists in the table
    assertTrue(server.handleCommand("alter table marks add name;").startsWith("[ERROR]"));
    //cannot drop id
    assertTrue(server.handleCommand("alter table marks drop id;").startsWith("[ERROR]"));



  }


  @Test
  void testJoin() {
    //initialize database and table
    assertTrue(server.handleCommand("CREATE DATABASE db;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE db;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE people (name, age, email);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO people VALUES ('Bob', 21, 'bob@bob.net');").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO people VALUES ('Harry', 32, 'harry@harry.com');").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO people VALUES ('Chris', 42, 'chris@chris.ac.uk');").startsWith("[OK]"));

    assertTrue(server.handleCommand("CREATE TABLE sheds (name, height, purchaserID);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO sheds VALUES('Dorchester', 1800, 3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO sheds VALUES('Plaza', 1200, 1);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO sheds VALUES('Excelsior', 1000, 2);").startsWith("[OK]"));

    assertTrue(server.handleCommand("join people and sheds on id and purchaserID;").startsWith("[OK]"));

    //test attribute does not exist in table
    assertTrue(server.handleCommand("JOIN people and sheds ON height AND id;").startsWith("[ERROR]"));
    //test table does not exist
    assertTrue(server.handleCommand("JOIN marks and sheds ON height AND id;").startsWith("[ERROR]"));

  }


}
