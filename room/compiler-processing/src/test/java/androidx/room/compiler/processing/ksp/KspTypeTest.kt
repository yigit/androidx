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
import org.jetbrains.kotlin.ksp.getDeclaredFunctions
import org.jetbrains.kotlin.ksp.symbol.KSPropertyDeclaration
import org.jetbrains.kotlin.ksp.symbol.KSTypeReference
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KspTypeTest {
    @Test
    fun assignability() {
        val src = Source.kotlin(
            "foo.kt", """
            package foo.bar;
            class Baz : AbstractClass(), MyInterface {
            }
            abstract class AbstractClass {}
            interface MyInterface {}
        """.trimIndent()
        )
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
            assertThat(
                interfaceSubject.isAssignableFrom(
                    abstractSubject
                )
            ).isFalse()
            assertThat(
                interfaceSubject.isAssignableFrom(
                    subject
                )
            ).isTrue()
            assertThat(
                abstractSubject.isAssignableFrom(
                    subject
                )
            ).isTrue()
        }
    }

    @Test
    fun errorType() {
        val src = Source.kotlin(
            "foo.kt", """
            package foo.bar;
            val errorType : IDontExist = TODO()
            val listOfErrorType : List<IDontExist> = TODO()
        """.trimIndent()
        )
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
        val src = Source.kotlin(
            "foo.kt", """
            package foo.bar;
            val listOfNullableStrings : List<String?> = TODO()
            val listOfInts : List<Int> = TODO()
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            invocation.requirePropertyType("listOfNullableStrings").let { type ->
                assertThat(type.nullability).isEqualTo(NONNULL)
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.nullability).isEqualTo(NULLABLE)
                    assertThat(
                        typeArg.isAssignableFrom(
                            invocation.processingEnv.requireType("kotlin.String")
                        )
                    ).isTrue()
                }
            }

            invocation.requirePropertyType("listOfInts").let { type ->
                assertThat(type.nullability).isEqualTo(NONNULL)
                assertThat(type.typeArguments).hasSize(1)
                type.typeArguments.single().let { typeArg ->
                    assertThat(typeArg.nullability).isEqualTo(NONNULL)
                    assertThat(
                        typeArg.isAssignableFrom(
                            invocation.processingEnv.requireType("kotlin.Int")
                        )
                    ).isTrue()
                }
            }
        }
    }

    @Test
    fun equality() {
        val src = Source.kotlin(
            "foo.kt", """
            package foo.bar;
            val listOfNullableStrings : List<String?> = TODO()
            val listOfNullableStrings_2 : List<String?> = TODO()
            val listOfNonNullStrings : List<String> = TODO()
            val listOfNonNullStrings_2 : List<String> = TODO()
            val nullableString : String? = TODO()
            val nonNullString : String = TODO()
        """.trimIndent()
        )
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
    fun rawType() {
        val src = Source.kotlin(
            "foo.kt", """
            package foo.bar;
            val simple : Int = 0
            val list : List<String> = TODO()
            val map : Map<String, String> = TODO()
            val listOfMaps : List<Map<String, String>> = TODO()
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            invocation.requirePropertyType("simple").let {
                assertThat(it.rawType.typeName).isEqualTo(ClassName.get("kotlin", "Int"))
            }
            invocation.requirePropertyType("list").let { list ->
                assertThat(list.rawType).isNotEqualTo(list)
                assertThat(list.typeArguments).isNotEmpty()
                assertThat(list.rawType.typeName)
                    .isEqualTo(ClassName.get("kotlin.collections", "List"))
            }
            invocation.requirePropertyType("map").let { map ->
                assertThat(map.rawType).isNotEqualTo(map)
                assertThat(map.typeArguments).hasSize(2)
                assertThat(map.rawType.typeName)
                    .isEqualTo(ClassName.get("kotlin.collections", "Map"))
            }
            invocation.requirePropertyType("listOfMaps").let { listOfMaps ->
                assertThat(listOfMaps.rawType).isNotEqualTo(listOfMaps)
                assertThat(listOfMaps.typeArguments).hasSize(1)
            }
        }
    }

    @Test
    fun isTypeChecks() {
        val src = Source.kotlin(
            "foo.kt", """
            val intProp : Int = 0
            val nullableIntProp : Int? = null
            val longProp : Long = 0
            val nullableLongProp : Long? = null
            val byteProp : Byte = 0
            val nullableByteProp :Byte? = null
            val errorProp : IDontExist = TODO()
            val nullableErrorProp : IDontExist? = TODO()
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = false
        ) { invocation ->
            fun mapProp(name: String) = invocation.requirePropertyType(name).let {
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
            assertThat(mapProp("errorProp")).containsExactly("isError")
            assertThat(mapProp("nullableErrorProp")).containsExactly("isError")
        }
    }

    @Test
    fun defaultValue() {
        val src = Source.kotlin(
            "foo.kt", """
            val intProp : Int = 3 // kotlin default value is unrelated, will be ignored
            val nullableIntProp : Int? = null
            val longProp : Long = 3
            val nullableLongProp : Long? = null
            val floatProp = 3f
            val byteProp : Byte = 0
            val nullableByteProp :Byte? = null
            val errorProp : IDontExist = TODO()
            val nullableErrorProp : IDontExist? = TODO()
            val stringProp : String = "abc"
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = false
        ) { invocation ->
            fun getDefaultValue(name: String) = invocation.requirePropertyType(name).defaultValue()
            // javac types do not check nullability but checking it is more correct
            // since KSP is an opt-in by the developer, it is better for it to be more strict about
            // types.
            assertThat(getDefaultValue("intProp")).isEqualTo("0")
            assertThat(getDefaultValue("nullableIntProp")).isEqualTo("null")
            assertThat(getDefaultValue("longProp")).isEqualTo("0")
            assertThat(getDefaultValue("nullableLongProp")).isEqualTo("null")
            assertThat(getDefaultValue("floatProp")).isEqualTo("0f")
            assertThat(getDefaultValue("byteProp")).isEqualTo("0")
            assertThat(getDefaultValue("nullableByteProp")).isEqualTo("null")
            assertThat(getDefaultValue("errorProp")).isEqualTo("null")
            assertThat(getDefaultValue("nullableErrorProp")).isEqualTo("null")
            assertThat(getDefaultValue("stringProp")).isEqualTo("null")
        }
    }

    @Test
    fun isTypeOf() {
        val src = Source.kotlin(
            "foo.kt", """
            val intProp : Int = 3
            val longProp : Long = 3
            val stringProp : String = "abc"
            val listProp : List<String> = TODO()
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            assertThat(
                invocation.requirePropertyType("stringProp").isTypeOf(
                    String::class
                )
            ).isTrue()
            assertThat(
                invocation.requirePropertyType("intProp").isTypeOf(
                    Int::class
                )
            ).isTrue()
            assertThat(
                invocation.requirePropertyType("longProp").isTypeOf(
                    Long::class
                )
            ).isTrue()
            assertThat(
                invocation.requirePropertyType("listProp").isTypeOf(
                    List::class
                )
            ).isTrue()
            assertThat(
                invocation.requirePropertyType("listProp").isTypeOf(
                    Set::class
                )
            ).isFalse()
            assertThat(
                invocation.requirePropertyType("listProp").isTypeOf(
                    Iterable::class
                )
            ).isFalse()
        }
    }

    @Test
    fun isSameType() {
        val src = Source.kotlin(
            "foo.kt", """
            val intProp : Int = 3
            val intProp2 : Int = 4
            val longProp : Long = 0L
            val nullableLong : Long? = null
            val listOfStrings1 : List<String> = TODO()
            val listOfStrings2 : List<String> = TODO()
            val listOfInts : List<Int> = TODO()
            val listOfNullableStrings : List<String?> = TODO()
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            fun check(prop1: String, prop2: String): Boolean {
                return invocation.requirePropertyType(prop1).isSameType(
                    invocation.requirePropertyType(prop2)
                )
            }
            assertThat(check("intProp", "intProp2")).isTrue()
            assertThat(check("intProp2", "intProp")).isTrue()
            assertThat(check("intProp", "longProp")).isFalse()
            // incompatible w/ java
            assertThat(check("longProp", "nullableLong")).isFalse()
            assertThat(check("listOfStrings1", "listOfStrings2")).isTrue()
            assertThat(check("listOfStrings1", "listOfNullableStrings")).isFalse()
            assertThat(check("listOfInts", "listOfStrings2")).isFalse()
        }
    }

    @Suppress("MapGetWithNotNullAssertionOperator")
    @Test
    fun extendsBounds() {
        val src = Source.kotlin(
            "foo.kt", """
            open class Foo;
            class Bar<T : Foo> {
            }
            class Bar_NullableFoo<T : Foo?>
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            val env = (invocation.processingEnv as KspProcessingEnv)
            val classNames = listOf("Bar", "Bar_NullableFoo")
            val typeArgs = classNames.associateWith {
                env.resolver.findClass(it)!!
                    .asStarProjectedType()
                    .arguments
                    .single()
                    .type
                    .let {
                        invocation.wrap(typeRef = it!!)
                    }
            }
            assertThat(typeArgs["Bar"]!!.typeName)
                .isEqualTo(ClassName.get("", "Foo"))
            assertThat(typeArgs["Bar"]!!.nullability).isEqualTo(NONNULL)
            assertThat(typeArgs["Bar_NullableFoo"]!!.typeName)
                .isEqualTo(ClassName.get("", "Foo"))
            assertThat(typeArgs["Bar_NullableFoo"]!!.nullability).isEqualTo(NULLABLE)
        }
    }

    @Test
    fun wildcardJava() {
        val src = Source.java(
            "foo.bar.Baz", """
            package foo.bar;
            import java.util.List;
            public class Baz {
                private void wildcardMethod(List<? extends Number> list) {
                } 
            }
        """.trimIndent()
        )
        runKspTest(
            listOf(src),
            succeed = true
        ) { invocation ->
            val env = (invocation.processingEnv as KspProcessingEnv)
            val method = env.resolver
                .findClass("foo.bar.Baz")
                ?.getDeclaredFunctions()
                ?.first {
                    it.simpleName.asString() == "wildcardMethod"
                } ?: throw AssertionError("cannot find test method")
            val paramType = invocation.wrap(method.parameters.first().type!!)
            val arg1 = paramType.typeArguments.single()
            assertThat(arg1.typeName)
                .isEqualTo(ClassName.get("kotlin", "Number"))
            assertThat(arg1.extendsBound()).isNull()
        }
    }

    private fun TestInvocation.requirePropertyType(name: String): KspType {
        (processingEnv as KspProcessingEnv).resolver.getAllFiles().forEach { file ->
            val prop = file.declarations.first {
                it.simpleName.asString() == name
            } as KSPropertyDeclaration
            return checkNotNull(prop.type?.let {
                wrap(it)
            }) {
                "cannot find type for $name"
            }
        }
        throw IllegalStateException("cannot find any property with name $name")
    }

    private fun TestInvocation.wrap(typeRef: KSTypeReference): KspType {
        return (processingEnv as KspProcessingEnv).wrap(typeRef)
    }
}