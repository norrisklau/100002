package test

/**
 * Testing decorator patterns and seeing how they work in scala.
 */
import scala.collection.mutable.ArrayBuffer

class Coffee {
	def ingredients() : String = {
	  "Coffee"
	}
}

trait WithSugar extends Coffee {
  override def ingredients() : String = {
    super.ingredients() + " with sugar"
  }
}

trait WithPeanuts extends Coffee {
  override def ingredients() : String = {
    super.ingredients() + " with peanuts"
  }
}

object CoffeeTest {
  def main(args : Array[String]) : Unit = {
    val coffee = new Coffee with WithSugar with WithPeanuts
    println(coffee.ingredients())
  }
}