package serializers

trait SerializerComponent{
  val serializer:SerializerImpl

  trait SerializerImpl{
    def deserialize[A](bytes:Array[Byte]):A
    def serialize(obj:Any):Array[Byte]
  }
}
