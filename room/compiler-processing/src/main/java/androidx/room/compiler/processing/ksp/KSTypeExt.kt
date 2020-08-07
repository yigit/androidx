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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import org.jetbrains.kotlin.ksp.symbol.KSClassifierReference
import org.jetbrains.kotlin.ksp.symbol.KSDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSName
import org.jetbrains.kotlin.ksp.symbol.KSType
import org.jetbrains.kotlin.ksp.symbol.KSTypeReference

// catch-all type name when we cannot resolve to anything.
internal val UNDEFINED = ClassName.get("androidx.room.compiler.processing.kotlin.error", "Undefined")

/**
 * Turns a KSTypeReference into a TypeName
 *
 * We try to achieve this by first resolving it and iterating.
 * If some types cannot be resolved, we do a best effort name guess from the KSTypeReference's
 * element.
 */
internal fun KSTypeReference?.typeName(): TypeName {
    if (this == null) {
        return UNDEFINED
    }
    val resolved = resolve()
    return resolved?.typeName() ?: fallbackClassName()
}

private fun KSTypeReference.fallbackClassName(): ClassName {
    return (element as? KSClassifierReference)?.let {
        ClassName.get("", it.referencedName())
    } ?: UNDEFINED
}

private fun KSName.typeName(): ClassName? {
    if (asString().isBlank()) {
        // fallback to reference
        return null
    }
    return ClassName.get(getQualifier(), getShortName())
}

private fun KSDeclaration.typeName(): ClassName? {
    // we are only interested in qualified name as if it does not exist, it is an error for Room
    return qualifiedName?.typeName()
}

internal fun KSType.typeName(): TypeName? {
    return if (this.arguments.isNotEmpty()) {
        val args: Array<TypeName> = this.arguments.map {
            it.type.typeName()
        }.toTypedArray()
        val className = declaration.typeName() ?: return null
        ParameterizedTypeName.get(
            className,
            *args
        )
    } else {
        return this.declaration.typeName()
    }
}
