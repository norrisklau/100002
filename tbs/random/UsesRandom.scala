package tbs.random
import scala.util.Random

trait UsesRandom extends Serializable {
	private var random : Random = new Random()
	
	def getRandom() : Random = {
	  random
	}
	
	def setRandom(rng : Random) = {
	  random = rng
	}
}  