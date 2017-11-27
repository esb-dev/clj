; Programmierung in Clojure Vorlesung 9
; Beispiel zu Protocols
; (c) 2014 - 2017 by Burkhardt Renz, THM

(ns clj.poly-protocol)

stop

(defrecord Kunde [kndNr name vorname])

(defn kunde [kndNr name vorname]
  (Kunde. kndNr name vorname))

; identisch zu poly-multi
(def test-kunden
  [(kunde 1 "Schneider" "Hans")
   (kunde 2 "Henning" "Christa")
   (kunde 3 "Berendt" "Uwe")])

test-kunden

(defrecord Artikel [artNr bez preis])

(defn artikel [artNr bez preis]
  (Artikel. artNr bez preis))

(def test-artikel
  [(artikel 1 "BitteEinBit" 1.20)
   (artikel 2 "VinoVino" 9.90)
   (artikel 3 "Hennessy" 24.95)])

test-artikel

(def test-matrix
  [[ "K1" "Schneider" "Hans"]
   [ "K2" "Bauer" "Maria"]
   [ "K3" "Maining" "Theo"]
   [ "K4" "Klausen" "Petra"]])

(defprotocol RowInfo
  (fld-names [this])
  (fld-count [this])
  (fld [this index]))

(extend-protocol RowInfo
  Kunde
  (fld-names [this]
    (map (comp clojure.string/capitalize name) (keys this)))
  (fld-count [this]
    (count (keys this)))
  (fld [this index]
    (nth (vec (vals this)) index)))

; Diese Funktion ist vollkommen identisch zur gleichnamigen Funktion in poly-multi

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

(print-html-table test-kunden)

(defn format-money [m] (format "%.2f" m))

; für Artikel und Vektoren

(extend-protocol RowInfo
  Artikel
  (fld-names [this]
    ["ArtNr", "Bez", "Preis", "davon MWSt"])
  (fld-count [this]
    4)
  (fld [this index]
     (case index
       0 (:artNr this)
       1 (:bez this)
       2 (format-money (:preis this))
       3 (format-money (* 0.159693 (:preis this)))))
  clojure.lang.PersistentVector
  (fld-names [this]
    (range (count this)))
  (fld-count [this]
    (count this))
  (fld [this index]
    (nth this index)))
  
    
(print-html-table test-kunden)
(print-html-table test-artikel)
(print-html-table test-matrix)

; Demo zu den Eigenschaften von Records

(def k1 (first test-kunden))

(:name k1)
; => "Schneider"

(assoc k1 :name "Neumann")
; => #clj.poly_protocol.Kunde{:kndNr 1, :name "Neumann", :vorname "Hans"}

(assoc k1 :plz "35390")
; => #clj.poly_protocol.Kunde{:kndNr 1, :name "Schneider", :vorname "Hans", :plz "35390"}