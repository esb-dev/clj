package clj.javafriseur;

public class Damenfriseur extends Friseur {

  public Damenfriseur(String name) {
    super(name);
  }
  
  public void frisiert(Frau f) {
    System.out.println("Damenfriseur " + name + " frisiert Frau " + f.name + ".");
  }  

}
