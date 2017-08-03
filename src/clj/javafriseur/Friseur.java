package clj.javafriseur;

public class Friseur extends Person {

  public Friseur(String name) {
    super(name);
  }
  
  public static void main(String[] args) {
    
    Person anna = new Frau("Anna");
    Person benno = new Mann("Benno");
    
    anna.spricht();
    benno.spricht();
    
    Friseur clara = new Damenfriseur("Clara");
    Friseur dirk = new Herrenfriseur("Dirk");
    
    //clara.frisiert(anna);
    //The method frisiert(Person) is undefined for the type Friseur
    
    Damenfriseur erna = new Damenfriseur("Erna");
    
    //erna.frisiert(anna);
    //The method frisiert(Frau) in the type Damenfriseur 
    //  is not applicable for the arguments (Person)
  }
}
