; Programmierung in Clojure Vorlesung 12  
; Makros - Metaprogrammierung in Clojure
; (c) 2014 - 2015 by Burkhardt Renz, THM

(ns clj.l12-macros
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Makros - Metaprogrammierung in Clojure")

;; 1. Konzept der Makroprogrammierung

;; 1.1 Wie funktioniert ein Makro?
(pres :add "
# Wie funktioniert ein Makro?

![Clojure-Compiler](resources/compile.jpg)
")

;; 1.2 Warum man Makros braucht 
;: -- was wäre, wenn and eine Funktion wäre?

;; Metainformationen von and
(meta #'and)
; => {:macro true ...

;; Wir definieren eine Funktion für and
(defn fand [x y]
  (if x
    (if y
      true
      false)
    false))

;; Vergleich des Verhaltens der Funktion mit dem Makro
(fand (do (println "x auswerten") false) 
      (do (println "y auswerten") true))
; x auswerten
; y auswerten
; => false, obwohl man das Ergebnis bereits nach dem ersten Schritt kennt

(and (do (println "x auswerten") false) 
     (do (println "y auswerten") true))
; x auswerten
; => false

;; and soll nicht alle Argumente auswerten, 
;; sondern nach dem ersten falschen aufhören --
;; warum geht des mit der Funktion nicht?

;; sehen wir was das Makro and macht
(macroexpand-1 '(and x y))

;; und or
(macroexpand-1 '(or x y))
               
(doc and)

(source and)
; zeigt  folgendes:
(comment
  (defmacro and
    "Evaluates exprs one at a time, from left to right. If a form
  returns logical false (nil or false), and returns that value and
  doesn't evaluate any of the other expressions, otherwise it returns
  the value of the last expr. (and) returns true."
    {:added "1.0"}
    ([] true)
    ([x] x)
    ([x & next]
     `(let [and# ~x]
        (if and# (and ~@next) and#))))
  )

;; Das Ziel dieser Vorlesung ist es, diesen Code zu verstehen!

;; 1.3 Wozu man Makros verwenden kann -> DSLs

(pres "
# Wozu man Makros verwenden kann

- Erweitern der Sprache z.B. um Kontrollstrukturen, Beispiel `foreach`
- Sehr viele Formen von Clojure sind selbst Makros
- Domänenspezifische Sprachen (DSL _domain specific language_)
- Interne DSLs wegen der Homoikonizität von Clojure leicht möglich, denn
  das Makrosystem sieht selbst aus wie Clojure
")

;; Beispiel foreach
(defmacro foreach [[sym coll] & body]
  `(loop [coll# ~coll]
     (when-let [[~sym & xs#] (seq coll#)]
       ~@body
       (recur xs#))))

(foreach [x [1 2 3]]
   (println x))      


;; 2. Syntax -- Entwickeln von Makros

;; 2.1 Quote - verhindert die Auswertung
(and (= 1 1) (= 1 0))
; => false

'(and (= 1 1) (= 1 0))
; => (and (= 1 1) (= 1 0))

(quote (and (= 1 1) (= 1 0)))
; => (and (= 1 1) (= 1 0))

;; eval wertet eine Liste aus
(eval '(and (= 1 1) (= 1 0)))
; => false

;; Mit Quote kann man also Makros schreiben:
(defmacro sq [x]
  (list '* x x))

(sq (+ 3 4))
; => 49

(macroexpand-1 '(sq (+ 3 4)))
; => (* (+ 3 4) (+ 3 4))

;; das geht aber auch bequemer mit Syntax-Quote:
 
;; 2.2. Syntax-Quote

;; normales Quote
'(* x x)
; => (* x x)

;; Backtick = Syntax-Quote
`(* x x)
; => (clojure.core/* clj.l12-macros/x clj-l12.macros/x)

;; Syntax-Quote wandelt Symbole in mit dem Namensraum 
;; qualifizierte Symbole um

;; Unquote
(def x 2)

;; Innerhalb einer Form mit Syntax-Quote ist Unquoting möglich
`(* x ~x)
; => (clojure.core/* clj.l12-macros/x 2)

;; 2.3 Unquote und Unquote-Splicing

;; Unquote in einem Ausdruck mit Syntaxquote führt dazu, 
;; dass der Ausdruck ausgewertet wird
(let [x 2] `(* x ~x))
; => (clojure.core/* clj.l12-macros/x 2)

(let [x '(1 2 3) ] `(* x ~x))
; => (clojure.core/* clj.l12-macros/x (1 2 3))

(let [x [1 2 3] ] `(* x ~x))
; => (clojure.core/* clj.l12-macros/x [1 2 3])

;; Unquote-Splicing packt eine Datenstruktur beim Unquote aus:
(let [x '(1 2 3)] `(* x ~@x))
; => (clojure.core/* clj.l12-macros/x 1 2 3)

(let [x [1 2 3]] `(* x ~@x))
; => (clojure.core/* clj.l12-macros/x 1 2 3)

;; Damit ergibt sich eine Methode, um Makros zu entwickeln:

;; 2.4 Code-Schablonen

(pres "
# Makros mit Code-Schablonen entwickeln

1. Man schreibt den Ausdruck wie eine Funktion    
   und zitiert ihn mit Syntaxquote
2. Man markiert die Platzhalter durch Unquote und Unquote-Splicing   


Beispiel: `unless`

Ziel: `(unless Bedingung Code)`

Der Code soll ausgeführt werden, wenn die Bedingung _nicht_ erfüllt ist
")

;; unless entwickeln
;; Schritt 1
(defmacro unless [condition & body]
  `(when (not condition) body))

;; Was haben wir erreicht?
(macroexpand-1 '(unless (= 1 2) (println "hallo")))
(macroexpand '(unless (= 1 2) (println "hallo")))
; => (if (clojure.core/not clj.l12-macros/condition) (do clj.l12-macros/body))

;; Wenn wir das anwenden wollen?
(unless (= 1 2) (println "hallo"))
; => CompilerException java.lang.RuntimeException: No such var: clj.l12-macros/condition

;; Die Bedingung muss ja tatsächlich ausgewertet und _nicht_ zitiert werden

;: Schritt 2: Parameter verwenden
(defmacro unless [condition & body]
  `(when (not ~condition) body))

(macroexpand '(unless (= 1 2) (println "hallo")))
; => (if (clojure.core/not (= 1 2)) (do clj.l12-macros/body))

;; anwenden:
(unless (= 1 2) (println "hallo"))
; => CompilerException java.lang.RuntimeException: No such var: clj.l12-macros/body

;; Auch der Code selbst muss ausgewertet werden
;; Versuch:
(defmacro unless [condition & body]
  `(when (not ~condition) ~body))
 
(unless (= 1 2) (println "hallo"))
;; was ist falsch?

(macroexpand '(unless (= 1 2) (println "hallo")))
; => (if (clojure.core/not (= 1 2)) (do ((println "hallo"))))

;; Bei der Parameterübergabe mit & body wird aus den 
;; restlichen Parametern eine Liste,
;; Wir wollen aber die Elemente der Liste selbst.
;; Was hilft?

;; Noch ein Versuch!
(defmacro unless [condition & body]
  `(when (not ~condition) ~@body))

(unless (= 1 2) (println "hallo"))

(unless true (println "hallo"))

(macroexpand '(unless (= 1 2) (println "hallo")))
; => (if (clojure.core/not (= 1 2)) (do (println "hallo")))

(unless (= 1 2) "hallo" "world")
; => world -- wegen do, zu dem der Body expandiert

;; 2.5 Generierte Symbole gensym 
;; Möchte man in einem Makro lokale Symbole verwenden, entsteht ein Problem:

;; Schritt 1 der Schablonen-Technik

(defmacro sq [x]
  `(* x x))

(macroexpand '(sq 7))

;; Schritt 2: Unquoten

(defmacro sq [x]
  `(* ~x ~x))

(macroexpand '(sq (+ 3 4)))

(sq (+ 3 4))

;; statt x zweimal auszuwerten, wäre ein let besser:
(defmacro sq [x]
  `(let [local ~x] (* local local)))

(sq 7)
; => CompilerException java.lang.ExceptionInfo: Call to clojure.core/let did not conform to spec:
; => weitere Angaben von clojure.spec

;; Backtick macht aus Symbolen qualifizierte Symbole, 
;; also gerade keine lokalen Symbole, also
;; können sie nicht in let verwendet werden, 
;; denn sie repräsentieren ja schon einen Wert vor dem let!!

;; Abhilfe: ein im Kontext von let generiertes Symbol --
;; dazu gibt es gensym 

(doc gensym)
; clojure.core/gensym
; ([] [prefix-string])
; Returns a new symbol with a unique name. If a prefix string is
; supplied, the name is prefix# where # is some unique number. If
; prefix is not supplied, the prefix is 'G__'.

(gensym)

;; Der Reader von Clojure macht aus jedem Symbol, 
;; das mit # endet mit gensym ein generiertes Symbol

`(my-symbol#)
; => (my-symbol__1911__auto__) (oder so)

;; Also müssen wir im let ein generiertes Symbol verwenden:
(defmacro sq [x]
  `(let [local# ~x] (* local# local#)))

(sq (+ 3 4))
; => 49

(macroexpand-1 '(sq (+ 3 4)))
; => (clojure.core/let [local__1614__auto__ (+ 3 4)] (clojure.core/* local__1614__auto__ local__1614__auto__)) 

;; Jetzt verstehen wir den Code von (and ...)
(source and)

(defmacro myand 
  ([] true)
  ([x] x)
  ([x & next]
  `(let [myand# ~x]
    (if myand# (myand ~@next) myand#))))
; man braucht die Variante mit 0 und 1 Argumente, damit die
; Rekursion auch mal endet!!

(myand false false true)
(myand true true true)

;; 3. Softwaretechnische Überlegungen

(pres "
# Softwaretechnische Überlegungen zu Makros

- Makros sind eine elegante Möglichkeit DSLs zu entwickeln
- Wir werden solche noch kennenlernen in den Präsentationen
- Die Verwendung von Makros lebt von einer brauchbaren Dokumentation
- Andererseits: Makros können kompliziert sein
- Fehler in Makros sich manchmal schwer zu finden
- Deshalb: Wo es eine Funktion tut, besser eine Funktion verwenden     
  Makros nur, wenn man  sie wirklich benötigt
")


;; Offene Enden:

;; (1)
;; &form und &env in der Makrodefinition 
;; &env innerhalb eines Makros ergibt die Umgebung, d.h. ein Map der
;;   lokal gebundenen Symbole mit der Adresse der zugehörigen lokalen Vars
;; &form innerhalb eines Makros ergibt den Aufruf des Makros 

;; (2)
;; Makros in Funktionen höherer Ordnung als Parameter problematisch!!
