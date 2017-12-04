; Programmierung in Clojure Vorlesung 10  
; Identität, Zustand und Synchronisationskontrolle
; (c) 2014 - 2017 by Burkhardt Renz, THM

(ns clj.l10-state
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Identität, Zustand und Synchronisationskontrolle")

(pres :add "
# Clojures Verständnis von Identität und Zeit

![Zeitmodell](resources/time-model.jpg)

- Box repräsentiert Identität (Referenz)
- Box beinhaltet einen Wert (Wert)
- Inhalt ist ein Wert zu einer bestimmten Zeit (Zustand)
- Funktion verändert den Inhalt der Box, aber nicht den Wert!
- Beobachter sehen immer nur gültige Zustände
")

(pres "
# Klassifikation von Referenztypen

![Referenztypen](resources/ref-types.jpg)

")

; koordiniert = mehrere Zugriffe werden koordiniert
; unkoordiniert = atomare Zugriffe

(pres "
# Synchronisationskontrolle
- Transaktionaler Speicher    
  _Software Transactional Memory_     
  d.h. Transaktionen bei Speicherzugriffen
- Konzept: Multiversionierung    
  _Multiversion Concurrency Control MVCC_    
  garantiert:    
    - Atomarität
    - Konsistenz
    - Isolation
")

(pres "
# Referenztypen 1: Atoms

- Atoms halten _einen_ Wert
- der selbst aber komplex sein kann, eben eine Datenstruktur von Clojure
- Ändern des Werts in der Box geschieht _atomar_
- aber nicht koordiniert mit Änderungen anderer Atoms
- also: synchron/nicht koordiniert
")


;; Atoms

;; atom erzeugt ein atom
(def age (atom 25))

age

;; deref oder kürzer @ gibt den Wert in der Box
(deref age)
; => 25

@age
; => 25

;; swap! ändert den Wert des Atoms mittels der übergebenen Funktion
(swap! age inc)
; => 26 

;; Die Funktion nimmt als ersten Parameter den Inhalt der Box und schreibt
;; das Ergebnis in die Box

@age
; => 26

;; compare-and-set! ändert den Wert in der Box, vorausgesetzt der aktuelle Wert
;; ist identisch mit dem angegebenen Wert
(compare-and-set! age 26 27)
; => true

@age

(compare-and-set! age 26 28)
; => false -- denn jetzt ist age ja schon 27

;; reset! weist neuen Wert zu
(reset! age 30)
; => 30

@age
; => 30

;; Makros zur Demonstration vom MVCC aus Clojure Programming S.173
;; Mehr über Makros kommt später

(defmacro futures
  "erzeugt n futures für jeden übergebenen Ausdruck"
  [n & exprs]
  (->> (for [expr exprs]
         `(future ~expr))
    (repeat n)
    (mapcat identity)
    vec))

(defmacro wait-futures
  "erzeugt n futures für jeden übergebenen Ausdruck und blockiert bis alle fertig sind"
  [& args]
  `(doseq [f# (futures ~@args)]
     @f#))

; Beispiel aus Clojure Programming S.174

(def xs (atom #{1 2 3}))

@xs

(defn f4 [v]
  (Thread/sleep 250)
  (println "trying 4")
  (conj v 4))

(defn f5 [v]
  (Thread/sleep 500)
  (println "trying 5")
  (conj v 5))

(wait-futures 1 (swap! xs f4)
                (swap! xs f5))

;trying 4
;trying 5
;trying 5
;=> nil

;; f5 wird zweimal aufgerufen wird
;; Warum?

(pres "
# MVCC - Semantik von swap!

![swap!](resources/atoms.jpg)

- f4 und f5 starten fast gleichzeitig
- f5 liest zunächst den Wert, wartet dann aber länger als f4
- beim Schreiben ist das atom nicht mehr im gleichen Zustand
- also: restart von f5
")

(pres "
# Möglichkeiten mit Referenztypen

- Referenztyp kann mit Validator versehen werden
- Referenztyp kann mit Beobachter ( _watch_ ) versehen werden
")

;; Atom mit Validator
(def age (atom 19 :validator #(>= % 18)))

@age

(swap! age dec)
; => 18

(swap! age dec)
; => IllegalStateException Invalid reference state

;; Watches

;; Dem Atom kann man eine Watch-Funktion zuordnen, die
;; folgende Signatur hat:
;; key = Keyword zur Identifizierung der Funktion
;; ref = der Referenztyp, in unserem Beispiel das Atom
;; old = der bisherige Wert
;; new = der neue Wert
;;
;; Ein _watch_ ist vergleichbar mit einem Trigger in einem DBMS
  
(defn echo-watch
  [key ref old new]
  (println key old "=>" new))

;; add-watch ordnet der Atom die Watch-Funktion zu
(add-watch age :echo echo-watch)

(swap! age inc)

(reset! age 25)
; Man sieht, dass in beiden Fällen die Funktion aufgerufen wird

(pres "
# Referenztypen 2: Refs

- Refs werden in Transaktionen verwendet, um atomare Änderungen mehrerer
  Boxen zu erreichen
- also: synchron/koordiniert
- Synchronisation erfolgt durch Clojures STM, _nicht_ durch den Programmierer
- keine _race conditions_ möglich
- keine Verklemmung möglich
- Verhalten entspricht _snapshot isolation_ in DBMS mit MVCC  
")

;; Refs mit Konto

;; Record für Konto
(defrecord Account [id balance])

;; Zwei Refs für Konto 1 und 2 mit Saldo 100
;; ref erzeugt ein Ref
(def a1-ref (ref (Account. "1" 100)))
(def a2-ref (ref (Account. "2" 100)))

;; deref oder @ ergeben den Wert der Ref
@a1-ref
; => #clj.l10_state.Account{:id "1", :balance 100}

;; Ein paar Operationen mit Konten

;; Geld abheben
(defn withdraw
  "Withdraws an amount of money from an account"
  [account amount]
  (assoc account :balance (- (:balance account) amount))) 

(def a1 (Account. "1" 100))
(withdraw a1 20)
; => #clj.state.Account{:id "1", :balance 80}

;; Geld einzahlen
(defn deposit
  "Deposits an amount of money to an account"
  [account amount]
  (assoc account :balance (+ (:balance account) amount))) 

(deposit a1 20)
; => #clj.state.Account{:id "1", :balance 120}


;; Beide Funktionen erzeugen _neues_ Objekt!
(identical? a1 (deposit a1 20))
; => false

;; Geld überweisen
(defn transfer
  "Transfers an amount of money from one account to another"
  [from to amount]
  (alter from withdraw amount)
  (alter to deposit amount))

; Zweites Konto
(def a2 (Account. "2" 100))

(transfer a1 a2 20)
; => ClassCastException clj.state.Account cannot be cast to clojure.lang.Ref
;; alter geht nur mit Refs und in einer Transaktion

(defn account-info [a-ref]
  (println "id" (:id @a-ref) "balance" (:balance @a-ref)))

(account-info a1-ref)
  
;; Also brauchen wir Transaktionen für unseren beiden Refs von oben
; Geld abheben
(defn withdraw! [account amount ms]
  (dosync
    (do
      (Thread/sleep ms)
      (println (.getName (Thread/currentThread)) "attempts to withdraw")
      (alter account withdraw amount)
      (println (.getName (Thread/currentThread)) "tries to commit"))))
      
; Geld überweisen
(defn transfer! [from to amount ms]
  (dosync
    (do
      (Thread/sleep ms)
      (println (.getName (Thread/currentThread)) "attempts to transfer")
      (transfer from to amount)
      (println (.getName (Thread/currentThread)) "tries to commit"))))
      

; Wir starten mit jeweils 100 pro Konto
; t1: 100 von a1 auf a2 überweisen -> a1 = 0, a2 = 200
; t2:  50 von a1 abziehen          -> a1 = 0, a2 = 150
(dosync (ref-set a1-ref (Account. "1" 100)))
(dosync (ref-set a2-ref (Account. "2" 100)))
(let [t1 (Thread. #(transfer! a1-ref a2-ref 100 150) "Thread 1")
      t2 (Thread. #(withdraw! a2-ref 50 100) "Thread 2")]
  (.start t1)
  (.start t2)
  (.join t1)
  (.join t2)
  (account-info a1-ref)
  (account-info a2-ref))
  
;; In der Konsole kann man sehen, dass t1 zweimal versucht zu überweisen.
;; Warum?

(pres "
# Semantik von _dosync_

![Semantik von dosync](resources/refs.jpg)

")


(pres "
# _alter_ und _commute_

- _alter_ gibt den Wert zurück, der tatsächlich beim commit gesetzt wird
- _commute_ gibt den Wert zurück, den die Ref im Moment hat
- aber die Funktion von _commute_ wird beim commit erneut aufgerufen und
  verändert erst dann die mittlerweile durch andere veränderte Ref
- dadurch wird ein Neustart der Transaktion vermieden
- aber: die Funktion, die man _commute_ übergibt, muss so sein, dass
  die Reihenfolge der Ausführung keine Rolle spielt
- die Bezeichnung _commute_ kommt von _kommutativ_ (a + b = b + a)  
")

;; Beispiel mit commute
(defn withdraw! [account amount ms]
  (dosync
    (do
      (Thread/sleep ms)
      (println (.getName (Thread/currentThread)) "attempts to withdraw")
      (commute account withdraw amount)
      (account-info account)
      (println (.getName (Thread/currentThread)) "tries to commit"))))

(dosync (ref-set a1-ref (Account. "1" 100)))
(dosync (ref-set a2-ref (Account. "2" 100)))

(let [t1 (Thread. #(withdraw! a1-ref 20 100) "Thread 1")
      t2 (Thread. #(withdraw! a1-ref 30 100) "Thread 2")]
  (.start t1)
  (.start t2)
  (.join t1)
  (.join t2)
  (account-info a1-ref))

@a1-ref

(pres "
# Zwei gleichzeitige Abbuchungen - unser Beispiel

- Konto 1 hat einen Saldo von 100
- Transaktion 1 liest Saldo 100, zieht 20 ab, Ergebnis: 80
- Transaktion 2 liest Saldo 100, zieht 30 ab, Ergebnis: 70
- Transaktion 1 schreibt beim commit den Wert 80
- Transaktion 2 versucht beim commit den Wert 70 zu schreiben   
  stellt jedoch einen Schreibkonflikt fest!
- bei _alter_: Transaktion 2 wird abgebrochen und automatisch neu gestartet
- bei _commute_: Transaktion 2 liest den veränderten Wert 80 aus der Ref und
  führt die Funktion erneut aus und schreibt den Wert 50
- Fazit: _commute_ vermeidet den Neustart der Transaktion  
")

;; wenn man Glück hat, sieht man, dass einmal der Saldo 80 und einmal 70 ist,
;; erst ganz am Ende stimmt wieder alles.

(pres "
# _Write skew_ bei _Snapshot isolation_

![write skew](resources/writeskew.jpg)

- Transaktion 1 liest den Wert von _a_
- Transaktion 2 ändert den Wert von _a_
- Transaktion 1 ändert den Wert von _b_, obwohl sich der Wert von _a_ geändert hat
- dieses Verhalten ist _nicht_ Serialisierbarkeit!

")

(pres "
# Vermeiden des Phänomens _write skew_

![ensure](resources/ensure.jpg)

- Transaktion 1 liest den Wert von _a_ mit _ensure_
- dadurch wird dem STM mitgeteilt, dass _a_ jetzt eine neue Version hat
- Transaktion 2 kann jetzt den Wert von _a_ nicht mehr ändern, weil ein
  Schreibkonflikt auftritt
- entspricht in DBMS `select ... for update`  

")

(pres "
# Hinweise zu Atoms und Refs

- Man darf nur _reine_ Funktionen verwenden
- Bei Refs kann man wie bei Atoms Validatoren und Watches einsetzen
- Möglichst kurze Transaktionen machen
- Clojure versucht zu verhindern, dass eine Transaktion _verhungert_, 
  weil sie immer wieder gestartet wird.     
  Es gibt eine maximale Zahl von Wiederholungsversuchen
- In Funktionen, die I/O durchführen, soll man einen _io!_-Block verwenden, weil
  dieser eine Exception auslöst, wenn er innerhalb einer Transaktion aufgerufen wird

")

(doc io!)

(pres "
# Referenztyp 3: Agent

- ein _agent_ enthält einen Wert, der asynchron, aber atomar verändert werden kann
- Änderungen geschehen durch Funktionen, die dem _agent_ gesendet werden.    
  Zu einem späteren Zeitpunkt hat der _agent_ dann den Wert, der durch die Funktion
  berechnet wurde.
- Ein _agent_ kann jederzeit dereferenziert werden.    
  Er hat dann den bisherigen Wert oder einen neuen Wert nach dem Senden einer Funktion.
- _agents_ sind integriert in den transaktionalen Speicher.    
  D.h. eine in einer Transaktion vorgesehene Aktion wird erst beim Commit ausgeführt und eventuell
  nicht, wenn die Transaktion neu gestartet werden muss.
")

;; agent definiert einen agent
(def ag (agent 42))

;; ein agent
ag

;; sein Wert
(deref ag)
; => 42
@ag
; => 42

;; Ändern des Werts eine Agents
;; die Funktion soll lange dauern
(send ag (fn [x] (dotimes [_ 10000000000] x) (inc x)))
(deref ag)
; => 42

(deref ag)
; => 43 -- etwas verzögert!


(pres "
# Funktionen mit _agents_

- `send` und `send-off` geben dem Agenten eine Funktion zur Ausführung auf dem
  aktuellen Wert
- `send` verwendet eine Thread aus einem fixen Pool - sollte man für Aufträge
  verwenden, die schnell ausgeführt werden können
- `send-off` verwendet einen Thread aus einem Pool, der auch wachsen kann.    
  `send-off` eignet sich also z.B. auch für I/O
- `await`, `await-for` blockiert den aktuellen Thread und wartet die Ausführung aller
  laufenden Aktionen der _agents_ ab, evtl. mit Timeout
- `shutdown-agents` beendet alle Threads im Pool der _agents_ 
- _agents_ haben eine ausgefeilte Fehlerbehandlung
- Wie bei anderen Referenztypen kann man Validatoren und Watches einsetzen
 
")

;; Beispiel für die Verwendung von Agents zur Parallelisierung

;; Passwörter knacken, inspiriert durch
;; http://travis-whitton.blogspot.de/2009/06/clojures-agents-scientists-monkeys-and_18.html

;; Wir wollen Passwörter bestehend aus 4 Buchstaben knacken

;; lazy seq aller möglicher Klartexte
(def words
  (let [chars (map char (range (int \a) (inc (int \z))))]
    (for [l1 chars, l2 chars, l3 chars, l4 chars]
      (str l1 l2 l3 l4))))

;; die ersten 5 aller möglichen Klartexte
(take 5 words)

(count words)

;; md5sum
(import '(java.security NoSuchAlgorithmException MessageDigest))

;; Verschlüsselung
(defn md5sum
  "calculates MD5 hashes"
  [^String str]
  (let [alg (doto (MessageDigest/getInstance "MD5")
            (.reset)
            (.update (.getBytes str)))]
  (try
    (.toString (new BigInteger 1 (.digest alg)) 16)
    (catch NoSuchAlgorithmException e
      (throw (new RuntimeException e))))))

; aus der Java Dokumentation:
; A MessageDigest object starts out initialized. 
; The data is processed through it using the update methods. 
; At any point reset can be called to reset the digest. 
; Once all the data to be updated has been updated, one of 
; the digest methods should be called to complete the 
; hash computation.

;; Beispiel für die Verschlüsselung eines Klartexts
(md5sum "abcd")
; => "e2fc714c4727ee9395f324cd2e7f331f"

;; Memoize merkt sich einmal berechnete Werte
(def md5-memo (memoize md5sum))

;; Entschlüsseln - brute force
(defn decode-md5 [md5]
  (first (filter #(= md5 (md5-memo %)) words)))

(decode-md5 "e2fc714c4727ee9395f324cd2e7f331f")
; => "abcd"

(decode-md5 (md5sum "abcz"))
; => "abcz"

;; Entschlüsseln einer Kollektion von verschlüsselten Passwörtern
(defn decode-bucket [bucket]
  (map decode-md5 bucket))

;; produziert size zufällige kodierte Passwörter
(defn random-bucket [size]
  (vec (repeatedly size #(md5-memo (rand-nth words)))))

;; 20 zufällige kodierte Passwörter
(def large-bucket (random-bucket 20))

large-bucket

(time 
  (println (decode-bucket large-bucket)))
; => 5 sec oder so

;; nun verteilen auf  Agenten

;; Zahl der Agenten
(def num-agents 10)
 
;; Aufteilen der Arbeit
(def work-buckets (partition (int (/ (count large-bucket)
                                   num-agents)) large-bucket))

work-buckets

;; Pro Bucket wird ein Agent gestartet
(defn spawn-agents [agents buckets]
  (if (empty? buckets)
    agents
    (recur (conj agents (agent (first buckets)))
         (rest buckets))))

#_(def agents (spawn-agents [] work-buckets))
 
; Jeder Agent kriegt die Dekodierfunktion für seinen Teil
#_(doseq [agent agents]
  (send agent decode-bucket))
 
; Warten bis die Agenten fertig sind
#_(apply await agents)
 
; Ergebnis
#_(doseq [agent agents]
  (doseq [result @agent] (println result)))
 
; Aufräumen
#_(shutdown-agents)

; Messung
(time
  (let [agents (spawn-agents [] work-buckets)]
  (doseq [agent agents]
	    (send agent decode-bucket))
	  (apply await agents)
    (doseq [agent agents]
      (println @agent))))
; => etwa 4 sec

(pres "
# Referenztyp 4: Vars

- Eine _Var_ ist ein Speicherort für einen Wert (also auch eine Funktion) in Clojure
- Vars werden mit `def` oder einer davon abgeleiteten Form erzeugt
- Vars sind an einen Namensraum gebunden
- sie haben Metadaten
")

(pres "
# Mehr über Vars

- Private Vars können in einem anderen Namensraum nur mit voll qualifiziertem Namen verwendet
  werden
- Vars können eine Dokumentation haben
- Vars können als konstant deklariert werden
- Vars sind normalerweise durch den lexikalischen Bereich (scope) festgelegt, also im Prinzip
  global
- sie können jedoch auch als _dynamic_ definiert werden und können dann durch `binding` für
  den aktuellen Thread dynamisch gebunden werden    
  Beispiel: `*out*` ist eine dynamische Var, die im globalen Scope auf `stdout` steht
")

;; Private Var
(def ^:private intim 42)

;; in unserem Namensraum
intim
; => 42

;; Wechsel des Namensraums
(ns other)

intim
; => Exception Symbol unbekannt

;; und zurück 
(ns clj.l10-state)

intim
; => 42

;; Dokumentation zu einer Var
(def uz
  "Die ultimative Zahl"
  42)

(doc uz)
; ------------------------
; clj.l10-state/a
; Die ultimative Zahl

; Konstanten
(def ^:const pi (Math/PI))

pi
;=> 3.141592653589793


(pres "
# Konstante Vars
  
Welche Wirkung hat ^:const?

Normalerweise wird der Wert eines Symbols in der Root-Umgebung gemerkt
und bei jeder Verwendung wird der Wert dort ermittelt.

Wenn man eine Var mit ^:const versieht, kann der Compiler Code erzeugen,
bei dem der Wert direkt (inline) eingesetzt wird.

Das bringt eine Beschleunigung."
)

(pres "
# Verzögerte Referenzen

- Delays
- Futures
- Promises
")


(pres "
# Delays

- Ein _delay_ ist eine Referenz auf ein Stück Code, dessen Wert erst auf
  Anforderung ermittelt wird
- Die Ausführung wird durch Dereferenzierung oder `force` erzwungen
- Der Delay wird nur einmal ausgeführt und hat dann stets das gecachte Ergebnis  
")

;; Definition eines delays
(def de (delay (println "Delay wird ausgeführt")
              :fertig))

;; bereits ausgeführt?
(realized? de)
; => false

;; Dereferenzieren
@de
; Delay wird ausgeführt 
; => :fertig

(realized? de)
; => true

@de
; => :fertig
; Beim zweiten Aufruf wird println nicht mehr ausgeführt


(pres "
# Futures

- Ein _future_ ist eine Auswertung eines Ausdrucks in einem anderen Thread
- Clojure verwendet einen Thread-Pool für die Futures
- Ein Future in Clojure ist ein `java.util.concurrent.Future`

## Funktionen

- `future`, `future-call` erzeugen einen Future
- `deref`, `@` oder `force` werten den Future aus und blockieren, bis die
   Auswertung erfolgt ist
- `future-cancel` bricht die Ausführung ab   
")

;; Definition eines Futures
(def f1 (future (apply + (range 1e8))))

;; Schon fertig?
(realized? f1)
; => false, wenn man nicht zu lange wartet

(realized? f1)
; => später true

(deref f1)
; => 4999999950000000

(pres "
# Promises

- Ein _promise_ ist eine Referenz auf einen Wert, der dadurch in die Welt kommt,
  dass er irgendwann von jemand anderem gesetzt wird.
- Man kann einen Promise als eine einwertige Pipe sehen, die man einmal verwenden kann
- Promises eignen sich für _Datenflussvariablen_:    
  Ein Thread blockiert beim Auswerten eines Promise solange, bis ein anderer Thread den
  Wert des Promise setzt.
")

;; Definition eines Promise
(def p1 (promise))

;; Wert gesetzt?
(realized? p1)
; => false

;; Wert des Promise setzen
(deliver p1 42)
; => #object[clojure.core$promise$reify__6779 0xcdeda6e {:status :ready, :val 42}]

;; Wert gesetzt?
(realized? p1)
; => true

;; Dereferenzierung
@p1 
; => 42

;; Beispiel für Datenfluss-Steuerung Clojure Programming S.164

(def a (promise))
(def b (promise))
(def c (promise))

;; Wir haben 3 promises und wollen, dass
;; c erst dann einen Wert bekommt, wenn a und b einen
;; Wert haben und dieser Wert soll die Summe von @a und @b
;; sein.

(realized? a)
; => false

;; ein neuer Thread, der a und b verwendet
(future
  (deliver c (+ @a @b))
  (println "c hat jetzt einen Wert"))
; => #object[clojure.core$future_call$reify__6736 0x39b13b55 {:status :pending, :val nil}]

(deliver a 21)
@a
; => 21

(realized? c)
; => false

(deliver b 21)

(deref c)
; c hat jetzt einen Wert
; => 42

(pres "
# Was noch, wo weiter?

- für Szenarien, in denen die besprochenen Mechanismen der Synchronisationskontrolle
  nicht ausreichen:    
  `locking` sowie `java.util.concurrent.*`
- `core.async` für nebenläufige Aktivitäten, die über Kanäle kommunizieren    
  Programmiermodell à la Hoare's _Communicating Sequential Processes_    
")