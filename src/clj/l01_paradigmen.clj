; Programmierung in Clojure Vorlesung 1
; Paradigmen der Programmierung
; (c) 2014 - 2017 by Burkhardt Renz, THM

(ns clj.l01-paradigmen
  (:refer-clojure :exclude [==])
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all])
  (:require [clojure.core.logic :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Paradigmen von Programmiersprachen")

;------------------------------------------------------------------------
(pres "
# Paradigmen von Programmiersprachen

_Paradigma_ kommt aus dem Griechischen und bedeutet 
„Lehrbeispiel“, „Vorbild“, „Vorzeigestück“

Verwendet oft im Sinne von „grundlegende Denkweise“, „Leitbild“, 
„Weltanschauung“
")

(pres :add"
Bezüglich Programmiersprachen:

- wie sieht man den Prozess einer Berechnung? 
- wie definiert man eine Berechnung?
- wie definiert und verwendet man Daten?

Wichtigste Paradigmen:

- imperative Programmierung
- funktionale Programmierung
- logische, bzw. relationale Programmierung 
- objektorientierte Programmierung

")

;------------------------------------------------------------------------
(pres "
# Das imperative Paradigma

- Von-Neumann-Architektur:    
  Speicher – Steuerwerk – Rechenwerk
- Detaillierte Beschreibung, wie der Speicher sukzessive modifiziert werden soll    
  → _imperativ_ (lat. imperare = befehlen)
- Programmierung durch Veränderung des Speicherzustands mittels Wertzuweisung
- Maschinenorientierte Datenstrukturen
- Beispiele: FORTRAN, Pascal, C   
  Summieren in Java (siehe `Summieren.java`)
")

;------------------------------------------------------------------------
(pres "
# Das funktionale Paradigma

- Programm ↔ Berechnung einer Funktion basierend auf Alonzo Churchs λ-Kalkül
- Funktionen sind „Bürger erster Klasse“ – können selbst Parameter oder Rückgabewert einer Funktion sein
- Konstruktion von Funktionen mittels anderer Funktionen:  
  _Funktionen höheren Typs_
- _Referenzielle Transparenz_:    
  Ein Ausdruck entspricht seinem Wert und hat immer denselben Wert
- Werte,  _immutable objects_,  statt zustandsbehaftete Objekte
- Beispiele: Lisp, ML, Haskell, Scheme, Erlang, Clojure
- Auch eingebettet in objektorientierte Sprachen: JavaScript, C#, Java, Scala, Python, . . .
")

;; Summieren in Clojure
(reduce + (range 1 11))

; jeder Teilausdruck entspricht einem Wert:
(range 1 11) ; ist die Folge der Zahlen 1 .. 10

; reduce wenden die Funktion + sukzessive auf die Folge der Zahlen an
(doc reduce)
; also
(+ (+ (+ 1 2) 3) 4) ;; etc

; Definition der Funktion 'summieren'
(defn summieren
  [anfang ende]
  (reduce + (range anfang ende)))

(summieren 1 11)


;------------------------------------------------------------------------
(pres "
# Das logische oder relationale Paradigma

- Programmausführung ↔ Ableitungsprozess, Beweis 
- Programm spezifiziert die Fragestellung, nicht einen Weg zur Lösung
- Keine lineare Berechnung:    
  System sucht eine Bindung logischer Variablen, die die 
  in der Spezifikation gestellten Bedingungen erfüllen
- Mehrere Lösung (sogar unendlich viele) möglich
- Beispiele: PROLOG, Coq, MiniKanren, core.logic in Clojure
")

;; Relationale Programmierung mit core.logic

(run* [x y s]
     (membero x [1 2 3])
     (membero y [1 2 3])
     (project [x y]
              (== s (+ x y))))

; berechnet alle möglichen Summen, die man mit x und y aus [1 2 3] bilden kann
; Anmerkungen:
; x y s sind logische Variablen
; (membero ...) legt Wertebereich für mögliche Bindungen fest
; (project ..) "projiziert" die logischen Variablen auf die gebundenen Werte
; (== ...) Unifikation


;------------------------------------------------------------------------
(pres "
# Das objektorientierte Paradigma

- Programm zur Laufzeit ↔ Geflecht von Objekten, die durch Nachrichten interagieren
- Objekte kapseln ihren Zustand, der durch Methoden/Nachrichten geändert wird
- Klassen sind Vorlagen für Objekte, die zur Laufzeit instanziiert werden
- Vererbung von Schnittstellen und Verhalten 
- Polymorphismus
- Beispiel: Smalltalk, C++, Java, C#    
  Stack in Java (`JavaStack.java`) vs. Stack in Clojure
")

;; Stack in Java
;; siehe JavaStack.java

;; und hier jetzt Stack in Clojure

; st ist ein Symbol für den Stack [1 2]
(def st [1 2])

st

; Clojure hat keine Funktion, die push heißt, also nennen wir 
; die in Clojure übliche Funktion conj jetzt mal push
(def push conj)

; wir legen 3 auf den Stack
(push st 3)
;=> [1 2 3]

; Wir erhalten einen "neuen" Stack als Rückgabewert
; st ist unverändert

st

; oberstes Element des Stacks
(peek st)
(peek (push st 3))

; oberstes Element "herunternehmen"
(pop st)

; auch das ändert st nicht!
st


;------------------------------------------------------------------------
(pres :add "
# Vergleich 00 und FP

>OO makes code understandable by encapsulating moving parts.

>FP makes code understandable by minimizing moving parts.
 
>Michael Feathers

")

;------------------------------------------------------------------------
(pres "
# Arten funktionaler Sprache

## Auswertungsart

### Strikte/applikative Auswertung

Erst werden die Parameter evaluiert und _dann_ wird deren Wert 
an die Funktion übergeben ( _eager evaluation_ ) z.B. Scheme, Clojure

### Verzögerte/normale Auswertung

Auswertung der Parameter erst dann, wenn sie tatsächlich benötigt werden 
( _lazy evaluation_ ) z.B. Haskell

")

;; Strikte Auswertung in Clojure
; folgendes Beispiel wirft eine Exception

(count [1 (/ 2 0) 3])
;=> ArithmeticException Divide by zero  clojure.lang.Numbers.divide (Numbers.java:156)

; aber: es gibt in Clojure auch "lazy sequences":
; dazu kommt später mehr in der Vorlesung

(take 10 (range))

(class (take 10 (range)))
;=> clojure.lang.LazySeq

(realized? (take 10 (range)))
;=> false


;------------------------------------------------------------------------
(pres "
# Arten funktionaler Sprache

## Syntax

### Infix-Notation

> `(2 + 3) * square(3)`

z.B. Scala

### Präfix-Notation

> `(* (+ 2 3) (square 3))` 

z.B. Scheme, Clojure
    
Code = Daten, Homoikonizität

")

;------------------------------------------------------------------------
(pres "
# Arten funktionaler Sprache

## Typisierung

### Dynamische Typisierung

- Typ eines Werts wird zur Laufzeit bestimmt
- Datenstrukturen können typischerweise Werte verschiedener Typen enthalten
- basiert auf klassischem λ-Kalkül
- z.B. Lisp, Clojure (jedoch: Clojure kompiliert zu Java)

### Statische/strikte Typisierung
- Typinferenz zur Kompilierzeit
- Definition von Funktionen für bestimmte Typen 
- Datenstrukturen haben typischerweise Werte desselben Typs 
- basiert auf λ-Kalkül mit Typen
- z.B. Haskell, F#, ML

")

;------------------------------------------------------------------------
(pres "
# Fazit: Eigenschaften funktionaler Sprachen

1. Mächtige Konstrukte → kompakter Code
2. Interaktive Entwicklung in der REPL
3. Modularisierung
4. Gute Wiederverwendbarkeit reiner Funktionen
5. Verifizierbarkeit
6. Gut geeignet für Nebenläufigkeit, inhärent thread-sicher
")

;; fin Vorlesung 1 Paradigmen
