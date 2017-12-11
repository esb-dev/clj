; Programmierung in Clojure Vorlesung 11  
; Interoperabilität mit Java
; (c) 2014 - 2017 by Burkhardt Renz, THM

(ns clj.l11-java-iop
  (:require [clj.presentation :refer :all])
  (:require [clojure.repl :refer :all]))

stop
; B zurück zur letzten Seite!

(init "Interoperabilität mit Java")

(pres :add "
# Clojure, Java und die JVM

- Clojure ist eine Sprache für die JVM
- Clojure kompiliert zu Bytecode, hat direkten Zugriff auf die JVM
- Clojure verwendet Java, z.B.    
     - Clojure Strings sind Java Strings
     - Clojure Funktionen sind Methoden in Java
     - Clojure Records sind Klassen in Java
     - Clojure Protocols sind Interfaces in Java
- Clojure kann aber auch auf anderen Plattformen verwendet werden:    
     - Clojure auf die CLR _Common Language Runtime_ von .NET
     - ClojureScript kompiliert zu JavaScript 
")

;; Beispiel für die Verwendung von Java
;; eine HTML-Seite in den Speicher laden

;; Wir importieren die benötigte Klasse
(import 'java.net.URL)

;; URL. ist der Konstruktor für ein Objekt der Klasse URL
(def mysite (URL. "http://homepages.thm.de/~hg11260"))

;; Von welcher Klasse ist mysite?
(class mysite)
; => java.net.URL

(.getHost mysite)
; => "homepages.thm.de"

(slurp mysite)
; => Inhalt meiner Startseite

;; Statische Methoden und Felder

Math/PI
; => 3.141592653589793

(Integer/parseInt "123")
; => 123

;; Konstruktor von Objekten
(import 'java.awt.Point)

;; (Class. args) ruft den Konstruktor auf
(def pt (Point. 5 10))
; => #'clj.l11-java-iop/pt

;; Explizites new geht auch
(def pt1 (new Point 5 10))
; => #'clj.l11-java-iop/pt1

pt
; => #object<java.awt.Point [x=5,y=10]>

pt1
; => #object<java.awt.Point [x=5,y=10]>

(= pt pt1)
;=> true
; Warum: Point implementiert equals auf Basis der Werte von x und y

(identical? pt pt1)

;; Felder von Objekten
(set! (.x pt) 42)
; => 42

pt
; => #object<java.awt.Point [x=42,y=10]> 

;; Wert der x-Koordinate
(.x pt)
; => 42

;; Aufruf einer Methode des Objekts
(.move pt 1 1)
; => nil

[(.x pt) (.y pt)]
; => [1 1]
;; wie man sieht ist das Objekt kein Wert!

;; Syntaktischer Zucker bei Folgen von Java-Aufrufen
;; Threading macro  thread-first ->
(defn dec2hex [x]
  (.toUpperCase (Integer/toString (Integer/parseInt x) 16)))

(dec2hex "12")
; => "C"

;; dec2hex liest sich so schöner, wie eine Pipeline
;; nehme x, stecke es in parseInt, stecke das Ergebnis in toString etc
(defn dec2hex [x]
  (-> x
      Integer/parseInt
      (Integer/toString 16)
      .toUpperCase))

(dec2hex "12")
; => "C"

(macroexpand-1 '(-> x
    Integer/parseInt
    (Integer/toString 16)
    .toUpperCase))

(doc ->)
;
; clojure.core/->
; ([x & forms])
; Macro
; Threads the expr through the forms. Inserts x as the
; second item in the first form, making a list of it if it is not a
; list already. If there are more forms, inserts the first form as the
; second item in second form, etc.

; Es gibt auch thread-last -->

(doc ->>)
;
; clojure.core/->>
; ([x & forms])
; Macro
; Threads the expr through the forms. Inserts x as the
; last item in the first form, making a list of it if it is not a
; list already. If there are more forms, inserts the first form as the
; last item in second form, etc.

(comment
  In objekt-orientierten Sprachen, auch in Java, hat man gerne
  das Verketten von Methodenaufrufen. Man nennt das auch "Fluent interface"

  Ein einfaches Beispiel in Java:
  System.getProperties().get("os.name")
  )

;; Das Beispiel wäre in Clojure
(. (. System (getProperties)) (get "os.name"))
; => "Mac OS X"

; leichter zu lesen:
; mit ..
(.. System (getProperties) (get "os.name"))
; => "Mac OS X"

;; Das Beispiel ist übertrieben umständlich, es ginge auch
(System/getProperty "os.name")
; => "Mac OS X"


;; Eine weitere Vereinfachung bei der Verwendung von Java
;; doto
(import 'java.util.ArrayList)

;; Mehrere Methoden desselben Objekts ausführen
;; erst wird ein Objekt der Klasse ArrayList erzeugt, dann wird auf dieses mehrfach add angewandt
(doto (ArrayList.)
  (.add 1)
  (.add 2)
  (.add 3))
; => [1 2 3]

;; extrem praktisch bei der Konstruktion von Oberflächen
;; Beispiel aus iSQL in Clojure
(import 'javax.swing.JButton)
(import 'java.awt.event.KeyEvent)
(import 'java.awt.Dimension)

(def button-conn (doto (JButton. "Connect to Database")
                      (.setMnemonic KeyEvent/VK_D)
                      (.setPreferredSize (Dimension. 240 30))))

(.getLabel button-conn)
; => "Connect to Database"

(.getPreferredSize button-conn)
; #object[java.awt.Dimension 0x40f85fea "java.awt.Dimension[width=240,height=30]"]

(doc doto)
;
; clojure.core/doto
; ([x & forms])
; Macro
; Evaluates x then calls all of the methods and functions with the
; value of x supplied at the front of the given arguments.  The forms
; are evaluated in order.  Returns x.


;; Nützliche Funktionen

;; Von welcher Klasse ist ein Objekt/ein Wert?
(class 12)
; => java.lang.Long

(class "hallo")
; => java.lang.String

;; Ist ein Objekt von einem bestimmten Typ
(instance? String "hallo")
; => true

(instance? String 123)
; => false

;; Welche Typen hat ein Klasse noch?
(supers (class "hallo"))
; => #{java.io.Serializable java.lang.Comparable java.lang.CharSequence java.lang.Object}

(supers (class 123))
; => #{java.io.Serializable java.lang.Comparable java.lang.Number java.lang.Object}

;; Exceptions

;; Beispiel wirft Java-Exception
(Integer/parseInt "abc")
; => NumberFormatException For input string: "abc" 

;; Beispiel mit try/catch/finally
(defn s2int [s]
  (try
    (Integer/parseInt s)
    (catch NumberFormatException e
      (println "Error parsing" s))
    (finally
      (println "Tried to parse" s))))

(s2int "abc")  
; Error parsing abc
; Tried to parse abc
; => nil 

(s2int "1234")
; Tried to parse 1234
; => 1234

;; with-open erleichtert Exception-Handling mit Ressourcen
(with-open [r (java.io.FileReader. "books.clj")]
  (loop [c (.read r)]
    (if (not= c -1)  
      (do
        (print (char c))
        (recur (.read r))))))

;; Impliziter try/catch/finally-Block
;; Der geöffnete FileReader wird automatisch geschlossen
;; setzt voraus, dass die verwendete Ressource eine close-Methode hat

;; Guter Stil ist das Verwenden vorhandener Java-Exceptions

(pres "
# Clojure und Reflection in Java

- Clojure ist dynamisch, d.h. in vielen Fällen ist der Typ eines Werts erst zur Laufzeit 
  bekannt
- Der Clojure-Compiler macht Typinferenz, d.h. ermittelt 'automatisch' den Typ eines Objekts
- Dies geht nicht immer, etwa wenn Java-Klassen gleichnamige Methoden haben
- In diesem Fall wird Code erzeugt, der zur Laufzeit Reflection einsetzt     
      - Folge: Einbuße an Geschwindigkeit
      - Abhilfe: Typ-Hinweise
")

;; Funktion ohne Typhinweise
(defn capitalize [s]
  (-> s
    (.charAt 0)
    Character/toUpperCase
    (str (.substring s 1))))
; => #'clj.java-iop/capitalize

(capitalize "clojure")

(time (doseq [s (repeat 10000 "hallo")]
        (capitalize s)))
; "Elapsed time: ca 150 msecs"

;; Ist langsam -- Clojure macht Reflection
;; charAt ist Methode von String und von StringBuffer und weiteren Klassen

;; wir lassen den Clojure Compiler ausgeben, ob er Code erzeugt, der
;; Reflection verwendet

(set! *warn-on-reflection* true) ;; muss man explizit in der REPL ausführen??

*warn-on-reflection*

(defn capitalize' [s]
  (-> s
    (.charAt 0)
    Character/toUpperCase
    (str (.substring s 1))))
; Reflection warning  - call to charAt can't be resolved.
; Reflection warning - call to toUpperCase can't be resolved.
; Reflection warning - call to substring can't be resolved.
; => #'clj.java-iop/capitalize

;; Man kann dem Compiler mitteilen, welche Methode gemeint ist
(defn capitalize [^String s]
  (-> s
    (.charAt 0)
    Character/toUpperCase
    (str (.substring s 1))))
; => #'clj.java-iop/capitalize
; die Warnungen sind weg

(time (doseq [s (repeat 10000 "hallo")]
        (capitalize s)))
; "Elapsed time: ca 8 msecs"
;; deutlich schneller

;; Man kann auch den Typ des Rückgabewerts angeben
(defn capitalize ^String [^String s]
  (-> s
    (.charAt 0)
    Character/toUpperCase
    (str (.substring s 1))))
; => #'clj.java-iop/capitalize
;; ist aber in diesem Fall unnötig, weil der Typ ohnehin durch
;; Clojures Typinferenz ermittelt wird

(capitalize "clojure")

(comment
  Vorgehen:
  normalerweise schreibt man Code ohne Typhinweise
  erst wenn das Programm zu langsam ist, überprüft man, ob Reflection dabei eine Rolle spielt)

(pres "
# Mit Clojure Java erzeugen

- `reify` produziert eine Instanz einer anonymen Klasse, Methoden sind Java-Methoden
- `proxy` produziert eine Instanz einer anonymen Klasse, Methoden sind Clojure-Funktionen
- `gen-class` volle Implementierung einer Java-Klasse
- `deftype` Implementierung von Interfaces
- `defrecord` Implementierung einer werteorientierten Java-Klasse (Entität)

![Java-Klassen erzeugen](resources/java-gen.jpg)
")