; Programmierung in Clojure Vorlesung 3
; Interaktive Entwicklung in der REPL und das Substitutionsmodell
; (c) 2013 - 2017 by Burkhardt Renz, THM

(ns clj.l03-repl
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all])
  (:require [clojure.java.javadoc :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Interaktive Entwicklung in der REPL und das Substitutionsmodell")

(pres :add "
#Die REPL - Read-Evaluate-Print-Loop
 
![Die REPL Ablauf](resources/repl.jpg)
")

; wir starten eine REPL und sehen, was man da alles Hilfreiches hat
; wir blenden die Symbole aus den obigen Namenräumen ein

(doc read)

(source read)

(find-doc "eval")
; findet alle docs in den "eval" vorkommt

(doc eval)

(source eval)

(javadoc String)


(pres "
#Der Reader

- Syntaktische Analyse
- ''the reader has syntax defined in terms of characters'' – reader forms
- Funktion `read', die die nächste _form_ liest und ein Objekt erzeugt, das diese _form_ repräsentiert

")

; -------------------------------------------------------------------------------------------------
; Literale
(pres :add "
# Literale

- Strings    : umrahmt von doppelten Anführungszeichen, wie in Java `\"Hello World!\"`
- Characters : beginnen mit einem Backslash `\\a`, `\\tab`, `\\00e4` für `ä` 
- Boolean    : true oder false 
- nil        : nil steht für null in Java, in logischen Ausdrücken für false
- Zahlen     : beginnen mit optional + oder - oder einer Ziffer ...
- Keywords   : beginnen mit :, können zu einem Namensraum gehören

")

; Strings
"Hello World!"

(print "Dies ist ein Anführungszeichen: \"")

; Characters
\a

\u00e4

(str \h \a \l \l \o)

; Boolean
true

(boolean true)

(class true)

false

(boolean false)
(boolean (Boolean/FALSE))

nil

(boolean nil)

(boolean "foo")

; boolean false, nil, (Boolean/FALSE) => false
;         alles andere => true

(doc boolean)

(doc true?)

(true? "foo")

(doc false?)

(false? "foo")

; "foo" ist weder der Wert true noch der Wert false

(if true 1 0)
(if false 1 0)
(if nil 1 0)

; aber "foo" wertet zu true aus als logischer Ausdruck
(if "foo" 1 0)

; Zahlen

(class 42)
; => java.lang.Long

(class 42N)
; => clojure.lang.BigInt

(class 3.14)
; => java.lang.Double

(class 3.14M)
; => java.math.BigDecimal

(/ 22 7)

(class 22/7)
; => clojure.lang.Ratio

; Keywords
(class :name)
; => clojure.lang.Keyword

(namespace :name)
; => nil

(namespace :user/location)
; => "user"

(class ::location)
(namespace ::location)
; => "clj.l03-repl"

; -------------------------------------------------------------------------------------------------
; Symbols
(pres "
#Symbole

- Symbole ( _symbols_ ) sind Bezeichner ( _identifier_ ) für etwas anderes: z.B. 
    - eine Funktion in einem Namensraum, 
    - einen Funktionsparameter, 
    - ein lokales mit `let` eingeführtes Symbol, das für einen Wert steht etc.
- In Clojure sind Symbole keine Speicherplätze, sie können aber Bezeichner für solche sein.
- Symbole beginnen mit einem Buchstaben (aber keiner Ziffer) und können 
  `*, +, !, -, _` und `?` enthalten.
- `/` und `.` kommen in Bezeichnern von Namensräumen vor 
- `.` am Ende und `:` am Anfang und am Ende sind für Clojure reserviert.
- Symbole werden mit `(def ...)` definiert.

")

symbol

(symbol? s1)
; Das Symbol s1 wird versucht auszuwerten, das geht aber nicht

(def s1)

s1

(def s1 1)

s1

(def s1 "hallo")
; kann man in der REPL nochmals tun, sonst sollte man die so erzeugte Var
; als 'dynamic' kennzeichnen

; Vorsicht: (def ...) erzeugt eine globale Zuordnung eines Werts zu einem Symbol
; egal auf welcher Klammernebene man def verwendet!!!

(pres :add
  "aus der Dokumentation von Clojure:
  
  Using def to modify the root value of a var at other than the top level is 
  usually an indication that you are using the var as a mutable global, and 
  is considered bad style. Consider either using binding to provide a thread-local 
  value for the var, or putting a ref or agent in the var and using transactions 
  or actions for mutation."
)
  

(symbol? s1)
; false -- warum? s wird ausgewertet

(symbol? 's1)
; true -- quote verhindert die Auswertung

; -------------------------------------------------------------------------------------------------
; Kollektionsliterale

(pres "
#Kollektionen - und wie man sie literal schreibt

Listen   : sind umklammerte forms
         (1 2 3 4 5), die leere Liste: ()
Vektoren : sind eckig umklammerte forms
         [1 2 3], [1 [1 2] 2], [1 \"hallo\"]
Maps     : sind geschweift umklammerte forms, in gerader Anzahl
         jeweils interpretiert als Key und Value
         {:monday 1, :tuesday 2, ...}
Mengen   : beginnen mit #{ folgend die Elemente und schließen mit } ab
          #{1 2 3}

")

(list? '(1 2 3 4 5))

(list? '())

(list? ())

[1 2 3]

(class [1 2 3])

[1 [1 2] 3]

[1 , "hallo"]
; , ist "white-space"

{:monday 1, :tuesday 2, :wednesday 3, :thursday 4, :friday 5}

{"Montag" 1 "Dienstag" 2}

{{"Montag" 1 "Dienstag" 2} 42}

(class {:monday 1, :tuesday 2, :wednesday 3, :thursday 4, :friday 5})

#{1 2 3}

#{1 1 1}
; nicht erlaubt

(class #{1 2 3})

(def menge #{1 2})
(conj menge  1)
; erlaubt

(conj menge  3)

menge  
(disj menge  1)
(disj menge  3)

; -------------------------------------------------------------------------------------------------
; Makrozeichen

(pres "
#Makrozeichen für den Reader

`'`      : zitiert form und verhindert so die Auswertung 
         `'(+ 2 3) → (quote (+ 2 3))` 
`;`      : Kommentar bis zum Zeilenende

`#_`     : Ignoriere die nächste form

`@`      : Deref von Referenzen in Clojure, ref, var, atom, agent etc

`^`      : Metadaten, auch für Typinformationen für Java

`#\"pattern\"`: Reguläre Ausdrücke, z.B. `#\"[0-9]+\"`

`#’`     : Var-Objekt zu einem Symbol
         `#’x → (var x)` 
`#(...)` : Kurzform zum Erzeugen einfacher anonymer Funktionen

`‘, ~, ~@` : gebraucht für die Makroprogrammierung

")

; Quote ---
(+ 2 3)

'(+ 2 3)

(quote (+ 2 3))

; dies ist ein Kommentar ---

#_(+ 2 3)
#_(+ 2 3)

(comment (+ 2 3))

; deref ---

(def kontoA (ref 100))

kontoA

(deref kontoA)

@kontoA

; Metadaten ---

(def v1 ^"Mein Vektor" [1 2 3])
; ergibt meta mit key :tag

v1

(meta v1)

(def v2 ^{:name "Mein Vektor"} [1 2 3])
; ergibt meta wie angegeben

v2

(meta v2)

; Reguläre Ausdrücke

(re-seq #"[0-9]+" "Dieser String enthält die Zahlen 123 und 456")

(class #"[0-9]+")
; => java.util.regex.Pattern
; pattern vom Reader erzeugt

(class (re-pattern "[0-9]+"))
; pattern zur Laufzeit erzeugt

; Var quote

(doc var)

(def x1 42)

x1

(var x1)

#'x1

(meta (var x1))

(meta (var meta))

(meta #'seq)

(doc seq)

; Anonyme Funktionen

(map #(* % %) [1 2 3])

(map (fn [x] (* x x)) [1 2 3])

(filter #(= 0 (mod % 3)) [1 2 3 4 5 6 7 8 9])

; -------------------------------------------------------------------------------------------------
; Extensible Reader

(pres "
#Reader Macros - Clojures extensibler Reader

- Common Lisp kennt _reader macros_, mit denen man die Syntax ändern kann.   
  Z.B. kann man den Reader dazu bringen, dass er die Zeichen `#v` auf eine spezielle benutzerdefinierte Art verwendet.
- Rich Hickey hat sich stets gegen einen solchen Mechanismus gewandt – Warum?
- Clojure hat einen extensible Reader
- Beispiel UUID    
  `#uuid \"f81d4fae-7dec-11d0-a765-00a0c91e6bf6\"` wird vom Reader   
  zu einem Objekt der Klasse `java.util.UUID` gemacht
- Man kann eigene solche Datareader schreiben, die aber in einem Namespace sein müssen.
- Registrieren in `data_readers.clj`

")

(class #uuid "f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
; => java.util.UUID

(pres "
#Reader Conditionals

- Manchmal unterscheiden sich Quellen zwischen Clojure und ClojureScript nur in Kleinigkeiten
- Deshalb gibt es seit Clojure 1.7 eine neue Datei-Extension `.cljc`, die für Clojure Common steht
- Braucht man dort kleine Unterschiede kann man _Reader Conditionals_ einsetzen

Beispiele aus der Dokumentation von Clojure:

- unterschiedliche Datentypen
```
        #?(:clj     Double/NaN
           :cljs    js/NaN   
           :default nil)
```   

- unterschiedliche Daten
```
        [1 2 #?@(:clj [3 4] :cljs [5 6])]
```
")

; -------------------------------------------------------------------------------------------------
; Evaluationsregel 1

(pres "
#Auswertung von Clojure _forms_ - Das Substitutionsmodell

## Evaluationsregel 1

- Alle Ausdrücke evaluieren zu sich selbst mit Ausnahme von Symbolen und Listen.
- Wenn Ausdrücke andere Ausdrücke enthalten, werden diese nach den 
Evaluationsregeln ausgewertet (Substitutionsmodell)
")

42

"Hallo"

[1 2 3]

;aber
(1 2 3)

'(1 2 3)

(eval '(1 2 3))

; -------------------------------------------------------------------------------------------------
; Evaluationsregel 2

(pres :add "
## Evaluationsregel 2
Ein Symbol wird ausgewertet nach:   

- hat es einen qualifizierten Namensraum → Wert der gebundenen Var
- hat es ein qualifiziertes Package → entsprechende Java-Klasse sonst    
    1. steht es für eine special form siehe unten
    2. lookup im aktuellen Namensraum nach Java-Klasse
    3. lookup nach lokaler Bindung (Funktionsargument oder let)
    4. lookup im aktuellen Namensraum nach var
- Wenn keine Auflösung möglich ist, erhält man einen Fehler.

")

symb
; unable to resolve symbol symb

(def symb)
symb
; unbound symbol

(+ 1 symb)
; geht nicht

(def symb 2)
symb

(+ 1 symb)

(def add-symb (fn [x] (+ x symb)))

(add-symb 1)
(add-symb 7)

; Konstante in einer Java-Klasse
java.lang.Math/PI

; -------------------------------------------------------------------------------------------------
; Evaluationsregel 3

(pres :add "
## Evaluationsregel 3
- Die leere Liste () evaluiert zu sich selbst
- Eine nicht-leere Liste ist ein Aufruf (call) einer special form, eines Makros oder einer Funktion  
  Das erste Element der Liste gibt an, um welchen Aufruf es sich handelt: (operator operands*)
- Handelt es sich um einen Funktionsaufruf:    
    1. Auswerten aller weiteren Ausdrücke der Liste von links nach rechts
    2. Auswerten der Funktion mit den Werten der weiteren Ausdrücke als Argumente

")

()
; ()

(or (= 1 1) (= 1 0))
; true

(macroexpand-1 '(or (= 1 1) (= 1 2)))
; or ist ein macro
; warum?

(doc or)

(source or)

(macroexpand-1 '(+ 1 2))
; + ist eine Funktion

(doc +)

(1 2 3)
; das erste Element der Liste muss eine Funktion, ein Makro oder eine special form sein

(doc if)
; special form
; warum?

; -------------------------------------------------------------------------------------------------
; special forms

(pres "
# Warum _special forms_?

Könnte nicht einfach _alles_ eine Funktion oder ein Makro sein? 

`(def symb 42)`   

Welches Problem tritt nach den Evaluierungsregeln auf?


⇒ def ist eine _special form_, oder auch ein _primitives Element_ von Clojure.
")

; (def symb 42) könnte nach den Evaluierungsregeln nicht funktionieren, weil
; symb ja ausgewertet werden würde, aber hier erst definiert werden soll

(pres :add "
# Welche _special forms_ gibt es in Clojure?

`(def symbol init?)` : erzeugt eine var und gibt ihr einen Namen

`(if test then else?)` : wertet test aus und verfährt entsprechend

`(do exprs*)` : wertet Ausdrücke der Reihe nach aus und gibt den Wert des letzten zurück

`(let [bindings*] exprs*)` : bindet Werte an Symbole und verwendet sie in Ausdrücken 
                            `(let [x 1, y x] y) -> 1`
`(quote form)` : ergibt die form ohne dass sie ausgewertet wird

`(var symbol)` : ergibt das Var-Objekt, nicht seinen Wert

`(fn name? [params*] exprs*)`  : erzeugt eine Funktion

`(fn name? ([params*] exprs*)+)` : erzeugt eine Funktion mit Überladungen 

`(loop [bindings*] exprs)` : wie let aber Ansprungpunkt für recur

`(recur exprs*)` : end-rekursiver Aufruf

")

(def s1 42)

; if
(if (< 2 3) "kleiner" "größer") 

(def kleiner (fn [x y] (if (< x y) "kleiner" "größer"))) 

(kleiner 42 4)

(kleiner 42 "x")

; do
(do 
  (+ 3 4)
  (+ 2 3)
  (+ 1 2))
; relativ sinnlos

(do
  (println "erster Schritt")
  (println "zweiter Schritt"))
; hat Seiteneffekte

; let
(def x1 42)

(let [x1 1
      y x1]
     y)

x1

(let [x1 1
      y x1]
     (do
       (println x1)
       y))

; quote

(1 2 3)
; 1 ist keine Funktion

(quote (1 2 3))

'(1 2 3)

'(1 (1 2) 3)
; wirkt für alle Teile!

; var

(var x1)

#'x1

; fn

((fn this
      ([] 1)
      ([x] x)
      ([x y] (* x y))
      ([x y & more]
          (apply this (this x y) more))) 2 3 4 5)
; this braucht man, um die Funktion später selbst verwenden zu können

(def mult 
  (fn this
      ([] 1)
      ([x] x)
      ([x y] (* x y))
      ([x y & more]
          (apply this (this x y) more))))

(mult)

(mult 2)

(mult 2 3 4 5 6 7)

; loop, recur
; Rekursion und Endrekursion kommt später ausführlich

(pres "
# _Special forms_ für die Java-Interoperabilität

`(throw expr)` : expr wird ausgewertet (`Throwable`!) und geworfen

`(try expr* catch* finally)` : Try-Catch-Block

`(. obj method args*)` : Java Methodenaufruf

`(. Class func args*)` oder `(Class/func args*)` : Java statische Funktion einer Klasse

`(new Class args*)` oder `(Class. args*)` : Konstruktoraufruf

`(set! (.obj field) expr)` : setzt Wert einer Objektvariable

")

; throw, try 
; im Prinzip wie in Java

; dot
(. java.lang.Math tan 0.5)

; mehr lispy
(java.lang.Math/tan 0.5)

; new
(def dt (new java.util.Date))

(. dt toString)

; new per dot hinter dem Klassennamen
(def dt2 (java.util.Date.))

(. dt2 toString)

; mehr lispy
(.toString dt2)

(def sb (new java.lang.StringBuffer "abc"))

sb
(.toString sb)

(. sb append "d")

sb

(.append sb "e")

sb

; später mehr über Java Interop
