# Third-party notice: AndroidLiquidGlass backdrop

Source: https://github.com/Kyant0/AndroidLiquidGlass
Module: `backdrop`
Upstream commit used for the shader source: 65ab177e90e5c1d8c62e70cf7755841982da65f6
License: Apache License 2.0 (see `LICENSE-APACHE-2.0.txt` in this directory)
Copyright: Copyright 2025 Kyant

What Ivy uses:

- `Shaders.kt`: the rounded-rectangle refraction AGSL shader (`RoundedRectRefractionShaderString`).
  This file keeps the original Apache-2.0 copyright header.

What Ivy does NOT use:

- The upstream `GraphicsLayer` based capture/recording code. Ivy is on Compose 1.6.8, which does
  not expose `rememberGraphicsLayer`/`GraphicsLayer.record` (added in Compose 1.7). The capture
  layer in `LiquidGlassBackdrop.kt` is an independent implementation built on
  `android.graphics.RenderNode`, the same platform primitive the upstream code ends up using on
  Android. It is not copied from the upstream module.

Modifications: the vendored shader string is used unchanged, with the package renamed to
`com.ivy.legacy.ui.liquidglass`.
