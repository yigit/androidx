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
import androidx.room.compiler.processing.XNullability
import org.jetbrains.kotlin.ksp.isOpen
import org.jetbrains.kotlin.ksp.isPrivate
import org.jetbrains.kotlin.ksp.isProtected
import org.jetbrains.kotlin.ksp.isPublic
import org.jetbrains.kotlin.ksp.symbol.KSClassDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSPropertyDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSTypeAlias
import org.jetbrains.kotlin.ksp.symbol.Modifier
import kotlin.reflect.KClass

internal abstract class KspElement(
    protected val env: KspProcessingEnv,
    open val declaration: KSDeclaration
) : XElement, XEquality {
    override val name: String
        get() = declaration.simpleName.getShortName()
    override val packageName: String
        get() = declaration.qualifiedName?.getQualifier() ?: ""
    override val nullability: XNullability
        get() = TODO("Not yet implemented")
    override val enclosingElement: XElement? by lazy {
        (declaration.parentDeclaration as? KSClassDeclaration)?.let(env::wrapClassDeclaration)
    }

    override fun isPublic() = declaration.isPublic()

    override fun isProtected() = declaration.isProtected()

    override fun isAbstract() = declaration.modifiers.contains(Modifier.ABSTRACT)

    override fun isPrivate() = declaration.isPrivate()

    override fun isStatic() = declaration.modifiers.contains(Modifier.JAVA_STATIC)

    override fun isTransient() = declaration.modifiers.contains(Modifier.JAVA_TRANSIENT)

    override fun isFinal() = !declaration.isOpen()

    override fun kindName(): String {
        return when(declaration) {
            is KSClassDeclaration -> "class"
            is KSPropertyDeclaration -> "property"
            is KSTypeAlias -> "type alias"
            // TODO cover all reasonable options?
            else -> declaration::class.java.simpleName
        }
    }

    override fun <T : Annotation> toAnnotationBox(annotation: KClass<T>): XAnnotationBox<T>? {
        TODO("Not yet implemented")
    }

    override fun hasAnnotationWithPackage(pkg: String): Boolean {
        return declaration.annotations.any {
            it.annotationType.resolve()?.declaration?.qualifiedName?.getQualifier() == pkg
        }
    }

    override fun hasAnnotation(annotation: KClass<out Annotation>): Boolean {
        return declaration.annotations.any {
            it.annotationType.resolve()?.declaration?.qualifiedName?.asString() == annotation.qualifiedName
        }
    }

    override fun toString(): String {
        return declaration.toString()
    }

    override fun equals(other: Any?): Boolean {
        return XEquality.equals(this, other)
    }

    override fun hashCode(): Int {
        return XEquality.hashCode(equalityItems)
    }
}