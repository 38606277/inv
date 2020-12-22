import React from 'react';
import { Settings as LayoutSettings, PageLoading } from '@ant-design/pro-layout';
import { notification } from 'antd';
import { history, RequestConfig, RunTimeLayoutConfig } from 'umi';
import RightContent from '@/components/RightContent';
import Footer from '@/components/Footer';
import { ResponseError } from 'umi-request';
import { queryMenu } from './services/user';
import { fakeAccountLogin } from './services/login';
import defaultSettings from '../config/defaultSettings';
import { MenuDataItem } from '@umijs/route-utils';
import LocalStorge from '@/utils/LogcalStorge.jsx';

const localStorge = new LocalStorge();

//菜单图标
import {
  HomeOutlined,
  TableOutlined,
  SettingOutlined,
  ProfileOutlined,
  BarChartOutlined,
  DollarOutlined,
  FileSearchOutlined,
  UserOutlined,
  TeamOutlined,
  KeyOutlined,
  RedEnvelopeOutlined,
  DatabaseOutlined,
  FileImageOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';

const IconMap = {
  home: <HomeOutlined />,
  table: <TableOutlined />,
  setting: <SettingOutlined />,
  profile: <ProfileOutlined />,
  barChart: <BarChartOutlined />,
  dollar: <DollarOutlined />,
  fileSearch: <FileSearchOutlined />,
  user: <UserOutlined />,
  team: <TeamOutlined />,
  key: <KeyOutlined />,
  redEnvelope: <RedEnvelopeOutlined />,
  database: <DatabaseOutlined />,
  fileImage: <FileImageOutlined />,
  unorderedList: <UnorderedListOutlined />,
};

/**
 * 获取用户信息比较慢的时候会展示一个 loading
 */
export const initialStateConfig = {
  loading: <PageLoading />,
};

/**
 * 自定义菜单 https://beta-pro.ant.design/docs/advanced-menu-cn
 */

export async function getInitialState(): Promise<{
  settings?: LayoutSettings;
  currentUser?: API.LoginInfoResult;
  menuData?: MenuDataItem[];
  fetchUserInfo?: () => Promise<API.LoginInfoResult | undefined>;
  getMenuConfig?: () => Promise<MenuDataItem[]>;
}> {
  //console.log('getInitialState', history.location.pathname)
  //获取当前用户信息
  const fetchUserInfo = async () => {
    console.log('fetchUserInfo')
    try {
      // let loginRequestParams = {
      //   UserCode: '',
      //   Pwd: '',
      //   import: "",
      //   isAdmin: ""
      // }
      let userInfo = localStorge.getStorage('userInfo');
      if (userInfo == '') {
        return undefined;
      } else {
        return userInfo;
      }

      // loginRequestParams.UserCode = userInfo.userCode;
      // loginRequestParams.Pwd = userInfo.Pwd;
      // const LoginInfoResult = await fakeAccountLogin(loginRequestParams);
      // let loginInfo = LoginInfoResult.data;  
      //return loginInfo;

    } catch (error) {
      history.push({
        pathname: '/user/login',
      });
    }
    return undefined;
  };

  //获取菜单配置
  const getMenuConfig = async () => {
    console.log('getMenuConfig')
    try {
      let userInfo = localStorge.getStorage('userInfo');
      if (userInfo == '') {
        return [];
      }
      const menuConfig = await queryMenu(userInfo.id);
      console.log('菜单数据：', menuConfig)
      return menuConfig.data;
    } catch (error) {
      history.push({
        pathname: '/user/login',
      });
    }
    return undefined;
  };

  // 如果是登录页面，不执行
  if (history.location.pathname !== '/user/login') {
    const currentUser = await fetchUserInfo();
    const menuData = await getMenuConfig();
    return {
      fetchUserInfo,
      getMenuConfig,
      currentUser,
      menuData: menuData,
      settings: defaultSettings,
    };
  }
  return {
    fetchUserInfo,
    getMenuConfig,
    menuData: [],
    settings: defaultSettings,
  };
}
/**
 * 转换图标
 * @param menus 
 */
const loopMenuItem = (menus: MenuDataItem[]): MenuDataItem[] =>
  menus.map(({ icon, children, ...item }) => {
    let newIconName = '';
    let arr = icon.split("-");

    for (let i in arr) {
      let iconItem = arr[i];
      if (i != '0') {
        newIconName += iconItem.replace(iconItem[0], iconItem[0].toUpperCase());
      } else {
        newIconName += iconItem;
      }
    }

    //    console.log('newIconName', newIconName)
    return ({
      ...item,
      icon: IconMap[newIconName],
      children: children && loopMenuItem(children),
    })
  });


export const layout: RunTimeLayoutConfig = ({ initialState }) => {

  return {
    rightContentRender: () => <RightContent />,
    disableContentMargin: false,
    footerRender: () => <Footer />,
    onPageChange: () => {
      const { location } = history;

      // 如果没有登录，重定向到 login
      if (!initialState?.currentUser && location.pathname !== '/user/login') {
        history.push({
          pathname: '/user/login',
        });
      }
    },
    menuDataRender: (menuData: MenuDataItem[]) => {
      if (initialState?.menuData) {
        //console.log('initialState?.menuData', initialState?.menuData)

        menuData = initialState?.menuData
        if (menuData.length == 0) {
          return menuData;
        }

        return loopMenuItem(menuData)
      } else {
        return menuData
      }
    },
    menuHeaderRender: undefined,
    // 自定义 403 页面
    // unAccessible: <div>unAccessible</div>,
    ...initialState?.settings,
  };
};

const codeMessage = {
  200: '服务器成功返回请求的数据。',
  201: '新建或修改数据成功。',
  202: '一个请求已经进入后台排队（异步任务）。',
  204: '删除数据成功。',
  400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
  401: '用户没有权限（令牌、用户名、密码错误）。',
  403: '用户得到授权，但是访问是被禁止的。',
  404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
  405: '请求方法不被允许。',
  406: '请求的格式不可得。',
  410: '请求的资源被永久删除，且不会再得到的。',
  422: '当创建一个对象时，发生一个验证错误。',
  500: '服务器发生错误，请检查服务器。',
  502: '网关错误。',
  503: '服务不可用，服务器暂时过载或维护。',
  504: '网关超时。',
};

/**
 * 异常处理程序
 */
const errorHandler = (error: ResponseError) => {
  const { response } = error;

  // console.log('error.type', error.type);

  if (error.name === 'BizError') {
    notification.error({
      description: error?.message || '系统异常',
      message: '网络请求失败',
    });
    return error.data;
  }

  if (response && response.status) {
    const errorText = codeMessage[response.status] || response.statusText;
    const { status, url } = response;

    notification.error({
      message: `请求错误 ${status}: ${url}`,
      description: errorText,
    });
  }

  if (!response) {
    notification.error({
      description: '您的网络发生异常，无法连接服务器',
      message: '网络异常',
    });
  }

};

export const request: RequestConfig = {
  errorHandler,
  // requestInterceptors: [
  //   (url, options) => {
  //     return {
  //       url: url,
  //       options: {
  //         ...options,
  //       },
  //     };
  //   }

  // ],
  errorConfig: {
    adaptor: (resData) => {
      let isOk = resData?.resultCode == '1000' || resData?.status === 0;

      // console.log('resData', resData);
      // console.log('resData?.resultCode == 1000', resData?.resultCode == '1000');
      // console.log('resData?.status === 0', resData?.status === 0);

      if (typeof resData?.resultCode == 'undefined' && typeof resData?.status == 'undefined') {
        isOk = true;
      }

      //console.log('isOk', isOk)

      return {
        ...resData,
        success: isOk,
        errorMessage: resData.message,
      };
    },
  },

};
