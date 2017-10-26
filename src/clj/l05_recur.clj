; Programmierung in Clojure Vorlesung 5
; Rekursion
; (c) 2013 - 2017 by Burkhardt Renz, THM

(ns clj.l05-recur
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Rekursion")

;------------------------------------------------------------------------
(pres "
# Die Fakultätsfunktion


![Definition Fakultät](resources/fac.jpg)


")

; Die Definition kann man nun einfach nachbauen;

(defn fac1 [n]
  "recursive version of factorial (fac1 n) -> n!"
  (if (<= n 1)              ; Abbruchkriterium
    1                       ; Wert für den Basisfall
    (* n (fac1 (dec n)))))  ; Rekursion mit einem neuen Wert, der Richtung Abbruch führt

(fac1 0)
; => 1

(fac1 1)
; => 1

(fac1 10)
; => 3628800

(pres :add "
# Blaupause für Rekursion

    (if (Abbruchkriterium)
      Wert für den Basisfall
      Rekursion mit einem neuen Wert, 
         der Richtung Abbruch führt)
")

(fac1 21)
; Hoppla!
; => ArithmeticException integer overflow

(fac1 21N)
; => 51090942171709440000N

(class 21)
; => java.lang.Long

(class 21N)
; => clojure.lang.BigInt

(doc dec')
(doc *')

; Zweiter Ansatz mit beliebiger Präzision:

(defn fac2 [n]
  (if (<= n 1)
    1
    (*' n (fac2 (dec' n)))))

(fac2 21)

(fac2 50)
; => 30414093201713378043612608166064768844377641568960512000000000000N

(fac2 100)

(fac2 1000)

(fac2 50000)
; Hoppla!
; => StackOverflowError 

;------------------------------------------------------------------------
(pres "
#Analyse des Stacks bei `fac2`

Die Berechnung geht im Prinzip so:

> `(4 * ( 3 * (2 * 1)))`

d.h. die Aufrufhierarchie sieht so aus:

> `   (fac2 4)`    
> `=> (* 4 (fac2 3))`    
> `=> (* 4 (* 3 (fac2 2)))`   
> `=> (* 4 (* 3 (* 2 (fac2 1)))) ;Basisfall erreicht`  
> `=> (* 4 (* 3 (* 2 1)))`  
> `=> (* 4 (* 3 2))`  
> `=> (* 4 6)`  
> `=> 24)`  

")

; Geht das auch anders? - Diskussion

;------------------------------------------------------------------------
(pres :add "
#Andere Berechnung der Fakultät:

Wir können auch so rechnen:

> `((((1 * 4) * 3) * 2) * 1)`

Was ist passiert?

- Jetzt sind die vielen Klammern nicht mehr rechts sondern links.
- Dafür brauchen wir in jeder Berechnung die Akkumulation der bisherigen Berechnung
- Damit erreichen wir, dass keine Berechnung mehr ausgeführt werden muss, 
  wenn die rekursive Funktion zurückkommt.
  
Setzen wir das mal um:  
")


; Endrekursion

; wir führen einen Akkumulator ein, der dazu führt, dass
; keine Berechnung mehr durchgeführt werden muss, wenn die rekursive
; Funktion zurückkommt

(defn fac3
  ([n] (fac3 n 1))
  ([n acc]
    (if (<= n 1)
      acc
      (fac3 (dec n) (*' n acc)))))

(fac3 4)
(fac3 10)

(fac3 50000)
; Hoppla! 
; => StackOverflowError Oups!!
; Aber warum???

;------------------------------------------------------------------------
(pres "
#Endrekursion in Clojure

Warum ist das Problem nicht verschwunden?

- Es gibt Compiler und Laufzeitsysteme (z.B. für Scheme), die automatische
 Erkennung von Endrekursion haben, sogenannte _tail call optimization_.
- Die JVM kann das nicht.
- Nun könnte der Clojure-Compiler trotzdem Endrekursion erzeugen.
- Beim Design von Clojure hat sich Rich Hickey entschieden, ein eigenes
  Konstrukt einzuführen, so dass der Compiler sieht, was der Entwickler 
  beabsichtigt.
- Das Konstrukt ist `loop - recur`  

")

; fac3 mit recur
(defn fac3'
  ([n] (fac3' n 1))
  ([n acc]
    (if (<= n 1)
      acc
      (recur (dec n) (*' n acc)))))

(fac3' 50000)
; => eine sehr große Zahl

; loop/recur
; Die ultimative Fassung
(defn factorial [n]
  "tail recursive version of n!"
  (loop [cur n, acc 1]
    (if (<= cur 1)
      acc
      (recur (dec cur) (*' cur acc)))))

(factorial 4)
; => 24

(factorial 50000)
; => eine sehr große Zahl


;------------------------------------------------------------------------
(pres "
#Wo ist die Schleife geblieben?

Es geht aber auch noch ganz anders:

- Clojure bringt eine ganze Reihe von Funktionen mit, die Folgen (sequences) verwenden. 
- Damit kann man sehr eleganten Code schreiben, der den _Loop_ zum Verschwinden bringt:

")

; fac4
(defn fac4 [n]
  (reduce *' (range 1 (inc n))))

(fac4 4)
; => 24

(fac4 50000)
; => eine sehr große Zahl

; Vergleich der Laufzeit

(time
  (dotimes [_ 10000]
    (factorial 100)))
; => 77 msecs

(time
  (dotimes [_ 10000]
    (fac4 100)))
; => 63 msecs


; Wechselseitige Rekursion -------------------------------
(pres "
#Wechselseitige Rekursion ( _mutual recursion_ )

##Beispiel: Partität einer ganzen Zahl

###Definition

Die _Parität_ einer ganzen Zahl ist die Eigenschaft gerade oder ungerade zu sein.

In Clojure gibt es die Funktion `even?` dafür

In unserem Beispiel wollen wir aber mal eine rekursive Funktion dafür definieren
")

(doc even?)

; Probieren wir mal dies selbst (naiv) zu programmieren:

(defn is-even? [n]
  (if (zero? n) ;; 0 ist gerade
    true
    (if (pos? n) ;; bei positiven Zahlen 
      (is-odd? (dec n)) ;; gucken wir ob (dec n) ungerade ist
      (is-odd? (inc n))))) ;; sonst ob (inc n) ungerade ist
; Nanu!?

; Klar:
; Wir brauchen eine Deklaration, weil wir is-odd? verwenden
; ehe die Funktion definiert ist

(declare is-odd?)

(defn is-even? [n]
  (if (zero? n)
    true
    (if (pos? n)
      (is-odd? (dec n))
      (is-odd? (inc n)))))

(defn is-odd? [n]
  (if (zero? n)
    false
    (if (pos? n)
      (is-even? (dec n))
      (is-even? (inc n)))))

; is-even? und is-odd? rufen sich gegenseitig auf

(is-even? 10000)
; => StackOverflowError

; Aussichtslos?

; Clojure hat eine Funktion trampoline

(doc trampoline)

; Vorbereitung für trampoline
(declare is-odd?)

(defn is-even? [n]
  (if (zero? n)
    true
    (if (pos? n)
      #(is-odd? (dec n))
      #(is-odd? (inc n)))))

(defn is-odd? [n]
  (if (zero? n)
    false
    (if (pos? n)
      #(is-even? (dec n))
      #(is-even? (inc n)))))

; Durch das Reader-Makro # wird bei der Evaluation nicht
; die Funktion is-odd? bzw. is-even? aufgerufen, sondern
; zu einer Funktion evaluiert, deren Körper is-odd? bzw.
; is-even? ist. Die Funktion trampoline verwendet dann diese
; eingebettete Funktion

(trampoline is-even? 10000)
; => true

(trampoline is-even? 100001)
; => false

; Natürlich ist das keine besonders intelligente Weise festzustellen,
; ob eine Zahl gerade oder ungerade ist:

(source even?)

(source trampoline)

; Lektüre ------------------------------------------------
(pres "

#Lektüre zum Thema Rekursion

Gerald Jay Sussman und Harold Abelson:      
[_Structure and Interpretation of Computer Programms_](http://mitpress.mit.edu/sicp/full-text/book/book.html),      
Section 1.2 Procedures and the Process They Generate

")
