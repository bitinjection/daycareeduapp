(ns daycare.activities.counting
  "Pure logic for a \"count the objects\" round.

   No React, no storage, no device APIs. Everything here is a function of
   (difficulty, seed), which buys us two things: the test suite runs in
   milliseconds under Node, and any failing property test can be replayed
   exactly from the seed it reports.")

;; ---------------------------------------------------------------------------
;; Seeded randomness
;;
;; cljs has no seedable rand, and we need reproducibility far more than we need
;; cryptographic quality. Mulberry32 is a few lines and good enough for deciding
;; how many ducks to draw.

(defn- step
  "Advance the generator. Returns [fraction next-state], fraction in [0, 1)."
  [state]
  (let [state (bit-or (+ state 0x6D2B79F5) 0)
        z     state
        z     (js/Math.imul (bit-xor z (unsigned-bit-shift-right z 15))
                            (bit-or z 1))
        z     (bit-xor z (+ z (js/Math.imul (bit-xor z (unsigned-bit-shift-right z 7))
                                            (bit-or z 61))))
        z     (unsigned-bit-shift-right (bit-xor z (unsigned-bit-shift-right z 14)) 0)]
    [(/ z 4294967296) state]))

(defn- shuffle-with
  "Fisher-Yates driven by `state`. Returns [shuffled next-state]."
  [coll state]
  (loop [v (vec coll)
         i (dec (count v))
         s state]
    (if (pos? i)
      (let [[fraction s'] (step s)
            j             (int (* fraction (inc i)))]
        (recur (assoc v i (nth v j) j (nth v i)) (dec i) s'))
      [v s])))

;; ---------------------------------------------------------------------------
;; Difficulty
;;
;; Level 1 stays inside the range a two-year-old can subitize (recognise without
;; counting). Levels climb toward the teens. Ceilings are nested on purpose, so
;; raising difficulty can never hand a child an easier round from the same seed.

(def ^:private difficulty->ceiling
  {1 3, 2 5, 3 10, 4 15, 5 20})

(def ^:private difficulty->choice-count
  {1 2, 2 3, 3 3, 4 4, 5 4})

(defn generate
  "Build one counting round: `:target` objects to show, and `:choices` numerals
   to offer, exactly one of which is correct.

   Distractors are drawn from near the target — offering 7 and 8 teaches
   counting, offering 7 and 19 teaches guessing. The final order is shuffled so
   the correct button does not sit in a predictable position."
  [difficulty seed]
  (let [ceiling     (difficulty->ceiling difficulty)
        n-choices   (difficulty->choice-count difficulty)
        [fraction s] (step (bit-or seed 0))
        target      (inc (int (* fraction ceiling)))
        near-misses (->> (range 1 (inc ceiling))
                         (remove #(= target %))
                         (sort-by (juxt #(js/Math.abs (- % target)) identity))
                         (take (+ (dec n-choices) 2)))
        [pool s]    (shuffle-with near-misses s)
        [choices _] (shuffle-with (conj (vec (take (dec n-choices) pool)) target) s)]
    {:activity/kind :counting
     :difficulty    difficulty
     :seed          seed
     :target        target
     :choices       (vec choices)}))

(defn correct?
  "Did the child pick the right numeral?"
  [round answer]
  (= (:target round) answer))
