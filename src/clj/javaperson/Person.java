package clj.javaperson;

import java.time.LocalDate;
import java.time.Month;

/**
 * Created by br on 30.06.16.
 */
public class Person {

    private String name;
    private String vorname;
    private LocalDate gebdat;


    public Person(String name, String vorname, LocalDate gebdat) {
        this.name = name;
        this.vorname = vorname;
        this.gebdat = gebdat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public LocalDate getGebdat() {
        return gebdat;
    }

    public void setGebdat(LocalDate gebdat) {
        this.gebdat = gebdat;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", vorname='" + vorname + '\'' +
                ", gebdat=" + gebdat +
                '}';
    }

    public static void main(String args[]) {

        Person p = new Person("Bloch", "Joshua", LocalDate.of(1961, Month.AUGUST, 28));

        System.out.println(p.toString());

        // so etwas geht nicht, schon der Editor warnt mich
        //Person x = new Person(112, "Joshua", "Happy birthday");
    }
}



