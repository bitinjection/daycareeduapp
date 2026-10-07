(ns daycare.dev.format
  "cljfmt driven through shadow-cljs's own JVM, so this project needs nothing
   installed beyond a JDK and Node — no Clojure CLI, no native binaries.

     npm run fmt:check   ; fails if anything is misformatted (used by CI)
     npm run fmt:fix     ; rewrites the offenders in place"
  (:require
   [cljfmt.core :as fmt]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def ^:private source-paths ["src" "dev"])

(def ^:private extensions #{"clj" "cljs" "cljc" "edn"})

(defn- source-file? [file]
  (and (.isFile file)
       (contains? extensions (last (str/split (.getName file) #"\.")))))

(defn- source-files []
  (->> source-paths
       (map io/file)
       (filter #(.exists %))
       (mapcat file-seq)
       (filter source-file?)
       (sort-by #(.getPath %))))

;; A UTF-8 byte-order mark is easy to introduce on Windows — PowerShell's
;; `Set-Content -Encoding utf8` and several editors add one — and the
;; ClojureScript compiler reads it as a stray symbol at 1:1, producing a
;; baffling "Use of undeclared Var cljs.user/?" warning. cljfmt does not strip
;; it, so treat it as a formatting error in its own right.
(def ^:private bom (char 0xFEFF))

(defn- strip-bom [s]
  (if (and (seq s) (= bom (first s)))
    (subs s 1)
    s))

(defn- misformatted
  "Files whose contents differ from their formatted form, as
   [file original formatted]."
  []
  (keep (fn [file]
          (let [original  (slurp file)
                formatted (fmt/reformat-string (strip-bom original))]
            (when (not= original formatted)
              [file original formatted])))
        (source-files)))

(defn check
  "Exit non-zero if any source file is misformatted."
  [& _args]
  (let [offenders (misformatted)]
    (if (seq offenders)
      (do
        (println "Misformatted files:")
        (doseq [[file] offenders]
          (println " " (.getPath file)))
        (println)
        (println "Run `npm run fmt:fix` to correct them.")
        ;; Thrown rather than System/exit: exit would take down a running
        ;; shadow-cljs server along with the task.
        (throw (ex-info (str (count offenders) " file(s) need formatting")
                        {:files (mapv #(.getPath (first %)) offenders)})))
      (println "All" (count (source-files)) "source files are formatted."))))

(defn fix
  "Rewrite every misformatted source file in place."
  [& _args]
  (let [offenders (misformatted)]
    (doseq [[file _ formatted] offenders]
      (spit file formatted)
      (println "formatted" (.getPath file)))
    (println (count offenders) "file(s) rewritten.")))
