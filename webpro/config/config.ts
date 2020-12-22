// https://umijs.org/config/
import { defineConfig } from 'umi';
import defaultSettings from './defaultSettings';
import proxy from './proxy';
import routes from './routes';

const { REACT_APP_ENV } = process.env;

export default defineConfig({
  //路由基础路径 http://192.168.0.1/#/XXX/user/login  XXX路径名称
  //base: process.env.NODE_ENV === 'production' ? '/antdProH/' : '/',
  //文件资源路径 http://192.168.50.211:8080/XXX/abd.js XXX路径名称

  publicPath: process.env.NODE_ENV === 'production' ? '/' : '/',
  history: {
    type: "hash"
  },
  hash: true,
  antd: {},
  dva: {
    hmr: true,
  },
  layout: {
    name: '仓库管理系统',
    locale: false,
    siderWidth: 208,
    ...defaultSettings,
  },
  locale: {
    // default zh-CN
    default: 'zh-CN',
    antd: true,
    // default true, when it is true, will use `navigator.language` overwrite default
    baseNavigator: true,
  },
  //按需加载
  dynamicImport: {
    //loading: '@ant-design/pro-layout/es/PageLoading',
    loading: '@/components/Loading',
  },
  targets: {
    ie: 11,
  },
  // umi routes: https://umijs.org/docs/routing
  routes,
  // Theme for antd: https://ant.design/docs/react/customize-theme-cn
  theme: {
    'primary-color': defaultSettings.primaryColor,
  },
  esbuild: {},
  title: false,
  //忽略Moment本地文件 开启由 560KB -》 152KB
  ignoreMomentLocale: true,
  proxy: proxy[REACT_APP_ENV || 'dev'],
  manifest: {
    basePath: '/',
  },
  // https://github.com/zthxxx/react-dev-inspector
  plugins: ['react-dev-inspector/plugins/umi/react-inspector'],
  inspectorConfig: {
    // loader options type and docs see below
    exclude: [],
    babelPlugins: [],
    babelOptions: {},
  },
  resolve: {
    includes: ['src/components'],
  },
  request: {
    dataField: 'data',
  },

  //headScripts: ['http://localhost:8097'],
  outputPath: '../srv/app/web',
  chunks: [
    // 'core_js',
    // 'rc',
    // 'react_widget',
    //'react',
    //   //'ant_icon',
    // 'ant_compatible',
    // 'antd_pro',
    // 'antd',
    //'vendor',
    'umi',
  ],
  chainWebpack: function (config, { webpack }) {
    //console.log('chainWebpack config :', config);
    config.merge({
      optimization: {
        splitChunks: {
          // chunks: 'async',
          // minSize: 30000,
          // minChunks: 3,
          // automaticNameDelimiter: '.',

          cacheGroups: {
            vendor: {
              test: /[\\/]node_modules[\\/]/,
              name(module) {
                // get the name. E.g. node_modules/packageName/not/this/part.js
                // or node_modules/packageName
                const packageName = module.context.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/)[1];

                // npm package names are URL-safe, but some servers don't like @ symbols
                return `npm.${packageName.replace('@', '')}`;
              },
            }
          },

          // cacheGroups: {

          //   styles: {
          //     name: 'styles',
          //     test: /\.(css|less)$/,
          //     chunks: 'async',
          //     minChunks: 1,
          //     minSize: 0,
          //   },

          //   //提取core-js
          //   core_js: {
          //     name: 'core_js',
          //     test: /_core-js/,
          //     priority: 11,
          //     chunks: 'async',
          //     enforce: true,
          //   },
          //   //提取rc控件
          //   // rc: {
          //   //   name: 'rc',
          //   //   test: /_rc-/,
          //   //   priority: 11,
          //   //   chunks: 'async',
          //   //   enforce: true,
          //   // },
          //   rc: {
          //     name: 'rc',
          //     test: /rc-select|rc-tree|rc-time-picker|rc-menu|rc-tabs|rc-table|rc-calendar|rc-trigger|rc-form/,
          //     chunks: 'async',
          //     priority: 13,
          //     enforce: true,
          //   },

          //   //提取react
          //   react_widget: {
          //     name: 'react_widget',
          //     test: /react-dnd/,
          //     chunks: 'async',
          //     priority: 11,
          //     enforce: true,
          //   },
          //   //提取react
          //   react: {
          //     name: 'react',
          //     test: /react|react-dom|react-router|react-router-dom|react-router-config/,
          //     chunks: 'async',
          //     priority: 11,
          //     enforce: true,
          //   },
          //   //提取antd-design_compatible
          //   ant_icon: {
          //     name: 'ant_icon',
          //     test: /ant-design_icons/,
          //     priority: 11,
          //     chunks: 'async',
          //     enforce: true,
          //   },
          //   //提取antd-design_compatible
          //   ant_compatible: {
          //     name: 'ant_compatible',
          //     test: /ant-design_compatible/,
          //     priority: 11,
          //     chunks: 'async',
          //     enforce: true,
          //   },
          //   //提取antd_pro
          //   antd_pro: {
          //     name: 'antd_pro',
          //     test: /pro-table|pro-filed|pro-form|pro-descriptions|pro-cil|pro-skelenton/,
          //     priority: 11,
          //     chunks: 'async',
          //     enforce: true,
          //   },

          //   antd_login: {
          //     name: 'antd_login',
          //     test: /pro-utils|pro-provider|pro-layout/,
          //     priority: 12,
          //     chunks: 'async',
          //     enforce: true,
          //   },

          //   antd_test: {
          //     name: 'antd_test',
          //     test: /antd\/es\/layout/,
          //     priority: 12,
          //     chunks: 'async',
          //     enforce: true,
          //   },


          //   //提取antd es
          //   antd_es: {
          //     name: 'antd_es',
          //     test: /antd\/es/,
          //     priority: 10,
          //     chunks: 'async',
          //     enforce: true,
          //   },

          //   //提取antd lib
          //   antd_lib: {
          //     name: 'antd_lib',
          //     test: /antd\/lib/,
          //     priority: 10,
          //     chunks: 'async',
          //     enforce: true,
          //   },

          //   //剩余的都打包值vendor
          //   vendor: {
          //     name: 'vendor',
          //     test: /[\\/]node_modules[\\/]/,
          //     chunks: 'all',
          //     priority: 9,
          //     enforce: true,
          //   }
          // },
        },
      }
    });
  }

});
