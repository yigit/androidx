/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.room.compiler.processing.ksp

import androidx.room.compiler.processing.XDeclaredType
import androidx.room.compiler.processing.XEquality
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XTypeElement
import com.squareup.javapoet.TypeName
import org.jetbrains.kotlin.ksp.symbol.KSType
import org.jetbrains.kotlin.ksp.symbol.KSTypeReference
import kotlin.reflect.KClass

internal class KspType private constructor(
    protected val env: KspProcessingEnv,
    val ksTypeReference: KSTypeReference?,
    resolved: KSType?
) : XDeclaredType, XEquality {
    constructor(env: KspProcessingEnv, ksTypeReference: KSTypeReference) : this(
        env = env,
        ksTypeReference = ksTypeReference,
        resolved = null
    )
    constructor(env: KspProcessingEnv, resolved: KSType) : this(
        env = env,
        ksTypeReference = null,
        resolved = resolved
    )
    private val ksType by lazy {
        resolved ?: ksTypeReference?.resolve()
    }
    override val typeArguments: List<XType> by lazy {
        val args = ksTypeReference?.element?.typeArguments ?: resolved?.arguments ?: emptyList()
        args.map {
            // TODO what if it.type is null?
            env.wrap(it.type!!)
        }
    }
    override val typeName: TypeName by lazy {
        ksType?.typeName() ?: ksTypeReference?.typeName() ?: UNDEFINED
    }

    override fun asTypeElement(): XTypeElement {
        TODO("Not yet implemented")
    }

    override fun isAssignableFrom(other: XType): Boolean {
        TODO("Not yet implemented")
    }

    override fun isError(): Boolean {
        TODO("Not yet implemented")
    }

    override fun defaultValue(): String {
        TODO("Not yet implemented")
    }

    override fun boxed(): XType {
        TODO("Not yet implemented")
    }

    override fun isNone(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isType(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTypeOf(other: KClass<*>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSameType(other: XType): Boolean {
        TODO("Not yet implemented")
    }

    override fun erasure(): XType {
        TODO("Not yet implemented")
    }

    override fun extendsBound(): XType? {
        TODO("Not yet implemented")
    }

    override val equalityItems: Array<out Any?> by lazy {
        arrayOf(ksType)
    }

    override fun equals(other: Any?): Boolean {
        return XEquality.equals(this, other)
    }

    override fun hashCode(): Int {
        return XEquality.hashCode(equalityItems)
    }

    override fun toString(): String {
        return ksType?.toString() ?: ksTypeReference?.toString() ?: super.toString()
    }
}