(ns htsclj.server
  (:require log
            http.header
            http.req)
  (:import java.net.ServerSocket
           java.io.PrintWriter
           java.nio.charset.Charset)
  (:gen-class))

(def logs (log/init "htsclj.server" :debug))

(defn file
  ([] (slurp "src/htsclj/resources/index.html"))
  ([path] (try (slurp (str "src/htsclj/resources" path))
            (catch Exception e ((logs :error) (.getMessage e))))))

(defn server [p]
  "open a connection on port p and returns a new connection being made to it"
  (ServerSocket. p))

(defn getReq [socket]
  "prints the request made to a socket"
  (let [inputStream (.getInputStream socket)
        length (.available inputStream)]
        (apply str (map char (vec (.readNBytes inputStream length))))))

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

(defn -main []
  (let [ssocket (server 8080)]
    (while true
      (let [boundSock (.accept ssocket)]
        (if (.isConnected boundSock)
          (do
            ((logs :trace) (str "connected to: "  (.toString (.getInetAddress boundSock))))
            (.setSendBufferSize boundSock 1024)
            (let [outputStream (.getOutputStream boundSock)
                  request (getReq boundSock)]
              (let [request-info (http.req/req request)]
                ((logs :info) (str (request-info :method) " " (request-info :path)))
                (write-bytes outputStream (response request-info))
                (.close boundSock)
              ))))))))
