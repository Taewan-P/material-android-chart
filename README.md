# material-android-chart

Simple Material You(3) style Android line chart library. Inspired by Google Finance widget.

<p float="left">
  <img src="https://github.com/Taewan-P/material-android-chart/assets/27392567/2ad0e92e-cebc-4509-9282-2a721c9216b4" width="250" />
  <img src="https://github.com/Taewan-P/material-android-chart/assets/27392567/feb760ea-0573-489f-8de5-4d3d2e03a1e5" width="250" /> 
</p>

## Supported

- Material 3 style & color override
- Color customization for small section of line graph
- On touch label
- Guideline
- Dynamic Axis tick change

## Unsupported (Maybe in the future...)

- Various data types
- Other data in X axis
- Scroll or Zoom
- Pull our library from maven

## Test App

1. Clone our repo
2. Checkout to `main` branch
3. Build the app

## How to use 

We did not package our release to any remote repositories, so you have to manually add a module in your project.
Just add a submodule to your repo where the module should be by using:

```shell
git submodule add -b release https://github.com/Taewan-P/material-android-chart <your_module_folder_location>
```

The release branch is a subtree of the `main` branch's materialchart module folder, which only contains the data that you need for other projects.

Adding a submodule that tracks the release branch is all you need.
