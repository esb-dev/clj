(ns clj.presentation
  (:import clj.WebViewer
           (javafx.application Platform Application))
  (:require [markdown.core :refer :all]))

;; Functions using the WebViewer
(defn launch
  []
  (future (Application/launch clj.WebViewer (into-array String []))))

(defmacro show
  [html]
  `(let [f# (fn [] (clj.WebViewer/loadHtml ~html))]
     (Platform/runLater f#)))

(defmacro loadUrl
  [url]
  `(let [f# (fn [] (clj.WebViewer/loadUrl ~url))]
     (Platform/runLater f#)))

(defn close
  []
  (Platform/exit)
  (System/exit 1)
  )

;; Markdown to Html with pegdown
(def html-head
  "<!DOCTYPE html>
   <html>
   <head>
   <base href='file:///Users/br/ESBDateien/Projekte/clj/' />
   <link rel='stylesheet' href='resources/clj-presentation.css' type='text/css'>
   </head>
   <body>")

(def html-tail
  "</body>
   </html>")

(defn markdown
  "Use markdown.clj to transform markdown to html."
  [source]
  (str html-head (md-to-html-string source) html-tail))

;; Function to be used in presentations

; we need an atom to allow appending to a "slide"

(def last-source (atom ""))

(defn pres
  "(pres source) Present source in WebViewer\n
   (pres :add source) Append source to current slide"
  ([source] (pres :replace source))
  ([mode source]
    ; modify atom last-source
   (case mode
     :replace (reset! last-source source)
     :add (swap! last-source #(str % \newline source)))
    ; transform to html and show
   (-> @last-source
       markdown
       show)))

(defn init
  ([titel] (init "# Vorlesung Clojure\n\n" titel))
  ([head title]
   (memoize  ; do the init just once in a REPL
     (do (launch)
         (Thread/sleep 1000)
         (pres (str head title "\n\n"))))))


