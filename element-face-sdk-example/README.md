![element](images/element.png "element")
# Element Face SDK
The Element Face SDK is an API library to create the biometrics models that can be used to authenticate users. This document contains information to integrate the Element Face SDK into an Android application by using Android Studio.

## Version Support
### Android SDK & Android Studio
- The Element FM SDK requires Android 5.0+ / API 21+ (Lollipop and up)
- Android Target SDK Version 28, Build Tool Version 28.0.3, and AndroidX
- Android Studio 3.2.0 with Gradle Wrapper 4.6

### Dependencies
- AndroidX WorkManager: 2.0.1
- AndroidX Kotlin extensions: 1.0.1
- Google Play Service Location: 17.0.0
- Google Material Design: 1.0.0
- Google Guava for Android: 27.0.1-android
- Amazon AWS Mobile SDK: 2.8.5

## Prerequisites
### Element Dashboard
The Element Dashboard is the gateway to the assets in order to use the Element Face SDK. The URL of the Element Dashboard varies based on your region. Also an account is required to access the Element Dashboard. Please contact [Element](README.md#questions) for more information.

### AAR
The Element Face SDK is in the [AAR](https://developer.android.com/studio/projects/android-library) format. Download the AAR:
1. Log into the Element Dashboard with your account.
1. Select `Account` tab in the left navigation bar.
1. Find the menu item named `SDK`, click the `View` button next to it.
![dashboard-account](images/dashboard-account.jpg "dashboard-account")
1. Under `SDK Files` section, click the SDK download link and save it to the desktop of your computer.
![dashboard-sdk-files](images/dashboard-sdk-files.jpg "dashboard-sdk-files")

### Register the Application Id (App Id) to Element and obtain the Encrypted Access Key (EAK)
The Element Face SDK requires the *Encrypted Access Key* (*EAK*) file. The *EAK* file carries encrypted information including the [Application Id (App Id)](https://developer.android.com/studio/build/application-id) of your Android app. Your registered *EAK* is available on the Element Dashboard, under `Account -> SDK`. Here is how you get a new *EAK* file:
1. On the same page of where you download the SDK file, fill in the `App Id` field with your `application id`. You can find your `application id` in your module-level `build.gradle` file. Leave other fields unchanged and click `Create EAK`.
1. You new EAK will be listed on the page. Hover your mouse on the EAK you want to download and a little download icon will appear next to your `app id`. Click it, name the file `element.eak` and save it to the desktop of your computer.

![dashboard-create-eak](images/dashboard-create-eak.jpg "create-eak")

## Setup with Android Studio
### Import the AAR
1. Open your project in Android Studio.
1. On the top menu bar, click `File -> New -> New Module`. In the `Create New Module` window, click `Import .JAR/.AAR Package`, then click `Next`.
1. In the next window, click the `...` next to `File name` field and select the AAR (`element-face-core-[VERSION].aar`) file in your computer's desktop directory. Then type in `element-face-sdk` in the `Subproject Name` field.
1. Click the `Finish` button and wait for Android Studio to finish building the project.

### Add element-face-sdk and dependencies
1. Add `element-face-sdk` module to your project by adding the following code to the `settings.gradle` file in the root directory of your project:
    ```
      include ':element-face-sdk'
    ```
1. On the top menu bar, click `File -> Project Structure`.
1. Select your app module under `Modules` on the left pane, click on the `Dependencies` tab, and click on the `+` button at the bottom of the window. In the popup, click `Module Dependency` and select `:element-face-sdk`. Click `Ok`.
1. Add the following dependencies to the module-level `build.gradle`:
    ```
        dependencies {
            .....
            implementation 'androidx.work:work-runtime:2.0.1'
            implementation "androidx.core:core-ktx:1.0.1"
            implementation 'com.amazonaws:aws-android-sdk-core:2.8.5'
            implementation 'com.amazonaws:aws-android-sdk-s3:2.8.5'
            implementation 'com.google.android.gms:play-services-location:17.0.0'
            implementation 'com.google.android.material:material:1.0.0'
            implementation 'com.google.guava:guava:27.0.1-android'
        }
    ```
    Note that you might have already declared some of these dependencies in your module-level `build.gradle` file, so please make sure you did not declare them twice. And you might also have to tweak a little bit on the versions of the dependencies as well as `compileSdkVersion` and `targetSdkVersion` in the `build.gradle`. Please follow the Android Studio's prompts on this. More information can be found [here](https://developer.android.com/studio/build/#module-level).
1. In gradle.properties, add the following lines to enable AndroidX support:
    ```
        android.useAndroidX=true
        android.enableJetifier=true
    ```
1. Wait for the Android Studio to sync.

### Include the EAK in the application
1. Create a resources directory at `[project dir]/app/src/main/resources`.
1. Copy the `element.eak` file into the `resources` directory.
![resources](images/resources.jpg "resources")

## Using the Element Face SDK APIs
### Initialize the Element Face SDK
1. Create a class which extends [android.app.Application](https://developer.android.com/reference/android/app/Application) if you haven't, and initialize the Element Face SDK in `onCreate()` method:
    ```
        public class MainApplication extends Application {
          @Override
          public void onCreate() {
            super.onCreate();
            ElementFaceSDK.initSDK(this);
          }
        }
    ```
1. Declare the `MainApplication` class in AndroidManifest.xml:
    ```
        <manifest>
          .....
          <application android:name=".MainApplication">
            .....
          </application>
        </manifest>
    ```

### Ask for user permissions
1. The Element Face SDK requires the following permissions:
    - `android.Manifest.permission.CAMERA`
    - `android.Manifest.permission.ACCESS_FINE_LOCATION`
    - `android.Manifest.permission.ACCESS_COARSE_LOCATION`
    Those permissions are declared in the Element Face SDK AAR, so no need to declare them again in the manifest in your app.
1. The Element Face SDK provides `PermissionUtils.verifyPermissions(Activity activity, String... permissionsToVerified)` for requesting the permissions:
    ```
        PermissionUtils.verifyPermissions(
          MainActivity.this,
          Manifest.permission.CAMERA,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION);
    ```
    For the Android Marshmallow 6.0 (API 23) OS and up, make sure the permissions are granted before starting any Activity provided by the Element Face SDK.

### User enrollment
The Element Face SDK utilizes the `ElementFaceEnrollActivity` for user enrollment. It's based on the [`startActivityForResult`](https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)) method.
1. Enroll a user and obtain the `UserInfo`. The `UserInfo` contains a unique `userId` (ElementId). The pair of the userId and the appId (`context.getPackageName()`) is mainly used in the Element Face SDK to inquire the user's information and status. In the `Activity` where you want to start the enrollment process:
    ```
        UserInfo userInfo = UserInfo.enrollUser(
          getBaseContext(),
          getPackageName(),
          firstName,
          lastName,
          new HashMap<String, String>());
    ```
1. Declare a request code:
    ```
        public static final int ENROLL_REQ_CODE = 12800;
    ```
1. Start the `ElementFaceEnrollActivity`:
    ```
        Intent intent = new Intent(this, ElementFaceEnrollActivity.class);
        intent.putExtra(ElementFaceEnrollActivity.EXTRA_ELEMENT_USER_ID, userInfo.userId);
        startActivityForResult(intent, ENROLL_REQ_CODE);
    ```
1. Override the [`onActivityResult`](https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)) method to receive the enrollment results:
    ```
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          if (requestCode == ENROLL_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
              // User enrolled successfully
            } else {
              // Enrollment cancelled
            }
          }
        }
    ```
1. Declare the `ElementFaceEnrollActivity` in AndroidManifest.xml:
    ```
        <manifest>
          .....
          <application android:name=".MainApplication">
            .....
            <activity android:name="com.element.camera.ElementFaceEnrollActivity"
              android:theme="@style/CamTheme.Full.Light"
              android:clearTaskOnLaunch="true"          
              android:hardwareAccelerated="true" />
            .....
          </application>
        </manifest>
    ```

### User authentication
User authentication is similar to user enrollment, using `ElementFaceAuthActivity`.
1. Declare the request code:
    ```
        public static final int AUTH_REQ_CODE = 12801;
    ```
1. Start the `ElementFaceAuthActivity`:
    ```
        Intent intent = new Intent(this, ElementFaceAuthActivity.class);
        intent.putExtra(ElementFaceAuthActivity.EXTRA_ELEMENT_USER_ID, userInfo.userId);
        startActivityForResult(intent, AUTH_REQ_CODE);
    ```
1. Override the [`onActivityResult`](https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)) method to receive the authentication results:
    ```
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          if (requestCode == AUTH_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
              String results = data.getStringExtra(ElementFaceAuthActivity.EXTRA_AUTH_RESULTS);
              if (ElementFaceAuthActivity.USER_VERIFIED.equals(results)) {
                  // The user is verified
              } else if (ElementFaceAuthActivity.USER_FAKE.equals(results)) {
                  // the user was spoofing
              } else {
                  // The user is not verified
              }
            } else {
              // Verification cancelled
            }
          }
        }
    ```
1. Declare the `ElementFaceAuthActivity` in AndroidManifest.xml:
    ```
        <manifest>
          .....
          <application android:name=".MainApplication">
            .....
            <activity android:name="com.element.camera.ElementFaceEnrollActivity"             
              android:theme="@style/CamTheme.Full.Light"
              android:clearTaskOnLaunch="true"          
              android:hardwareAccelerated="true" />
            <activity android:name="com.element.camera.ElementFaceAuthActivity"   
              android:theme="@style/CamTheme.Full.Light"          
              android:clearTaskOnLaunch="true"          
              android:hardwareAccelerated="true" />
            .....
          </application>
        </manifest>
    ```

### Activity configuration
The Element Face SDK (`element-face-core-[VERSION].aar`) is compatible with the Element Face UI (`element-face-ui-[VERSION].aar`) to enhance UI / UX. Please refer to [README-element-face-ui.md](README-element-face-ui.md) for more details.

There are a few options to configure the `ElementFaceEnrollActivity` and the `ElementFaceAuthActivity` by passing the extras in the intent.
- `EXTRA_LIVENESS_DETECTION` - Enable face liveness detection. The default value is `false`.
- `EXTRA_TUTORIAL` - Display tutorials to guide the user step-by-step. Only available when the Element Face UI is included in the app. The default value is `false`.

### User enquiries
The Element Face SDK provides a few ways to query users with ProviderUtil.
- List alls users
    ```
    public static List<UserInfo> getUsers(@NonNull Context context, @NonNull String appId, String selection)
    ```
- Get an user
    ```
    public static UserInfo getUser(@NonNull Context context, @NonNull String appId, @NonNull String userId)
    ```
- Delete users
    ```
    public static boolean deleteUser(@NonNull Context context, @NonNull String appId, @NonNull String userId)
    public static void deleteAllUsers(@NonNull Context context, @NonNull String appId)
    ```
- Update an user
    ```
    public static int updateUserInfo(@NonNull Context context, @NonNull UserInfo userInfo)
    public static void insertUserInfo(@NonNull Context context, @NonNull UserInfo userInfo)
    ```

The FaceModelMeta can be used to get the user enroll status.
- Find out if an user is enrolled
    ```
    public static boolean isEnrolled(String userId)
    ```

### Questions?
If you have questions, please contact devsupport@discoverelement.com.
