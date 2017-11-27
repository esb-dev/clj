; Programmierung in Clojure Vorlesung 7
; Kollektionen und Folgen (sequences)
; (c) 2014 - 2017 by Burkhardt Renz, THM

(ns clj.l07-seq
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop
; B zurück zur letzten Seite!
 
(init "Kollektionen und Folgen ( _sequences_ )")

(pres :add "
#Clojures Design als Sprache

> ''Having an open, and large, set of functions operate 
  upon an open, and small, set of extensible abstractions 
  is the key to algorithmic reuse and library interoperability.''     
  Rich Hickey
  
> ''It is better to have 100 functions operate on one data 
  structure than to have 10 functions operate on 10 data structures.''     
  [Alan J. Perlis](http://pu.inf.uni-tuebingen.de/users/klaeren/epigrams.html)
  
")

(pres "
#Spezifische vs generische Datenstrukturen - ein Vergleich

Java

    public class Person {
      private String name;
      private String vorname;
      private Date   gebdatum;
  
      public Person(String name, String vorname, ...) {...};
  
      public String getName(){....};
      public void setName(String name){....};
      
Clojure
      
      {:name \"Schneider\", :vorname \"Klaus\", :gebdatum \"1993-05-06\"}
  
")


; Kollektionen -------------------
(pres "
#Die Abstraktion _collection_

Alle Datenstrukturen in Clojure können als _collection_ betrachtet werden:

- `conj` fügt ein Element hinzu
- `count` zählt die Zahl der Elemente
- `empty` erzeugt eine leere Kollektion
- `=` vergleicht (Kollektionen sind Werte!)
- `seq` liefert eine Folge der Elemente der Kollektion

Clojure hat eine einheitliche Art und Weise mit solchen Datenstrukturen als ganzen
elementweise umzugehen, die Abstraktion _sequence_ (Folge), 
alle solche Datenstrukturen sind _seqable_.
")

; Das Seq Interface --------------
(pres "
#Das _Seq_ Interface

- `(first coll)` Erstes Element der Kollektion
- `(rest coll)`  Rest der Kollektion als Folge, eventuell leer
- `(next coll)`  wertet erstes Element des Rests aus
- `(cons item seq)` Folge mit `item` als erstem Element und `seq` als Rest

")

(first '(1 2 3 4))
; => 1

(rest '(1 2 3 4))
; => (2 3 4)

(next '(1 2 3 4))
; => (2 3 4)

(first '[1 2 3 4])
; => 1

(rest '[1 2 3 4])
; => (2 3 4) -- eine seq!

(first {:a 1 :b 2 :c 3})
; => [:a 1]

(rest {:a 1 :b 2 :c 3})
; => ([:b 2] [:c 3])

(def s1 (seq [1 2 3]))

(def s1' (cons 0 s1))

s1 

s1'

(class s1)
;=> clojure.lang.PersistentVector$ChunkedSeq

(class s1')
; => clojure.lang.Cons
; nicht gerade konsistent!

(class (seq s1'))
; => immer noch clojure.lang.Cons
; Vorsicht mit cons bei Funktionen, die seq verwenden

; rest versus next

(rest [])
; => () d.h. ergibt eine leere Sequenz, aber nicht nil

(next [])
; => nil d.h. next schaut nach, was das erste Element des Rests ist, nämlich nichts

(seq (rest []))
; => nil, denn (next %) = (seq (rest %))

(rest nil)
; => ()

(next nil)
; => nil

; Warum? Wenn man wirklich ganz verzögert (lazy) sein will, dann will man
; nicht das erste Element des Rests ansehen, um zu sehen, ob es gar nicht da ist,
; so wie next das tut.

(pres :add "

# Folgen und Iteratoren

  Seqs haben eine gewisse Ähnlichkeit zu Iteratoren, haben aber ein funktionales Konzept.
  
> ''Seqs differ from iterators in that they are persistent 
  and immutable, not stateful cursors into a collection. 
  As such, they are useful for much more than foreach - 
  functions can consume and produce seqs, they are 
  thread safe, they can share structure etc.''      
  (Aus der Dokumentation von Clojure)
")

; Folgen und Iteratoren ----------
(pres "
# Folgen und Iteratoren

Das Konzept von Sequences kommt von LISP, dort aber ist die Sequence definiert auf einer
konkreten Datenstruktur, der Liste.

In Clojure ist die _sequence_ ein logisches Konzept, das auf Basis verschiedener konkreter
Datenstrukturen funktioniert.

>  ''A sequence is a sequential _view_ on a collection, it is not the collection itself.''   
  (Meikel Brandmeyer)
  
In Clojure ist alles eine _Folge_. 
")


; Alles ist eine Folge ------------

(seq [1 2 3 4])
; => (1 2 3 4)

(seq #{1 2 3 4})
; => (1 4 3 2)

(seq {:a 1 :b 2 :c 3})
; => ([:a 1] [:c 3] [:b 2])

; Java String
(seq "Alles ist eine Folge")
; => (\A \l \l \e \s \space \i \s \t \space \e \i \n \e \space \F \o \l \g \e)

; Java Collection
(seq (java.util.ArrayList. '(1 2 3 4)))
; => (1 2 3 4)

(seq nil)
; => nil

(seq [])
; => nil

; Ein Missverständnis -----------------
(pres :add "
# Ein Missverständnis

Im Internet findet man immer wieder Äußerungen wie

> \". . . has the weird result of making  
  `(map #(* 2 %) [1 2 3])`   
   a lazy-list instead of a vector.\"      
   (from stackoverflow)
   
In Clojure muss man unterscheiden, ob eine Funktion die _konkrete_ Datenstruktur zurückgibt, 
oder die _Abstraktion_ Folge. Dies macht die ganzen Funktionen so leicht kombinierbar.

Wenn man als Ergebnis wieder einen Vektor braucht, muss man aus der Folge einen machen, 
oder eine Funktion verwenden, die auf der konkreten Datenstruktur arbeitet, wie etwa `mapv`.

")

(class (map #(* 2%) [1 2 3]))
; => Ergebnis ist eine Folge, kein Vektor

(mapv #(* 2%) [1 2 3])
; => [2 4 6] Ergebnis ist ein Vektor

(vec (map #(* 2%) [1 2 3]))
; => [2 4 6]

; Mit Clojure 1.7 gibt es Transducer, die Transformationen
; konkreter Datenstrukturen erlauben, ohne an die konkrete
; Datenstruktur gebunden zu sein -- sehen wir in der nächsten Vorlesung

; Verzögerte Folgen (Lazy sequences) ------
(pres "
# Verzögerte Folgen  

Das Auswertungsmodell, das wir bisher bei Clojure kennengelernt haben, ist _strikte_ Auswertung,
d.h. zuerst werden die Parameter ausgewertet, dann die Werte an die Funktion
übergeben. Man nennt diese Auswertungsstrategie auch _eager_.

Dem steht gegenüber die verzögerte Auswertung ( _lazy_ ), die einen Wert erst dann erzeugt,
wenn er auch wirklich gebraucht wird.

Clojure verwendet diese Strategie bei den sogenannten _lazy sequences_, wobei eine Sequenz
nicht wirklich realisiert wird. Man muss sich das so vorstellen: Bei der Definition der
Folge wird nicht die Folge selbst erzeugt, sondern eine Funktion, die die Folge erzeugen
kann. Wird ein Element der Folge benötigt, dann wird diese Funktion aufgerufen
(Clojure tut dies nicht einzeln pro Element, sondern in Blöcken (chunks)).

")

(doc iterate)

; -------------------------
; clojure.core/iterate
; ([f x])
;   Returns a lazy sequence of x, (f x), (f (f x)) etc. f must be free of side-effects

; besser nicht ausführen: dauert sehr lange und führt zu Exception!
(iterate inc 1)
; => OutOfMemoryError Java heap space 

; besser so:
(take 10 (iterate inc 1))

(def natural-numbers (iterate inc 1))

(take 10 natural-numbers)

; Beispiel für eine selbst programmierte verzögerte Folge -----------

; Wir wollen nun an einem Beispiel eine lazy sequence selbst programmieren und
; ihr Verhalten untersuchen (aus Clojure Programming S. 93)

(defn random-ints
  "Erzeugt eine verzögerte Folge von Zufallszahlen und protokolliert die Erzeugung"
  [limit]
  (lazy-seq
    (println "ich erzeuge eine Zufallszahl")
    (cons (rand-int limit) (random-ints limit))))
;=> #'fpc.vl09/random-ints

(doc cons)
;-------------------------
;clojure.core/cons
;([x seq])
;  Returns a new seq where x is the first element and seq is
;    the rest. 

;(random-ints 49)
;=> OutOfMemoryError Java heap space  java.util.Arrays.copyOf (:-1) 

; Dieser Aufruf war keine gute Idee: random-ints erzeugt Zufallszahlen ohne Ende!

; Zwei Möglichkeiten:
; Die Funktion (take n ...) gibt die ersten n Elemente der Folge aus
; Die dynamische Var *print-length* sorgt dafür, dass die REPL nach dieser Zahl aufhört

(take 10 (random-ints 49))

(set! *print-length* 10)
(random-ints 49)

; Nun wollen wir beobachten, wie die Zufallszahlen erzeugt werden:

(def rands (random-ints 49))
;=> #'fpc.vl09/rands

(realized? rands)

; es gibt keine Meldung, dass eine Zufallszahl erzeugt wurde
; wir sehen also, dass keine Auswertung der Funktion erfolgt ist

(first rands)
;=> ich erzeuge eine Zufallszahl
;=> 24

(nth rands 3)
;=> ich erzeuge eine Zufallszahl
;=> ich erzeuge eine Zufallszahl
;=> ich erzeuge eine Zufallszahl
;=> 10

; man sieht wie die verzögerte Auswertung erfolgt

; Funktionen für Folgen ------------------------------------------------------------------------

; Folgen erzeugen ---------------------------

; range
(range 10)
; => (0 1 2 3 4 5 6 7 8 9)

(range 2 11 2)
; => (2 4 6 8 10)

(doc range)

; repeat
(repeat 5 "hallo")
; => ("hallo" "hallo" "hallo" "hallo" "hallo")

; iterate
(take 5 (iterate #(/ % 2) 1))
; => (1 1/2 1/4 1/8 1/16)

; cycle
(take 5 (cycle [:a :b]))
; => (:a :b :a :b :a)

; interleave
(interleave [:a :b :c :d] [1 2 3 4]) 
; => (:a 1 :b 2 :c 3 :d 4)

(apply hash-map (interleave [:a :b :c :d] [1 2 3 4]))
; => {:a 1, :c 3, :b 2, :d 4}

(interleave [:a] #{1 2 3})
; => (:a 1)

; interpose
(interpose "," ["MNI" "LSE" "EW"])
; => ("MNI" "," "LSE" "," "EW")

(require '(clojure [string :as str]))

; join
(str/join ", " ["MNI" "LSE" "EW"])
; => "MNI, LSE, EW"

; Folgen filtern ---------------------------------

; filter
(filter even? (range 10))
; => (0 2 4 6 8)

(filter odd? (range 10))
; => (1 3 5 7 9)

; take-while
; nimmt Buchstaben bis ein Vokal kommt
(take-while (complement #{\a \e \i \o \u}) "Clojure")
; => (\C \l)

; drop-while
; wirft alle Buchstaben weg, bis ein Vokal kommt
(drop-while (complement #{\a \e \i \o \u}) "Clojure")
; => (\o \j \u \r \e) 

; split-at
; trennt am Index n
(split-at 3 [1 2 3 4 5 6])
; => [(1 2 3) (4 5 6)]

; split-with
; trennt wenn Prädikat false wird
(split-with #(<= % 5) (range 15))
; => [(0 1 2 3 4 5) (6 7 8 9 10 11 12 13 14)]

(split-with even? (range 10))

; Prädikate für Folgen ---------------------------------

; every?
(every? odd? [1 2 3])
; => false

(every? even? (range 2 6 2))
; => true

; some
(some even? [1 2 3])
; => true

(some even? [1 3 5])
; => nil

; Warum hat some kein ? am Ende?
; some ist nicht wirklich ein Prädikat, sondern liefert den ersten Wert,
; der nicht nil ist.
; bei even? ist das true!!

; erster Wert, der nicht nil ist
(some identity [nil false 1 nil 2])
; => 1

; not-every?
(not-every? even? [2 4 6 8 9])
; => true

; not-any?
(not-any? even? [1 3 5 7 8])
; => false

; Folgen transformieren -------------------------
(pres "
# Folgen transformieren mit `map` und `reduce`

##Elementweises Ausführen der Funktion `map`

                (map f [e1 e2 e3 e4 ....])
        
                 e1    e2    e3    e4    e5  
                 ↧     ↧     ↧     ↧     ↧
                f(e1) f(e2) f(e3) f(e4) f(e5) 
                
")

; map
(map even? [1 2 3])
; => (false true false)

(map inc [1 2 3])
; => (2 3 4)


(pres :add "

## Folge mit einer Funktion zusammenklappen `reduce`

               (reduce f [e1 e2 e3 e4])
               
               (f (f (f e1 e2) e3) e4)
               
               
                            f
                           / \\
                          f   e4 
                         / \\
                        f   e3
                       / \\
                      e1  e2
")

; reduce
(reduce + (range 101))
;=> 5050

(reduce + (filter even? (range 101)))
;=> 2550

(reduce + (map inc (filter even? (range 101))))
;=> 2601

; mit Startwert
(reduce + 1000 (map inc (filter even? (range 101))))

; reduce in Clojure ist ein left-fold, siehe oben
; bei kommutativen Operatoren spielt das keine Rolle, bei anderen
; schon

(reduce - [0 1 2 3])
;=> -6
; denn:
(- (- (- 0 1) 2 ) 3)
;=> -6

; infix: (((0 - 1) - 2) - 3)

; aber rechts geklammert
; infix: (0 - ( 1 - (2 - 3)))

(- 0 (- 1 (- 2 3)))
; => -2


; Weitere Funktionen der Abstraktion 'seq' ----------------------

; sort
(sort [3 5 7 12 3 2])
; => (2 3 3 5 7 12)

(sort > [3 5 7 12 3 2])
; => (12 7 5 3 3 2)

(sort-by #(.toString %) '(42 1 14 34 341))
; => (1 14 34 341 42)

(sort-by :year [{:name "Chablis" :year 2002} {:name "Pinot noir" :year 2000}])
; => ({:name "Pinot noir", :year 2000} {:name "Chablis", :year 2002})

; for
(for [x (range 3) y (range 3)] (* x y))
; => (0 0 0 0 1 2 0 2 4)

(for [x (range 3) y (range 3)] [x y])
; => ([0 0] [0 1] [0 2] [1 0] [1 1] [1 2] [2 0] [2 1] [2 2])

(for [x [:a :b :c] y (range 1 4)] {x y})
; => ({:a 1} {:a 2} {:a 3} {:b 1} {:b 2} {:b 3} {:c 1} {:c 2} {:c 3})


; Head retension ------------------------------------------
(pres "
# _Head retension_

Man muss achtgeben, dass man nicht unnötig eine Referenz auf eine Folge hält:

      ; Dies besser nicht tun
      (let [[t d] (split-with #(< % 12) (range 1e8))]
        [(count d) (count t)])\n
      ; => OutOfMemoryError Java heap space  -- dauert sehr lange
      
      
      ; besser
      (let [[t d] (split-with #(< % 12) (range 1e8))]
        [(count t) (count d)])
      ; aber - dauert sehr lange!  
")

; Dies besser nicht tun
;(let [[t d] (split-with #(< % 12) (range 1e8))]
;  [(count d) (count t)])
; => OutOfMemoryError Java heap space  -- dauert sehr lange

;(let [[t d] (split-with #(< % 12) (range 1e8))]
;  [(count t) (count d)])
; => dauert mir zu lange!!

; Weitere Abstraktionen in Clojure
(pres "
# Weitere Abstraktionen in Clojure

- Associative
- Indexed
- Stack
- Set
- Sorted

")
; Abstraktion Associative ---------------------------------

; assoc
(def p1 {:name "Schneider" :vorname "Klaus" :alter 26})

p1

(assoc p1 :alter 27 :plz "35491")
; => {:alter 27, :name "Schneider", :vorname "Klaus", :plz "35491"}

(dissoc p1 :vorname)
; => {:alter 26, :name "Schneider"} 

(get p1 :name)
; => "Schneider"

; idomatisch
(:name p1)
; => "Schneider"

(contains? p1 :vorname)
; => true

(contains? p1 :plz)
; => false

; Indexed ----------------------------------------------------

(def v1 [1 2 3 4 5])

(nth v1 0)
; => 1

(nth v1 5)
; => IndexOutOfBoundsException

(nth v1 5 :not-found)
; => :not-found

(nth "Clojure" 0)
; => \C

; Stack --------------------------------------------------------

(def st [1 2 3])

(def st2 (conj st 4))

st2
; => [1 2 3 4]

(pop st2)
; => [1 2 3]

(= (pop st2) st)
; => true

(peek st2)
; => 4

; Set ---------------------------------------------------------

(def set1 #{1 2 3 4})

(disj set1 0)
; => #{1 2 3 4}
; 0 war nicht in der Menge

(disj set1 1)
; => #{2 3 4}

(conj set1 1)
; => #{1 2 3 4}

(conj set1 5)
; => #{1 2 3 4 5}

; Sorted ------------------------------------------------------

(def sset (sorted-set 4 3 5 1 2))

sset
; => #{1 2 3 4 5}

(rseq sset)
; => (5 4 3 2 1)

(subseq sset < 3)
; => (1 2)

(rsubseq sset < 3)
; => (2 1)