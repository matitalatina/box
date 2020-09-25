const ScalaJS = require("./scalajs.webpack.config");
const Merge = require("webpack-merge");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');

const WebApp = Merge(ScalaJS, {
    mode: "production",
    output: {
        filename: "box-app.js",
        publicPath: "bundle/"
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ["style-loader", MiniCssExtractPlugin.loader, "css-loader"]
            },
            {
                test: /\.ttf$/,
                use: ['file-loader']
            }
        ]
    },
    plugins: [
        new HtmlWebpackPlugin(),
        new MiniCssExtractPlugin({}),
        new MonacoWebpackPlugin({
            publicPath: "bundle"
        })
    ]
});

module.exports = WebApp;