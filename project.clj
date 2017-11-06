(defproject clj "0.2.0-SNAPSHOT"
  :description "Vorlesung Programmierung in Clojure"
  :url "http://homespages.mni.thm.de/~hg11260"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [org.clojure/spec.alpha "0.1.134"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/core.logic "0.8.11"]
                 [org.clojure/tools.macro "0.1.5"]
                 [org.clojure/tools.trace "0.7.9"]
                 [seesaw "1.4.5" :exclusions [org.clojure/clojure]]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [markdown-clj "1.0.1"]
                 [dire "0.5.4"]
                 [quil "2.6.0"]]
  :aot [clj.webviewer]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})