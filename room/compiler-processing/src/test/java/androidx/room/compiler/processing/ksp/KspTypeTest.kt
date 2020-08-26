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
import androidx.room.compiler.processing.util.TestInvocation
import androidx.room.compiler.processing.util.runKspTest
import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.ClassName
import org.jetbrains.kotlin.ksp.getDeclaredProperties
import org.jetbrains.kotlin.ksp.symbol.KSPropertyDeclaration
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KspTypeTest {
    @Test
    fun assignability() {
        val src = Source.kotlin("foo.kt", """
            package foo.bar;
            class Baz : AbstractClass(), MyInterface {
            }
            abstract class AbstractClass {}
            interface MyInterface {}
        """.trimIndent())
        runKspTest(listOf(src), succeed = true) {
            val subject = it.processingEnv.requireType("foo.bar.Baz")
            assertThat(subject.typeName).isEqualTo(
                ClassName.get("foo.bar", "Baz")
            )
            // basic assertions for abstract class
            val abstractSubject = it.processingEnv.requireType("foo.bar.AbstractClass")
            // basic assertions for interface declaration
            val interfaceSubject = it.processingEnv.requireType("foo.bar.MyInterface")
            // check assignability
            assertThat(interfaceSubject.isAssignableFrom(
                abstractSubject
            )).isFalse()
            assertThat(interfaceSubject.isAssignableFrom(
                subject
            )).isTrue()
            assertThat(abstractSubject.isAssignableFrom(
                subject
            )).isTrue()
        }
    }

    @Test
    fun errorType() {
        val src = Source.kotlin("foo.kt", """
            package foo.bar;
            val errorType : IDontExist = TODO()
            val listOfErrorType : List<IDontExist> = TODO()
        """.trimIndent())
        runKspTest(
            listOf(src),
            succeed = false
        ) { invocation ->
            invocation.requirePropertyType("errorType").let { type ->
                assertThat(type.isError()).isTrue()
                assertThat(type.typeArguments).isEmpty()
                assertThat(type.typeName).isEqualTo(ClassName.bestGuess("IDontExist"))
            }

            invocation.requirePropertyType("listOfErrorType").let { type ->
                assertThat(type.isError()).isFalse()
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.isError()).isTrue()
                    assertThat(typeArg.typeName).isEqualTo(ClassName.bestGuess("IDontExist"))
                }
            }
        }
    }
    @Test
    fun typeArguments() {
        val src = Source.kotlin("foo.kt", """
            package foo.bar;
            val listOfNullableStrings : List<String?> = TODO()
            val listOfInts : List<Int> = TODO()
        """.trimIndent())
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            invocation.requirePropertyType("listOfNullableStrings").let { type ->
                assertThat(type.nullability).isEqualTo(NONNULL)
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.nullability).isEqualTo(NULLABLE)
                    assertThat(typeArg.isAssignableFrom(
                        invocation.processingEnv.requireType("kotlin.String")
                    )).isTrue()
                }
            }

            invocation.requirePropertyType("listOfInts").let { type ->
                assertThat(type.nullability).isEqualTo(NONNULL)
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.nullability).isEqualTo(NONNULL)
                    assertThat(typeArg.isAssignableFrom(
                        invocation.processingEnv.requireType("kotlin.Int")
                    )).isTrue()
                }
            }
        }
    }

    @Test
    fun equality() {
        val src = Source.kotlin("foo.kt", """
            package foo.bar;
            val listOfNullableStrings : List<String?> = TODO()
            val listOfNullableStrings_2 : List<String?> = TODO()
            val listOfNonNullStrings : List<String> = TODO()
            val listOfNonNullStrings_2 : List<String> = TODO()
            val nullableString : String? = TODO()
            val nonNullString : String = TODO()
        """.trimIndent())
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            val nullableStringList = invocation.requirePropertyType("listOfNullableStrings")
            val nonNullStringList = invocation.requirePropertyType("listOfNonNullStrings")
            assertThat(nullableStringList).isNotEqualTo(nonNullStringList)
            assertThat(nonNullStringList).isNotEqualTo(nullableStringList)

            val nullableStirngList_2 = invocation.requirePropertyType("listOfNullableStrings_2")
            val nonNullStringList_2 = invocation.requirePropertyType("listOfNonNullStrings_2")
            assertThat(nullableStringList).isEqualTo(nullableStirngList_2)
            assertThat(nonNullStringList).isEqualTo(nonNullStringList_2)

            val nullableString = invocation.requirePropertyType("nullableString")
            val nonNullString = invocation.requirePropertyType("nonNullString")
            assertThat(nullableString).isEqualTo(
                nullableStringList.typeArguments.single()
            )
            assertThat(nullableString).isNotEqualTo(
                nonNullStringList.typeArguments.single()
            )
            assertThat(nonNullString).isEqualTo(
                nonNullStringList.typeArguments.single()
            )
            assertThat(nonNullString).isNotEqualTo(
                nullableStringList.typeArguments.single()
            )
        }
    }

    @Test
    fun erasure() {
        val src = Source.kotlin("foo.kt", """
            package foo.bar;
            val simple : Int = 0
            val list : List<String> = TODO()
            val map : Map<String, String> = TODO()
            val listOfMaps : List<Map<String, String>> = TODO()
        """.trimIndent())
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            invocation.requirePropertyType("simple").let {
                assertThat(it.erasure()).isEqualTo(it)
            }
            invocation.requirePropertyType("list").let {list ->
                assertThat(list.erasure()).isNotEqualTo(list)
                assertThat(list.typeArguments).isNotEmpty()
                assertThat(list.erasure().typeArguments).isEmpty()
                assertThat(list.erasure().typeName).isEqualTo(ClassName.get(List::class.java))
            }
            invocation.requirePropertyType("map").let {map ->
                assertThat(map.erasure()).isNotEqualTo(map)
                assertThat(map.typeArguments).hasSize(2)
                assertThat(map.erasure().typeArguments).isEmpty()
                assertThat(map.erasure().typeName).isEqualTo(ClassName.get(Map::class.java))
            }
            invocation.requirePropertyType("listOfMaps").let {listOfMaps ->
                assertThat(listOfMaps.erasure()).isNotEqualTo(listOfMaps)
                assertThat(listOfMaps.typeArguments).hasSize(1)
                assertThat(listOfMaps.erasure().typeArguments).isEmpty()
            }
        }
    }

    @Test
    fun isTypeChecks() {
        val src = Source.kotlin("foo.kt", """
            val intProp : Int = 0
            val nullableIntProp : Int? = null
            val longProp : Long = 0
            val nullableLongProp : Long? = null
            val byteProp : Byte = 0
            val nullableByteProp :Byte? = null
            val errorProp : IDontExist = TODO()
            val nullableErrorProp : IDontExist? = TODO()
        """.trimIndent())
        runKspTest(
            listOf(src),
            succeed = false
        ) { invocation ->
            fun mapProp(name : String) = invocation.requirePropertyType(name).let {
                listOf(
                    "isInt" to it.isInt(),
                    "isLong" to it.isLong(),
                    "isByte" to it.isByte(),
                    "isError" to it.isError(),
                    "isNone" to it.isNone()
                ).filter {
                    it.second
                }.map {
                    it.first
                }
            }
            assertThat(mapProp("intProp")).containsExactly("isInt")
            assertThat(mapProp("nullableIntProp")).containsExactly("isInt")
            assertThat(mapProp("longProp")).containsExactly("isLong")
            assertThat(mapProp("nullableLongProp")).containsExactly("isLong")
            assertThat(mapProp("byteProp")).containsExactly("isByte")
            assertThat(mapProp("nullableByteProp")).containsExactly("isByte")
            // fails right now due to https://github.com/android/kotlin/issues/123
            assertThat(mapProp("errorProp")).containsExactly("isError")
            assertThat(mapProp("nullableErrorProp")).containsExactly("isError")
        }
    }

    private fun TestInvocation.requirePropertyType(name: String) : KspType {
        (processingEnv as KspProcessingEnv).resolver.getAllFiles().forEach {file ->
            val prop =  file.declarations.first {
                it.simpleName.asString() == name
            } as KSPropertyDeclaration
            return checkNotNull(prop.type?.let {
                processingEnv.wrap(it)
            }) {
                "cannot find type for $name"
            }
        }
        throw IllegalStateException("cannot find any property with name $name")
    }
}