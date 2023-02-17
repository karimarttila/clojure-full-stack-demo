(ns backend.domaindb.users
  (:require [clojure.tools.logging :as log]
            [buddy.sign.jwt :as buddy-jwt]
            [clojure.data.codec.base64 :as base64]
            [clj-time.core :as clj-time]))


;; Demonstration: For development purposes.
(def my-atom (atom {}))
#_(comment
    @my-atom
    (keys @my-atom)
    (:db @my-atom)
    (keys @(:db @my-atom))
    (:users @(:db @my-atom)))

(def my-hex-secret
  "Creates dynamically a hex secret when the server boots."
  ((fn []
     (let [my-chars (->> (range (int \a) (inc (int \z))) (map char))
           my-ints (->> (range (int \0) (inc (int \9))) (map char))
           my-set (lazy-cat my-chars my-ints)
           hexify (fn [s]
                    (apply str
                           (map #(format "%02x" (int %)) s)))]
       (hexify (repeatedly 24 #(rand-nth my-set)))))))


(defn check-password [env username password]
  (log/debug (str "ENTER check-password"))
  ;; For demonstration
  ;(reset! my-atom env)
  (log/debug (str username))
  (let [users (:users @(:db env))]
    (= 1 (count (filter (fn [user]
                          (and (= (:username user) username) (= (:password user) password)))
                        users)))))

(defn generate-token [env username]
  (log/debug (str "ENTER generate-token, username: " username))
  (let [my-secret my-hex-secret
        exp-time (clj-time/plus (clj-time/now) (clj-time/seconds (get-in env [:options :jwt :exp])))
        my-claim {:username username :exp exp-time}
        json-web-token (buddy-jwt/sign my-claim my-secret)]
    json-web-token))


(defn add-session [env session]
  (log/debug (str "ENTER add-session"))
  (let [db (:db env)]
    ;; First remove the old session, if exists.
    (let [old-session (first (filter (fn [s]
                                       (and (= (:username s) (:username session))
                                            (= (:password s) (:password session))))
                                     (:sessions @db)))]
      (when old-session
        (swap! db update-in [:sessions] disj old-session)))
    ;; Then add a new session.
    (swap! db update-in [:sessions] conj session)))


(defn validate-user [env username password]
  (let [token (if (check-password env username password)
                (generate-token env username)
                nil)]
    (when token
      (let [session {:username username :password password :token token}]
        (add-session env session)))
    token))


(defn validate-token
  [env token]
  (log/debug (str "ENTER validate-token, token: " token))
  (let [;_ (reset! my-atom token)
        db (:db env)
        session (first (filter (fn [s] (= (:token s) token))
                               (:sessions @db)))]
    ;; Part #1 of validation.
    (if (nil? session)
      (do
        (log/warn (str "Token not found in the session database - unknown token: " token))
        nil)
      ;; Part #2 of validation.
      (let [decoded-token (try
                            (buddy-jwt/unsign token my-hex-secret)
                            (catch Exception e
                              (if (.contains (.getMessage e) "Token is expired")
                                (do
                                  (log/debug (str "Token is expired, removing it from my sessions and returning nil: " token))
              ; TODO : remove token
                                  nil)
            ; Some other issue.
                                (do
                                  (log/error (str "Some unknown exception when handling expired token, exception: " (.getMessage e)) ", token: " token)
                                  nil))))]
        (if decoded-token
          (if (= (:username session) (:username decoded-token))
            decoded-token
            (do
              (log/error (str "Token username does not match the session username"))
              nil))
          nil)))))


(comment
  @my-atom
  (buddy-jwt/unsign @my-atom my-hex-secret))


(comment
  (keys (user/env))
  (:db (user/env))
  (keys @(:db (user/env)))
  (:users @(:db (user/env)))
  (def users (:users @(:db (user/env))))
  users
  (filter (fn [user]
            (and (= (:username user) "jartsa") (= (:password user) "joo")))
          users)
  (= 1 (count (filter (fn [user]
                        (and (= (:username user) "jartsa") (= (:password user) "joo")))
                      users)))
  ;; Now move expression to the function above (and change hardcoded valules to function arguments).
  (check-password (user/env) "jartsa" "joo")
  (check-password (user/env) "jartsa" "WRONG")
  @(:db (user/env))

  (:sessions @(:db (user/env)))
  (def sessions (:sessions @(:db (user/env))))
  sessions
  (def session {:username "jartsa",
                :password "joo",
                :token
                "JEEE"})
  (filter (fn [s]
            (and (= (:username s) (:username session)) (= (:password s) (:password session))))
          (:sessions @(:db (user/env))))

  (def mydb (atom @(:db (user/env))))
  mydb
  (def old-session (first (filter (fn [s]
                                    (and (= (:username s) (:username session)) (= (:password s) (:password session))))
                                  (:sessions @mydb))))

  (def session {:username "jartsa",
                :password "joo",
                :token
                "JEEE"})

  (def mydb (atom {:sessions #{session},
                   :users [{:username "jartsa", :password "joo"}]}))



  @mydb
  (swap! mydb update-in [:sessions] disj session))