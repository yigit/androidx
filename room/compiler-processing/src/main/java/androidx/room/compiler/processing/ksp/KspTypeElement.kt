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

import androidx.room.compiler.processing.XConstructorElement
import androidx.room.compiler.processing.XDeclaredType
import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XTypeElement
import androidx.room.compiler.processing.XVariableElement
import androidx.room.compiler.processing.javac.JavacDeclaredType
import com.squareup.javapoet.ClassName
import org.jetbrains.kotlin.ksp.symbol.ClassKind
import org.jetbrains.kotlin.ksp.symbol.KSClassDeclaration

internal class KspTypeElement(
    env: KspProcessingEnv,
    override val declaration: KSClassDeclaration
) : KspElement(env, declaration), XTypeElement {
    override val qualifiedName: String by lazy {
        checkNotNull(declaration.qualifiedName) {
            "missing qualified name for $declaration"
        }.asString()
    }
    override val type: KspType by lazy {
        env.wrap(declaration.asStarProjectedType())

    }
    override val superType: XType?
        get() = TODO("Not yet implemented")
    override val className: ClassName by lazy {
        // TODO add a test w/ nested types to ensure they are implemented properly
        ClassName.get(packageName, name)
    }

    override fun isInterface() = declaration.classKind == ClassKind.INTERFACE

    override fun isKotlinObject() = declaration.classKind == ClassKind.OBJECT

    override fun getAllFieldsIncludingPrivateSupers(): List<XVariableElement> {
        TODO("Not yet implemented")
    }

    override fun findPrimaryConstructor(): XConstructorElement? {
        TODO("Not yet implemented")
    }

    override fun getDeclaredMethods(): List<XMethodElement> {
        TODO("Not yet implemented")
    }

    override fun getAllMethods(): List<XMethodElement> {
        TODO("Not yet implemented")
    }

    override fun getAllNonPrivateInstanceMethods(): List<XMethodElement> {
        TODO("Not yet implemented")
    }

    override fun getConstructors(): List<XConstructorElement> {
        TODO("Not yet implemented")
    }
    override val equalityItems: Array<out Any?> by lazy {
        arrayOf(declaration)
    }
}