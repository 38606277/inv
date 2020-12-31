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

  //publicPath: process.env.NODE_ENV === 'production' ? '/' : '/',
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
  // chunks: [
  //   // 'core_js',
  //   // 'rc',
  //   // 'react_widget',
  //   //'react',
  //   //   //'ant_icon',
  //   // 'ant_compatible',
  //   // 'antd_pro',
  //   // 'antd',
  //   //'rc_base',
  //   //'vendor',
  //   'umi',
  // ],
  // chainWebpack: function (config, { webpack }) {
  //   //console.log('chainWebpack config :', config);
  //   config.merge({
  //     optimization: {
  //       splitChunks: {
  //         chunks: 'async',
  //         // chunks(chunk) {
  //         //     // exclude `my-excluded-chunk`
  //         //     return chunk.name !== 'my-excluded-chunk';
  //         // },
  //         minSize: 1024, //生成块的最小大小（以字节为单位）1024字节=1KB。
  //         minChunks: 1,   //拆分前必须共享模块的最小块数。
  //         maxInitialRequests: 30, //入口点的最大并行请求数。

  //         // cacheGroups: {
  //         //   vendor: {
  //         //     test: /[\\/]node_modules[\\/]/,
  //         //     name(module:any) {
  //         //       // get the name. E.g. node_modules/packageName/not/this/part.js
  //         //       // or node_modules/packageName
  //         //       const packageName = module.context.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/)[1];

  //         //       // npm package names are URL-safe, but some servers don't like @ symbols
  //         //       return `npm.${packageName.replace('@', '')}`;
  //         //     }
  //         //   }
  //         // },

  //         cacheGroups: {
  //           login_icon: {
  //             name: 'login_icon',
  //             test({ resource }: any) {
  //               if (!resource) return false;
  //               let test1 = /ant-design[\\/]icons-svg[\\/](es|lib)[\\/]asn[\\/](WarningFilled|ArrowRightOutlined|ArrowLeftOutlined|PlusOutlined|MenuFoldOutlined|MenuUnfoldOutlined)/.test(resource);
  //               let test2 = /ant-design[\\/]icons[\\/](es|lib)[\\/]icons[\\/](WarningFilled|ArrowRightOutlined|ArrowLeftOutlined|PlusOutlined|MenuFoldOutlined|MenuUnfoldOutlined)/.test(resource);
  //               let test3 = /ant-design[\\/]icons[\\/](es|lib)[\\/]components/.test(resource);
  //               let test4 = /ant-design[\\/]icons[\\/](WarningFilled|ArrowRightOutlined|ArrowLeftOutlined|PlusOutlined|MenuFoldOutlined|MenuUnfoldOutlined)/.test(resource);
  //               return test1 || test2 || test3 || test4;
  //             },
  //             priority: 19,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           login_antd_pro: {
  //             name: 'login_antd_pro',
  //             test: /pro-utils|pro-provider|pro-layout/,
  //             priority: 19,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           login_lodash: {
  //             name: 'login__lodash',
  //             test: /_lodash.isequal/,
  //             priority: 19,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           login_antd: {
  //             name: 'login_antd',
  //             test: /antd[\\/]es[\\/](tabs|result|breadcrumb|drawer|page-header|affix|_util[\\/](transButton|throttleByAnimationFrame))/,
  //             priority: 19,
  //             chunks: 'async',
  //             enforce: true,
  //           },


  //           // styles: {
  //           //   name: 'styles',
  //           //   test: /\.(css|less)$/,
  //           //   chunks: 'async',
  //           //   minChunks: 1,
  //           //   minSize: 0,
  //           // },

  //           //提取core-js
  //           core_js_lib: {
  //             name: 'core_js_lib',
  //             test: /core-js[\\/]library/,
  //             priority: 12,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           // core_js: {
  //           //   name: 'core_js',
  //           //   test: /_core-js/,
  //           //   priority: 10,
  //           //   chunks: 'all',
  //           //   enforce: true,
  //           // },

  //           //剥离 rc aysnc模块
  //           rc: {
  //             test: /_rc/,
  //             name(module: any) {
  //               // get the name. E.g. node_modules/packageName/not/this/part.js
  //               // or node_modules/packageName
  //               const packageName = module.context.match(/_rc-[a-z]*/);
  //               // npm package names are URL-safe, but some servers don't like @ symbols
  //               //console.log('packageName', packageName)
  //               return packageName;
  //             },
  //             priority: 12,
  //             enforce: true,
  //             chunks: 'async',
  //           },

  //           // rc_select: {
  //           //   name(module: any) {
  //           //     console.log('rc_select', module.resource);
  //           //     return 'rc_select'
  //           //   },
  //           //   test: /rc-select/,
  //           //   chunks: 'initial',
  //           //   priority: 20,
  //           //   enforce: true,
  //           // },

  //           // //提取react
  //           // react_widget: {
  //           //   name: 'react_widget',
  //           //   test: /react-dnd/,
  //           //   chunks: 'async',
  //           //   priority: 11,
  //           //   enforce: true,
  //           // },

  //           // //提取react
  //           // react: {
  //           //   name: 'react',
  //           //   test: /react|react-dom|react-router|react-router-dom|react-router-config/,
  //           //   chunks: 'async',
  //           //   priority: 11,
  //           //   enforce: true,
  //           // },

  //           //react 剥离
  //           _react: {
  //             name(module: any) {
  //               // get the name. E.g. node_modules/packageName/not/this/part.js
  //               // or node_modules/packageName
  //               const packageName = module.context.match(/_react-([a-z]|-)*/);
  //               // npm package names are URL-safe, but some servers don't like @ symbols
  //               //console.log('packageName', packageName)
  //               return packageName;
  //             },
  //             test: /_react-/,
  //             chunks: 'async',
  //             priority: 11,
  //             enforce: true,
  //           },


  //           //提取ant_icon_svg
  //           ant_icon_svg: {
  //             name: 'ant_icon_svg',
  //             //打包成单独文件 
  //             // name(module:any) {
  //             //   const packageName = module.resource.match(/[a-z|A-Z]*.js/);
  //             //   if (!packageName) return 'ant_icon_svg';
  //             //   return packageName;
  //             // },
  //             test: /ant-design_icons-svg/,
  //             priority: 12,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           //提取ant_icon
  //           ant_icon: {
  //             name: 'ant_icon',
  //             //打包成单独文件 
  //             // name(module: any) {
  //             //   const packageName = module.resource.match(/[a-z|A-Z]*.js/);
  //             //   if (!packageName) return 'ant_icon';
  //             //   return packageName;
  //             // },
  //             test: /ant-design_icons/,
  //             priority: 11,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           //提取antd_pro
  //           antd_pro: {
  //             name: 'antd_pro',
  //             test: /pro-table|pro-field|pro-form|pro-descriptions|pro-cil|pro-skeleton/,
  //             priority: 11,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           //lodash.XXXX
  //           lodashD: {
  //             name: 'lodashD',
  //             test: /_lodash./,
  //             priority: 11,
  //             chunks: 'async',
  //             enforce: true,
  //           },
  //           //lodash.@4.17.20
  //           lodashAt: {
  //             name: 'lodashAT',
  //             test: /_lodash@/,
  //             priority: 11,
  //             chunks: 'async',
  //             enforce: true,
  //           },


  //           //提取antd-design_compatible
  //           ant_compatible: {
  //             name: 'ant_compatible',
  //             test: /ant-design_compatible/,
  //             priority: 11,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           //提取antd es
  //           antd_es: {
  //             name: 'antd_es',
  //             test: /antd[\\/]es/,
  //             priority: 11,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           //提取antd lib
  //           antd_lib: {
  //             name: 'antd_lib',
  //             test: /antd[\\/]lib/,
  //             priority: 11,
  //             chunks: 'async',
  //             enforce: true,
  //           },

  //           //剥离umijs
  //           umijs: {
  //             test: /umijs_/,
  //             name(module: any) {
  //               // get the name. E.g. node_modules/packageName/not/this/part.js
  //               // or node_modules/packageName
  //               const packageName = module.context.match(/umijs_([a-z]|-)*/);
  //               // npm package names are URL-safe, but some servers don't like @ symbols
  //               //console.log('packageName', packageName)
  //               return packageName;
  //             },
  //             priority: 11,
  //             enforce: true,
  //             chunks: 'async',
  //           },

  //           //_babel-runtime
  //           babel_runtime: {
  //             name: 'babel_runtime',
  //             test: /_babel-runtime/,
  //             priority: 11,
  //             enforce: true,
  //             chunks: 'async',
  //           },

  //           hash_js: {
  //             name: 'hash_js',
  //             test: /_hash.js/,
  //             priority: 11,
  //             enforce: true,
  //             chunks: 'async',
  //           },


  //           //剩余的都打包值vendor
  //           vendor: {
  //             name: 'vendor',
  //             test: /[\\/]node_modules[\\/]/,
  //             chunks: 'async',
  //             priority: 9,
  //             enforce: true,
  //           }
  //         },
  //       },
  //     }
  //   });
  // }

});
