package com.deviantce.roadsigncarcontroller.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.deviantce.roadsigncarcontroller.R;
import com.deviantce.roadsigncarcontroller.util.AutoFitTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CameraPreviewFragment extends Fragment
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean isZoonEnabled;
    int id ;
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("zoom_enabled", isZoonEnabled);
        //Log.e("zoom : ", String.valueOf(isZoonEnabled));
    }

    /**
     * 화면 회전에서 JPEG 방향으로 변환
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Camera2BasicFragment";

    /**
     * 카메라 상태 : 카메라 미리보기를 표시합니다
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * 카메라 상태 : 초점이 고정되기를 기다립니다.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * 카메라 상태 : 노출이 사전 캡처 상태가되기를 기다립니다.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * 카메라 상태 : 노출 상태가 사전 캡처 이외의 상태가되기를 기다리는 중입니다.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * 카메라 상태 : 사진이 촬영되었습니다.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Camera2 API에서 보장하는 최대 미리보기 너비
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     *
     * Camera2 API에서 보장하는 최대 미리보기 높이
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.e("onSurfaceTexture : ",width+", "+height);
            openCamera(width, height);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * 카메라 미리보기의 {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     *
     * {@link CameraDevice}가 상태를 변경하면 {@link CameraDevice.StateCallback}이 호출됩니다.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {


        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            //이 메소드는 카메라가 열릴 때 호출됩니다. 여기에서 카메라 미리보기를 시작합니다.
            Log.e("mStateCallback : ","onOpened, "+cameraDevice);
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    /**
     *
     * UI를 차단해서는 안되는 작업을 실행하기위한 추가 스레드입니다.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler}
     * 백그라운드에서 작업을 실행합니다.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    private File mFile;

    /**
     * {@link ImageReader}.대한 콜백 객체입니다. 스틸 이미지를 저장할 준비가되면 "onImageAvailable"이 호출됩니다.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * 현재 카메라 장치가 플래시를 지원하는지 여부
     */
    private boolean mFlashSupported;

    /**
     * 카메라 센서방향
     */
    private int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback}JPEG 캡처와 관련된 이벤트를 처리합니다.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {//Log.e("mCaptureCallback :","process입장");
            switch (mState) {
                case STATE_PREVIEW: {
                    //  Log.e("process:","STATE_PREVIEW입장");
                    //카메라 미리보기가 정상적으로 작동하면 아무 조치도 취하지 않습니다.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    //   Log.e("process:","STATE_WAITING_LOCK");
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    //Log.e("process:","STATE_WAITING_PRECAPTURE");
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    //.Log.e("process:","STATE_WAITING_NON_PRECAPTURE");
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            //   Log.e("onCaptureProgressed:",session +", "+ request +", "+ partialResult);
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            // Log.e("onCaptureCompleted :",session +", "+ request +", "+ result);
            process(result);
        }

    };

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 카메라가 지원하는 {@code Size} 중 {@code choices}를 감안할 때,
     * 최소한 각각의 텍스처 뷰 크기보다 크고 최대 크기와 최대 크기가 가장 작은 최소 크기를 선택하십시오.
     * 종횡비가 지정된 값과 일치합니다. 그런 크기라면 존재하지 않는 경우, 각각의 최대 크기만큼 크며 종횡비가 지정된 값과 일치하는 가장 큰 크기를 선택하십시오.
     *
     * @param choices             카메라가 의도 한 출력 등급을 지원하는 크기 목록
     * @param textureViewWidth  센서 좌표를 기준으로 한 텍스처 뷰의 너비
     * @param textureViewHeight 센서 좌표를 기준으로 한 텍스처 뷰의 높이
     * @param maxWidth          선택할 수있는 최대 너비
     * @param maxHeight         선택할 수있는 최대 높이
     * @param aspectRatio       종횡비
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {


        //Log.e("chooseOptimalSize :",maxWidth+", height :"+maxHeight);
        // Log.e("Size[] choices :", String.valueOf(choices));
        //최소한 미리보기 표면보다 큰 지원 해상도를 수집하십시오.
        List<Size> bigEnough = new ArrayList<>();
        // 미리보기 표면보다 작은 지원되는 해상도 수집
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        // Log.e("aspectRatioW,H :", w+", h: "+h);
        // Log.e("textureViewWH :", textureViewWidth+", H: "+ textureViewHeight);

        for (Size option : choices) {
            //  Log.e("option size :", option.getWidth()+", h : "+option.getHeight());
            // Log.e("option h / w :", String.valueOf(option.getWidth() * h / w));

            if  (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= maxWidth && option.getHeight() >= maxHeight) {

                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                    //  Log.e("bigEnough :", String.valueOf(bigEnough));
                } else {
                    notBigEnough.add(option);
                    //  Log.e("notBigEnough :", String.valueOf(notBigEnough));
                }
            }
        }


        //충분히 큰 것 중에서 가장 작은 것을 선택하십시오. 충분히 큰 사람이 없으면 충분히 큰 사람을 선택하십시오.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            // Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static CameraPreviewFragment newInstance() {
        return new CameraPreviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        //view.findViewById(R.id.picture).setOnClickListener(this);
        //   view.findViewById(R.id.info).setOnClickListener(this);
        // Log.e("onViewCreated :", "onViewCreated입장");
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null)
        {
            isZoonEnabled = savedInstanceState.getBoolean("zoom_enabled", false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        Log.d("카메라동작 확인 : ", "onResume");
        startBackgroundThread();

        // 이 뷰의 종횡비를 설정합니다. 뷰의 크기는 비율에 따라 측정됩니다.
        // 화면을 껐다가 다시 켤 때 SurfaceTexture는 이미 사용 가능하며 "onSurfaceTextureAvailable"은 호출되지 않습니다.
        // 이 경우 카메라를 열고 여기에서 미리보기를 시작할 수 있습니다 (그렇지 않으면 SurfaceTextureListener에서 표면이 준비 될 때까지 기다립니다).
        if (mTextureView.isAvailable()) {
             Log.e("isAvailable : ", "openCamera");
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
              Log.e("!isAvailable : ", "setSurfaceTextureListener");
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * 카메라관련 변수 설정
     *
     * @param width  카메라 프리뷰에 사용가능한 크기의 너비
     * @param height  카메라 프리뷰에 사용가능한 크기의 너비
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {



          Log.d("카메라동작 확인 : ", "setUpCameraOutputs");
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
              Log.d("cameraId 확인 : ", "시작");

            for(String cameraid : manager.getCameraIdList()){
                    Log.d("cameraId 확인 : ", "확인중");
                    Log.e("cameraId 확인 : ", cameraid);
                id = Integer.parseInt(cameraid);
            }


            for (String cameraId : manager.getCameraIdList()) {

                if(id < 2){
                    cameraId = String.valueOf(0);
                    //     Log.e("카메라아이디 :",cameraId);
                }
                else{
                    cameraId = String.valueOf(7);
                    //     Log.e("카메라아이디 :",cameraId);
                }


                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);
                //  Log.e("cameraId : ", String.valueOf(characteristics));

                //이 샘플에서는 전면 카메라를 사용하지 않습니다.
                //Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                //    continue;
                //}

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // 스틸 이미지 캡처에는 사용 가능한 최대 크기를 사용합니다.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // 센서 좌표를 기준으로 미리보기 크기를 얻기 위해 치수를 교체해야야하는지 알아본다.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //CameraCharacteristics.SENSOR_ORIENTATION 키값으로 얻는 카메라의 방향도 자주 이용하는 정보인데,
                // 이 정보를 이용해야 사진 촬영 시 사진의 방향을 정상으로 출력할 수 있습니다.
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                //   Log.e("displayRotation : ", String.valueOf(displayRotation));
                //  Log.e("mSensorOrientation : ", String.valueOf(mSensorOrientation));

                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        //     Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    //  Log.e("swappedDimensions: ", String.valueOf(swappedDimensions));
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    //    Log.e("MAX_PREVIEW_WIDTH : ", String.valueOf(MAX_PREVIEW_WIDTH));
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    Log.e("MAX_PREVIEW_HEIGHT : ", String.valueOf(MAX_PREVIEW_HEIGHT));
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                //
                //위험, W.R.! 미리보기 크기를 너무 크게 사용하려고하면 카메라 버스의 대역폭 제한을 초과하여 멋진
                // 미리보기가 발생하지만 가비지 캡처 데이터는 저장 될 수 있습니다.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);
                //  Log.e("mPreviewSize1 : ", rotatedPreviewWidth+", "+rotatedPreviewHeight+", "+maxPreviewWidth+", "+maxPreviewHeight);



                //TextureView의 종횡비를 선택한 미리보기 크기에 맞 춥니 다.
                int orientation = getResources().getConfiguration().orientation;
                //   Log.e("orientation : ", String.valueOf(orientation));
                //  Log.e("LANDSCAPE : ", String.valueOf(Configuration.ORIENTATION_LANDSCAPE));

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //    Log.e("orientation : ","==Configuration.ORIENTATION_LANDSCAPE");
                    //    Log.e("mPreviewSize2 : ",mPreviewSize.getWidth()+", "+ mPreviewSize.getHeight());
                    //미리보기 사이즈는 여기서 맞춰주면 된다
                    //가로 모드의 경우 Width와 Hight를 바꿔준다.
                    mTextureView.setAspectRatio(
                            720,480);
                } else {
                    //   Log.e("mPreviewSize : ",mPreviewSize.getWidth()+", "+ mPreviewSize.getHeight());
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                //   Log.e("mCameraId : ",mCameraId);
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            //  ErrorDialog.newInstance(getString(R.string.camera_error))
            //      .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Opens the camera specified by {@link CameraPreviewFragment#mCameraId}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera(int width, int height) {
        Log.d(TAG,"Open Camera");
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"No permission Camera");
            //   requestCameraPermission();
            return;
        }
        Log.d(TAG,"Open Camera1");
        setUpCameraOutputs(width, height);
        Log.d(TAG,"Open Camera2");
        configureTransform(width, height);
        Log.d(TAG,"Open Camera3");
        Activity activity = getActivity();

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG,e.getLocalizedMessage());
            //e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        Log.d("카메라동작 확인 : ", "startBackgroundThread");
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 필요한 {@link Matrix} 변환을`mTextureView`로 구성합니다.
     * 이 메소드는 setUpCameraOutputs에서 카메라 미리보기 크기가 결정되고
     * mTextureView의 크기가 고정 된 후에 호출되어야합니다.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect;
        //   Log.e("configureTransform : ", "rotation :"+rotation+", Matrix: "+matrix);
        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
            bufferRect = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {  // 90, 270
            bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            // Resize the distorted rectangle in viewRect back to the dimensions of the source (bufferRect).
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            // Scale the rectangle back so that it just covers the viewfinder.
            float viewLongEdge = viewWidth > viewHeight ? viewWidth : viewHeight;
            float viewShortEdge = viewWidth <= viewHeight ? viewWidth : viewHeight;
            float scale = Math.max(
                    (float) viewShortEdge / mPreviewSize.getHeight(),
                    (float) viewLongEdge / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            // Rotate the rectangle to the correct orientation.
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePicture() {
        lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void captureStillPicture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback captureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    showToast("Saved: " + mFile);
                    //  Log.d(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.info: {
//                Activity activity = getActivity();
//                if (null != activity) {
//                    new AlertDialog.Builder(activity)
//                            .setMessage("코드 체크바랍니다.")
//                            .setPositiveButton(android.R.string.ok, null)
//                            .show();
//                }
//                break;
//            }
//        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            }
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     public static class ConfirmationDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Fragment parent = getParentFragment();
    return new AlertDialog.Builder(getActivity())
    .setMessage(R.string.request_permission)
    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
    parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
    REQUEST_CAMERA_PERMISSION);
    }
    })
    .setNegativeButton(android.R.string.cancel,
    new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
    Activity activity = parent.getActivity();
    if (activity != null) {
    activity.finish();
    }
    }
    })
    .create();
    }
    }
     */

}