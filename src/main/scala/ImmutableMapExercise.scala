object ImmutableMapExercise extends App {
  val m1: Map[String, String] = Map.empty[String, String].updated("key", "value")
  val m2: Map[String, String] = m1.updated("key2", "value2")
  val m3: Map[String, String] = m2.updated("key2", "another2")
  val m4: Map[String, String] = m3.removed("key")
  val valueFromM3 = m3.get("key")
  val valueFromM4 = m4.get("key")
}
