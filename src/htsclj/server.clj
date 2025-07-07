(ns htsclj.server
  (:require log
            http.header
            http.req)
  (:import java.net.ServerSocket
           java.io.PrintWriter
           java.nio.charset.Charset)
  (:gen-class))

(def logs (log/init "htsclj.server" :debug))

(defn server "open a connection on port p and returns a new connection being made to it"
  [p] (ServerSocket. p))

(defn -main []
  (let [ssocket (server 8080)
        boundSock (.accept ssocket)]
      (while (not= nil (getReq inputStream))
        (do
          ((logs :trace) (str "connected to: "  (.toString (.getInetAddress boundSock))))
          (.setSendBufferSize boundSock 1024)
          (let [request (getReq inputStream)
                request-info (http.req/req request)]
            (if (= nil request-info) nil
              (do 
                ((logs :info) (str (request-info :method) " " (request-info :path)))
                (write-bytes outputStream (response request-info))
                (.close boundSock)
                (.close ssocket))))
        nil))
      ((logs :error) "socket not connected")))
