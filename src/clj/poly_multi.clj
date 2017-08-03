; Programmierung in Clojure Vorlesung 9
; Beispiel zu Multimethods
; (c) 2014 - 2015 by Burkhardt Renz, THM

(ns clj.poly-multi)

stop

; Entitäten werden in Clojure als Maps mit Meta-Informationen implementiert:
(defn kunde [kndNr name vorname]
   ^{:type ::kunde} {:kndNr kndNr, :name name, :vorname vorname})

::kunde
; => :clj.poly-multi/kunde
; d.h. ::kunde ist ein keyword mit "eingebautem" Namespace

; ^{:type ::kunde}
; sind Metadaten zur jeweiligen Entität
; man kann Entitäten danach befragen:
(type (kunde 1 "a" "b"))
; => :clj.poly-multi/kunde

(def test-kunden
  [(kunde 1 "Schneider" "Hans")
   (kunde 2 "Henning" "Christa")
   (kunde 3 "Berendt" "Uwe")])
  
(defn artikel [artNr bez preis]  
  ^{:type ::artikel} {:artNr artNr, :bez bez, :preis preis})

(def test-artikel
  [(artikel 1 "BitteEinBit" 1.20)
   (artikel 2 "VinoVino" 9.90)
   (artikel 3 "Hennessy" 24.95)])

(def test-matrix
  [[ "K1" "Schneider" "Hans"]
   [ "K2" "Bauer" "Maria"]
   [ "K3" "Maining" "Theo"]
   [ "K4" "Klausen" "Petra"]])

; Deklarationen
(declare fld-names)
(declare fld-count)
(declare fld)

; Funktion zur Ausgabe der Html-Tabelle
(defn print-html-table [data]
  (println "<table>")
  (print "<tr>")
  (doseq [fld-name (fld-names (first data))]
    (print (str "<th>" fld-name "</th>")))
  (println "</tr>")
  (doseq [row data]
    (print "<tr>")
    (doseq [index (range (fld-count row))]
      (print (str "<td>" (fld row index) "</td>")))
    (println "</tr>"))
  (println "</table>"))

; Multimethods

; Definition der Funktionen mit ihrer Dispatch-Funktion
(defmulti fld-names (fn [this] (type this)))
(defmulti fld-count (fn [this] (type this)))
(defmulti fld (fn [this index] (type this)))

; Für Kunde
(defmethod fld-names ::kunde [_]
  ["KndNr" "Name" "Vorname"])
(defmethod fld-count ::kunde [_]
  3)
(defmethod fld ::kunde [this index]
  (nth (vec (vals this)) index))

; Für Artikel
(defn format-money [m] (format "%.2f" m))
(defmethod fld-names ::artikel [_]
  ["ArtNr" "Bez" "Preis" "davon MWSt"])
(defmethod fld-count ::artikel [_]
  4)
(defmethod fld ::artikel [this index]
  (case index
    0 (:artNr this)
    1 (:bez this)
    2 (format-money (:preis this))
    3 (format-money (* 0.159693 (:preis this)))))

; Für Vektor

; Was ergibt type für einen Vektor?
(type [1 2 3])
; => clojure.lang.PersistentVector
; also ist das der Wert, den die Dispatchfunktion liefert

(defmethod fld-names clojure.lang.PersistentVector [this]
  (map #(str (char (+ % 97))) (range (count this))))
(defmethod fld-count clojure.lang.PersistentVector [this]
  (count this))
(defmethod fld clojure.lang.PersistentVector [this index]
  (nth this index))

(print-html-table test-artikel)
(print-html-table test-kunden)
(print-html-table test-matrix)