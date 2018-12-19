(defproject clj "0.2.0-SNAPSHOT"
  :description "Vorlesung Programmierung in Clojure"
  :url "http://homespages.mni.thm.de/~hg11260"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/core.logic "0.8.11"]
                 [org.clojure/tools.macro "0.1.5"]
                 [org.clojure/tools.trace "0.7.10"]
                 [seesaw "1.5.0" :exclusions [org.clojure/clojure]]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [markdown-clj "1.0.5"]
                 [dire "0.5.4"]
                 [quil "2.8.0"]]
  :aot [clj.webviewer]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})