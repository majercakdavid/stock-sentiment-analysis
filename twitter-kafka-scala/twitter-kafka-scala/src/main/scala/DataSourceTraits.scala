trait DataSourceTraits {
    def start(): Unit
    def stop(): Unit
    def take(): String
}