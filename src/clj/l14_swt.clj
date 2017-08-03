; Programmierung in Clojure Vorlesung 13  
; Softwaretechnik mit Clojure
; (c) 2014 - 2016 by Burkhardt Renz, THM

(ns clj.l14-swt
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all])
  (:require [clojure.string :as str])
  (:require [clojure.math.combinatorics :refer (selections combinations)])
  (:require [clojure.tools.trace :refer :all])
  (:require [schema.core :as s]))

stop

(init "Softwaretechnik mit Clojure")

;; 1. Softwaretechnik im Allgemeinen

;; 1.1. Reine Funktionen

(pres :add "
# Softwaretechnik im Allgemeinen

## Reine Funktionen

Eine Funktion ist eine _reine_ Funktion, wenn sie keine Seiteneffekte hat
und bei jedem Aufruf mit bestimmten Parametern denselben Wert zurück gibt

- Testbarkeit    
  kann leicht getestet werden, da unabhängig vom Kontext
- Parallele Ausführbarkeit     
  kann auf verschiedenen Threads ohne gegenseitige Beeinflussung ausgeführt werden
- Wiederholbarkeit      
  Ergebnisse können gemerkt werden und müssen ggfs nicht neu berechnet werden ( _memoization_ )
- Ersetzbarkeit    
  können leicht durch andere Implementierung ersetzt werden, ohne den Rest des
  Systems zu beeinflussen (Lokalität)
  
")

(pres "
## Reine Funktionen und Werteorientierung

Joshua Bloch: _Effective Java_ Item 13: Favor Immutability    

''An immutable class is simply a class whose instances cannot be modified''

- ''Immutable objects are simple''
- ''Immutable objects are inherently thread-safe''
- ''Immutable objects can be shared freely''
- ''Immutable objects make great building blocks for other objects''

")
;; 1.2 Identität -- Zustand -- Wert
(pres "
# Softwaretechnik im Allgemeinen

## Identität - Zustand - Wert

- Identität    
  Eine logische Entität ...
- Zustand - Wert  
  ... hat verschiedene Werte im Verlauf der Zeit
- Zustandsübergang      
    - Neuer Wert ist Funktion des bisherigen Werts (funktional)
    - Änderung des Werts der Referenz kontrolliert durch das System    
      (STM _Software Transactional Memory_ mit _Multiversion Concurrency Control_)
  
")

;; 1.3 Daten sind die API
(pres "
# Softwaretechnik im Allgemeinen

## Daten sind die API

''The data *is* the API. 

Design the data structures you’re going to accept & return at all 
the public entry-points of your library or application. 

That’s your API design.'' (Stuart Sierra)
")

;; Beispiel für dieses Konzept (aus der Dokumentation von Seesaw)
(use 'seesaw.core)

(input "Pick a city"
    :choices [{ :name "New York"   :population 8000000 }
              { :name "Ann Arbor"  :population 100000 }
              { :name "Twin Peaks" :population 5201 }]
    :to-string :name)


;; Offenbar muss man die ganzen keywords kennen,
;; sie stellen die API dar und müssen mit den entsprechenden Werten versorgt werden

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
- keine Prüfung durch Compiler - aber es gibt auch _Typed Clojure_
  oder _Prismatic Schema_
")

(pres :add "
## Maps und Records in Clojure

- Maps sind generisch
- Records werden mit `defrecord` zu Java-Klassen kompiliert
- Records haben automatische ''Konstruktoren'':        
   `->Record` und `map->Record`
- aber: auch hier keine Prüfung durch Compiler
- man kann sogar den Record ''erweitern'' ohne Warnung:       
   Segen oder Fluch??
- Ansatz: Variante von `defrecord` von Prismatic Schema,
  siehe [Prismatic Schema](https://github.com/Prismatic/schema)
")

;; Beispiel Kunde
;; als map

(defn kunde [knr name vorname]
  "erzeugt kunde mit knr (integer), name (string) und vorname (string)"
  {:type :kunde
   :knr knr
   :name name
   :vorname vorname})

(def k1 (kunde 1 "Schneider" "Hans"))
(def k2 (kunde 2 "Künzel" "Gisela"))

k1

(class k1)
; => clojure.lang.PersistentArrayMap

(def k3 (kunde "hallo" 123 {:x 12}))

k3
; => {:type :kunde, :knr "hallo", :name 123, :vorname {:x 12}}

;; wir sehen, dass es keinerlei Prüfung gibt
;; was ein Compiler prüfen könnte, hätte man ein Typsystem
;; muss der Programmierer korrekt anwenden

; Kunde als record

(defrecord Kunde [knr name vorname])

(def k1 (->Kunde 1 "Schneider" "Hans"))
(def k2 (map->Kunde {:knr 2 
                     :name "Künzel"
                     :vorname "Gisela"}))

k1

(class k1)
; => clj.l13_swt.Kunde

(def k3 (->Kunde "hallo" 123 {:x 12}))

k3
; => #clj.l13_swt.Kunde{:knr "hallo", :name 123, :vorname {:x 12}}

(def k3+ (assoc k3 :plz 35390))

k3+
; => #clj.l13_swt.Kunde{:knr "hallo", :name 123, :vorname {:x 12}, :plz 35390}

(class k3+)
; => clj.l13_swt.Kunde

; auch das geht:
(def k4 (map->Kunde {:knr 4
                     :name "Strasser"
                     :vorname "Helga"
                     :land "at"}))
; wie man sieht, hat die Dynamik den Vorteil der leichten Erweiterbarkeit
; aber den enormen Preis, dass wirklich so gut wie nichts zur Compile-Zeit
; überprüft wird

; weiterer Versuch
; durch Typhinweise kann man das etwas verbessern??
(defrecord Artikel [^int artnr, ^String bez, ^int preis]) ; preis in Cent

(def a1 (->Artikel 1 "Regenschirm" 1299))
; okay

(def a2 (->Artikel "hallo" "Regenschirm" 1299))
; => CompilerException java.lang.ClassCastException


; aber was passiert dann hier?
(def a3 (map->Artikel
          {:artnr 3
           :bez "Hemd"
           :preis 5095}))

; CompilerException java.lang.ClassCastException: 
; java.lang.Long cannot be cast to java.lang.Integer


;; Artikel mit Prismatic Schema

(s/defrecord Artikel
             [artnr :- s/Int
              bez   :- s/Str
              preis :- s/Int])

(def a5 (map->Artikel
          {:artnr 5
           :bez "Hemd"
           :preis 5095}))

(def a6 (map->Artikel
          {:artnr "eine sechs hier"
           :bez "Hemd"
           :preis 5095}))

(s/validate Artikel a5)
; okay

(s/validate Artikel a6)
; CompilerException clojure.lang.ExceptionInfo: 
; Value does not match schema: {:artnr (not (integer? "eine sechs hier"))} 

(s/check Artikel a5)
; => nil

(s/check Artikel a6)
; => {:artnr (not (integer? "eine sechs hier"))}

(s/explain Artikel)
; => (record clj.l13_swt.Artikel {:artnr Int, :bez Str, :preis Int})

(pres :add "
## Prismatic Schema

Prismatic Schema eignet sich gut für folgendes Vorgehen:

- Daten, die wir selbst erzeugen, prüfen wir zur Laufzeit nicht
- Daten, die wir aus externen Quellen entgegennehmen aber schon
")



;; 2. Softwaretechnik im Kleinen

;; 2.1 Kontrakt von Funktionen

(pres "
# Softwaretechnik im Kleinen

## Kontrakt von Funktionen

- Vorbedindungen
- Nachbedingungen 
- Assertions

Beispiel:

Auf dem Parkplatz sind Autos und Motorräder. Wir kennen die
Anzahl der Fahrzeuge und die Anzahl der Räder.

Gesucht ist die Zahl der PKWs

")

;; Erster Versuch
(defn zahl-pkw
  "Berechnet die Zahl der PKWs ausgehend von 
   n der Zahl der Fahrzeuge und m der Zahl der Räder auf dem Parkplatz"
  [n m]
    (/ (- m (* 2 n)) 2))

;; Probieren wir das mal
(zahl-pkw 2 8)
; => 2

(zahl-pkw 5 8)
; => -1 Ups!

;; Vor- und Nachbedingungen in Clojure

(defn zahl-pkw
  "Berechnet die Zahl der PKWs ausgehend von 
   n der Zahl der Fahrzeuge und m der Zahl der Räder auf dem Parkplatz"
  [n m]
  { :pre  [(even? m), (<= 0 (* 2 n) m (* 4 n))]
    :post [(>= % 0)] }          
    (/ (- m (* 2 n)) 2))
    

(zahl-pkw 5 8)
; => AssertionError Assert failed: (<= 0 (* 2 n) m (* 4 n))  clj.l13-swt/zahl-pkw

(zahl-pkw 2 1)
; => AssertionError Assert failed: (even? m)  clj.l13-swt/zahl-pkw

; Die Prüfung kann man aus- und einschalten
(set! *assert* false)

*assert*

;; neu compilieren
(defn zahl-pkw
  "Berechnet die Zahl der PKWs ausgehend von 
   n der Zahl der Fahrzeuge und m der Zahl der Räder auf dem Parkplatz"
  [n m]
  { :pre  [(even? m), (<= 0 (* 2 n) m (* 4 n))]
    :post [(>= % 0)] }          
    (/ (- m (* 2 n)) 2))

(zahl-pkw 2 1)

;; Alternative

;; Externe Zuordnung der Vorbedingung mit dire
(use 'dire.core)

(defn zahl-pkw
  "Berechnet die Zahl der PKWs ausgehend von 
   n der Zahl der Fahrzeuge und m der Zahl der Räder auf dem Parkplatz"
  [n m]
    (/ (- m (* 2 n)) 2))

(with-precondition! #'zahl-pkw
  "Prüft Argumente von zahl-pkw"
  :check-n-m
  (fn [n m]
    (and (even? m) (<= 0 (* 2 n) m (* 4 n)))))

(with-handler! #'zahl-pkw
  {:precondition :check-n-m}
  (fn [e & args] (apply str "Precondition failure for argument list: " (vector args))))

;; Ausprobieren
(zahl-pkw 2 1)

(zahl-pkw 2 8)

(zahl-pkw 1 8)

(zahl-pkw 3 10)

; 2.2 Testen

(pres :add "

## Testen

- es ist sehr einfach _Unittests_ zu machen

")

;; wieder die Funktion über die Zahl der PKWs, diesmal mit einer Exception

(defn zahl-pkw
  "Berechnet die Zahl der PKWs ausgehend von 
   n der Zahl der Fahrzeuge und m der Zahl der Räder auf dem Parkplatz"
  [n m]
  (if (and (even? m) (<= 0 (* 2 n) m (* 4 n)))
    (/ (- m (* 2 n)) 2)
    (throw (ArithmeticException. "Parameter nicht erlaubt."))))

;; siehe test/clj/swt-test.clj aufrufen

; 2.3 Tracing

(pres :add "
## Tracing

- Verfolgen der Funktionsaufrufe (`clojure.tools.trace`)
- auch _Spyscope_
")

;; Wir definieren die Funktion nicht mit defn, sondern mit deftrace

(deftrace zahl-pkw
  "Berechnet die Zahl der PKWs ausgehend von 
   n der Zahl der Fahrzeuge und m der Zahl der Räder auf dem Parkplatz"
  [n m]
  (if (and (even? m) (<= 0 (* 2 n) m (* 4 n)))
    (/ (- m (* 2 n)) 2)
    (throw (ArithmeticException. "Parameter nicht erlaubt."))))


(zahl-pkw 2 8)

;; schön sieht man die rekursiven Aufrufe
(deftrace fib [n]
	 (if (or (= n 0) (= n 1))
     1
     (+ (fib (- n 1)) (fib (- n 2)))))

(fib 4)

;: trace
(reduce + (range 1 10))
(reduce #(trace :reduction (+ %1 %2)) (range 1 10))

;; nicht so sehr praktisch!

;; 3. Softwaretechnik im Großen

;; 3.1 Komponentenkonzept Namespaces

(pres "
# Softwaretechnik im Großen

## Komponentenkonzept Namespaces

- Jedes Symbol lebt in einem Namensraum
- Man kann Namensräume in den globalen Scope einblenden
- ...oder mit einem Alias ansprechen
")

;; Funktion aus clojure.string
(capitalize "hallo")
; => CompilerException java.lang.RuntimeException: Unable to resolve symbol: capitalize in this context

(str/capitalize "hallo")
; => "Hallo"

;; Funktion aus clojure.math.combinatorics
;; siehe (ns ...) am Anfang der Datei
(selections #{\a \b \c} 2)
; => ((\a \a) (\a \b) (\a \c) (\b \a) (\b \b) (\b \c) (\c \a) (\c \b) (\c \c))

(pres :add "
## Diskussion

- Viele Funktionen in einem Namensraum    
  Beispiel `clojure.core`
- Öffentliche API, die von Funktionen aus vielen Namensräumen besteht und
  abhängt     
  Beispiel `ring` hat 30+ Namensräume
- Tricks zum Trennen der API für Verwender und für die Implementierung    
  `potemkin`    
- siehe auch: [Mark Engelberg über Namensräume] (http://programming-puzzler.blogspot.de/2013/12/frustrations-with-namespaces-in-clojure.html)
  
")

; 3.2 Große Systeme
(pres "
# Softwaretechnik im Großen

## Stuart Sierras Component Model

- Large applications often consist of many stateful processes which must be started 
  and stopped in a particular order.     
  The component model makes those relationships explicit and declarative, 
  instead of implicit in imperative code.
- Components provide some basic guidance for structuring a Clojure application, 
  with boundaries between different parts of a system. Components offer some encapsulation, 
  in the sense of grouping together related entities.
- Having a coherent way to set up and tear down all the state associated with an application 
  enables rapid development cycles without restarting the JVM.
- [Dokumentation von `component`](https://github.com/stuartsierra/component)  

")

; 3.3 Domänenspezifische Sprachen
(pres "
# Softwaretechnik im Großen

## Domänenspezifische Sprachen

- Externe vs. interne DSLs    
  XText vs Clojure    
  Einsatz Fachexperte / Entwickler
- Wrapper für Konstrukte anderer Sprachen    
  Beispiel: `hiccup` für HTML
- Makros für eine syntaktische Konstrukte   
  Beispiel `tools.trace` - `deftrace`
")