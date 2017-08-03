package clj.java;

import java.util.Stack;

public class JavaStack {

  public static void main(String[] args) {

    Stack<Integer> st = new Stack<Integer>();
    System.out.println( "Stack: " + st );
    
    st.push(1);
    System.out.println( "Stack: " + st );
    
    st.push(2);
    System.out.println( "Stack: " + st );
    
    System.out.println( "Oben drauf: " + st.peek() );
    
    System.out.println( "Oben drauf (eben noch): " + st.pop() );
    
    System.out.println( "Oben drauf: " + st.peek() );
    
    System.out.println( "Stack: " + st );
    
  }

}
