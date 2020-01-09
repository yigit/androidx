/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.ui.text.font

import androidx.compose.Immutable
import androidx.ui.util.lerp

/**
 * The thickness of the glyphs, in a range of [1, 1000].
 *
 * @see Font
 * @see FontFamily
 */
@Immutable
/* inline */ data class FontWeight private constructor(
    /**
     * Can be in the range of [1,1000]
     */
    internal val weight: Int
) : Comparable<FontWeight> {

    companion object {
        /** Thin, the minimum thickness */
        val W100 = FontWeight(100)
        /** Extra-light */
        val W200 = FontWeight(200)
        /** Light */
        val W300 = FontWeight(300)
        /** Normal / regular / plain */
        val W400 = FontWeight(400)
        /** Medium */
        val W500 = FontWeight(500)
        /** Semi-bold */
        val W600 = FontWeight(600)
        /** Bold */
        val W700 = FontWeight(700)
        /** Extra-bold */
        val W800 = FontWeight(800)
        /** Black, maximum thickness */
        val W900 = FontWeight(900)
        /** The default font weight. */
        val Normal = W400
        /** A commonly used font weight that is heavier than normal. */
        val Bold = W700

        /** A list of all the font weights. */
        internal val values: List<FontWeight> = listOf(
            W100,
            W200,
            W300,
            W400,
            W500,
            W600,
            W700,
            W800,
            W900
        )
    }

    internal val index: Int get() = weight / 100 - 1

    override fun compareTo(other: FontWeight): Int {
        return weight.compareTo(other.weight)
    }

    override fun toString(): String {
        return when (index) {
            0 -> "FontWeight.W100"
            1 -> "FontWeight.W200"
            2 -> "FontWeight.W300"
            3 -> "FontWeight.W400"
            4 -> "FontWeight.W500"
            5 -> "FontWeight.W600"
            6 -> "FontWeight.W700"
            7 -> "FontWeight.W800"
            8 -> "FontWeight.W900"
            else -> "FontWeight Unknown"
        }
    }
}

/**
 * Linearly interpolate between two font weights
 *
 * Rather than using fractional weights, the interpolation rounds to the
 * nearest weight.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid (and can
 * easily be generated by curves).
 *
 * Values for [fraction] are usually obtained from an [Animation<Float>], such as
 * an `AnimationController`.
 */
fun lerp(start: FontWeight, stop: FontWeight, fraction: Float): FontWeight {
    val index = lerp(start.index, stop.index, fraction)
        .coerceIn(0, FontWeight.values.size - 1)

    return FontWeight.values[index]
}