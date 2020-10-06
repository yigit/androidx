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

package androidx.wear.watchface.control.data;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.versionedparcelable.ParcelField;
import androidx.versionedparcelable.ParcelUtils;
import androidx.versionedparcelable.VersionedParcelable;
import androidx.versionedparcelable.VersionedParcelize;
import androidx.wear.watchface.control.IWallpaperWatchFaceControlService;
import androidx.wear.watchface.data.DeviceConfig;
import androidx.wear.watchface.data.IdAndComplicationData;
import androidx.wear.watchface.data.SystemState;
import androidx.wear.watchface.style.data.UserStyleWireFormat;

import java.util.List;

/**
 * Parameters for {@link IWallpaperWatchFaceControlService#createInteractiveWatchFaceInstance}.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@VersionedParcelize
@SuppressLint("BanParcelableUsage") // TODO(b/169214666): Remove Parcelable
public class WallpaperInteractiveWatchFaceInstanceParams
        implements VersionedParcelable, Parcelable {

    /** The id for the new instance, must be unique. */
    @ParcelField(1)
    @NonNull
    String mInstanceId;

    /** The {@link DeviceConfig} for the host wearable. */
    @ParcelField(2)
    @NonNull
    DeviceConfig mDeviceConfig;

    /** The {@link SystemState} for the host wearable. */
    @ParcelField(3)
    @NonNull
    SystemState mSystemState;

    /** The initial {UserStyleWireFormat} if known, or null otherwise. */
    @ParcelField(4)
    @Nullable
    UserStyleWireFormat mUserStyle;

    /** The initial state of the complications if known, or null otherwise. */
    @ParcelField(100)
    @Nullable
    List<IdAndComplicationData> mIdAndComplicationData;

    /** Used by VersionedParcelable. */
    WallpaperInteractiveWatchFaceInstanceParams() {}

    public WallpaperInteractiveWatchFaceInstanceParams(
            @NonNull String instanceId,
            @NonNull DeviceConfig deviceConfig,
            @NonNull SystemState systemState,
            @Nullable UserStyleWireFormat userStyle,
            @Nullable List<IdAndComplicationData> idAndComplicationData) {
        mInstanceId = instanceId;
        mDeviceConfig = deviceConfig;
        mSystemState = systemState;
        mUserStyle = userStyle;
        mIdAndComplicationData = idAndComplicationData;
    }

    @NonNull
    public String getInstanceId() {
        return mInstanceId;
    }

    @NonNull
    public DeviceConfig getDeviceConfig() {
        return mDeviceConfig;
    }

    @NonNull
    public SystemState getSystemState() {
        return mSystemState;
    }

    @Nullable
    public UserStyleWireFormat getUserStyle() {
        return mUserStyle;
    }

    @Nullable
    public List<IdAndComplicationData> getIdAndComplicationData() {
        return mIdAndComplicationData;
    }

    /**
     * Serializes this WallpaperInteractiveWatchFaceInstanceParams to the specified {@link Parcel}.
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(ParcelUtils.toParcelable(this), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<WallpaperInteractiveWatchFaceInstanceParams> CREATOR =
            new Parcelable.Creator<WallpaperInteractiveWatchFaceInstanceParams>() {
                @Override
                public WallpaperInteractiveWatchFaceInstanceParams createFromParcel(Parcel source) {
                    return WallpaperInteractiveWatchFaceInstanceParamsParcelizer.read(
                            ParcelUtils.fromParcelable(source.readParcelable(
                                    getClass().getClassLoader())));
                }

                @Override
                public WallpaperInteractiveWatchFaceInstanceParams[] newArray(int size) {
                    return new WallpaperInteractiveWatchFaceInstanceParams[size];
                }
            };
}
