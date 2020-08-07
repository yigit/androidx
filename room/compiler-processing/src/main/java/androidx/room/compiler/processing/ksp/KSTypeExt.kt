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

private val UNDEFINED = ClassName.get("androidx.room.compiler.processing.kotlin.error", "Undefined")
internal fun KSTypeReference?.typeName(): TypeName {
    if (this == null) {
        return UNDEFINED
    }
    val resolved = resolve()
    if (resolved == null) {
        // error, try to get some value out of it
        (element as? KSClassifierReference)?.let {

        }
        return UNDEFINED
    } else {
        return resolved.typeName(this)
    }
}

private fun KSTypeReference.fallbackClassName() : ClassName {
    return (element as? KSClassifierReference)?.let {
        ClassName.get("", it.referencedName())
    } ?: UNDEFINED

}

private fun KSName.typeName(reference: KSTypeReference): ClassName {
    if (asString().isBlank()) {
        // fallback to reference
        return reference.fallbackClassName()
    }
    return ClassName.get(getQualifier(), getShortName())
}

private fun KSDeclaration.typeName(reference: KSTypeReference): ClassName {
    return qualifiedName?.typeName(reference) ?: simpleName.typeName(reference)
}

private fun KSType.typeName(reference: KSTypeReference) : TypeName {
    return if (this.arguments.isNotEmpty()) {
        val args: Array<TypeName> = this.arguments.map {
            it.type.typeName()
        }.toTypedArray()
        val className = declaration.typeName(reference)
        ParameterizedTypeName.get(
            className,
            *args
        )
    } else {
        return this.declaration.typeName(reference)
    }
}