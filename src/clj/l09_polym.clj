; Programmierung in Clojure Vorlesung 9
; Polymorphismus - Multimethod und Protocol/Record
; (c) 2014 - 2017 by Burkhardt Renz, THM

(ns clj.l09-polym
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Polymorphismus - Multimethod und Protocol/Record")

;------------------------------------------------------------------------
(pres :add "
# Polymorphismus

- Dynamischer Polymorphismus
- Dispatch in Java oder C++ über den Typ des Objekts, d.h.
  nach _einem_ Kriterium
- Beispiel Entwurfsmuster _Visitor_ für einen Syntaxbaum:     
    - Jeder Knoten des Baums hat eine Methode `accept`, die einen Visitor
      als Parameter hat
    - Jeder Visitor braucht eine Methode `visit` pro Typ des Knotens des Baums  
    - Leicht: neuen Visitor hinzufügen
    - Schwer: neuer Knotentyp, weil alle Visitoren angepasst werden müssen
    - Grund: Java kann nur nach dem Typ von this dispatchen, nicht nach dem ersten
      Parameter zusätzlich
- Fragestellung:     
  Wie kann man einen vorhandenen Typ erweitern, ohne ihn zu verändern?
- in Clojure:   
  Multimethods und Records/Protocols
  
(Darstellung inspiriert durch eine [Präsentation](http://www.infoq.com/presentations/Clojure-Expression-Problem)  
von Chris Houser)
")

; Jeder Knoten implementiert Node::accept(visitor) und ruft in der Methode
; auf: visitor.visit(this)
; Jeder Visitor muss als pro Knotenklasse eine eigene Methode
; Visitor::visit(concreteNode) haben

;------------------------------------------------------------------------
(pres "
# Klasse Kunde

Angenommen wir haben eine Klasse _Kunde_, etwa so:

      public class Kunde {
        public int    kndNr;
        public String name;
        public String vorname;
        
        public Kunde(int kndNr, String name, String vorname) {
          this.kndNr = kndNr;
          this.name = name;
          this.vorname = vorname;
        } 
      }
      
Wir wollen Listen von Kunden in HTML ausgeben      

siehe `Kunde.java` und `poly.html` im Package `javapoly`
")

(pres :add "
## Weitere Anforderungen:

- Wir wollen nicht nur Kunden, sondern auch Artikel in  HTML ausgeben
- Und nicht nur das: wir möchten auch eine Liste von Vektoren in HTML ausgeben
- Aber: Vektor ist eine Klasse, deren Quellcode wir nicht verändern können
")

;------------------------------------------------------------------------
(pres "
# Das Erweiterungsproblem ( _expression problem_ )

''The Expression Problem is a new name for an old problem. 
The goal is to define a datatype by cases, where one can add new cases 
to the datatype and new functions over the datatype, without recompiling 
existing code, and while retaining static type safety (e.g., no casts).''     
Philip Wadler 1998

## Aufgaben

1. Erweiterung einer gegebenen Funktionalität auf einen neuen Typ von Objekten
2. Erweiterung einer gegebenen Menge von Typen mit einer neuen Funktionalität


In objekt-orientierten Sprachen ist die erste Aufgabe einfach:   
Man definiert die Funktionalität in einem Interface und der neue Typ muss es erfüllen.

Die zweite Aufgabe jedoch ist aufwändig:    
Erweitert man das Interface, muss man jede Klasse anpassen, die es erfüllen soll.
")

;------------------------------------------------------------------------
(pres "
# Ansätze, die Aufgabe in Java zu lösen

## Ansatz 1: Methode in der Klasse `Kunde`

- Methode `toHtml` siehe `Kunde1.java` und `Poly1.java`
- Problem 1: `toHtml` ist kontextabhängig
- Problem 2: nicht in Klasse `Artikel` wiederverwendbar
")

(pres :add "
## Ansatz 2: Interface

- Interface `RowInfo` siehe `Kunde2.java`, `Artikel2.java` und `Poly2.java`
- `printHtmlTable` kann nun für Kunden und Artikel verwendet werden
- Problem: wir müssen die Klassen „aufbohren“ - wie auf Vektor erweitern?
")

(pres "
# Ansätze, die Aufgabe in Java zu lösen

## Ansatz 3: Adapter für Vektor
- ''Convert the interface of a class into another interface clients expect. 
  Adapter lets classes work together that couldn’t otherwise because of 
  incompatible interfaces.'' (GoF)
- Also: Adapter für Vektor, siehe `VectorAdapter.java` und `Poly3.java`  
- Problem: die Objekte, die wir verwenden, sind jetzt keine Vektoren mehr!
")

(pres "
# Ansätze, die Aufgabe in Java zu lösen

## Ansatz 4: Monkey Patch

''In Python, the term monkey patch only refers to dynamic modifications 
of a class or module at runtime, motivated by the intent to patch 
existing third-party code as a workaround to a bug or feature which 
does not act as desired.'' (Wikipedia)
  
Also übertragen für Java etwas, was in Java syntaktisch nicht geht:
  
      patch class Vector<T> implements RowInfo {
      
         @Override
         public List<String> getFldNames() {
           List<String> result = new Vector<String>();
           for (int i = 0; i < this.size(); i++){....}
           
         @Override
         public String get(int index) {
           return this.get(index).toString();}
      }  


- Geht nicht in Java
- Ferner: welches `get` soll aufgerufen werden?      
  Das von Vector oder das der gepatchten Klasse??
")

(pres "
# Fazit der Versuche

- Ansatz 1: untauglich
- Ansatz 2: brauchbar, aber nur wenn man den Code der Klassen ändern kann
- Ansatz 3: Adapter unschön
- Ansatz 4: was ein Hack!
- Geht das in Clojure besser?

")

;------------------------------------------------------------------------
(pres "
# Multimethods in Clojure

- Lösung für das Beispiel siehe `poly-multi.clj`

![Multimethod](Resources/multimethod.jpg)

")

; aus der Dokumentation von Clojure

(doc defmulti)

(doc defmethod)

;------------------------------------------------------------------------
(pres "
# Records and Protocols in Clojure

- Lösung für das Beispiel siehe `poly-protocol.clj`


## Records

- ''defrecord provides a complete implementation of a persistent map, including:
    - value-based equality and hashCode
    - metadata support
    - associative support
    - keyword accessors for fields
    - extensible fields (you can assoc keys not supplied with the defrecord definition)
    - ...''     
(aus der Dokumentation von Clojure)

## Protocols

- Ein _protocol_ ist eine definierte Menge von Methoden mit ihren Signaturen
- Intern wird ein Java-Interface erzeugt
- Ein Protokoll kann durch unterschiedliche Typen erfüllt werden
- Die Funktionen werden auf Basis des Typs des ersten Arguments ( _this_ ) polymorph ausgeführt.
- Makros zur Implementierung von Protokollen
    - `extend-protocol`
    - `extend-type`

")

(doc extend-protocol)

(doc extend-type)

;------------------------------------------------------------------------
(pres "
# Fazit

- _Multimethod_    
    - erlaubt beliebigen Polymorphismus nach benutzerdefinierten Kriterien
    - hat relativ hohen Laufzeitaufwand
- _Records_ und _Protocols_
    - verwenden Mechanismus der Plattform mit Polymorphismus auf Basis des Typs von Objekten
    - Objekte haben aber trotzdem Werte-Semantik
    - sind schneller als Multimethods
- Von Maps zu Records
    - Hat man eine Funktion, die Maps aus Werten konstruiert, muss man nur diesen Konstruktor 
      ändern, um Records zu verwenden
    - Der Code für den Benutzer der Map bleibt nach der Änderung gleich  
    
")
