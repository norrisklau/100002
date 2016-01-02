package test

import tbs.entity._
import tbs.effect._
import tbs.rendering._
import tbs.map._
import tbs.game.scenario._
import java.io._
import java.lang._


object EntityTest {
  private val outputFile = "entity.ser"
  
	def run() : Boolean = {
	  var success = true
	  // Write to file
	  val hp = new LoadScenarioMessage(new ScenarioData)
	  val fileOut = new FileOutputStream(outputFile)
	  val objOut = new ObjectOutputStream(fileOut)
	  objOut.writeObject(hp)
	  objOut.close()
	  
	  // Read from file
	  val fileIn = new FileInputStream(outputFile)
	  val objIn = new ObjectInputStream(fileIn)
	  val cl = objIn.readObject()
	  objIn.close()
	  
	  println(hp.toString())
	  cl match {
	    case _ => println(cl.toString())
	  }
	  success 
	}
  
  def testSerializability (obj : Object) {
	  val fileOut = new FileOutputStream(outputFile)
	  val objOut = new ObjectOutputStream(fileOut)
	  objOut.writeObject(obj)
	  objOut.close()
	  
	  // Read from file
	  val fileIn = new FileInputStream(outputFile)
	  val objIn = new ObjectInputStream(fileIn)
	  val cl = objIn.readObject()
	  objIn.close()
	  
	  println(obj.toString())
	  cl match {
	    case _ => println(cl.toString())
	  }
  }
}