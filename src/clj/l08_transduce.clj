; Programmierung in Clojure Vorlesung 8
; Reducers and Transducers
; (c) 2014 - 2015 by Burkhardt Renz, THM

(ns clj.l08-transduce
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop
; B zurück zur letzten Seite!

(init "_Reducers_ und _Transducers_")

(pres :add "
# Beispiel

Was wollen wir tun?

1. Wir nehmen einen recht großen Vektor von Zahlen
2. Wir suchen uns die ungeraden dieser Zahlen heraus
3. Diese Zahlen quadrieren wir
4. und schließlich adieren wir das alles auf

(Die Idee zu dieser Einführung in das Thema _Reducers_ und _Transducers_ habe ich 
aus einer Präsentation von Christoph Grand und Khalid Jebbari beim
Paris Clojure Meetup: Historie de l'itération moderne: de la boucle for aux transducers)
")

; Wir definieren einen großen Vektor

(def coll (vec (range 1e6)))

(count coll)

[(first coll) (last coll)]

(class coll)


; imperativer Stil
(pres "
# Lösung im imperativen Stil

Wir nehmen eine Variable für die Summe

Wir machen eine Schleife über die Elemente der
Kollektion und machen für jedes Element:

Wenn es ungerade ist, addieren wir das Quadrat zu unserer
Variablen.")

(defn imperativ [coll]
  (let [sum (atom 0)]
    (doseq [zahl coll]
      (if (odd? zahl)
        (swap! sum + (*' zahl zahl))))
      @sum))

; Ein Atom ist in Clojure eine Referenz auf einen  Speicherplatz, der einen Wert halten kann
; swap! ändern diesen Wert durch die angegebene Funktion d.h. sum' = (+ sum (*' zahl zahl))
; doseq führt die Anweisungen in einer Schleife pro zahl in coll aus
; @sum dereferenziert das Atom

(imperativ coll)

(time 
  (dotimes [_ 100]
    (imperativ coll)))
;=> 2831 msecs

; Der imperative Stil ist nicht das, was wir in Clojure tun!!
; Vielmehr macht man das in Clojure so:

(pres :add "
Der imperative Stil ist nicht das, was wir in Clojure tun!!

Sondern:

# Idiomatischer Clojure-Stil

erst `filter`

dann `map`

dann `reduce`")

; idiomatischer Stil in Clojure

(defn idiomatic [coll]
  (reduce + (map #(*' % %) (filter odd? coll))))

(idiomatic coll)

(time
  (dotimes [_ 100]
    (idiomatic coll)))
;=> 3843 msecs


(pres :add "
# Geschwindigkeit
      
Die idiomatische Version ist langsamer
woran kann das liegen? 

Diskussion")


(pres :add "
# Andere Schachtelung der Aufrufe

Was passiert, wenn wir das Erzeugen von Zwischenfolgen 
vermeiden?

Wir schieben mal `map` und `filter` _in_ die Funktion, 
die beim `reduce` verwendet wird.
")

(defn onefunc [coll]
  (reduce (fn [sum zahl] (if (odd? zahl) (+ sum (*' zahl zahl)) sum))
          coll))

(onefunc coll)

(time
  (dotimes [_ 100]
    (onefunc coll)))
;=> 1986 msecs

; Wie man sieht kostet die Allokation von Folgen in den Zwischenschritten Zeit!

; Genau das hat vor zwei, drei Jahren in Clojure 1.5 zu den Reducern geführt

(pres "
# Reducers

aus der Dokumentation von Clojure:

> A reducer is the combination of a reducible collection 
> (a collection that knows how to reduce itself) with a 
> reducing function (the ''recipe'' for what needs to be 
> done during the reduction). 

> The standard sequence 
> operations are replaced with new versions that do not 
> perform the operation but merely transform the reducing 
> function. Execution of the operations is deferred until 
> the final reduction is performed. This removes the 
> intermediate results and lazy evaluation seen with sequences.
")

; Verwenden wir die mal
; Lade clojure.string, Zugriff mit alias str
(require '(clojure.core [reducers :as r]))

(defn reducers [coll]
  (r/reduce + (r/map #(*' % %) (r/filter odd? coll))))

(reducers coll)

(time
  (dotimes [_ 100]
    (reducers coll)))
;=> 2462 msecs

; Aber nicht nur, dass Reducers die Allokation von Zwischenfolgen vermeidet,
; die Bibliothek erlaubt auch die parallele Ausführung mit r/fold

(defn reducers' [coll]
  (r/fold + (r/map #(*' % %) (r/filter odd? coll))))

(reducers' coll)

(time
  (dotimes [_ 100]
    (reducers' coll)))
;=> 1435 msecs

(pres :add "
und weiter:
  
> In general most users will not call `r/reduce` directly and instead should 
  prefer `r/fold`, which implements parallel reduce and combine.
  
Hinweis zur Anwendung von _reducers_:

> **When to use**    
  Use the reducer form of these operations when:     
  
> - Source data can be generated and held in memory
  - Work to be performed is computation (not I/O or blocking)
  - Number of data items or work to be done is ''large''
  
")

(pres "
# _Transducers_

Es gibt aber noch ein zweites Problem mit dem idiomatischen Ansatz, der bei der
Lösung mit Reducers bestehen bleibt:     

Unsere Funktionen wie `map`, `filter` etc arbeiten nur mit Folgen bzw. Kollektionen

Bei der Entwicklung von ClojureScript hat es sich gezeigt, dass man diese ganzen
Funktionen neu implementieren muss, wenn man einen anderen Lieferanten der zu
verarbeitenden Daten hat: keine Kollektion, sondern z.B. einen Kanal
(Wir werden Kanäle mit _core.async_ auch noch kennenlernen)

**Idee:** Trenne die Transformation der Daten von ihrer Herkunft und von ihrem Zielort

auf die Bühne: **_Transducers_**

")

; Wir machen uns einen Transducer für unsere Aufgabe:

(def xf (comp (filter odd?) (map #(*' % %))))

; transduce ohne initialem Wert -- geht nur, wenn die übergebene Funktion
; mit der Arität 0 aufgerufen werden kann, wie (+)

(transduce xf + coll)

(+)
;=> 0  

; transduce mit initialem Wert
(transduce xf + 0 coll)

(defn transducer [coll]
  (let [xf (comp (filter odd?) (map #(*' % %)))]
    (transduce xf + coll)))

(transducer coll)

(time
  (dotimes [_ 100]
    (transducer coll)))
;=> 2442 msecs

; Wir können auch andere Dinge mit unserem Transducer tun:

; strikte Anwendung
(into [] xf (vec (range 100)))

; verzögerte Anwendung
(def s (sequence xf (vec (range 100))))

(class s)
;=> clojure.lang.LazySeq

(take 5 s)


(pres "
# Idee von _Transducers_

Man kann `map`, `filter` etc als Funktionen sehen, die einen _Transformationsprozess_
beeinflussen.

In dieser Sichtweise steht nicht die Folge oder Kollektion, die verändert wird im
Vordergrund, sondern der Prozess der Transformation selbst, unabhängig davon, woher
die zu bearbeitenden Elemente kommen und wo sie weiterverwendet werden.

(Hat eine gewisse Ähnlichkeit mit _dependency injection_: Die Transformation selbst
wird von ihrem Kontext getrennt - und erst später wieder hinzugefügt: _Transducing functions_
und _transducible context_).



## Zum Begriff _transducer_

Aus einer Präsentation von Rich Hickey:

> - reduce     
  'lead back' (zurückführen)
  - ingest    
  'carry into' (aufnehmen)
  - transduce    
  'lead across' (überführen)
")

(pres "
# Die Essenz von `reduce`

Was macht `reduce'?

1. startet mit einem initialen Wert
2. bearbeitet den nächsten Wert durch eine Funktion, die
    - den initialen Wert als Ergebnis soweit nimmt im ersten Schritt
    - das bisherige Ergebnis und das aktuelle Element zum neuen Ergebnis verarbeitet
3. am Ende das Ergebnis ausgibt    
")

(pres "
# `map` und `filter` sind Varianten von `reduce`

      (defn map [f coll]
        (reduce (fn [result item] (conj result (f item)))
          [] coll))
          
          
      (defn filter [pred coll]
        (reduce (fn [result item] 
                  (if (pred item) (conj result item) result))
           [] coll))       
           
⇒ Die Funktion, die `reduce` verwendet, nennt man _step function_.
           
")

(pres :add "
# Abstraktion

- Wir wollen nur die reduzierende Funktion haben, ohne `coll`
- Wir wollen, dass `conj` von außen kommt

⇒ Ein _Transformer_  besteht aus 3 Funktionen:     

  - `init [] -> res`     
  - `step [res, item] -> res`  _step function_
  - `complete [res] -> res`

⇒ Ein _Transducer_ ist eine Funktion, die _Transformer_ transformiert!
")

(pres "
# Wie sieht nun `map` aus?

      (defn map [f]
        (fn [transformer]
          (fn
            ([] (transformer))
            ([res item] (transformer res (f item)))
            ([res] (transformer result)))))
")

(pres :add "
# und wie `filter`?

      (defn filter [pred]
        (fn [transformer]
          (fn
            ([] (transformer))
            ([res item] 
              (if (pred item) (transformer res item) res))
            ([res] (transformer result)))))
")

(pres "
# Wie wird der Kontext zum _Transducer_ gebracht?

Beispiel `transduce`

Parameter:    

- Transducer `xform`, z.B. Komposition von `filter` und `map`
- Binäre Funktion `f` für die eigentliche Reduktion
- erster Wert `init` z.B. `0` bei Addition
- die Kollektion `coll`


      (defn transduce
        ([xform f coll] (transduce xfrom f (f) coll))
        ([xform f init coll]
          (let [xf (xform f) ;; eine Funktion mit 3 Varianten, 
                             ;; reduce verwendet step
                res (reduce xf init coll)]
            (xf res) ;; die complete-Variante von xf)))   
")


(def xform (comp (filter odd?) (map #(*' % %))))
; xform ist die Komposition der Transducer filter und map
; d.h. eine Funktion, die einen Transformer erwartet

(def xf (xform +))

(xf)
; => 0

(xf 12)
; => 12

(xf 0 2)
; => 0 
; warum?

(xf 0 3)
; => 9

(xf 12 3)

(let [xf (xform +)]
  (reduce xf 0 coll))

(pres "
# In welchem Kontext kann man Transducer anwenden?

---------------- | -----------------------
`transduce`      | Reduktion mit Transducer
`into`           | Transformation in andere Kollektion (strikt)
`sequence`       | Verzögerte Folge des Ergebnisses
`eduction`       | Eine Kollektion zusammen mit einer Transformation, die noch nicht ausgeführt wurde - der Verwender kann sie auswerten
channel          | Kanal von `core.async`                   

")


