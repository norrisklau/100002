package tbs.action

/**
 * Describes Actions (and possibly effects) that have a chance of success or failure.
 * 
 * Hit and Miss actions/effects do *not* necessarily involve any RNG: For example, you could
 * have an attack that only hits on even turns.
 * 
 * That said, assume the vast majority of cases to involve a % chance to hit for any situation
 */
abstract trait HitAndMiss {
  /**
   * Roll to see if this Attack/Effect hit.
   * 
   * @return
   * True if the attack succeeded, false otherwise
   */
  def attemptHit() : Unit = {
    if (checkIfHit()) {
      resolveHit()
    } else {
      resolveMiss()
    }
  }
  
  def checkIfHit() : Boolean
  
  def resolveHit() : Unit
  
  def resolveMiss() : Unit
}