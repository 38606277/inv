export default [
  {
    name: 'loginTest',
    layout: false,
    path: '/user/loginTest',
    component: './login',
    exact: true
  },
  {
    name: 'login',
    layout: false,
    path: '/user/login',
    component: './user/login',
    exact: true
  },
  {
    name: 'user',
    path: '/user',
    routes: [
      {
        name: 'user.user-list',
        path: '/user/userList',
        component: './user/UserList',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/user/userInfo/:userId',
        component: './user/UserInfo',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/user/UpdatePwd/:userId',
        component: './user/UpdatePwd',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/user/userView/:userId',
        component: './user/UserView',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/user',
        redirect: '/user/userList',
        exact: true
      }
    ],
  },
  {
    name: 'dbs',
    path: '/dbs',
    routes: [
      {
        name: 'user.user-list',
        path: '/dbs/dbsList',
        component: './system/dbs/DbsList',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/dbs/dbInfo/:name',
        component: './system/dbs/DbInfo',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/dbs/dbView/:name',
        component: './system/dbs/DbView',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/dbs',
        redirect: '/dbs/dbsList',
        exact: true
      }
    ]

  },
  {
    name: 'rule',
    path: '/rule',
    routes: [
      {
        name: 'user.user-list',
        path: '/rule/ruleInfo/:roleId',
        component: './system/rule/RuleInfo',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/rule',
        redirect: '/rule/ruleInfo/null',
        exact: true
      }
    ]
  },
  {
    name: 'Auth',
    path: '/Auth',
    component: './system/auth/Auth',
    exact: true
  },

  {
    name: 'role',
    path: '/role',
    routes: [
      {
        name: 'user.user-list',
        path: '/rule/ruleInfo/:roleId',
        component: './system/rule/RuleInfo',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/role/roleList',
        component: './system/role/RoleList',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/role/roleInfo/:roleId',
        component: './system/role/RoleInfo',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/role/roleUser/:roleId',
        component: './system/role/RoleUser',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/role',
        redirect: '/role/roleList',
        exact: true
      }
    ]

  },

  {
    name: 'authType',
    path: '/authType',
    routes: [
      {
        name: 'user.user-list',
        path: '/authType/authTypeList',
        component: './system/authType/AuthTypeList',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/authType/authTypeInfo/:name',
        component: './system/authType/AuthTypeInfo',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/authType',
        redirect: '/authType/authTypeList',
        exact: true
      }
    ]

  },

  {
    name: 'org',
    path: '/org',
    routes: [
      {
        name: 'user.user-list',
        path: '/org/OrgManager',
        component: './system/org/OrgManager',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/org',
        redirect: 'org/OrgManager',
        exact: true
      }
    ]
  },
  {
    name: 'menu',
    path: '/menu',
    routes: [
      {
        name: 'user.user-list',
        path: '/menu/menuManager',
        component: './system/menu/MenuManager',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/menu/menuEdit/:action/:id',
        component: './system/menu/menuEdit',
        exact: true
      },
      {
        name: 'user.user-list',
        path: '/menu',
        redirect: '/menu/MenuManager'
      }
    ]
  },
  {
    name: 'storage',
    path: '/storage',
    routes: [
      {
        name: 'user.user-list',
        path: '/storage/storageList',
        component: './storage/storage/StorageList'
      },
      {
        name: 'user.user-list',
        path: '/storage/inStorageList',
        component: './storage/inStorage/InStorageList'
      },
      {
        name: 'user.user-list',
        path: '/storage',
        redirect: '/storage/storageList',
      }
    ]
  },

  {
    path: '/welcome',
    name: 'welcome',
    icon: 'smile',
    component: './Welcome',
    exact: true
  },
  {
    path: '/admin',
    name: 'admin',
    icon: 'crown',
    component: './Admin',
    routes: [
      {
        path: '/admin/sub-page',
        name: 'sub-page',
        icon: 'smile',
        component: './Welcome',
        exact: true
      },
    ]
  },
  {
    name: 'list.table-list',
    icon: 'table',
    path: '/list',
    component: './ListTableList',
    exact: true
  },
  {
    path: '/',
    //redirect: '/user',
    exact: true
  },
  {
    component: './404',
    exact: true
  },
];
