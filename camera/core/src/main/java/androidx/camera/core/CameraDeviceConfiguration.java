/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.camera.core;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.camera.core.CameraX.LensFacing;

/**
 * Configuration containing options for configuring a Camera device.
 *
 * <p>This includes options for camera device intrinsics, such as the lens facing direction.
 */
public interface CameraDeviceConfiguration extends Configuration.Reader {

    /**
     * Option: camerax.core.camera.lensFacing
     *
     * @hide
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    Option<LensFacing> OPTION_LENS_FACING =
            Option.create("camerax.core.camera.lensFacing", CameraX.LensFacing.class);

    /**
     * Retrieves the lens facing direction for the primary camera to be configured.
     *
     * @param valueIfMissing The value to return if this configuration option has not been set.
     * @return The stored value or <code>valueIfMissing</code> if the value does not exist in this
     * configuration.
     */
    @Nullable
    default CameraX.LensFacing getLensFacing(@Nullable LensFacing valueIfMissing) {
        return retrieveOption(OPTION_LENS_FACING, valueIfMissing);
    }

    /**
     * Retrieves the lens facing direction for the primary camera to be configured.
     *
     * @return The stored value, if it exists in this configuration.
     * @throws IllegalArgumentException if the option does not exist in this configuration.
     */
    default CameraX.LensFacing getLensFacing() {
        return retrieveOption(OPTION_LENS_FACING);
    }

    // Option Declarations:
    // ***********************************************************************************************

    /**
     * Builder for a {@link CameraDeviceConfiguration}.
     *
     * @param <C> The top level configuration which will be generated by {@link #build()}.
     * @param <B> The top level builder type for which this builder is composed with.
     */
    interface Builder<C extends Configuration, B extends Builder<C, B>>
            extends Configuration.Builder<C, B> {

        /**
         * Sets the primary camera to be configured based on the direction the lens is facing.
         *
         * <p>If multiple cameras exist with equivalent lens facing direction, the first ("primary")
         * camera for that direction will be chosen.
         *
         * @param lensFacing The direction of the camera's lens.
         * @return the current Builder.
         */
        default B setLensFacing(CameraX.LensFacing lensFacing) {
            getMutableConfiguration().insertOption(OPTION_LENS_FACING, lensFacing);
            return builder();
        }
    }
}
