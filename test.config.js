const HtmlWebpackPlugin = require("html-webpack-plugin");
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');



const WebApp = {
    output: {
        filename: "client-test-fastopt-bundle.js"
    },
    entry: {
        main: 'client-test-fastopt-loader.js'
    },
    mode: "development",
    module: {
        rules: [
            {
                test: /\.css$/i,
                use: ['style-loader', 'css-loader'],
            },
            {
                test: /\.(woff|woff2|eot|ttf|otf|svg)$/,
                use: ['file-loader']
            }
        ]
    },
    plugins: [
        // new HtmlWebpackPlugin({
        //     'templateContent': ({htmlWebpackPlugin}) => `
        //     <html>
        //       <head>
        //
        //       </head>
        //       <body>
        //         <script src="fixQueryCommandSupported.js"></script>
        //       </body>
        //     </html>
        //   `
        // }),
        // new MonacoWebpackPlugin({
        //     publicPath: "bundle"
        // })
    ]
};

module.exports = WebApp;