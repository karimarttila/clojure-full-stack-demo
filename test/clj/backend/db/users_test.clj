(ns backend.db.users-test
  (:require [clojure.test :refer [deftest use-fixtures is testing]]
            [clojure.tools.logging :as log]
            [backend.db.users :as b-users]
            [backend.test-config :as test-config]))

(defn init-fixture []
  ;; Do nothing.
  )


(defn domain-test-fixture
  [f]
  (log/debug "ENTER domain-test-fixture")
  (test-config/test-system-fixture-runner init-fixture f)
  (log/debug "EXIT domain-test-fixture"))


(use-fixtures :each domain-test-fixture)


;; Experimental testing.
;; (def my-atom (atom nil))


(deftest get-users-test
  (log/debug "ENTER get-users-test")
  (testing "Testing users"
    (let [env (:backend/env @test-config/test-system)]
      (b-users/clear-sessions env)
      (is (= (count (b-users/get-sessions env)) 0))
      (let [token-jartsa (b-users/validate-user env "jartsa" "joo")]
        (is (not (nil? token-jartsa)))
        (let [decoded-token (b-users/validate-token env token-jartsa)]
          (is (not (nil? token-jartsa)))
          (is (= "jartsa" (:username decoded-token)))
          (let [sessions (b-users/get-sessions env)
                session (first sessions)]
            (is (= 1 (count sessions)))
            (is (= "jartsa" (:username session)))
            (is (= "joo" (:password session)))
            (is (= token-jartsa (:token session)))))
        (let [_token-rane (b-users/validate-user env "rane" "jee")]
          (let [sessions (b-users/get-sessions env)]
            (is (= 2 (count sessions)))))
        ;; Create a new session for jartsa: the old one should be removed
        (let [token-jartsa2 (b-users/validate-user env "jartsa" "joo")]
          (is (not (nil? token-jartsa2)))
          (let [sessions (b-users/get-sessions env)]
            (is (= 2 (count sessions)))))))))

(deftest user-validation-fails-test
  (log/debug "ENTER user-validation-fails-test")
  (testing "User validation fails"
    (let [env (:backend/env @test-config/test-system)]
      (b-users/clear-sessions env)
      (is (= 0 (count (b-users/get-sessions env))))
      (let [token-jartsa (b-users/validate-user env "jartsa" "WRONG")]
        (is (nil? token-jartsa))
        (let [sessions (b-users/get-sessions env)]
          (is (= 0 (count sessions))))))))