; Programmierung in Clojure Vorlesung 6
; Einfache und zusammengesetzte Datentypen
; (c) 2014 - 2015 by Burkhardt Renz, THM

(ns clj.l06-dt
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop

(init "Einfache und zusammengesetzte Datentypen")

; Einfache Datentypen ------------------------------------------------------------------------------
(pres :add "
#Einfache Datentypen

- Wahrheitswerte
- Numerische Literale
- Numerische Operatoren (3 Arten)
- Gleichheit in Clojure
- Geschwindigkeit durch primitive Java-Typen
- Brüche
- Strings
")

; Wahrheitswerte ---------------------------
(pres "
#Wahrheitswerte und `nil`

- `true` und `false` als Literale repräsentieren     
  `java.lang.Boolean/TRUE` bzw.      
  `java.lang.Boolean/FALSE`
- `true?` und `false?` prüfen auf exakt diese Werte.
- `false` und `nil` werden in logischen Auswertungen als false ausgewertet,
  alle anderen Werte als true
- `(boolean x)` erzeugt aus einem logischen Wert einen primitiven Java-Typ
")

(class true)
; => java.lang.Boolean

(identical? true java.lang.Boolean/TRUE)
; => true

(true? (= 1 1))
; => true

(true? 1)
; => false

; aber
(if 1 :true :false)
; => :true

(defn is-true? [x]
  (if x :true :false))

(is-true? "hallo")
; => :true

(is-true? {:one 1})
; => :true

(is-true? 123)
; => :true

(is-true? false)
; => :false

(is-true? nil)
; => :false


; Numerische Literale ----------------------
(pres "
#Numerische Literale

- long (64-bit signed): `42, 0x2a, 2r101010, 052`
- double (64-bit): ` 3.14, 0.271e1`
- clojure.lang.BigInt: `42N`
- java.math.BigDecimal: `2.71M`
- clojure.lamg.Ratio: `22/7`
")

(= 42 0x2a)
; => true

(= 42 2r101010)
; => true

(= 42 052)
; => true

(class 42)
; => java.lang.Long

(class 0.271e1)
; => java.lang.Double

(class 42N)
; => clojure.lang.BigInt

(class 2.71M)
; => java.math.BigDecimal

(class 22/7)
; => clojure.lang.Ratio

; Drei Arten von numerischen Operatoren -------
(pres"
# Drei Arten von numerischen Operatoren

- Automatische Konversion bei Operationen mit verschiedenen Typen   
  long → BigInt → Ratio → BigDecimal → double
- Beschränkte Präzision mit Exception    <br/> 
  `(+ 1 Long/MAX_VALUE)`
- Beliebig genaue Präzision     <br/>
  `(+’ 1 Long/MAX_VALUE)`
- Beschränkte Präzision mit Overflow <br/>
  `(unchecked-add 1 Long/MAX_VALUE)`
")

; Automatische Konversion
(class (+ 1 1N))
; => clojure.lang.BigInt

(class (+ 1 1.0))
; => java.lang.Double

(class (+ 1 3/2))
; => clojure.lang.Ratio

(class (+ 3/2 1M))
; => BigDecimal

(class (+ 1.0 1M))
; => java.lang.Double

(def gl Long/MAX_VALUE)

gl
; => 9223372036854775807

(+ 1 gl)
; => ArithmeticException integer overflow

(+' 1 gl)
; => 9223372036854775808N

(+ 1N gl)
; => 9223372036854775808N

(unchecked-add 1 9223372036854775807)
; => -9223372036854775808

(unchecked-add 1 Long/MAX_VALUE)
; => -9223372036854775808

(unchecked-add 1 (long gl))
; => -9223372036854775808

; aber:
(unchecked-add 1 gl)
; => ArithmeticException integer overflow
; das liegt wohl daran, dass gl gar kein primitives long ist, sondern ein Long!!


; Gleichheit ---------------------------------------
(pres "
# Drei Arten von Gleichheit

- Erinnerung an Java: <br/>
  `==` versus `obj.equals(obj')` 
- `(identical? x y)` <br/> 
  Gleiches Objekt?
- `(= x y & more)` <br/>
  Gleicher Wert und Typ?
- `(== x y & more)` <br/>
  Gleicher numerischer Wert, typ-unabhängig
")

(identical? 1 1)
; => true

(identical? 1234567 1234567)
; => false

(= 1234567 1234567)
; => true

(= 1234567 1234567.0)
; => false

(== 1234567 1234567.0)
; => true

(== 1.5 3/2)
; => true

(= 1.5 3/2)
; => false

; Primitive Typen -----------------------------------
(pres"
#Geschwindigkeit durch primitive Typen

![Mandelbrot](resources/mandelbrot.jpg)

siehe `com.clojurebook.mandelbrot.clj`
")

; Brüche --------------------------------------------
(pres "
#Brüche, rationale Zahlen

- Clojure hat einen speziellen Datentyp für Brüche (rationale Zahlen)
- denn: Gleitkommazahlen eignen sich nicht für Brüche  <br/>     
  `(+ 0.1 0.1 0.1)` vs `(+ 1/10 1/10 1/10)`
- `(class 1/10)`      <br/>
  `=> clojure.lang.Ratio`
- Rechnen mit Brüchen     <br/>
  `(/ 5 15) => 1/3`     <br/>
  `(numerator 1/3) => 1` <br/>
  `(denominator 1/3) => 3` 
- Erzeugen von Brüchen aus Gleitkommazahlen      <br/>
  `(rationalize 0.3) => 3/10`
- Konversion von Brüchen     
  `(double 1/3) => 0.3333333333333333` <br/>
")

(+ 0.1 0.1 0.1)
; => 0.30000000000000004

(+ 1/10 1/10 1/10)
; => 3/10

(class 1/10)
; => Clojure.lang.Ratio

5/15
; => 1/3

-5/15
; => -1/3

(/ 5 15)
; => 1/3

(numerator 1/3)
; => 1

(denominator 1/3)
; => 3

(+ 1 3/2)
; => 5/2

(rationalize 0.3)
; => 3/10

(double 1/3)
; => 0.3333333333333333


; Strings und Characters -----------------------------------------
(pres "
#Strings und Characters

- Clojure Characters sind Java Characters     <br/>
  Literal: `\\n`, `\\newline`
- Clojure Strings sind Java Strings 
- Verwendung von Java-Funktionen für Strings     <br/>
  `(.toUpperCase \"hello\")`
- Erzeugen von Strings      <br/>
  `str` und `format`
- String-Funktionen     <br/>
  `subs`, `clojure.string.*`
- Clojure kann Strings als Folgen von Buchstaben behandeln

")

; Der Buchstabe n
\n

; Sonderzeichen
\newline

(char-name-string \newline)
; => "newline"

(char-escape-string \newline)
; => "\\n"

(.toUpperCase "hello")
; => "HELLO"

(str 12)
; => "12"

(str 1 2 3 4 5 6)
; => "123456"

(str \h \e \l \l \o)
; => "hello"

(str 1 \h 2 \g)
; => "1h2g"

(str "Hello" "Clojure")
; => "HelloClojure"

(println "Hello" "Clojure")
; => nil
; Ausgabe: Hello Clojure

(format "%04d-%02d-%02d" 2015 01 11)
; => "2015-01-11"

; Lade clojure.string, Zugriff mit alias str
(require '(clojure [string :as str]))

(str/blank? "")
; => true

(str/blank? " ")
; => true

(str/blank? "\t\n ")
; => true

(str/capitalize "hello")
; => "Hello"

(str/capitalize "HELLO")
; => "Hello"

(str/capitalize "ärger")
; => "Ärger"

(str/lower-case "Ärger mit Umlauten?")
; => "ärger mit umlauten?" 

(str/upper-case "Ärger mit Umlauten?")
; => "ÄRGER MIT UMLAUTEN?"

(str/join "," [1 2 3])
; => "1,2,3"

(str/replace "Ärger" "Ä" "Ae")
; => "Aerger"

; ---------------------------------------------------------------------------------------------------------------

; Zusammengesetzte Datentypen ----------------------------
(pres"
#Zusammengesetzte Datentypen

- Zusammengesetzte Datentypen in Clojure: Listen, Vektoren, Mengen und Maps
- Zusammengesetzte Datentypen sind _Werte_ 
- Sprechweise: _immutable_ und _persistent_
- „A data structure that supports multiple versions is called persistent“ (Chris Okasaki)
- _structure sharing_ – bei „Änderung“ wird möglichst viel der bisherigen Datenstruktur wiederverwendet
- Garantierte Schranken für die Zugriffszeit

Beispiel für _structure sharing_

![Liste](resources/list.jpg)
")

(def l1 (list 1 2 3))

(def l2 (conj l1 0))

l1
; => (1 2 3)
l2
; => (0 1 2 3)

(def v1 [1 2 3])

v1

(conj v1 0)

; Eigenschaften von Kollektionen -------------------------
(pres"
#Eigenschaften von Kollektionen

- Kollektionen sind nicht typisiert, können jeden beliebigen Wert halten, insbesondere wieder Kollektionen
- `conj` verlängert um weitere Elemente
- `into` nimmt die Elemente einer Kollektion und verlängert damit die andere
- `count`, `empty?` zählt Elemente, ist die Kollektion leer? 
- `empty` „leert“ Kollektion
")

(list 1 "hallo" [1 2 3] (list 1 1 1))
; Listen können beliebige Werte enthalten

; conj

(conj '(1 2 3) :x)
; => (:x 1 2 3) Listen wachsen vorne

(conj [1 2 3] :x)
; => [1 2 3 :x] Vektoren wachsen hinten

(conj {:one 1 :two 2 :three 3} [:new :x])
; => {:new :x, :one 1, :three 3, :two 2}

(conj #{1 2 3} :x)
; => #{1 2 3 :x}

; into

(into '() '(1 2 3))
;= (3 2 1)

(into [1 2 3] [:a :b :c])
;= [1 2 3 :a :b :c]

(into '() [1 2 3])
;= (3 2 1)

(into [] '([1 2 3]))
;= [[1 2 3]]

; count, empty?

(count '(1 2 3))
; => 3

(empty? [])
; => true

(empty? nil)
; => true

(empty? '())
; => true

(empty? [1 2 3])
; => false

; aber:
(empty [1 2 3])
; => []

; Strings verhalten sich wie eine Kollektion
(empty "hallo")
; => nil

(empty? "hallo")
;=> false

(count "hallo")
; => 5

; Funktionen für Listen ------------------------
(pres"
#Funktionen für Listen

- Listen erzeugen <br/>
  `’()`, `list`, `list*`
- Liste als Stack verwenden <br/>  
  `peek`, `pop`
- Ist ein Wert eine Liste? <br/>  
  `list?`
")

'(1 2 3)
; => (1 2 3)

(list 1 2 3)
; => (1 2 3)

(= '(1 2 3) (list 1 2 3))
;=> true

(list 1 [2 3])
; => (1 [2 3])

; im Unterschied dazu packt list* das letzte Argument aus
(list* 1 [2 3])
; => (1 2 3)

(list* 1 '(2 3))
; => (1 2 3)

(list* [1 2 3 4])
;=> (1 2 3 4)

(apply list [1 2 3 4])
;=> (1 2 3 4)


(def l '(1 2 3))

(peek l)
; => 1

(pop l)
; => (2 3)

(list? l)
; => true

(list? [1 2 3])
; => false

; Funktionen für Vektoren -------------------------
(pres"
#Funktionen für Vektoren

- Vektoren erzeugen <br/>
  `[]`, `vector`, `vec`, `vector-of`
- Vektoren verwenden <br/>
  `get`, `nth`, `peek`, `rseq`
- Vektoren 'ändern' <br/>
  `assoc`, `pop`, `subvec`, `replace`
- Ist ein Wert eine Vektor? <br/>  
  `vector?`
")

(def v1 [1 2 3])

v1
; => [1 2 3]

(= v1 (vector 1 2 3))
; => true

(= v1 (vector-of :long 1 2 3))
; => true
; obwohl intern als primitiver Typ gespeichert

(= v1 (vec '(1 2 3)))
; => true

v1

(get v1 2)
; => 3

(nth v1 2)
; => 3

(nth v1 3 :not-found)
; => :not-found

(nth v1 3)
;=> IndexOutOfBoundsException

(peek v1)
; => 3 Beim Vektor als Stack ist oben = hinten, bei der Liste ist oben = vorne

(rseq v1)
; => (3 2 1) eine Folge der Elemente des Vektors in umgekehrter Reihenfolge

(vec (rseq v1))
; => [3 2 1] Vektor in umgekehrter Reihenfolge

v1

(assoc v1 0 :x)
; => [:x 2 3]

(pop v1)
; => [1 2] das oberste = letzte Element fehlt

(subvec v1 1)
; => [2 3]

(subvec v1 0 2)
; => [1 2]

(replace [1 :x] [1 1 2 1 1])
; => [:x :x 2 :x :x]

(vector? [])
; => true

(vector? '())
; => false

(pres "
## Klassische Datenstrukturen in Clojure

- Listen wachsen vorne, sequentieller Zugriff
- Vektoren wachsen hinten, direkter Zugriff
- Queues wachsen hinten, schrumpfen vorne

Clojure hat keine literale Darstellung für Queues, 
es gibt aber eine Implementierung, die man (etwas
umständlich) verwenden kann.
")

;; Queues in Clojure
(def q clojure.lang.PersistentQueue/EMPTY)

(def q1 (conj q 1 2 3 4 5))

(peek q1)

(rest q1)

(peek (pop q1))

(rest (pop q1))

(rest (conj (pop q1) 6))

(peek (conj (pop q1) 6))

(peek (pop (conj (pop q1) 6)))


; Funktionen für Maps --------------------------------
(pres"
#Funktionen für Maps

- Maps erzeugen <br/>
  `{}`, `hash-map`, `sorted-map`  <br/>
  `sorted-map-by`, `zipmap` 
- Maps verwenden  <br/>
  `get`, `contains?`, `find`, `keys`, `vals`
- Maps 'ändern'   <br/>
  `assoc`, `dissoc`, `select-keys`, `merge`, `merge-with` 
- Map-Eintrag verwenden  <br/>
  `key`, `val`
- Ist ein Werte eine Map? <br/>  
  `map?`
")


(def m1 {:one 1, :two 2, :three 3})

m1
; => {:one 1, :three 3, :two 2}

(= m1 (hash-map :one 1 :two 2 :three 3))
; => true

(def sm (sorted-map :one 1 :two 2 :three 3))

sm
; => {:one 1, :three 3, :two 2}

(sorted-map-by > 1 "a", 2 "b", 3 "c")
; => {3 "c", 2 "b", 1 "a"}

(= m1 (zipmap [:one :two :three] [1 2 3]))
; => true

m1

(get m1 :one)
; => 1

(contains? m1 :two)
; => true

(contains? m1 :four)
; => false

(find m1 :two)
; => [:two 2]

(keys m1)
; => (:one :three :two)

(vals m1)
; => (1 3 2)

m1

(assoc m1 :one "eins")
; => {:one "eins", :three 3, :two 2}

(dissoc m1 :one)
; => {:three 3, :two 2}

(select-keys m1 [:one :three])
; => {:three 3, :one 1}

(merge m1 {:one "x" :four 4 :five "fünf"})
; => {:one "x", :five "fünf", :three 3, :two 2, :four 4}

(merge-with + {:a 2 :b 3 :c 5} {:a 1 :b 0 :c -2 :d 3})
; => {:d 3, :a 3, :c 3, :b 3}

(key (first m1))
; => :one

(val (first m1))
; => 1

(val (find m1 :two))
; => 2

(map? m1)
; => true

(map? (first m1))
; => false

(vector? (first m1))
; => true

; Funktionen für Mengen ----------------------------------
(pres"
#Funktionen für Mengen

- Mengen erzeugen <br/>
  `#{}`, `set`, `hash-set`  <br/>
  `sorted-set`, `sorted-set-by`
- Mengen verwenden <br/>  
  `get`, `contains?`
- Mengen 'ändern' <br/>  
  `conj`, `disj`
- Ist ein Wert eine Menge? <br/>  
  `set?`
- Mengenoperatoren   <br/>
  `union`, `difference`, `intersection`
- Relationale Algebra <br/>  
  `join`, `select`, `project`
- Tests  <br/>
  `subset?`, `superset?`
")

(def s1 #{1 2 3 4 5})

s1
; => #{1 2 3 4 5}

#{1 1 1}
; => IllegalArgumentException Duplicate key: 1

(= s1 (set '(5 4 3 2 1)))
; => true

(= s1 (set [1 1 2 2 3 3 4 4 5 5]))
; => true

(= s1 (hash-set 2 3 1 5 4))
; => true

(sorted-set 5 4 3 2 1)
; => #{1 2 3 4 5}

(sorted-set 5 5 1 2 3 4)
; => #{1 2 3 4 5}

(sorted-set-by > 3 4 2 1 5)
; => #{5 4 3 2 1}

(set "aeiou")
; => #{\a \e \i \o \u}

(get s1 1)
; => 1

(get s1 0)
; => nil

(contains? s1 1)
; => true

(contains? s1 0)
; => false

(conj s1 6)
; => #{1 2 3 4 5 6}

(disj s1 1)
; => #{2 3 4 5}

; Relationale Algebra
(require '(clojure [set :as s]))

(def s1 #{1 2 3})
(def s2 #{3 4 5})

(s/union s1 s2)
; => #{1 2 3 4 5}

(s/intersection s1 s2)
; => #{3}

(s/difference s1 s2)
; => #{1 2}

(def r1 #{ {:a 1 :b 2} {:a 1 :b 3}})
(def r2 #{ {:b 2 :c 1} {:b 2 :c 2}})
; => Relationen anzeichnen

(s/select #(= 2 (get % :b)) r1)
; => #{{:a 1, :b 2}}

; einfacher -- siehe Kollektionen und Keys als Funktionen
(s/select #(= 2 (:b %)) r1)
; => #{{:a 1, :b 2}}

(s/select #(= 2 (:b %)) r2)
; => #{{:c 2, :b 2} {:c 1, :b 2}}

(s/join r1 r2)
; => #{{:a 1, :c 1, :b 2} {:a 1, :c 2, :b 2}}

(s/subset? s1 s2)
; => false

(s/subset? (s/intersection s1 s2) s2)
; => true

(s/superset? r1 r1)
; => true

(s/subset? (s/select #(= 2 (:b % )) r1) r1)
; => true

(s/subset? #{} r1)
; => true

; Kollektionen und Keys sind Funktionen -------------------------
(pres "
#Kollektionen und Keys _sind_ Funktionen

- `(get coll key)` entspricht `(coll key)`
- `(coll key)` entspricht (bei Maps und Mengen) `(key coll)` 
- idiomatisch: `(key coll)`

")


; get bei Vektor: Key = Index
(get [:a :b :c] 2)
; => :c

([:a :b :c] 2)
; => :c

; get bei Map:
(get {:a 1 :b 2 :c 3} :b)
; => 2

({:a 1 :b 2 :c 3} :b)
; => 2

; aber auch
(:b {:a 1 :b 2 :c 3}) 
; => 2

(def kunde1 {:name "Schneider", :vorname "Klaus"})

(:name kunde1)

(:vorname kunde1)

(get {:a 1 :b 2 :c 3} :d 99) ; mit Default 99
; => 99, obwohl :d nicht in der Map vorkommt

({:a 1 :b 2 :c 3} :d 99)
; => 99

({:a 1 :b 2 :c 3} :a 99)
; => 1

(:d {:a 1 :b 2 :c 3} 99)
; => 99

(:a {:a 1 :b 2 :c 3} 99)
; => 1

; get bei Mengen: Key = Wert
(get #{3 2 1} 2)
; => 2

(#{3 2 1} 2)
; => 2

(2 #{3 2 1})
; => ClassCastException java.lang.Long cannot be cast to clojure.lang.IFn

; aber
(:2 #{:3 :2 :1})
; => :2

(:4 #{:3 :2 :1})

; Destructuring --------------------------------------
(pres"
# Zerlegende Variablenbindung ( _destructuring_ )

- Es gibt oft Situationen, in denen Werte zusammengesetzter Datenstrukturen 
  als Parameter von Funktionen auftreten
- Ebenso können sie durch `let` gebunden werden
- Clojure bietet einen cleveren Mechanismus, wie man die Werte 
  innerhalb eines zusammengesetzten Werts in Funktionen oder in `let`
  verwenden kann.
- Dies nennt man zerlegende Variablenbindung ( _destructuring_ )

Konzept:

- Man baut im Parameter oder `let` die Struktur der zusammengesetzten Daten nach
")

(def vt [1 2 3])

vt

; Wenn wir nun die Summe der drei Werte in vt berechnen wollen, 
; könnten wir folgendes tun:

(+ (nth vt 0) (nth vt 1) (nth vt 2))
;=> 6

; Der Mechanismus der zerlegenden Variablenbindung (engl. destructuring)
; gestattet es uns, dies viel eleganter zu machen:

(let [[x y z] vt]
  (+ x y z))
; => 6

; Dieser Mechanismus funktioniert bei let und bei Parametern für Funktionen

(def v1 [1 2 [3 4]])

; angenommen uns interessieren nur die beiden Zahlen im inneren Vektor
; _ ist Platzhalter für Werte, die uns nicht interessieren
(let [[_ _ [x y]] v1]
   (+ x y))
; => 7

(def v1' [1 2 3 [3 4]])

(let [[_ _ [x y]] v1']
   (+ x y))
;=> Exception

(def v2 [1 2 3 4 5 6 7 8 9])

; wir möchten nur einen Teil der Folge und auch noch den Rest
; & steht für restliche Werte
(let [[x  y & more] v2]
  [x y more])
; => [1 2 (3 4 5 6 7 8 9)]
; man beachte, dass der Rest kein Vektor mehr ist, sondern eine Folge (sequence) -- später mehr dazu

; Behalten des ursprünglichen Wertes
(let [[x & more :as orig] v2]
  (reduce + orig))
; => 45

(let [[x & more :as orig] v2]
  [x more (reduce + orig)])

; Zerlegung bei der Definition variadischer Funktionen

(defn concat-rest [x & more]
  (apply str more))

(concat-rest 0 1 2 3)
; => "123"


; Destrukturierung von Maps

(def user1 {:alter 32 :name "Schneider", :vorname  "Klaus"})

(let [{:keys [name vorname]} user1]
      (format "Der User heißt %s %s." vorname name))
; => "Der User heißt Klaus Schneider."

; Transients -----------------------------------------------
(pres "
#Transients

- Datentypen sind persistent, es gibt aber auch transiente Implementierungen
- Hintergrund: Zeitgewinn innerhalb einer Funktion
- Vergleich: `String` und `StringBuilder` in Java
- Beispiel `frequencies` aus `clojure.core`
")

(source frequencies)

(frequencies [1 1 1 1 2 3 3 3 4 4 4 1 2 6 6 6])