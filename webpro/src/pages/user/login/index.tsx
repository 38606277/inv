import {
  LockTwoTone,
  UserOutlined,
} from '@ant-design/icons';
import { Alert, message } from 'antd';
import React, { useState } from 'react';
import ProForm, { ProFormCheckbox, ProFormText } from '@ant-design/pro-form';
import { useIntl, Link, history, FormattedMessage, SelectLang, useModel } from 'umi';
import Footer from '@/components/Footer';
import { fakeAccountLogin, LoginParams, encodePwd } from '@/services/login';

import LocalStorge from '@/utils/LogcalStorge.jsx';

import styles from './index.less';

const localStorge = new LocalStorge();

const LoginMessage: React.FC<{
  content: string;
}> = ({ content }) => (
  <Alert
    style={{
      marginBottom: 24,
    }}
    message={content}
    type="error"
    showIcon
  />
);

/**
 * 此方法会跳转到 redirect 参数所在的位置
 */
const goto = () => {
  console.log('goto')
  if (!history) return;
  console.log('goto end')
  setTimeout(() => {
    const { query } = history.location;
    const { redirect } = query as { redirect: string };
    history.push({ pathname: redirect || '/' });
  }, 10);
};

const Login: React.FC<{}> = () => {
  const [submitting, setSubmitting] = useState(false);
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const { initialState, setInitialState } = useModel('@@initialState');

  const intl = useIntl();

  const fetchUserInfo = async () => {
    const userInfo = await initialState?.fetchUserInfo?.();
    if (typeof userInfo == 'undefined') {
      return;
    }

    const menuData = await initialState?.getMenuConfig?.();
    if (typeof menuData == 'undefined') {
      return;
    }
    console.log('fetchUserInfo', userInfo)
    if (userInfo) {
      setInitialState({
        ...initialState,
        currentUser: userInfo,
        menuData: menuData
      });
    }
  };

  const handleSubmit = async (values: LoginParams) => {
    setSubmitting(true);
    try {

      //加密密码
      let encodePwdResult = await encodePwd(values.Pwd)

      console.log('handleSubmit encodePwdResult', encodePwdResult)

      if (typeof encodePwdResult == 'undefined') {
        message.error('登录失败，请重试！');
      } else if (typeof encodePwdResult.resultCode == 'undefined') {
        values.encodePwd = encodePwdResult.encodePwd;
      } else if (encodePwdResult.resultCode == '1000') {
        values.encodePwd = encodePwdResult.data.encodePwd;
      }

      if (values.encodePwd) {
        values.Pwd = values.encodePwd;
        // 登录
        let loginResult = await fakeAccountLogin(values)

        if (loginResult.status == '0') {

          let userInfo = loginResult.data;
          if (userInfo) {
            userInfo.Pwd = values.encodePwd;
          }

          //保存用户信息
          localStorge.setStorage('userInfo', userInfo);
          await fetchUserInfo();
          goto();

        } else {
          // 如果失败去设置用户错误信息
          setUserLoginState(loginResult);
        }
      }

    } catch (error) {
      console.log(error);

      message.error('登录失败，请重试！');
    }
    setSubmitting(false);
  };
  const { LOGINRESULT } = userLoginState;

  return (
    <div className={styles.container}>
      <div className={styles.lang}>{SelectLang && <SelectLang />}</div>
      <div className={styles.content}>
        <div className={styles.top}>
          <div className={styles.header}>
            <Link to="/">
              <img alt="logo" className={styles.logo} src={require('../../../../public/logo.svg')} />
              <span className={styles.title}>仓库管理系统</span>
            </Link>
          </div>

        </div>

        <div className={styles.main}>
          <ProForm
            initialValues={{
              autoLogin: true,
            }}
            submitter={{
              searchConfig: {
                submitText: intl.formatMessage({
                  id: 'pages.login.submit',
                  defaultMessage: '登录',
                }),
              },
              render: (_, dom) => dom.pop(),
              submitButtonProps: {
                loading: submitting,
                size: 'large',
                style: {
                  width: '100%',
                },
              },
            }}
            onFinish={async (values) => {
              handleSubmit(values);
            }}
          >
            <div
              style={{
                marginBottom: 96,
              }}
            />

            {LOGINRESULT === 'InvalidUser' && (
              <LoginMessage
                content={intl.formatMessage({
                  id: 'pages.login.accountLogin.errorMessage',
                  defaultMessage: '账户或密码错误',
                })}
              />
            )}

            <ProFormText
              name="UserCode"
              fieldProps={{
                size: 'large',
                prefix: <UserOutlined className={styles.prefixIcon} />,
              }}
              placeholder={intl.formatMessage({
                id: 'pages.login.username.placeholder',
                defaultMessage: '请输入用户名!',
              })}
              rules={[
                {
                  required: true,
                  message: (
                    <FormattedMessage
                      id="pages.login.username.required"
                      defaultMessage="请输入用户名!"
                    />
                  ),
                },
              ]}
            />
            <ProFormText.Password
              name="Pwd"
              fieldProps={{
                size: 'large',
                prefix: <LockTwoTone className={styles.prefixIcon} />,
              }}
              placeholder={intl.formatMessage({
                id: 'pages.login.password.placeholder',
                defaultMessage: '请输入密码！',
              })}
              rules={[
                {
                  required: true,
                  message: (
                    <FormattedMessage
                      id="pages.login.password.required"
                      defaultMessage="请输入密码！"
                    />
                  ),
                },
              ]}
            />
            <div
              style={{
                marginBottom: 24,
              }}
            >
              <ProFormCheckbox noStyle name="autoLogin">
                <FormattedMessage id="pages.login.rememberMe" defaultMessage="自动登录" />
              </ProFormCheckbox>
              <a
                style={{
                  float: 'right',
                }}
              >
                <FormattedMessage id="pages.login.forgotPassword" defaultMessage="忘记密码" />
              </a>
            </div>
          </ProForm>

        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Login;
