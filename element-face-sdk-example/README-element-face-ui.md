![element](images/element.png "element")
# Element Face UI
The Element Face UI is a library for customization on the face capture screens. It requires to work with the [Element FM SDK](README.md).

## Installation
1. Import the AAR (`element-face-ui-[VERSION].aar`) in Android studio
1. Declare `element-face-ui` in `settings.gradle` in the project root
    ```
    include ':element-face-ui'
    ```
1. Add `element-face-ui` as a dependency `build.gradle` in the app module
    ```
    dependencies {
        implementation project(path: ':element-face-ui')
    }
    ```

## Face UI Style Guide
Element Face Detection UI components can be themed/styled based on business requirements. This document discusses some of the classes involved in configuring them.

### Notable Overrides
#### Timings
- `elementFaceUiGazeFlowerAnimDurMs` - Animation speed for first, _spinning flower_, part of gaze animation.
- `elementFaceUiGazeLoaderAnimDurMs` - Animation speed for second, _checkmark loader_, part of gaze animation.
- `elementFaceUiOutlineAnimDurMs` - Animation speed for Face Detection progress outline.

#### Colors
- `elementFaceUiCheckmarkButton` - Checkmark button, not the checkmark itself which is always white.
- `elementFaceUiFlowerPetal` - The fanning circles in first half of `CheckmarkLoaderView` animation
- `elementFaceUiRingColor` - Starting color of "ring" in `CheckmarkLoaderView`, end color is `elementFaceUiCheckmarkButton`
- `elementFaceUiOutlineNormalColor` - Default color of Face Detection outline.
- `elementFaceUiOutlineDetectedColor` - Active color of Face Detection outline.
- `elementFaceUiOutlineErrorColor` - Error color of Face Detection outline.
- `elementFaceUiStencilBgColor` - Color used as background of Face Detection screen.

#### Drawables
- `elementFaceUiOutlineCompletionMarker` - The icon that appears when Face Detection is complete.
