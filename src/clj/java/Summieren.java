package clj.java;

public class Summieren {

  public static void main(String[] args) {
    
    int summe = 0;
    
    for ( int i = 1; i <= 10; i++ ) {
      summe = summe + i;
    }
    
    System.out.println( "Summe: " + summe );
  }
}
