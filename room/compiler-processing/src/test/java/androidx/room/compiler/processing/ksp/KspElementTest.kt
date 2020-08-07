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

import androidx.room.compiler.processing.XNullability.NONNULL
import androidx.room.compiler.processing.XNullability.NULLABLE
import androidx.room.compiler.processing.util.Source
import androidx.room.compiler.processing.util.runKspTest
import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.ClassName
import org.jetbrains.kotlin.ksp.symbol.KSPropertyDeclaration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// TODO note: make this a KSPType test and try to merge just that.
//  seems very feasible
@RunWith(JUnit4::class)
class KspElementTest {
    @Test
    fun typeElements() {
        val src = Source.kotlin("foo.kt", """
            package foo.bar;
            class Baz : AbstractClass(), MyInterface {
            }
            
            abstract class AbstractClass {}
            interface MyInterface {}
        """.trimIndent())
        runKspTest(listOf(src), succeed = true) {
            val subject = it.processingEnv.requireTypeElement("foo.bar.Baz")
            assertThat(subject.className).isEqualTo(
                ClassName.get("foo.bar", "Baz")
            )
            assertThat(subject.qualifiedName).isEqualTo("foo.bar.Baz")
            assertThat(subject.superType?.typeName)
                .isEqualTo(ClassName.get("foo.bar", "AbstractClass"))
            assertThat(subject.isInterface()).isFalse()
            assertThat(subject.isAbstract()).isFalse()

            // basic assertions for abstract class
            val abstractSubject = it.processingEnv.requireTypeElement("foo.bar.AbstractClass")
            assertThat(abstractSubject.superType).isNull()
            assertThat(abstractSubject.isInterface()).isFalse()
            assertThat(abstractSubject.isAbstract()).isTrue()

            // basic assertions for interface declaration
            val interfaceSubject = it.processingEnv.requireTypeElement("foo.bar.MyInterface")
            assertThat(interfaceSubject.superType).isNull()
            assertThat(interfaceSubject.isInterface()).isTrue()
            assertThat(interfaceSubject.isAbstract()).isFalse()

            // check assignability
            assertThat(interfaceSubject.type.isAssignableFrom(
                abstractSubject.type
            )).isFalse()
            assertThat(interfaceSubject.type.isAssignableFrom(
                subject.type
            )).isTrue()
            assertThat(abstractSubject.type.isAssignableFrom(
                subject.type
            )).isTrue()
        }
    }

    @Test
    fun typeArguments() {
        val src = Source.kotlin("foo.kt", """
            package foo.bar;
            val listOfNullableStrings : List<String?> = TODO()
            val listOfInts : List<Int> = TODO()
            val errorType : IDontExist = TODO()
            val listOfErrorType : List<IDontExist> = TODO()
        """.trimIndent())
        runKspTest(
            listOf(src),
            succeed = false
        ) {invocation ->
            val kspProcessingEnv = invocation.processingEnv as KspProcessingEnv
            val resolver = kspProcessingEnv.resolver
            val target = resolver.getAllFiles().first {
                it.fileName == "foo.kt"
            }
            fun requirePropType(name :String) : KspType {
                val prop =  target.declarations.first {
                    it.simpleName.asString() == name
                } as KSPropertyDeclaration
                return checkNotNull(prop.type?.let {
                    kspProcessingEnv.wrap(it)
                }) {
                    "cannot find type for $name"
                }
            }
            requirePropType("listOfNullableStrings").let { type ->
                assertThat(type.nullability).isEqualTo(NONNULL)
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.nullability).isEqualTo(NULLABLE)
                    assertThat(typeArg.isAssignableFrom(
                        invocation.processingEnv.requireType("kotlin.String")
                    )).isTrue()
                }
            }

            requirePropType("listOfInts").let { type ->
                assertThat(type.nullability).isEqualTo(NONNULL)
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.nullability).isEqualTo(NONNULL)
                    assertThat(typeArg.isAssignableFrom(
                        invocation.processingEnv.requireType("kotlin.Int")
                    )).isTrue()
                }
            }

            requirePropType("errorType").let { type ->
                assertThat(type.isError()).isTrue()
                assertThat(type.typeArguments).isEmpty()
                assertThat(type.typeName).isEqualTo(ClassName.bestGuess("IDontExist"))
            }

            requirePropType("listOfErrorType").let { type ->
                assertThat(type.isError()).isFalse()
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.isError()).isTrue()
                    assertThat(typeArg.typeName).isEqualTo(ClassName.bestGuess("IDontExist"))
                }
            }
        }
    }
}