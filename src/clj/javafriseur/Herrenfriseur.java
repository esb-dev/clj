package clj.javafriseur;

public class Herrenfriseur extends Friseur {

  public Herrenfriseur(String name) {
    super(name);
  }
  
  public void frisiert(Mann m) {
    System.out.println("Herrenfriseur " + name + " frisiert Herrn " + m.name + ".");
  }  

}
