; Programmierung in Clojure Vorlesung 2
; Einführung in Clojure
; (c) 2013 - 2017 by Burkhardt Renz, THM

(ns clj.l02-intro
  (:require [clj.presentation :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Einführung in Clojure")

; -------------------------------------------------------------------------------------------------
(pres :add "
#Warum Clojure?
 
Lassen wir Rich Hickey, den Designer und Entwickler von Clojure zu Wort kommen:
 
> Why did I write yet another programming language? Basically because I wanted:

> - A Lisp
> - for Functional Programming
> - symbiotic with an established Platform
> - designed for Concurrency

> and couldn't find one.
")

; -------------------------------------------------------------------------------------------------
(pres "
#Clojure ist ein LISP

- 1958 John McCarthy MIT List Processing
- 1970 Guy Steele & Gerald Sussman MIT Scheme 
- 1984 Common Lisp
- 2007 Clojure
- Skalare und (in Clojure nicht nur) Listen
- Code = Daten und Daten = Code – Homoikonizität
- Syntaktische Erweiterungen durch Metaprogrammierung: Makros und DSLs
")

; -------------------------------------------------------------------------------------------------
(pres "
#Die Syntax von Lisp

Ausdruck

    2 * 20 + 2
     
ergibt folgenden abstrakten Syntaxbaum:     

              +
             / \\
            *   2
           / \\
          2   20
          
Diesen Baum kann man auch als Liste schreiben:          

    (+ (* 2 20) 2)
    
In einem Lisp ist diese Liste ausführbar, die 
Syntax ist der abstrakte Syntaxbaum selbst 
(fast jedenfalls).    
")

(+ (* 2 20) 2)


; -------------------------------------------------------------------------------------------------
(pres "
#Homoikonizität

Code ist in Lisp identisch zu einer Datenstruktur: der Liste

Wenn man die Liste haben will nicht als Code, sondern als Daten
muss man dies durch 'Quote' quasi als Zitat kennzeichen:

    (quote (+ (* 2 20) 2))
    
oder kurz:

    '(+ (* 2 20) 2)

Was passiert, wenn man jetzt den Ausdruck auswertet?

Was aber, wenn man sich eine solche Liste erzeugt und das
Ergebnis berechnen möchte?

    (eval '(+ (* 2 20) 2))
")

(quote (+ (* 2 20) 2))

'(+ (* 2 20) 2)

(eval '(+ (* 2 20) 2))


; -------------------------------------------------------------------------------------------------
(pres "
#Clojure ist eine funktionale Programmiersprache

- 1936 Alonzo Church λ-Kalkül
- 1938 Alan Turing: Im λ-Kalkül berechenbare Funktionen sind genau diejenigen, die durch eine 
  Turing-Maschine berechenbar sind.
- Reine Funktionen vs. Seiteneffekte
- Anonyme Funktionen (auch Lambdas genannt)
- Funktionen höherer Ordnung, d.h. Funktionen können Funktionen als Argumente haben sowie zu 
  einem Wert auswerten, der eine Funktion ist.
- Reine Funktionen sind nur sinnvoll, wenn alle verwendeten Datenstrukturen _Werte_ sind, 
  also unveränderlich.
")

; Anonyme Funktion (Lambda)
;
((fn [x] (* x x)) 2)

; kürzer auch
(#(* % %) 2)
;
; Funktion als Wert
(def square (fn [x] (* x x)))

square

(def two 2)
(square two)

; Eine Funktion, die eine Funktion als Argument hat:
;
(map (fn [x] (* x x)) [1 2 3 4])
;
; geht auch kürzer:
;
(map #(* % %) [1 2 3 4])
;
; und auch lesbarer:
;
(map square [1 2 3 4])

; Eine Funktion, deren Auswertung selbst eine Funktion ergibt:
;
(defn make-adder [x] (fn [y] (+ y x)))
;
; Wenn man make-adder auswertet, sehen wir, dass der Wert 
; selbst eine Funktion ist
make-adder
;
; und nun setzen wir make-adder mal ein
;
((make-adder 3) 2)
;
; was ergibt das?

; Alle Datenstrukturen sind Werte, auch z.B. Vektoren oder Maps

(def v1 [1 2 3])

v1

(conj v1 4)

v1

(def m1 {:a 1 :b 2})

m1

(assoc m1 :a 0)

m1

; -------------------------------------------------------------------------------------------------
(pres "
#Clojure ist eine dynamische Programmiersprache

- Interaktive Entwicklung
- Typinferenz
- Ändern und Laden von Code zur Laufzeit 
- Automatisches Compilieren zu Java Bytecode 
- deshalb: _dynamisch_ und _typisiert_
")


; Typinferenz
(class 2)

(class 2M)

(class (+ 2 123456))

(class (+ 2M 123456))
(class '(+ 2M 123456))

; -------------------------------------------------------------------------------------------------
(pres "
#Clojure läuft auf der Java Virtual Machine

- Clojure kann jede Java-Bibliothek nutzen 
- Clojure-Bibliotheken können in Java verwendet werden
- Clojure-Anwendungen können wie Java-Anwendungen ausgeliefert werden

")

; Folgendes Beispiel ist aus der Dokumentation von clojure.org:

(import '(javax.swing JFrame JLabel JTextField JButton)
        '(java.awt.event ActionListener)
        '(java.awt GridLayout))
(defn celsius []
  (let [frame (JFrame. "Celsius Converter")
        temp-text (JTextField.)
        celsius-label (JLabel. "Celsius")
        convert-button (JButton. "Convert")
        fahrenheit-label (JLabel. "Fahrenheit")]
    (.addActionListener
     convert-button
     (reify ActionListener 
            (actionPerformed
             [_ evt]
             (let [c (Double/parseDouble (.getText temp-text))]
               (.setText fahrenheit-label
                         (str (+ 32 (* 1.8 c)) " Fahrenheit"))))))
    (doto frame
      (.setLayout (GridLayout. 2 2 3 3))
      (.add temp-text)
      (.add celsius-label)
      (.add convert-button)
      (.add fahrenheit-label)
      (.setSize 300 80)
      (.setVisible true))))
(celsius)

; anderes Beispiel: MNI WebViewer

; zeige: clj.webviewer und clj.presentation

; -------------------------------------------------------------------------------------------------
(pres "
#Clojure hat Konzepte für Nebenläufigkeit und Parallelität

- Werte (und damit alle Datenstrukturen von Clojure) sind per se thread-sicher.
- Clojure unterscheidet Identität, Wert und Zustand - _Epochenmodell_ der Entwicklung
- Explizite Referenztypen
- Software transactional memory (STM), dadurch systemseitige Steuerung der Zugriffskontrolle
- Multiversionierung – _Snapshot Isolation_
")

; wir definieren zwei Refs für zwei Konten, mit denen wir eine Überweisung 
; machen wollen
(def KontoA (ref 100))
(def KontoB (ref 0))

; Was ist KontoA?
KontoA

; Welchen Wert hat KontoA?
(deref KontoA)

; oder kürzer
@KontoA
@KontoB

; Bei einer Überweisung müssen KontoA und KontoB simultan geändert werden:
(dosync
  (alter KontoA - 50)
  (alter KontoB + 50))

@KontoA

@KontoB

; -------------------------------------------------------------------------------------------------
(pres "
#Themen der Veranstaltung

- Read-Eval-Print-Loop:   
  Reader, Auswertung, Syntax, Homoikonität
- Funktionen:   
  Definition von Funktionen, Werte und Funktionen, Funktionen höherer Ordnung, reine Funktionen, Seiteneffekte
- Rekursive Funktionen:   
  Rekursion und Iteration, Endrekursion, wechselseitige Rekursion
- Werte und Datenstrukturen:  
  einfache Datentypen, komplexe Datentypen, Destructuring
- Abstraktionen:   
  Collection, Sequence, Associative, Indexed, Stack, Set, Sorted  
  Reducer und Transducer
- Datentypen und Polymorphismus:     
  Multimethods, Records, Protocols
- Identität, Zustand & Nebenläufigkeit:    
  Konzept, Vars, Atoms, Refs, Agents, STM
- Java-Interoperabilität:   
  Java verwenden, Exceptions, Typ-Hinweise, Arrays, Klassen und Interfaces
- Makroprogrammierung:      
  Syntax, Idiome und Muster
- Softwaretechnik mit Clojure:   
  Namespaces, Metadata, Test, Assertions, Performance
- Logik in Clojure mit der Logic Workbench lwb
")

; -------------------------------------------------------------------------------------------------
(pres "
# Installation und Entwicklungsumgebungen

- [Try Clojure](http://tryclj.com/)
- [ClojureScript REPL](http://clojurescript.net)
- [Clojure pur](http://clojure.org/downloads)
- Viele Entwickler verwenden [Leiningen](http://leiningen.org):
- Leiningen works with projects. 
  A project is a directory containing a group of Clojure 
  (and possibly Java) source files, along with a bit of metadata about them.   
  The metadata is stored in a file named `project.clj` in the project’s root directory, 
  which is how you tell Leiningen about things like
    * Project name
    * What libraries the project depends on ...
    
- Leiningen Kommandos    
    * `help` Display a list of tasks or help for a given task 
    * `new`  Generate project scaffolding based on a template 
    * `repl` Start a repl session either with the current project or standalone
    * `run`  Run the project’s `-main` function
    * `test` Run the project’s tests
    * `deps` Show details about dependencies
    * `version` Print version for Leiningen and the current JVM
")


; -------------------------------------------------------------------------------------------------
(pres "
# Entwicklungsumgebungen

- Eclipse mit [Counterclockwise](https://github.com/laurentpetit/ccw/wiki/GoogleCodeHome)
- [Light Table](http://www.lighttable.com)
- IntelliJ mit [Cursive](https://cursiveclojure.com)
- vim mit [fireplace](https://github.com/tpope/vim-fireplace)
- emacs mit [CIDER](https://github.com/clojure-emacs/cider)
- und andere mehr
")

; -------------------------------------------------------------------------------------------------
(pres "
# Internet-Quellen

- Rich Hickey and the Clojure Community:    
  [Clojure home](http://clojure.org)
- The Clojure Community:    
  [Community Resources](https://clojure.org/community/resources)
- The Clojure Community:     
  [Clojure Documentation](http://clojure-doc.org/)
- Grimoire:     
  [Grimoire - Clojure Documentation](http://conj.io)
- Bibliotheken:     
  [CrossClj](https://crossclj.info)     
  [The Clojure Toolbox](http://www.clojure-toolbox.com)     
- Vorlesungsskripte [Programmieren in Clojure](https://github.com/esb-dev/clj)
- und unzählige mehr  
")


; -------------------------------------------------------------------------------------------------
(pres "
# Literatur

- Carin Meier:    
  _Living Clojure_, O'Reilly 2015
- Chas Emerick, Brian Carper, Christophe Grand:  
  _Clojure Programming_, O’Reilly 2012.
- Dominikus Herzberg:   
  _Funktionale Programmierung mit Clojure_, [Blog](http://denkspuren.blogspot.de/2013/04/freies-clojure-buch-funktionale.html)
- Stefan Kamphausen, Tim Oliver Kaiser:    
  _Clojure_, dpunkt.verlag, 2010.
- Stuart Halloway, Aaron Bedra:   
  _Programming Clojure, Second Edition_, Pragmatic Programmers, 2012.
- Michael Fogus, Chris Houser:   
  _The Joy of Clojure, Second edition_, Manning 2014.
- Luke VanderHart, Ryan Neufeld:  
  _Clojure Cookbook_, O’Reilly 2014.  
")
