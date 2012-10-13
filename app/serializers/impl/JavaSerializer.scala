package serializers.impl

import models._
import serializers.SerializerComponent

import java.io._

trait JavaSerializer extends SerializerComponent{

  val serializer = new JavaSerializerImpl

  class JavaSerializerImpl extends SerializerImpl{
    def deserialize[A](bytes:Array[Byte])={
      val bis = new ByteArrayInputStream(bytes)
      val in = new ObjectInputStream(bis)
      in.readObject.asInstanceOf[A]
    }

    def serialize(obj:Any)={
      val bos = new ByteArrayOutputStream
      val out = new ObjectOutputStream(bos)
      out.writeObject(obj)
      bos.toByteArray
    }
  }
}
