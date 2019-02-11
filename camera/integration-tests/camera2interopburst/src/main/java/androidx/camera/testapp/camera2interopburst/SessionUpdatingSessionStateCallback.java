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

package androidx.camera.testapp.camera2interopburst;

import android.hardware.camera2.CameraCaptureSession;
import android.support.annotation.GuardedBy;
import android.util.Log;
import android.view.Surface;

/** A capture session state callback which updates a reference to the capture session. */
final class SessionUpdatingSessionStateCallback extends CameraCaptureSession.StateCallback {
    private static final String TAG = "SessionUpdatingSessionStateCallback";

    private final Object sessionLock = new Object();

    @GuardedBy("sessionLock")
    private CameraCaptureSession session;

    @Override
    public void onConfigured(CameraCaptureSession session) {
        Log.d(TAG, "onConfigured: session=" + session);
        synchronized (sessionLock) {
            this.session = session;
        }
    }

    @Override
    public void onActive(CameraCaptureSession session) {
        Log.d(TAG, "onActive: session=" + session);
    }

    @Override
    public void onClosed(CameraCaptureSession session) {
        Log.d(TAG, "onClosed: session=" + session);
        synchronized (sessionLock) {
            if (this.session == session) {
                this.session = null;
            }
        }
    }

    @Override
    public void onReady(CameraCaptureSession session) {
        Log.d(TAG, "onReady: session=" + session);
    }

    @Override
    public void onCaptureQueueEmpty(CameraCaptureSession session) {
        Log.d(TAG, "onCaptureQueueEmpty: session=" + session);
    }

    @Override
    public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
        Log.d(TAG, "onSurfacePrepared: session=" + session + ", surface=" + surface);
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {
        Log.d(TAG, "onConfigureFailed: session=" + session);
        synchronized (sessionLock) {
            if (this.session == session) {
                this.session = null;
            }
        }
    }

    CameraCaptureSession getSession() {
        synchronized (sessionLock) {
            return session;
        }
    }
}
