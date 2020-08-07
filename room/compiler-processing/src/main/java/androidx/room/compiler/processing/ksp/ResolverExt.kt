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

import org.jetbrains.kotlin.ksp.processing.Resolver
import org.jetbrains.kotlin.ksp.symbol.KSClassDeclaration
import java.util.Locale

internal fun Resolver.findClass(qName: String): KSClassDeclaration? {
    getClassDeclarationByName(
        getKSNameFromString(qName)
    )?.let {
        return it
    }
    // check to see if it matches one of the builtins
    if(!qName.contains('.')) {
        return getClassDeclarationByName(
            getKSNameFromString("kotlin.${qName.capitalize(Locale.US)}")
        )
    }
    return null
}

internal fun Resolver.requireClass(qName: String) = checkNotNull(findClass(qName)) {
    "cannot find class $qName"
}
