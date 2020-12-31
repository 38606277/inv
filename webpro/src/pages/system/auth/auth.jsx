
import React from 'react';
import Role from '@/services/RoleService.jsx';
import RuleService from '@/services/RuleService.jsx';

import { Table, Button, Card, Tooltip, Input, message, Tree, Tabs, Select } from 'antd';
import Pagination from 'antd/lib/pagination';
import HttpService from '@/utils/HttpService.jsx';

const TreeNode = Tree.TreeNode;
const _role = new Role();
const ruleSevie = new RuleService();
const Search = Input.Search;
const TabPane = Tabs.TabPane;
const Option = Select.Option;


class auth extends React.Component {
    constructor(props) {
        super(props);
        const panes = [];
        this.parentNodes = [];
        this.node = null;
        this.newTabIndex = 0;
        this.state = {
            roleId: this.props.match.params.roleId,
            authType: [],
            authData: [],
            checkedKeys: [],
            roleName: '',
            list: [],

            pageNum: 1,
            perPage: 10,
            listType: 'list',
            expandedKeys: [],
            autoExpandParent: true,

            selectedKeys: [],
            rowId: 0,
            selectedRowKeys: [],
            tabPosition: 'top',
            treeData: [],
            activeKey: "select",


            panes,
        };
    }

    componentDidMount() {
        //加载角色列表
        this.loadRoleList();
        //加载授权类型表，生成tab页
        let url = "reportServer/authType/getAllAuthType";
        HttpService.post(url, JSON.stringify({}))
            .then(res => {
                if (res.resultCode == "1000") {
                    this.setState({ authType: res.data });

                    console.log(this.state.authType);
                }
                else {
                    message.error(res.message);
                }

            });
        if (null != this.state.roleId && '' != this.state.roleId && 'null' != this.state.roleId) {
            this.selectedOnchage(this.state.roleId, '', '', '');
        }
        //加载第一页tab页数据
        this.loadAuthData('webFunc');
        this.setState({ activeKey: 'webFunc' });


    }
    loadRoleList() {
        let listParam = {};
        listParam.pageNum = this.state.pageNum;
        listParam.perPage = this.state.perPage;
        // 如果是搜索的话，需要传入搜索类型和搜索关键字
        if (this.state.listType === 'search') {
            listParam.roleName = this.state.roleName;
        }


        _role.getRoleList(listParam).then(response => {
            this.setState(response);

        }, errMsg => {
            this.setState({
                list: []
            });
        });

    }
    loadAuthData(activeKey) {
        //加载当前tab页的树形
        let url = "reportServer/auth/getDataByAuthType";
        HttpService.post(url, JSON.stringify({ authTypeName: activeKey }))
            .then(res => {
                if (res.resultCode == "1000") {
                    this.setState({ authData: res.data });
                    console.log(this.state.authData);
                }
                else {
                    message.error(res.message);
                }


            });
    }
    loadAuth(role_Id, auth_type) {
        //加载当前tab页的选中节点,只选中末级
        let url = "reportServer/auth/getAuthByAuthType";
        let param = { role_id: role_Id, auth_type: auth_type };
        HttpService.post(url, JSON.stringify(param))
            .then(res => {
                if (res.resultCode == "1000") {
                    let checks = [];
                    res.data.map((item, index) => {
                        checks.push(item.func_id);
                    })
                    this.setState({ checkedKeys: checks });
                    console.log("check", this.state.checkedKeys);
                }
                else {
                    message.error(res.message);
                }


            });

    }


    //角色选择改变时执行
    selectedOnchage(roleId, name, types, isChange) {
        //
        console.log(roleId);
        this.setState({ roleId: roleId, roleName: name });
        //刷新当前页的数据
        this.loadAuth(roleId, this.state.activeKey);
        //设置当前页




    };
    //tab页改变时
    onChangeTab = (activeKey) => {
        console.log(activeKey);
        this.setState({ activeKey: activeKey });
        this.loadAuthData(activeKey);
        this.loadAuth(this.state.roleId, activeKey);

    }



    onSave(checkedKeys) {
        let param = [this.state.roleId, this.state.activeKey, this.state.checkedKeys];
        console.log(param);
        ruleSevie.saveAuthRules(param).then(response => {
            message.success("保存成功");
        });

    };




    onCheck = checkedKeys => {
        console.log('onCheck', checkedKeys);
        this.setState({ checkedKeys });
    };

    onValueChange(e) {
        let name = e.target.name,
            value = e.target.value.trim();

        this.setState({ roleName: value });
    }
    // 搜索
    onSearch() {
        this.setState({
            pageNum: 1,
            listType: 'search'
        }, () => {
            this.loadRoleList();
        });
    }
    // 页数发生变化的时候
    onPageNumChange(pageNum) {
        this.setState({
            pageNum: pageNum
        }, () => {
            this.loadRoleList();
        });
    }

    renderTreeNodes = data =>
        data.map(item => {
            if (item.children) {
                return (
                    <TreeNode title={item.name} key={item.id} dataRef={item}>
                        {this.renderTreeNodes(item.children)}
                    </TreeNode>
                );
            }
            return <TreeNode key={item.id} title={item.name} isLeaf={item.isLeaf == 1} dataRef={item} />;
        });
    // 选中行
    onClickRow = (record) => {
        return {
            onClick: () => {
                console.log("选中行" + record)
                //
                console.log(roleId);
                this.setState({ roleId: roleId, roleName: name });
                //刷新当前页的数据
                this.loadAuth(record.roleId, this.state.activeKey);
                //设置当前页

            },
        };
    }
    setRowClassName = (record) => {
        return record.id === this.state.rowId ? 'clickRowStyl' : '';
    }

    render() {
        this.state.list.map((item, index) => {
            item.key = index;
        })
        const dataSource = this.state.list;
        const columns = [{
            dataIndex: 'roleName',
            key: 'roleName',
            render: (text, record) => {
                return <a
                    onClick={() => this.selectedOnchage(record.roleId, record.roleName, '', '')} >
                    {text}
                </a>;
            }
        }];
        const contents = (
            <div>
                <Button type="primary" onClick={() => this.saveSelectObject()}>保存</Button>
                <Tree
                    checkable
                    onExpand={this.onExpand}
                    expandedKeys={this.state.expandedKeys}
                    autoExpandParent={this.state.autoExpandParent}
                    onCheck={this.onCheck}
                    checkedKeys={this.state.checkedKeys}
                    selectedKeys={this.state.selectedKeys}
                    checkStrictly
                >
                    {this.renderTreeNodes(this.state.treeData)}
                </Tree>
            </div>
        );

        const rowSelection = {
            selectedRowKeys: this.state.selectedRowKeys,
            onChange: (selectedRowKeys, selectedRows) => {
                console.log('selectedRowKeys changed: ', selectedRowKeys);
                console.log('selectedRows changed: ', selectedRows);
                this.setState({ selectedRowKeys: selectedRowKeys, selectedRows: selectedRows });
            },
        };

        return (
            <div id="page-wrapper" >
                <Card title="角色列表" style={{ float: "left", width: "20%" }}>
                    <Tooltip>
                        <Search
                            style={{ maxWidth: 190, marginBottom: '10px', border: '0' }}
                            placeholder={this.state.roleName == '' ? '请输入...' : this.state.roleName}

                            onSearch={value => this.onSearch(value)}
                            onChange={(e) => this.onValueChange(e)}
                            value={this.state.roleName}
                        />
                    </Tooltip>
                    <Table dataSource={dataSource} columns={columns} pagination={false} onRow={this.onClickRow}
                        showHeader={false} style={{ border: '0' }} rowClassName={this.setRowClassName}
                    />
                    <Pagination current={this.state.pageNum}
                        total={this.state.total}
                        onChange={(pageNum) => this.onPageNumChange(pageNum)} />
                </Card>

                <Card title="权限列表" style={{ float: "left", width: "80%" }}>
                    <Tabs onChange={this.onChangeTab} tabPosition={this.state.tabPosition} defaultActiveKey="1" >
                        {this.state.authType.map(item => (
                            <TabPane tab={item.name} key={item.value}>
                                <Button onClick={() => this.onSave(item.value)}>保存</Button>
                                <Tree
                                    checkable
                                    onCheck={this.onCheck}
                                    checkedKeys={this.state.checkedKeys}
                                >
                                    {this.renderTreeNodes(this.state.authData)}
                                </Tree>
                            </TabPane>
                        ))}
                    </Tabs>
                </Card>

            </div>
        )
    }
}

export default auth;