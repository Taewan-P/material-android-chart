# material-android-chart

Simple Material You(3) style Android line chart library. Inspired by Google Finance widget.

<p float="left">
  <img src="https://github.com/Taewan-P/material-android-chart/assets/27392567/2ad0e92e-cebc-4509-9282-2a721c9216b4" width="250" />
  <img src="https://github.com/Taewan-P/material-android-chart/assets/27392567/feb760ea-0573-489f-8de5-4d3d2e03a1e5" width="250" />
</p>

## Supported

- Material 3 style & color override
- Color customization for small section of line graph
- On long-touch label
- Guideline
- Dynamic Axis tick change

## Unsupported (Maybe in the future...)

- Various data types
- Other data in X axis
- Scroll or Zoom

## Try our Sample App

1. Clone our repo
2. Checkout to `main` branch
3. Build the app

## How to use

### By using remote repository

In your app build gradle:

```kts
implementation("app.priceguard:materialchart:0.2.3")
```

Make sure that you have maven central added to the repositories.

```kts
repositories {
    mavenCentral()
}
```

### By using submodule

Just add a submodule to your repo where the module should be by using:

```shell
git submodule add -b lib-latest https://github.com/Taewan-P/material-android-chart <your_module_folder_location>
```

The lib-latest branch is a subtree of the `main` branch's materialchart module folder, which only contains the data that you need for other projects.

Adding a submodule that tracks the release branch is all you need.
