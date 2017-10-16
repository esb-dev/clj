; Programmierung in Clojure Vorlesung 4
; Funktionen
; (c) 2013 - 2017 by Burkhardt Renz, THM

(ns clj.l04-fn
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all])
  (:require [clojure.java.javadoc :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Funktionen")

;------------------------------------------------------------------------
(pres "
# Funktionen

- Eine Funktion _f_ ordnet jedem Element _x_ einer Definitionsmenge _D_ genau ein 
  Element _y_ einer Zielmenge _Z_ zu:
> _f_ : _D_ → _Z_, 
>    
> _x_ → _y_, auch _f(x)_ = _y_  

- Oft wird die Funktion durch eine ''Rechenvorschrift'' definiert:    
  Zum Beispiel:
> _f_ : _Int_ → _Nat_, _f(x)_ = _x * x_ 

- Anwendung der Funktion:   
> _f(3)_ = _9_" 
)

(pres :add "
In Clojure kann man Funktionen definieren:

- Anonyme Funktionen (Lambdas)
- Binden der Funktion an ein Symbol 
- Funktionen mit mehreren Parametern
- Variadische Funktionen

")

; anonyme Funktion, Lambda ------------------------------------
(fn [x] (* x x))

; Anwendung der anonymen Funktion
((fn [x] (* x x)) 3) 

; anonyme Funktion mit Namen
(fn sq [x] (* x x))

(sq 3)
; sq kann nicht aufgelöst werden -- was soll das?

; anonyme rekursive Funktion
; n! = n * (n-1)! und 0! = 1
(fn fact [n] 
  (if (= n 0) 
    1
    (* n (fact (dec n)))))

((fn fact [n] 
  (if (= n 0) 
    1
    (* n (fact (dec n))))) 5)

; def ------------------------------------------------------------
; Funktion wird einer var zugewiesen und kann deshalb via eines
; Symbols aufgelöst werden
(def sq (fn [x] (* x x)))

; Anwendung der Funktion
(sq 3)

; defn
; ist ein Makro für die Definition von Funktionen

(defn sq
  "n -> n^2, n wird quadriert!"
  [n]
  (* n n))

(sq 3)

(sq -3)

(doc sq)

; Funktionen mit mehreren Parametern -------------------------------
(defn power
  "Calculates b to the power of exp"
  [b exp]
  (Math/pow b exp))

(power 2 3)

(def pow-base2 (partial power 2))

(doc partial)

(pow-base2 2)
(pow-base2 3)
(pow-base2 4)

; Kurzform für Funktionen ------------------------------------------
(#(* % %) 3)

(#(Math/pow %1 %2) 2 3)

(#(Math/pow % %2) 2 3)

; Variadische Funktionen -------------------------------------------
(+)
(+ 1)
(+ 1 2)
(+ 1 2 3)
(+ 1 2 3 4)

(doc +)

(source +)

;------------------------------------------------------------------------
(pres "
#Funktionen höherer Ordnung

##Definition
Eine Funktion höherer Ordnung ist eine Funktion, die    

- Funktionen als Argumente erhält oder   
- Funktionen als Ergebnis liefert
")

; Funktionen höherer Ordnung

(defn sum-str [& numbers]
	(str (apply + numbers)))
; sum-str verwendet apply und der erste Parameter von
; apply ist eine Funktiion, nämlich +

(sum-str 1 2 3 4 5)

(def sum-str' (comp str +))
; sum-str' ist eine Funktion höherer Ordnung, weil
; comp eine Funktion durch die Komposition von Funktionen erzeugt

(sum-str' 1 2 3 4 5)

((comp str +) 1 2 3 4 5)

(pres "
#Beispiel: Worte untersuchen

- Wir möchten eine Funktion haben, die ein Wort überprüft,  
 ob es mit einem bestimmten Buchstaben beginnt.
- Wir entscheiden uns dazu eine Funktion zu schreiben, die 
 eine Funktion erzeugt, die ein Wort auf einen je bestimmten Anfang überprüft.
- Gegeben eine Wortliste gibt es die Funktion `filter`, 
 die ein Prädikat auf jedes Element anwendet und entsprechend filtert
- Nun suchen wir mal alle Worte, die mit „a“ beginnen und alle, die mit „b“ beginnen
- Und nun mal alle Palindrome . . .
")

; Funktion, die Funktion erzeugt, die Wort auf seinen ersten Buchstaben testet
(defn check-word
  "check-word takes a prefix and returns a predicate 
  that checks a string whether it begins with that letter"
  [p]
  (fn [s] (.startsWith s p)))

(check-word "a")

; Filter, der Worte findet, die mit "a" beginnen
(def check-a (check-word "a"))

; Wortliste
(def wlist ["abba" "beta" "gamma" "delta" "aneta" "breeze" "clojure" "aisfine" "btoo" "regallager"])

; Finde alle Worte, die mit "a" beginnen
(filter check-a wlist)

; Finde alle Worte, die mit "b" beginnen
(filter (check-word "b") wlist)

; alternatives Vorgehen
(defn first?
  [ch str]
  (.startsWith str ch))

(first? "a" "abba")
(first? "b" "abba")

(filter (partial first? "a") wlist)

(filter #(first? "a" %) wlist)

; Finde alle Palindrome

; wir probieren interaktiv, welche Funktionen hilfreich sind:

(reverse "abc")
; liefert eine Sequenz von Buchstaben
; wir brauchen also auch das Wort selbst als Sequenz von Buchstaben:
(seq "cba")

; und schon können wir die Funktion konstruieren
(defn palindrome? [s]
  (= (seq s) (reverse s)))

(palindrome? "abba")

(palindrome? "abc")

(palindrome? "1221")

(palindrome? (str 1221))

; und verwenden
(filter palindrome? wlist)

(pres :add "
- Wie nennt man das als objekt-orientiertes Muster? 
- Warum gibt in Clojure dieses Muster nicht?

")

(pres "
#Funktionen höherer Ordnung in Clojure

- Funktionen, die Funktionen erzeugen
- Funktionen, die Funktionen als Argument erwarten

")

;; Funktionen, die Funktionen erzeugen

; partial
(def add-two (partial + 2))

(add-two 7)

(add-two 7 8)

;; Diskussion Currying

(pres "
# Diskussion: Currying

Man kann aus einer Funktion mit n Variablen stets
eine Folge von Funktionen mit einer Variablen machen:

Beispiel:

    (addiere 1 2)
    -> ((addiere 1) 2)

`(addiere 1)` ist die Funktion, die ein Argument erwartet
und dieses zu 1 addiert.

Diese Technik, Funktionen mehrerer Variablen in Funktionen
mit einer Variablen sukzessive aufzulösen hat Moses Schönfinkel
1924 entwickelt.

Später hat Haskell Brooks Curry dies aufgegriffen und systematisch
ausgearbeitet, deshalb spricht man von _Currying_, dabei müsste
es wohl _Schönfinkeln_ heißen.
")

(pres :add "
## in Clojure:

- Da Clojure variadische Funktionen unterstützt (und bevorzugt)
 wird die definierte Funktion für die jeweils angegebene Zahl von Parametern
 aufgerufen
- Es gibt aber die Funktion `partial` mit der man explizit eine
 Funktion mit einem Parameter weniger erzeugen kann
- Man kann auch variadische Funktionen definieren, die selbst Currying machen,
 siehe [Diskussion in der Clojure Google Group](https://groups.google.com/forum/#!topic/clojure/cE2FUrkPW8I) 
")

;; Weiter mit Funktionen, die Funktionen erzeugen
; comp
(def countif (comp count filter))

countif

(doc filter)
(doc count)
(doc comp)

; countif führt erst filter aus, erwartet also zwei Argumente: pred coll
; danach kommt count dran, was auf des Ergebnis von filter angewandt wird

(countif even? [1 2 3 4 5 6 7])
; => 3

(countif #(> % 1) [1 2 3 4 5 6])
; => 5 Zahlen in der Kollektion sind > 1

; complement
(def not-empty? (complement empty?))

(empty? [])
(not-empty? [])

(empty? [1 2 3])
(not-empty? [1 2 3])

(not (empty? [1 2 3]))
(not 12)
(not false)

; constantly
(def two (constantly 2))

(two 1 2 3 4 5 6)

(two "hallo")

; Funktionen, die Funktionen erwarten

(filter even? [1 2 3 4 5 6])
(filter odd? [1 2 3 4 5 6])

(remove even? [1 2 3 4 5 6])

(doc remove)

; map mit Funktion mit 1 Argument
(map inc [1 2 3 4 5])

; map mit Funktion mit 2 Argumenten
(map + [1 2 3] [4 5 6])
(map + [1 2 3 12] [4 5 6])

; reduce ohne Startwert
(reduce + [1 2 3 4 5])

(doc reduce)

; reduce mit Startwert
(reduce + 10 [1 2 3 4 5])

; iterate
(take 10 (iterate #(* 2 %) 2))

(take 10 (iterate #(+ 7 %) 7))

(doc iterate)

; repeatedly

(take 6 (repeatedly #(rand-int 49)))

(repeatedly 6 #(rand-int 49))
; wenn man so Lotto-Zahlen erzeugt, was könnte passieren?

(take 6 (distinct (map inc (repeatedly #(rand-int 49)))))

(doc distinct)

; ---------------------------------------------------------------------------------
(pres "
#Kontrollkonstrukte in Clojure (Teil 1)

- Bedingte Auswertung
- Lokale Bindung
- Konstrukte für Seiteneffekte

")

;; Bedingte Auswertung ---------------------------------

; if
(defn abs
  "(abs n) returns absolute value of number n"
  [n]
  (if (< n 0)
    (- n)
    n))
 
; Schablone für if:
(if test
  konsequenz
  alternative)

(abs 2)

(abs -2)

(abs -7.345)

(if (not true) 
  "das ist wahr")

; nil und false sind false
(if nil :t :f)
; => :f
(if false :t :f)
; => :f

; alles andere ergibt true
(if 42 :t :f)
(if (= 1 1) :t :f)
(if "hallo" :t :f)

; auch ein falsches Java false ergibt true
(def falsefalse (new Boolean false))
(if falsefalse :t :f)
; => :t Warum??

(javadoc Boolean)
; "It is rarely appropriate to use this (Boolean) constructor"

(if Boolean/FALSE :t :f)
; => :f

; kein else definiert
(if false :t)
; => nil

; when ----------------------------------
(defn pos 
  "1 if number n is positiv, otherwise nil"
  [n]
  (when (> n 0) 1))

(doc when)

(pos 1)
(pos 0)

; kann auch mehrere Ausdrücke in implizitem do auswerten
(defn is-pos [n]
  (when (> n 0) 
    (println n "is positive")
    true))

(is-pos 1)
(is-pos 0)

; cond ---------------------------------
(defn sign [x]
  (cond
    (< x 0) -1
    (> x 0) 1
    :else 0))


(doc cond)

(sign 5)
(sign -4.5)
(sign 0/1)
(sign 1/2)

; condp ---------------------------------
(defn one2three [n]
  (condp = n
    1 :one
    2 :two
    3 :three
    :unknown))

(one2three 1)

(one2three 4)

(doc condp)

(defn some' [coll]
  (condp some coll
    #{6} :>> inc
    #{4} :>> dec
    #{1} :>> #(+ % 3)) )

(some #{6} [1 2 3 4])
; => nil
(some #{4} [1 2 3 4])
; => 4

(doc some)

(some' [1 2 3 4])
; => 3

(some' [6 7 8])
; => 7

; case ---------------------------------
(defn one2four [n]
  (case n
    1 :one
    2 :two
    3 :three
    4 :four
    "hallo" :hallo
    :unknown))
    
(one2four 4)

(one2four "hallo")

(one2four "bye")


;; Lokale Bindung mit let

; let ---------------------------------------------------------------------------

(def x1 42)

x1

(let [x1 43] x1)

x1

(let [x1 43]
  (println "erste Ebene" x1)
  (let [x1 44] (println "zweite Ebene" x1))
  (println "wieder zurück" x1))  

x1

;; Beispiel
; Berechnung der Nullstellen von f(x) = x^2 + px + q
; nämlich:
; x_{1,2} = - \frac{p}{2} \pm \sqrt{{(\frac{p}{2})}^2 - q }

; Beispiel, wie man let gut einsetzen kann
(defn nullstellen
  "(nullstellen p q) berechnet die Nullstellen von x^2 + px + q"
  [p, q]
  (let [diskr (- (/ (* p p) 4) q)  ; Diskriminante
        dsqrt (Math/sqrt diskr)
        ph  (- (/ p 2))]
    [(+ ph dsqrt), (- ph dsqrt)]))
    
; Beispiele
(nullstellen -6 -16)
; hat zwei Nullstellen, nämlich 8 und -2

(nullstellen -4 5)
; hat keine Nullstellen

(nullstellen -4 4)
; hat nur eine Nullstelle, nämlich 2

; Probe:
; Wir definieren eine Funktion, die zu p und q eine quadratische Funktion erzeugt:
(defn qf [p q]
  (fn [x] (+ (* x x) (* p x) q)))

; Nun können wir nachprüfen:
((qf -6 -16) 8) 
((qf -6 -16) -2)
((qf -4 4) 2)

; Unterschied zwischen let und mit def definierten globalen Variablen:

(let [x 2]
  (defn add-two [y] (+ y x)))

(add-two 3)
;=> 5

(def x 2)

(defn add-two' [y] (+ y x))

(add-two' 3)
;=> 5

(def x 42)
(add-two' 3)
;=> 45

(add-two 3)
; => 5

; Wird in der Definition der Funktion die globale Variable x
; verwendet, findet dynamische Bindung, nicht lexikalische Bindung
; statt!!!

(def ^:dynamic *dynamic* 42)

*dynamic*
(binding [*dynamic* 43] *dynamic*)

*dynamic*

; Threading Macros -------------------------------------------------------------------------------

(pres "
#Threading Macros

- Ausdrücke muss man in LISP immer von innen nach außen lesen.
- Das kann mühsam sein:        

        (dec (/ (+ 5 3) 2))
 
- Die sogenannten Threading Macros erlauben es Werte ''in Funktionen zu stopfen''

        (-> 5 (+ 3) (/ 2) dec)        
  
- Es gibt       

        (-> ...)         thread-first
        (->> ...)        thread-last
        (as-> ...)       thread-as        
        (some-> ...)
        (some->> ...)
        (cond-> ...)          
 
- siehe [Threading Macros Guide](https://clojure.org/guides/threading_macros)
")

(dec (/ (+ 5 3) 2))
; => 3

(-> 5 (+ 3) (/ 2) dec)
; => 3

; Was passiert?
(-> 5 (+ 3))
; => 8, nämlich (+ 5 3)

(-> 8 (/ 2))
; => 4, nämlich (/ 8 2) 

(-> 4 dec)
; => 3, nämlich (dec 4)

; Im Unterschied hierzu

(->> 5 (+ 3) (/ 2) dec)
; => -3/4

(->> 5 (+ 3))
; => 8, nämlich (+ 3 5)

(->> 8 (/ 2))
; => 1/4, nämlich  (/ 2 8)

(->> 1/4 dec)
; => - 3/4, nämlich (dec 1/4)

; besonders praktisch beim Verwenden von Java-Funktionen:

(second (.split (.replace (.toUpperCase "abcd") "B" "2") ""))
; => 2

(-> "abcd"
    .toUpperCase
    (.replace "B" "2")
    (.split "")
    second)
; => 2

; Mehr zu den anderen Makros
; (https://clojure.org/guides/threading_macros)


; Seiteneffekte ----------------------------------------------------------------------------------
(pres "
#Seiteneffekte

- Eine Funktion wie _f(x)_ = _x^2_ nennt man eine _reine_ Funktion:     
  sie wird immer zu einem gegebenen _x_ denselben Wert berechnen, man kann sie also
  wie einen Wert verwenden ( _Referenzielle Transparenz_ ).
- Hätte Clojure nur reine Funktionen, dann könnten wir nur Programme erstellen, 
  die eine _without output machine_ sind und eine _without input machine_ (Umberto Eco)
- Deshalb gibt es in Clojure Funktionen, die _Seiteneffekte_ erzeugen können, 
  wie zum Beispiel Ausgaben auf der Konsole oder in einer GUI
- Wir werden später sehen, dass Clojure ein ausgefeiltes Konzept hat mit 
  Zustandsänderungen umzugehen.
  
")

(do
  (println "Dieser Ausdruck produziert einen Seiteneffekt")
  (println "Hello Clojure")
  (println "Folgender Ausdruck ist sinnlos")
  (+ 2 3)
  (println "Der letzte Ausdruck wird zurückgeliefert")
  6)
  
(dotimes [n 5]
  (println "Aktueller Wert ist" n))

(doseq [x [1 3 5] y [2 4]]
  (println "Produkt" x "und" y "ergibt" (* x y)))

(defn square
  "Quadriert -- mit Log des Funktionseinstiegs"
  [x]
  (println "Quadriere" x)
  (* x x)) 

(square 3)

; Man kann den Trace leicht ausschalten mit #_

; Reine Funktionen ------------------------------------------------------------------------------
(pres "
#Funktionale Prinzipien

##Reine Funktionen und Werte

Reine Funktionen . . .

- sind nur sinnvoll, wenn alle „Objekte“ Werte sind 
- sind _referenziell transparent_
- können leicht als korrekt bewiesen werden
- sind einfacher zu testen
- ihre Ergebnisse können gechacht werden ( _memoization_ )
- können leichter parallelisiert werden

")

(doc memoize)

; -----------------------------------------------------------------------------------------------
(pres "
#Funktionale Prinzipien

##Kombination und Abstrakton

A powerful programming language is more than just a means for instructing 
  a computer to perform tasks. The language also serves as a framework within 
  which we organize our ideas about processes. [...] 
  
Every powerful language has three mechanisms for accomplishing this:

- **primitive expressions**, which represent the simplest entities the language is concerned with,
- **means of combination**, by which compound elements are built from simpler ones, and
- **means of abstraction**, by which compound elements can be named and manipulated as units

(Harold Abelson, Gerald J Sussman: _Structure and Intewrpretation of Computer Programs_)
")

; eval und apply --------------------------------------------------------------------------------
(pres "
#Funktionale Prinizipien

##_eval_ und _apply_

- Daten = Code ( _Homoikonizität_ )
- `eval` verwendet Daten wie Code    
  eine REPL kann man im Prinzip in Clojure selbst mit `eval` schreiben
- Eine Liste `(fn a1 a2 ...)` wird ausgewertet, indem    
    - das erste Symbol ausgewertet wird (was eine Funktion ergeben muss)
    - die Argumente ausgewertet werden
    - dann die Funktion auf die Argumente angewandt wird ( nennt man im LISP-Jargon auch _apply_ )
- Davon unterschieden: Die Funktion `apply` von Clojure wendet eine Funktion auf eine Kollektion an, 
  indem sie die Elemente als Argumente nimmt
")

(def data '(let [a 10] (+ (* a 2) (* (inc a) 2)))) 
; data ist eine Liste von Listen

data
; evaluiert zu dieser Liste

(eval data)
; verwendet die Daten als Code

(def numbers '(1 2 3 4 5))
; numbers ist eine Liste von Zahlen

numbers
; evaluiert zu einer Liste

(apply + numbers)
(apply +  6 numbers)
(apply + 1 2 3 4 5 '())
; wendet die Funktion + auf die (ausgepackte) Liste an

(reduce + numbers)
 
(doc apply)

(max [1 2 3])
; => [1 2 3]

(apply max [1 2 3])
; => 3, denn apply packt Vektor aus

