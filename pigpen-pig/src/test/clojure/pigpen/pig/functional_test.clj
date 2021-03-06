;;
;;
;;  Copyright 2013-2015 Netflix, Inc.
;;
;;     Licensed under the Apache License, Version 2.0 (the "License");
;;     you may not use this file except in compliance with the License.
;;     You may obtain a copy of the License at
;;
;;         http://www.apache.org/licenses/LICENSE-2.0
;;
;;     Unless required by applicable law or agreed to in writing, software
;;     distributed under the License is distributed on an "AS IS" BASIS,
;;     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;     See the License for the specific language governing permissions and
;;     limitations under the License.
;;
;;

(ns pigpen.pig.functional-test
  (:require [clojure.test :refer [run-tests]]
            [pigpen.functional-test :as t :refer [TestHarness]]
            [pigpen.functional-suite :refer [def-functional-tests]]
            [pigpen.core :as pig]
            [pigpen.extensions.io :as io]
            [pigpen.pig :as ppig])
  (:import [org.apache.pig.pigunit PigTest]))

(def prefix "build/functional/pig/")

(.mkdirs (java.io.File. prefix))

(defn run-script [harness command]
  (let [script-file (t/file harness)]
    (->> command
      (ppig/write-script script-file
                         {:extract-references? false
                          :add-pigpen-jar? false}))
    (doto (PigTest. script-file)
      (.unoverride "STORE")
      (.runScript))))

(defn run-script->output [harness command]
  (let [output-file (t/file harness)]
    (->> command
      (pig/store-clj output-file)
      (run-script harness))
    (->> output-file
      (t/read harness)
      (map read-string))))

(def-functional-tests "pig"
  (reify TestHarness
    (data [this data]
      (let [input-file (t/file this)]
        (spit input-file
              (->> data
                (map prn-str)
                (clojure.string/join)))
        (pig/load-clj input-file)))
    (dump [this command]
      (if (-> command :type #{:store :store-many})
        (run-script this command)
        (run-script->output this command)))
    (file [this]
      (str prefix (gensym)))
    (read [this file]
      (apply concat
        (for [f (io/list-files file)
              :when (not (.endsWith f ".crc"))
              :when (not (.endsWith f "_SUCCESS"))]
          (when-let [contents (not-empty (slurp f))]
            (clojure.string/split-lines contents)))))
    (write [this lines]
      (let [file (t/file this)]
        (spit file (clojure.string/join "\n" lines))
        file)))

  ; Pending https://issues.apache.org/jira/browse/PIG-4298
  #{pigpen.functional.map-test/test-sort-desc
    pigpen.functional.map-test/test-sort-by-desc})
