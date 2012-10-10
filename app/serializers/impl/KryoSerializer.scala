package serializers.impl

import models._
import serializers.SerializerComponent

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.serializers.DefaultSerializers._

trait KryoSerializer extends SerializerComponent{
  class KryoSerializerImpl extends SerializerImpl{
    private def with_kryo[T](func:Kryo=>T):T={
      val kryo = new Kryo()
      kryo.setRegistrationRequired(false)
      kryo.register(classOf[User])
      kryo.register(classOf[CheapestPrice])
      kryo.register(classOf[SearchRequest])
      kryo.register(classOf[Monitor])
      func(kryo)
    }

    def deserialize[A](bytes:Array[Byte])={
      with_kryo( kryo => {
        val input = new Input(new ByteArrayInputStream(bytes), 4096)
        kryo.readClassAndObject(input).asInstanceOf[A];
      })
    }

    def serialize(obj:Any):Array[Byte]={
      with_kryo(kryo =>{
        val out = new ByteArrayOutputStream()
        val output = new Output(out, 4096)
        kryo.writeClassAndObject(output, obj)
        output.flush()
        out.toByteArray
      })
    }
  }
}
