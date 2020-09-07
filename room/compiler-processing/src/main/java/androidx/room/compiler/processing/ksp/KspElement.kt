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

import androidx.room.compiler.processing.XAnnotationBox
import androidx.room.compiler.processing.XElement
import androidx.room.compiler.processing.XEquality
import org.jetbrains.kotlin.ksp.isOpen
import org.jetbrains.kotlin.ksp.isPrivate
import org.jetbrains.kotlin.ksp.isProtected
import org.jetbrains.kotlin.ksp.isPublic
import org.jetbrains.kotlin.ksp.symbol.KSClassDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSFunctionDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSPropertyDeclaration
import org.jetbrains.kotlin.ksp.symbol.Modifier
import java.util.Locale
import kotlin.reflect.KClass

internal abstract class KspElement(
    protected val env:KspProcessingEnv,
    open val declaration: KSDeclaration
) : XElement, XEquality {
    override val name: String by lazy {
        declaration.simpleName.asString()
    }
    override val packageName: String by lazy {
        declaration.safeGetPackageName() ?: ERROR_PACKAGE_NAME
    }
    override val enclosingElement: XElement? by lazy {
        val parent = declaration.parentDeclaration
        if (parent is KSClassDeclaration) {
            env.wrapClassDeclaration(parent)
        } else {
            null
        }
    }

    override fun isPublic(): Boolean {
        return declaration.isPublic()
    }

    override fun isProtected(): Boolean {
        return declaration.isProtected()
    }

    override fun isAbstract(): Boolean {
        return declaration.modifiers.contains(Modifier.ABSTRACT)
    }

    override fun isPrivate(): Boolean {
        return declaration.isPrivate()
    }

    override fun isStatic(): Boolean {
        return declaration.modifiers.contains(Modifier.JAVA_STATIC)
    }

    override fun isTransient(): Boolean {
        return declaration.modifiers.contains(Modifier.JAVA_TRANSIENT)
    }

    override fun isFinal(): Boolean {
        return !declaration.isOpen()
    }

    override fun kindName(): String {
        return when(declaration) {
            is KSClassDeclaration -> (declaration as KSClassDeclaration).classKind.name
                .toLowerCase(Locale.US)
            is KSPropertyDeclaration -> "property"
            is KSFunctionDeclaration -> "function"
            else -> declaration::class.simpleName ?: "unknown"
        }
    }

    override fun <T : Annotation> toAnnotationBox(annotation: KClass<T>): XAnnotationBox<T>? {
        TODO("Not yet implemented")
    }

    override fun hasAnnotationWithPackage(pkg: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasAnnotation(annotation: KClass<out Annotation>): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        return XEquality.equals(this, other)
    }

    override fun hashCode(): Int {
        return XEquality.hashCode(equalityItems)
    }
}