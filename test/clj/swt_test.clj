; Programmierung in Clojure Vorlesung 13  
; Softwaretechnik mit Clojure - Beispiel von Unittests
; (c) 2014 - 2016 by Burkhardt Renz, THM


(ns clj.swt-test
  (:require [clojure.test :refer :all])
  (:require [clj.l14-swt :refer :all]))

;; Korrekte Ergebnisse
(deftest okay
  (testing "input fulfills preconditions"
    (is (= 0 (zahl-pkw 2 4)))
    (is (= 1 (zahl-pkw 1 4)))
    (is (= 1 (zahl-pkw 2 6)))
    (is (= 4 (zahl-pkw 10 28)))))

;; Nicht erlaubte Parameter
(deftest not-okay
  (testing "input violates preconditions"
     (is (thrown? ArithmeticException (zahl-pkw 1 3)))
     (is (thrown? ArithmeticException (zahl-pkw -1 4)))
     (is (thrown? ArithmeticException (zahl-pkw 3 9)))
     (is (thrown? ArithmeticException (zahl-pkw 5 2)))
     (is (thrown? ArithmeticException (zahl-pkw 2 10)))))

(run-tests)