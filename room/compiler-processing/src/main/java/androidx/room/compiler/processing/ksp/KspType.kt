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
import androidx.room.compiler.processing.XNullability
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XTypeElement
import com.squareup.javapoet.TypeName
import org.jetbrains.kotlin.ksp.symbol.KSClassDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSType
import org.jetbrains.kotlin.ksp.symbol.KSTypeReference
import org.jetbrains.kotlin.ksp.symbol.Nullability
import kotlin.reflect.KClass

internal class KspType private constructor(
    private val env: KspProcessingEnv,
    private val ksTypeReference: KSTypeReference?,
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
    override val nullability by lazy {
        when(ksType?.nullability) {
            Nullability.NULLABLE -> XNullability.NULLABLE
            Nullability.NOT_NULL -> XNullability.NONNULL
            else -> XNullability.UNKNOWN
        }
    }

    override fun asTypeElement(): XTypeElement {
        TODO("elements are not implemented yet")
    }

    override fun isAssignableFrom(other: XType): Boolean {
        check(other is KspType)
        val otherType = other.ksType ?: return false
        return ksType?.isAssignableFrom(otherType) == true
    }

    override fun isError(): Boolean {
        return ksType == null || ksType!!.isError
    }

    override fun defaultValue(): String {
        val type = ksType ?: return "null"
        if (type.nullability == Nullability.NULLABLE) {
            return "null"
        }
        val builtIns = env.resolver.builtIns
        return when(type) {
            builtIns.booleanType -> "false"
            builtIns.byteType, builtIns.shortType, builtIns.intType, builtIns.longType, builtIns
                .charType -> "0"
            builtIns.floatType -> "0f"
            builtIns.doubleType -> "0.0"
            else -> "null"
        }
    }

    override fun boxed(): XType {
        return this
    }

    override fun isInt(): Boolean {
        return ksType?.isAssignableFrom(env.resolver.builtIns.intType) == true
    }

    override fun isLong(): Boolean {
        return ksType?.isAssignableFrom(env.resolver.builtIns.longType) == true
    }

    override fun isByte(): Boolean {
        return ksType?.isAssignableFrom(env.resolver.builtIns.byteType) == true
    }

    override fun isNone(): Boolean {
        return ksType == null
    }

    override fun isType(): Boolean {
        return ksType != null && ksType!!.declaration is KSClassDeclaration
    }

    override fun isTypeOf(other: KClass<*>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSameType(other: XType): Boolean {
        check(other is KspType)
        return ksType != null && ksType == other.ksType
    }

    override fun erasure(): KspType {
        if (typeArguments.isEmpty()) return this
        return ksType?.let {
            env.wrap(it.replace(emptyList()))
        } ?: this
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