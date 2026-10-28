package com.juanmediahubph.pocketgizmo.domain

/**
 * Morse code timing for an SOS signal: ... --- ...
 *
 * Unit = 300 ms. A dot is on for 1 unit, a dash for 3. Elements inside a
 * letter are separated by 1 unit off, letters by 3 units off and complete
 * repetitions by 7 units off. The model stores the gaps explicitly so no
 * step ever adds two gaps together.
 */
data class SosStep(val on: Boolean, val units: Int)

object SosPattern {

    const val UNIT_MILLIS: Long = 300L

    val steps: List<SosStep> = buildList {
        fun letter(vararg elements: Boolean) {
            elements.forEachIndexed { index, isDot ->
                add(SosStep(on = true, units = if (isDot) 1 else 3))
                if (index != elements.lastIndex) {
                    add(SosStep(on = false, units = 1))
                }
            }
        }

        letter(true, true, true)      // S
        add(SosStep(on = false, units = 3))
        letter(false, false, false)   // O
        add(SosStep(on = false, units = 3))
        letter(true, true, true)      // S
        add(SosStep(on = false, units = 7))
    }

    val totalUnits: Int = steps.sumOf { it.units }

    val totalDurationMillis: Long = totalUnits * UNIT_MILLIS
}
