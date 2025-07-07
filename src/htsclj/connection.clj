(ns htsclj.connection
  (:require [clojure.java.io :refer [reader]])
  (:gen-class))

(defn file
  ([] (slurp "src/htsclj/resources/index.html"))
  ([path] (try (slurp (str "src/htsclj/resources" path))
            (catch Exception e ((logs :error) (.getMessage e))))))

(defn getReq  "returns the string reqresentation of the request made to a socket"
  [inputStream] (try
    (let [length (.available inputStream)]
      (if (= 0 length) nil
        (apply str (map char (vec (.readNBytes inputStream length))))))
    (catch NullPointerException e (do ((logs :error) (.getMessage e)) nil))))

(defn write-bytes
  [ostr response] (let [printWriter (PrintWriter. ostr true (Charset/forName "UTF-8"))]
                      ((logs :debug) response)
                      (.print printWriter response)
                      (.flush printWriter)
                    ))

(defn response
  ([] "HTTP/1.1 404 NOT FOUND\r\n\r\n")
  ([request-info] (let [content (file (request-info :path))]
                            (if (= (request-info :path) "/")
                              (response {:version (request-info :version)
                                         :method (request-info :method)
                                         :path "/index.html"})
                            (if (nil? content) (response :notfound)
              (str (request-info :version)
                   " 200 OK\r\nContent-Length:"
                   (count content)
                   "\r\n\r\n"
                   content))))))

(defn connect "Starts the connection with a client. Argument must implement Socket. Acts in another Thread"
  [socket]
  (let [in (reader (InputStreamReader/new (.getInputStream socket)))
        out (PrintWriter/new (.getOutputStream socket))]
    (fn [] (while (not= nil (first (line-seq in)))))))
