declare namespace API {
  export interface CurrentUser {
    avatar?: string;
    name?: string;
    title?: string;
    group?: string;
    signature?: string;
    tags?: {
      key: string;
      label: string;
    }[];
    userid?: string;
    access?: 'user' | 'guest' | 'admin';
    unreadCount?: number;
  }

  export interface LoginStateType {
    status?: 'ok' | 'error';
    type?: string;
  }

  export interface NoticeIconData {
    id: string;
    key: string;
    avatar: string;
    title: string;
    datetime: string;
    type: string;
    read?: boolean;
    description: string;
    clickClose?: boolean;
    extra: any;
    status: string;
  }

  export interface LoginInfoResult {
    LOGINRESULT?: string,
    import?: string,
    icon?: string
    id?: number,
    isAdmin?: number,
    pwd?: string
    userId?: string,
    userCode?: string,
    UserCode?: string,
    Pwd?: string

    avatar?: string;
    name?: string;
    title?: string;
    group?: string;
    signature?: string;
    tags?: {
      key: string;
      label: string;
    }[];
    userid?: string;
    unreadCount?: number;
  }

  export interface LoginResult {
    status?: number
    msg?: string,
    data?: LoginInfoResult,
    LOGINRESULT?: string
  }

  export interface EncodePwdResult {
    encodePwd?: stirng
  }


}
