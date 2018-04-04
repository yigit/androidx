/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.support

/**
 * The list of versions codes of all the libraries in this project.
 */
object LibraryVersions {
    /**
     * Version code of the support library components.
     */
    val SUPPORT_LIBRARY = Version("27.1.0-SNAPSHOT")

    /**
     * Version code for Room
     */
    val ROOM = Version("1.1.0-beta2")

    /**
     * Version code for Lifecycle extensions (ProcessLifecycleOwner, Fragment support)
     */
    val LIFECYCLES_EXT = Version("1.1.1")

    /**
     * Version code for Lifecycle LiveData
     */
    val LIFECYCLES_LIVEDATA = LIFECYCLES_EXT

    /**
     * Version code for Lifecycle ViewModel
     */
    val LIFECYCLES_VIEWMODEL = LIFECYCLES_EXT

    /**
     * Version code for Paging
     */
    val PAGING = Version("1.0.0-alpha7")

    val PAGING_RX = Version("1.0.0-alpha1")

    private val LIFECYCLES = Version("1.1.1")

    /**
     * Version code for Lifecycle libs that are required by the support library
     */
    val LIFECYCLES_CORE = LIFECYCLES

    /**
     * Version code for Lifecycle runtime libs that are required by the support library
     */
    val LIFECYCLES_RUNTIME = LIFECYCLES

    /**
     * Version code for shared code of flatfoot
     */
    val ARCH_CORE = Version("1.1.1")

    /**
     * Version code for shared code of flatfoot runtime
     */
    val ARCH_RUNTIME = ARCH_CORE

    /**
     * Version code for shared testing code of flatfoot
     */
    val ARCH_CORE_TESTING = ARCH_CORE

    /**
     * Version code for Navigation
     */
    val NAVIGATION = Version("0.7.0-alpha1")
}
