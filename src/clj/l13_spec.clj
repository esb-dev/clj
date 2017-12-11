; Programmierung in Clojure
; Spezifikationen mit clojure.spec
; (c) 2016 - 2017 by Burkhardt Renz, THM

(ns clj.l13_spec
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all])
  (:require [clojure.string :as str])
  (:require [clojure.spec.alpha :as s])
  (:require [clojure.spec.gen.alpha :as gen])
  (:require [clojure.spec.test.alpha :as stest])
  (:require [clojure.set :as set]))

; Drei Fenster: (1) Editor (2) REPL (3) Präsentation

(init "#Spezifikation von Datenstrukturen und Funktionen in Clojure mit `clojure.spec`")

; 1. Motivation der Thematik -------------------------------------------------------------------------------------------

(pres :add "
\n\n\n
- Clojure ist eine _dynamische_ Programmiersprache
- In der Clojure-Gemeinschaft wird auf den Grundsatz _Data is the API_
  Wert gelegt:<br />
  ''The data *is* the API. <br />
  Design the data structures you’re going to accept & return at all
  the public entry-points of your library or application. <br />
  That’s your API design.'' (Stuart Sierra)
- Beispiel: Person
")


(pres "
## Beispiel Person

Person in Java (javaperson/Person.java)

- 30 Zeilen Code oder so
- aber alles durch die IDE generiert
- spezielle Funktionen für Person z.B. toString()
- schon der Editor prüft Typ

")

; Person in Clojure

(def p1 {:name "Bloch"
        :vorname "Joshua"
        :gebdat "1961-08-28"})

p1

(pres :add "
Person in Clojure

- 0 Zeilen Code
- ein assoziatives Datenfeld (_Map_)
- werteorientiert statt zustandsbehaftet
- keine speziellen Funktionen, sondern die gesamte Maschinerie von Clojure
")

; Aber das ist in Clojure auch möglich:

(def x1 {:name 112
        :vorname "Joshua"
        :gebdat "Happy birthday"})

x1

(pres :add  "
_ABER_

- *keinerlei Typprüfung in Clojure*
" )


(pres "
## Daten sind die API - Diskussion

Pro

- wenige, mächtige Funktionen
- flexibel, agil
- verwendet generische Datenstrukturen
- ist werteorientiert

Aber

- „Versteckte“ Abhängigkeiten
- Dokumentation ist essentiell! (nicht so gutes Beispiel `(ns ...)`)
- keine Prüfung durch Compiler

Pragmatik

- Typvalidierung an den _Grenzen_ des eigenen Codes
- Sorgfalt im _eigenen_ Code

### `clojure.spec` erlaubt dies ''minimalinvasiv''

")

(pres "
## Möglichkeiten mit `clojure.spec`

- Spezifikation und Validierung von Datenstrukturen
- Spezifikation von Funktionen
- Dokumentation von Datenstrukturen und Funktionen
- Standardisierte Zerlegung von Datenstrukturen
- Validierung von Vor- und Nachbedingungen
- Instrumentierung von Funktionen zur Laufzeit
- Generieren von Testdaten und Tests
")

(pres :add "
## Beispiele für den Einsatz von `spec`

- Spezifikation einer beliebig geschachtelten Datenstruktur:<br />
  *Formeln der Aussagenlogik*
- Spezifikation strukturierter Kollektionen:<br />
  *Modell der Aussagenlogik* und *Kripke-Struktur*
- Spezifikation und Instrumentierung von Funktionen:<br />
  *Auswerten von Formeln der Aussagenlogik*
- dies alles an Beispielen aus der *Logic Workbench (LWB)*
- ... und ein (kritisches) Fazit
")

; 2. Spezifikation der Grammatik der Aussagenlogik ---------------------------------------------------------------------

; Unser Ziel besteht zunächst darin, dass wir den Aufbau von
; Formeln der Aussagenlogik spezifizieren, damit wir die
; Wohlgeformtheit mittels dieser Spezifikation überprüfen können.

; Wir bauen die Formeln als verschachtelte Listen auf:
; Atome werden durch Symbole repräsentiert. Ferner haben wir
; die beiden speziellen Werte true und false für Verum und Falsum.
; Operatoren sind auch Symbole, die am Kopf einer Liste stehen.

; Einige Beispiele von Formeln:

(def phi01 'true)

(def phi02 'false)

(def phi03 '(and P_<01> P_<02> P_<03> P_<04> P_<05>))

(def phi04 '(and (or P Q) (impl (not P) R)))

(def phi05 '(impl (and P (impl P Q)) Q))

(def phi06 '(impl (or (impl S (or R L)) (and (not Q) R)) (impl (not (impl P S)) R)))

; Fehlerhafte Ausdrücke:

(def phi11 '(impl P Q R)) ; Arität falsch

(def phi12 '(equiv :P Q)) ; Keyword statt Symbol

(def phi13 '(and or P Q)) ; Operator falsch


; Wir entwickeln die Spezifikation schrittweise.

; In einer Spezifikation können unäre Prädikate problemlos
; verwendet werden, sie werden automatisch in specs konvertiert.

; Mit valid? kann man überprüfen, ob ein Wert einer spec entspricht.

(s/valid? symbol? 'P)
; => true

(s/valid? symbol? :P)
; => false

(s/valid? symbol? 'true)
; => false

; Ferner gibt es die Funktion conform, die einen Wert überprüft
; und seine kanonische Form laut Spezifikation zurück gibt - oder
; :clojure/spec/invalid, wenn die Spezifikation nicht erfüllt wird.

; Für einfache Werte spielt die kanonische Form keine große Rolle,
; denn sie entspricht einfach dem Wert selbst. Bei zusammengesetzten
; Werten werden wir später sehen, welche Rolle conform spielt (oder
; spielen kann).

(s/conform symbol? 'P)
; => P

(s/conform symbol? :P)
; => :clojure.spec/invalid

; Was wir bisher gesehen haben, können wir verwenden, um die elementaren
; Bestandteile einer Formel der Aussagenlogik zu spezifizieren. Dazu
; definieren wir entsprechende Prädikate:

(defn op?
  "Is `symb` an operator of propositional logic?"
  [symb]
  (contains? '#{not, and, or, impl, equiv, xor, ite} symb))

(op? 'and)
; => true

(op? 'P)
; => false

(defn atom?
  "Is `symb` an atomar proposition?"
  [symb]
  (and (symbol? symb) (not (op? symb))))

(atom? 'P)
; => true

(atom? 'and)
; => false

(atom? 'true)
; => false

(s/valid? atom? 'P_<01>)
; => true

; Nun können wir diese Prädikate verwenden, um einen einfachen Ausdruck
; der Aussagenlogik zu spezifizieren. Mit s/def wird die spec in der
; Registry für specs unter dem klassifierten Keyword gespeichert
; und kann damit wiederverwendet werden.

; Specs können mit s/and und s/or kombiniert werden. Im Fall von s/or
; werden die Möglichkeiten durch ein Keyword gekennzeichnet.

(s/def ::simple-expr (s/or :bool boolean?
                           :atom atom?))

(s/describe ::simple-expr)

(s/valid? ::simple-expr 'true)
; => true

(s/valid? ::simple-expr 'P)
; => true

(s/valid? ::simple-expr 'and)
; => false

(s/conform ::simple-expr 'true)
; => [:bool true]

(s/conform ::simple-expr 'P)
; => [:atom P]

; Hier sehen wir zum ersten Mal, dass conform mehr liefert
; als nur den Wert: es wird auch angegeben, welchem der
; Möglichkeiten für einen einfachen Ausdruck der Wert entspricht.

; Die Funktion explain gibt auf *out* aus, weshalb ein Wert
; nicht der Spezifikation entspricht (wenn das so ist).

(s/explain ::simple-expr 'true)
; Success!
; => nil

(s/explain ::simple-expr 'not)
; val: not fails at: [:bool] predicate: boolean?
; val: not fails at: [:atom] predicate: atom?
; => nil

; Zwischenfazit

(pres "
## Funktionen für Spezifikationen

- `(s/valid? spec value)`<br />
  entspricht der Wert der Spezifikation?
- `(s/conform spec value)`  <br />
  kanonische Zerlegung des Werts entsprechend der Spezifikation
- `(s/explain spec value)`<br />
  Erläuterung, wenn der Wert nicht valide ist
- `(s/describe spec)` <br />
  Beschreibung der Spezifikation
- `(s/and spec1 spec2 ...)` <br />
  Konjunktion der Specs
- `(s/or :tag1 spec1 :tag2 spec2 ...)` <br />
  Disjunktion der Specs mit Tags für die Varianten
- `(s/def ::keyword spec)` <br />
  Speichern der Spezifikation in der zentralen Spec-Datenbank
  für die Wiederverwendung
")

; Nun wollen wir komplexe Ausdrücke spezifieren, dazu hat clojure.spec
; Operatoren wie man sie aus der Backus-Naur-Darstellung von
; Grammatiken kennt.

(pres "
## Spezifikation sequenzieller Datenstrukturen

- `(s/cat :tag1 spec1 :tag2 spec2 ...)` <br />
  Konkatenation von Specs
- `(s/alt :tag1 spec1 :tag2 spec2 ...)` <br />
  Wahl zwischen möglichen Specs
- `(s/* spec)` <br />
  beliebig viele Werte, die spec erfüllen
- `(s/+ spec)` <br />
  ein oder mehr Werte, die spec erfüllen
- `(s/? spec)` <br />
  ein oder kein Wert, der spec erfüllt
- `(s/& pattern pred)` <br />
  Pattern aufgreifen und Prädikat anwenden
")

; Zunächst definieren wir mal eine Variante, bei der
; in einen komplexen Ausdruck einfach Ausdrücke eingesetzt werden können.

(s/def ::compl-expr-1 (s/cat :op op? :params (s/* ::simple-expr)))

(s/valid? ::compl-expr-1 '(and P Q))
; => true

(s/valid? ::compl-expr-1 '[and P Q])
; => true

; Hier haben wir ein erstes kleines Problem: Es werden nicht nur
; Listen akzeptiert.

; Abhilfe:

(s/def ::compl-expr-2 (s/and list? (s/cat :op op? :params (s/* ::simple-expr))))

(s/valid? ::compl-expr-2 '(and P Q))
; => true

(s/valid? ::compl-expr-2 '[and P Q])
; => false

(s/valid? ::compl-expr-2 '(not P Q))
; => true

; Ein zweites Problem. Unsere bisherige Spezifikation erlaubt bei allen Operatoren
; beliebig viele Parameter.

; Um dieses Problem zu beheben, machen wir uns zuerst eine Funktion, die uns
; die Arität unserer Operatoren gibt.

(defn arity
  "Arity of operator `op`.
   -1 means n-ary, but better use `n-ary?`.
   requires: `op` an operator."
  [op]
  (cond
    ('#{not} op)            1
    ('#{impl equiv xor} op) 2
    ('#{ite} op)            3
    ('#{and or} op)        -1))

; Eine Alternative wäre, die grammatik so zu definieren, dass jeder
; Operator explizit in der Grammatik vorkommt

; Mit dem Ausdruck & von clojure.spec können wir uns nun den gefundenen Ausdruck
; hernehmen und mit einem zusätzlichen Prädikat versehen.

; Dazu sehen wir uns zunächst an, wie conform unseren Wert ausgibt:

(s/conform ::compl-expr-2 '(and P Q))
; => {:op and, :params [[:atom P] [:atom Q]]}

; Wir müssen also prüfen, ob die Arität von :op mit der
; Anzahl von :params passt.

(defn- arity-ok? [{:keys [op params]}]
  (let [arity (arity op)]
    (if (= arity -1) true
                     (= arity (count params)))))

(s/def ::compl-expr-3 (s/and list? (s/& (s/cat :op op? :params (s/* ::simple-expr)) arity-ok?)))

(s/valid? ::compl-expr-3 '(and P Q))
; => true

(s/valid? ::compl-expr-3 '(not P Q))
; => false

(s/explain ::compl-expr-3 '(not P Q))
; val: {:op not, :params [[:atom P] [:atom Q]]} fails predicate: arity-ok?
; => nil

; Fast fertig. Wir müssen jetzt nur noch dazu in der Lage sein, in komplexe Ausdrücke
; auch wieder (rekursiv) beliebige Ausdrücke einsetzen zu können.
; Das geht denkbar einfach:

(s/def ::compl-expr (s/and list? (s/& (s/cat :op op? :params (s/* ::fml)) arity-ok?)))

(s/def ::fml (s/or :simple-expr ::simple-expr
                   :compl-expr  ::compl-expr))

(s/valid? ::fml phi01)
(s/valid? ::fml phi02)
(s/valid? ::fml phi03)
(s/valid? ::fml phi04)
(s/valid? ::fml phi05)
(s/valid? ::fml phi06)
(s/valid? ::fml (list 'ite phi06 'P 'Q))
; => true

(s/valid? ::fml phi11)
(s/valid? ::fml phi12)
(s/valid? ::fml phi13)
; => false

(s/valid? ::fml (list 'ite phi06 'P))
; => false

(s/explain ::fml (list 'ite phi06 'P))
; val: (ite (impl (or (impl S (or R L)) (and (not Q) R)) (impl (not (impl P S)) R)) P) fails spec: :spec.guide/simple-expr at: [:simple-expr :bool] predicate: boolean?
; val: (ite (impl (or (impl S (or R L)) (and (not Q) R)) (impl (not (impl P S)) R)) P) fails spec: :spec.guide/simple-expr at: [:simple-expr :atom] predicate: atom?
; val: {:op ite, :params [[:compl-expr {:op impl, :params [[:compl-expr {:op or, :params [[:compl-expr {:op impl, :params [[:simple-expr [:atom S]]
; [:compl-expr {:op or, :params [[:simple-expr [:atom R]] [:simple-expr [:atom L]]]}]]}] [:compl-expr {:op and, :params [[:compl-expr {:op not, :params [[:simple-expr [:atom Q]]]}] [:simple-expr [:atom R]]]}]]}]
; [:compl-expr {:op impl, :params [[:compl-expr {:op not, :params [[:compl-expr {:op impl, :params [[:simple-expr [:atom P]]
; [:simple-expr [:atom S]]]}]]}] [:simple-expr [:atom R]]]}]]}] [:simple-expr [:atom P]]]} fails spec: :spec.guide/compl-expr at: [:compl-expr] predicate: arity-ok?
; => nil

; Man sieht hier, dass im Unterschied zu einer speziellen Validierungsfunktion die Meldungen über die Verletzung der
; Grammatik nicht unbedingt einfach zu lesen sind.

; Zusatz: Der Unterschied von s/or und s/alt:

(s/def ::or-example (s/cat :name string? :item (s/or :nums (s/* number?) :key keyword?)))

(s/valid? ::or-example ["name" 1])
; => false
(s/valid? ::or-example ["name" :x])
; => true
(s/valid? ::or-example ["name" [1 2 3]])
; => true

(s/def ::alt-example (s/cat :name string? :item (s/alt :nums (s/* number?) :key keyword?)))

(s/valid? ::alt-example ["name" 1])
; => true
(s/valid? ::alt-example ["name" :x])
; => true
(s/valid? ::alt-example ["name" 1 2 3])
; =>
(s/valid? ::alt-example ["name" [1 2 3]])
; => false

; Man sieht: s/or startet eine Sub-Sequenz, s/alt nicht!

; 3. Spezifikation strukturierter Kollektionen -------------------------------------------------------------------------

; 3.1 Modelle der Aussagenlogik ----------------------------------------------------------------------------------------

(pres "
## Spezifikation strukturierte Kollektionen 1
### Modelle der Aussagenlogik
- Ein Modell in der Aussagenlogik ist eine Abbildung der Atome der Sprache auf Wahrheitswerte,
- In Clojure repräsentieren wir ein Modell als einen Vektor von Atomen mit zugehörigem Wahrheitswert.
- Warum so? Diesen Vektor können wir in einem `let` verwenden, um die Formel in diesem
 Modell auszuwerten. Diese Repräsentation des Models entspricht also genau dem Binden von
 Werten an Symbole in der _Philosopie_ von Clojure.
")

(s/def ::model-1 (s/and vector? (s/* (s/cat :atom atom? :value boolean?))))

(s/valid? ::model-1 '[P true])
; => true

(s/valid? ::model-1 '[P true Q false])
; => true

(s/valid? ::model-1 '[P true Q R])
; => false

(s/valid? ::model-1 '[P true Q])
; => false

; subtiler Unterschied
; es spielt bei der Definition des Modells eine Rolle, ob wir
; einen Wahrheitswert oder ein Prädikat angeben!
(s/valid? ::model-1 '[P (= 1 1) Q false])
; => false

(s/valid? ::model-1 ['P (= 1 1) 'Q false])
; => true

; Wenn wir etwas präziser sein wollen, können wir auch noch ausdrücken, dass
; die Atome im Modell eindeutig sein müssen:

(s/def ::model (s/and vector? (s/& (s/* (s/cat :atom atom? :value boolean?))
                    #(apply distinct? (map :atom %)))))

(s/valid? ::model '[P true Q false])
; => true

(s/valid? ::model '(P true Q false))
; => false

(s/valid? ::model '[P true P false])
; => false

; 3.2 Kripke-Strukturen ------------------------------------------------------------------------------------------------

(pres "
## Spezifikation strukturierte Kollektionen 2
### Modelle der linearen temporalen Logik - Kripke-Strukturen

![Beispiel Kripke-Struktur](./resources/kripke.jpg)

- Ein Modell in der linearen temporalen Logik ist eine Kripke-Struktur (nach Saul Kripke,
amerikanischer Philosoph und Logiker)
- Endlicher Automat, Knoten repräsentieren Zeitpunkt
- Atomare Aussagen gelten zu einem Zeitpunkt (oder nicht)
- Kanten definieren die Übergangsrelation, sie ist links-total, d.h. jeder Knoten hat mindestens einen Folgeknoten.

### Repräsentation in Clojure
      {:nodes {:s_0 '#{P Q}
               :s_1 '#{P Q}
               :s_2 '#{P}}
       :initial :s_0
       :edges #{[:s_0 :s_1]
                [:s_1 :s_0]
                [:s_1 :s_2]
                [:s_2 :s_2]}}

")

(pres "
## Spezifikation von Maps

- In clojure.spec wird die Definition von Keys für Maps und den Daten dafür
  getrennt.
- Das bedeutet, dass dieselben Keys in unterschiedlichen Maps
  denselben Typ der Werte haben (sollten).
- Ein interessanter Punkt: Bei der Definition des Relationsschemas in der relationalen
  Algebra gehe ich (im Unterschied zu SQL und anderen Autoren) auch so vor!
- `clojure.spec` verwendet hier eine analoge Idee mit dem Ziel der Wiederverwendung von
  Spezifikationen von Daten.
")

; Also definieren wir zunächst die Attribute:
; Eine Kripke-Struktur hat ::nodes, einen ::initial node und ::edges:

(s/def ::nodes (s/map-of keyword? (s/coll-of atom? :kind set? :distinct true :into #{})))
(s/def ::initial keyword?)
(s/def ::edges (s/coll-of (s/tuple keyword? keyword?) :into #{}))

; Das Modell selbst ist eine Map aus diesen drei Teilen:

(s/def ::kripke-model (s/keys :req-un [::nodes ::initial ::edges]))

(def ks1 {:nodes {:s_0 '#{P Q}
                  :s_1 '#{P Q}
                  :s_2 '#{P}}
          :initial :s_0
          :edges #{[:s_0 :s_1]
                   [:s_1 :s_0]
                   [:s_1 :s_2]
                   [:s_2 :s_2]}})

(s/valid? ::kripke-model ks1)
; => true

; Man kann noch genauer sein, denn es muss gelten:
; (1) der initiale Knoten muss in der Knotenmenge sein
; (2) alle Knoten, die in der Kantenmenge vorkommen müssen in der Knotenmenge sein
; (3) zu jedem Knoten der Knotenmenge gibt es mindestens eine ausgehende Kante

(defn kripke-ok?
  [{:keys [nodes initial edges] :as ks}]
  (let [nodeset    (into #{} (keys nodes))
        nodeset'   (into #{} (flatten (seq edges)))
        nodeset''  (into #{} (map first (seq edges)))]
    (and (contains? nodeset initial)
         (= nodeset nodeset')
         (= nodeset nodeset''))))

; vollständige Spezifikation
(s/def ::kripke-model (s/and kripke-ok? (s/keys :req-un [::nodes ::initial ::edges])))

(s/valid? ::kripke-model ks1)

; Mit der Funktion s/describe kann man sich die Definition von Spezifikation ansehen:
; Nicht nur ansehen: Man bekommt den Ausdruck selbst zurück

(s/describe ::kripke-model)
; => (and kripke-ok? (keys :req-un [:spec.guide/nodes :spec.guide/initial :spec.guide/edges]))

(pres "
## Weitere Funktionen zur Spezifikation von Datenstrukturen

- `(s/keys :req ... :opt ...)`<br />
  `(s/keys :req-un ... :opt_un ...)`<br />
  Definition von Tags in Maps (mit oder ohne namespace)
- `(s/coll-of ...)`<br />
  Definition homogener Kollektionen
- `(s/tuple ...)`<br />
  Definitionen von Tupeln (Kollektionen fixer Länge)
- `(s/map-of ...)`<br />
  Definition homogener Maps
  ")

; 4. Spezifikation und Instrumentierung von Funktionen -----------------------------------------------------------------

;; Verwendung von specs für die Validierung

(pres "
## Validierung von Funktionsausrufen mittels specs
- Die erste und einfache Variante besteht darin, dass man valid? in der *Vor-* und *Nachbedingung*
  der Funktion verwendet
")

(defn eval-phi
  "Evaluates the formula `phi` with the given model.
  `model` must be `['atom1 true, 'atom2 false, ...]` for the
  propositional atoms of `phi`."
  [phi model]
  {:pre [(s/valid? ::model model) (s/valid? ::fml phi)]}
  (binding [*ns* (find-ns 'clj.l13_spec)]
    (eval `(let ~model ~phi))))

(def phi-ok '(or P Q))
(def phi-wrong '(and or P Q))

(def model-ok '[P true Q false])
(def model-wrong '[P true Q])

(eval-phi phi-ok model-ok)
; => true

(eval-phi phi-wrong model-ok)
; CompilerException java.lang.AssertionError: Assert failed: ...

(eval-phi phi-ok model-wrong)
; CompilerException java.lang.AssertionError: Assert failed: (s/valid? :spec.guide/model model)

; Das ist nichts wirklich neues
; nebenbei: es wird in diesem Beispiel nicht überprüft, ob alle Atome in phi im Modell vorkommen

(pres :add "
- In `clojure.spec` kann man aber auch Funktionen spezifizieren
 und zwar unabhängig von ihrer Definition selbst
- Diese Spezifikation kann man dann zur *Instrumentierung* der Funktion
 einsetzen: die Gültigkeit der Spezifikation wird zur Laufzeit überprüft
")

; Definition der Funktion selbst

(defn eval-phi
  "Evaluates the formula `phi` with the given model.
  `model` must be `['atom1 true, 'atom2 false, ...]` for the
  propositional atoms of `phi`."
  [phi model]
  (binding [*ns* (find-ns 'clj.l13_spec)]
    (eval `(let ~model ~phi))))

; Zusätzliche Spezifikation der Funktion

(s/fdef eval-phi
        :args (s/cat :phi ::fml :model ::model)
        :ret boolean?)

(eval-phi phi-ok model-ok)
; => true

; Wir instrumentieren die Funktion, d.h. zur Laufzeit wird gegen die Spezifikation geprüft
(stest/instrument `eval-phi)

(eval-phi phi-ok model-ok)
; => true

(eval-phi phi-wrong model-ok)
; Fehlermeldung

(eval-phi phi-ok model-wrong)
; Fehlermeldung

; Bisher prüft unsere Spezifikation nur, dass die beiden Argumente
; den korrekten Datentyp haben.
; Tatsächlich ist der Aufruf aber nur sinnvoll, wenn für alle Atome
; der Formel im Modell auch ein Wert angegeben wurde.

; Zunächst eine Funktion, die uns eine Menge der Atome der
; Formel gibt:

(defn atoms-of-phi
  "Sorted set of the propositional atoms of formula `phi`."
  [phi]
  (if (coll? phi)
    (apply sorted-set
           (filter #(not (or (op? %) (boolean? %))) (flatten phi)))
    (if (boolean? phi)
      #{}
      #{phi})))

; Und jetzt eine Funktion, die uns die Atome im Modell gibt

(defn atoms-in-model
  "Set of atoms in destructured model"
  [model]
  (set (map :atom model)))

; Vollständige Spezifikation

(defn wff? [phi] (s/valid? ::fml phi))
; Warum wff? und nicht ::fml - ::fml wird zerlegt, deshalb
; schlecht zu verwenden

(s/fdef eval-phi
        :args (s/and (s/cat :phi wff? :model (s/spec ::model))
                     #(set/subset? (atoms-of-phi (:phi %)) (atoms-in-model (:model %))))
        :ret boolean?)

(stest/instrument `eval-phi)

(eval-phi phi-ok model-ok)
; => true

; Beispiel für ein "zu kleines" Modell
(eval-phi phi-ok '[P true])
; CompilerException clojure.lang.ExceptionInfo: Call to #'spec.guide/eval-phi did not conform to spec:
; val: {:phi (and P Q), :model [{:atom P, :value true}]} fails at: [:args] predicate: (subset? (atoms-of-phi (:phi %)) (atoms-in-model' (:model %)))
; :clojure.spec/args  ((and P Q) [P true])
;{:clojure.spec/problems {[:args] {:pred (subset? (atoms-of-phi (:phi %)) (atoms-in-model' (:model %))), :val {:phi (and P Q), :model [{:atom P, :value true}]}, :via [], :in []}}, :clojure.spec/args ((and P Q) [P true])}, compiling:(/Users/br/ESBDateien/Projekte/spec/src/spec/guide.clj:1066:1)

; Hat man eine Spezifikation wird sie automatisch mit doc ausgegeben:
(doc eval-phi)
; (doc eval-phi)
; -------------------------
; spec.guide/eval-phi
; ([phi model])
; Evaluates the formula `phi` with the given model.
; `model` must be `['atom1 true, 'atom2 false, ...]` for the
; propositional atoms of `phi`.
; Spec
; args: (and (cat :phi wff? :model (spec :spec.guide/model)) (subset? (atoms-of-phi (:phi %)) (atoms-in-model (:model %))))
; ret: boolean?
; => nil


(pres "
## Spezifikation und Instrumentierung von Funktionen

- `(s/fdef fn-name :args ... :ret ... :fn ...)` <br />
  spezifiziert die Argumente, den Returnwert einer Funktion
  sowie Bedingungen, die die Berechnung erfüllen muss
- `(doc fn-name)`  <br />
   zeigt auch die Spezifikation an
- `(stest/instrument qual-fn-name)` <br />
  schaltet die Überprüfung der Spezifikation zur Laufzeit an
  (prüft z.Zt. tatsächlich nur die Argumente)
- `(stest/unstrument qual-fn-name)` <br />
  schaltet die Instrumentierung wieder aus.

")

; 6. Fazit

(pres "
## Offene Enden

- Generierung von Testdaten auf Basis von Spezifikationen
- Generierung von Tests
- Spezifikation von Argumenten für polymorphe Funktionen (multi-spec)
- Spezifikation von Funktionen höherer Ordnung
- Spezifikation von Makros")


(pres "
## Fazit eines ersten Blicks auf `clojure.spec`


### Pro

- ''Handgeschriebene'' Parser in LWB kann man sehr schön durch specs ersetzen
- Spezifikation von Datenstrukturen ist elegant und konzeptionell überzeugend (Grammatik statt textuelle Beschreibung)
- Standardisierte Zerlegung zusammengesetzter Datenstrukturen hilft
- Instrumentierung von Funktionen während der Entwicklung nützlich

### Fragwürdig?

- Fehlermeldungen zwangsläufig generisch, deshalb bei geschachtelten Strukturen
  schwer zu lesen 
- Standardisierte Zerlegung von Datenstrukturen kann auch stören (Beispiel Prüfung von `eval-phi`)
- Argument der Leichtigkeit, weil dynamische Sprache? Sind die Spezifikationen
  noch so? Aufwändig??
- Inhärent unvollständig -- sobald Daten Code sind! (Beispiel Modell in der Aussagenlogik: Prädikat statt Wahrheitswert)
- Generierte Tests (die Rich Hickey offenbar gut gefallen): in welchen Situationen wirklich sinnvoll?
")

; mit dem Punkt Daten = Code ist das Beispiel

;(s/valid? ::model-1 '[P (= 1 1) Q false])
; => false

;(s/valid? ::model-1 ['P (= 1 1) 'Q false])
; => true

; gemeint
