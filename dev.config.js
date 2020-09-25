const ScalaJS = require("./scalajs.webpack.config");
const Merge = require("webpack-merge");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');

const WebApp = Merge(ScalaJS, {
    mode: "development",
    output: {
        filename: "box-app.js",
        publicPath: "bundle/"
    },
    module: {
        rules: [
            {
                test: /\.css$/i,
                use: ['style-loader', 'css-loader'],
            },
            {
                test: /\.ttf$/,
                use: ['file-loader']
            }
        ]
    },
    plugins: [
        new HtmlWebpackPlugin(),
        new MonacoWebpackPlugin({
            publicPath: "bundle"
        })
    ]
});

module.exports = WebApp;