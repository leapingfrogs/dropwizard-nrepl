(ns dropwizard-nrepl.core
  (:gen-class
    :name com.leapingfrogs.dropwizard.NReplBundle
    :implements (com.yammer.dropwizard.Bundle)
    :main false
    :init init
    :state state
    :constructors {[int] []}
    :methods [[put [String Object] Object]
              [get [String] Object]])
  (:use [clojure.tools.nrepl.server :only (start-server stop-server)]))

(defn -init [port]
  [[] (ref {:port port})])

(defn -put [this key value]
  (dosync
    (alter (.state this) assoc (keyword key) value)))

(defn -get [this key]
    ((keyword key) @(.state this)))

(defn -initialize [this bootstrap]
  (-put this "bootstrap" bootstrap)
  (dosync
    (alter (.state this) assoc :bootstrap bootstrap)))

(defn -run [this environment]
  (-put this "environment" environment)
  (defonce state (.state this))
  (defonce nrepl-server (start-server :port (:port @(.state this)))))