package clj.javafriseur;

public class Person {

  public String name;

  public Person(String name) {
    this.name = name;
  }	
  
  void spricht() {
    System.out.println("Ich bin " + name + "!");
  }  
}
