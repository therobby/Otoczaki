import kotlin.random.Random

class NormalDot(
    radius : Double,
    startX : Double = 0.0,
    startY : Double = 0.0
) : Dot(radius, startX, startY) {

    //override fun isDead() : Boolean = alive > Random.nextInt(32767)    // Short.MAX_VALUE = 32767
    override fun isDead() : Boolean {
        val rng = Random.nextInt(100000)
        return alive > rng
    }
}