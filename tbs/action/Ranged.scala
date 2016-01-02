package tbs.action

import scala.math._

/**
 * Adds some helper fields and methods to ranged actions. 
 * Melee actions (which technically do have 1 range) should not inherit this trait 
 * they have variable range.
 * 
 * Invariant : Minimum range will always be <= maximum range, and both should be never
 * be negative.
 */
trait Ranged {
	protected var _minimumRange : Int = 1
	protected var _maximumRange : Int = 1
	
	def minimumRange_=(range : Int) = {
	  _minimumRange = range
	  if (_minimumRange < 0) _minimumRange = 0
	}
	
	def maximumRange_=(range : Int) = {
	  _maximumRange = range
	  if (_maximumRange < 0) _maximumRange = 0
	}
	
	/**
	 * Sets both minimum and maximum range to m
	 */
	def setRange(min : Int, max : Int) = {
	  minimumRange_=(min)
	  maximumRange_=(max)
	}
	
	
	def maximumRange: Int = {
	  _maximumRange
	}
	
	def minimumRange : Int = {
	  _minimumRange
	}
	
	/**
	 * Returns a tuple with first field being minimum range and second
	 * being maximum range.
	 */
	def getRange() : (Int, Int) = {
	  (_minimumRange, _maximumRange)
	}
}