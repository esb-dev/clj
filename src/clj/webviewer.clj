(ns clj.webviewer
  (:gen-class
   :name clj.WebViewer
   :extends javafx.application.Application
   :methods [#^{:static true} [loadUrl [String] void]
             #^{:static true} [loadHtml [String] void]])
  (:import (javafx.scene.web WebView)
           (javafx.scene Scene)
           (javafx.stage Stage)
           (javafx.event EventHandler)
           (javafx.scene.input KeyEvent KeyCode)))

(def web-engine (promise))

; Der WebViewer wird in den Präsentationen in der Regel durch den Aufruf
; von loadHtml verwendet. Ein solcher Aufruf führt nicht zu einem Eintrag in der
; History der Webengine, also kann man auch nicht dahin zurück.

; Deshalb wird das zustandbehaftete Atom last-html verwendet.
; Beim Aufruf von loadHtml wird es gesetzt, beim Drücken auf Backspace
; wird der dortige Inhalt erneut geladen.
(def last-html (atom ""))
(def back (proxy [EventHandler] []
            (handle [^KeyEvent event]
              (if (and (= (.getCode event) KeyCode/B) (not (clojure.string/blank? @last-html))) 
                (clj.WebViewer/loadHtml @last-html)))))

(defn -start [this ^Stage stage]
  (let [wv (WebView.)
        sc (Scene. wv)]
  (deliver web-engine (.getEngine wv))
  (.setOnKeyPressed wv back)
  (.setTitle stage "MNI WebViewer")
  (.setScene stage sc)
  (.show stage)))

(defn -loadUrl [url]
  (let [we @web-engine]
    (.load we url)))

(defn -loadHtml [html]
  (let [we @web-engine]
    (reset! last-html html)
    (.loadContent we html)))

